#!/usr/bin/env python3
"""Standard LoRA fine-tuning for a Qwen3.5-family multimodal model (Qwen3.6-27B),
run TEXT-ONLY, on the AIZU clone set.

A VLM-adapted sibling of train_lora.py. Same recipe (r=16/alpha=32, completion-only
loss, lr 2e-4, 3 epochs, seed 42) but three model-specific changes:

  1. Loads with AutoModelForImageTextToText (the model is
     Qwen3_5ForConditionalGeneration, not a CausalLM); BF16, no 4-bit.
  2. target_modules is a REGEX scoped to the TEXT tower only
     (model.language_model.layers.*) so LoRA does NOT touch the vision encoder
     or the auxiliary multi-token-prediction head (mtp.layers.*), which share the
     same q/k/v/o/gate/up/down_proj suffixes.
  3. Tokenization sets enable_thinking=False to match the eval path (this is a
     hybrid thinking model; the AIZU targets are plain JSON verdicts).

Eval is via transformers + PeftModel (the `qwen36_lora` backend), NOT GGUF: the
model already runs on transformers on this box (see load_qwen36 / exp_notes.md).

    CUDA_VISIBLE_DEVICES=4,5 env -u PYTHONPATH qwen36_venv/bin/python train_lora_vlm.py
"""
import argparse
import json
from pathlib import Path

# Text-tower-only LoRA targets: matches model.language_model.layers.<n>.<attn|mlp>.<proj>
# and deliberately excludes vision_* and mtp.layers.* (auxiliary MTP head).
TEXT_TARGET_REGEX = (
    r"model\.language_model\.layers\.\d+\."
    r"(self_attn\.(q_proj|k_proj|v_proj|o_proj)|mlp\.(gate_proj|up_proj|down_proj))"
)


def load_jsonl(path: str) -> list[dict]:
    with open(path) as f:
        return [json.loads(line) for line in f]


def main():
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--base-model", default="Qwen/Qwen3.6-27B")
    ap.add_argument("--train-file", default="finetune_data/aizu_train.jsonl")
    ap.add_argument("--val-file", default="finetune_data/aizu_val.jsonl")
    ap.add_argument("--output-dir",
                    default="finetune_models/qwen3.6-27b-aizu-lora")
    ap.add_argument("--max-len", type=int, default=2048)
    ap.add_argument("--epochs", type=float, default=3.0)
    ap.add_argument("--lr", type=float, default=2e-4)
    ap.add_argument("--batch-size", type=int, default=1)
    ap.add_argument("--grad-accum", type=int, default=16)
    ap.add_argument("--lora-r", type=int, default=16)
    ap.add_argument("--lora-alpha", type=int, default=32)
    ap.add_argument("--lora-dropout", type=float, default=0.05)
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()

    import torch
    from transformers import (AutoModelForImageTextToText, AutoTokenizer,
                              DataCollatorForSeq2Seq, Trainer, TrainingArguments,
                              set_seed)
    from peft import LoraConfig, get_peft_model

    set_seed(args.seed)

    tok = AutoTokenizer.from_pretrained(args.base_model)
    if tok.pad_token is None:
        tok.pad_token = tok.eos_token

    # ---- Tokenize: mask the prompt, supervise only the assistant answer ----
    # enable_thinking=False matches the qwen36 eval path (empty think block, so the
    # supervised target is exactly the JSON verdict).
    def encode(ex: dict) -> dict:
        msgs = ex["messages"]
        full = tok.apply_chat_template(msgs, tokenize=False,
                                       add_generation_prompt=False,
                                       enable_thinking=False)
        prompt = tok.apply_chat_template(msgs[:1], tokenize=False,
                                         add_generation_prompt=True,
                                         enable_thinking=False)
        full_ids = tok(full, add_special_tokens=False)["input_ids"]
        prompt_ids = tok(prompt, add_special_tokens=False)["input_ids"]
        answer_len = max(1, len(full_ids) - len(prompt_ids))
        if len(full_ids) > args.max_len:
            full_ids = full_ids[-args.max_len:]
        answer_len = min(answer_len, len(full_ids))
        labels = [-100] * (len(full_ids) - answer_len) + full_ids[-answer_len:]
        return {"input_ids": full_ids,
                "attention_mask": [1] * len(full_ids),
                "labels": labels}

    from datasets import Dataset
    train_ds = Dataset.from_list([encode(e) for e in load_jsonl(args.train_file)])
    val_ds = Dataset.from_list([encode(e) for e in load_jsonl(args.val_file)])
    print(f"train={len(train_ds)}  val={len(val_ds)}  max_len={args.max_len}", flush=True)

    # ---- Load the VLM (text-only training) and attach LoRA to the text tower ----
    model = AutoModelForImageTextToText.from_pretrained(
        args.base_model, dtype=torch.bfloat16, device_map="auto")
    # Disable cache for training (top + text config, defensively) and checkpoint.
    for cfg in (model.config, getattr(model.config, "text_config", None)):
        if cfg is not None:
            cfg.use_cache = False
    model.gradient_checkpointing_enable(
        gradient_checkpointing_kwargs={"use_reentrant": False})

    lora_cfg = LoraConfig(
        r=args.lora_r, lora_alpha=args.lora_alpha, lora_dropout=args.lora_dropout,
        bias="none", task_type="CAUSAL_LM",
        target_modules=TEXT_TARGET_REGEX,  # regex -> text tower only
    )
    model = get_peft_model(model, lora_cfg)
    model.print_trainable_parameters()

    targs = TrainingArguments(
        output_dir=args.output_dir,
        num_train_epochs=args.epochs,
        per_device_train_batch_size=args.batch_size,
        per_device_eval_batch_size=args.batch_size,
        gradient_accumulation_steps=args.grad_accum,
        learning_rate=args.lr,
        lr_scheduler_type="cosine",
        warmup_ratio=0.03,
        logging_steps=5,
        eval_strategy="epoch",
        save_strategy="epoch",
        save_total_limit=2,
        load_best_model_at_end=True,
        metric_for_best_model="eval_loss",
        greater_is_better=False,
        bf16=True,
        optim="adamw_torch",
        report_to="none",
        seed=args.seed,
    )

    trainer = Trainer(
        model=model, args=targs,
        train_dataset=train_ds, eval_dataset=val_ds,
        data_collator=DataCollatorForSeq2Seq(
            tok, padding=True, label_pad_token_id=-100),
    )
    trainer.train()

    out = Path(args.output_dir)
    model.save_pretrained(out)
    tok.save_pretrained(out)
    print(f"\nAdapter saved to {out}", flush=True)
    print("Next: evaluate with the `qwen36_lora` backend (transformers + PeftModel).",
          flush=True)


if __name__ == "__main__":
    main()
