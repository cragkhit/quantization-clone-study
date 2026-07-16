#!/usr/bin/env python3
"""QLoRA fine-tuning of Qwen2.5-Coder-7B-Instruct on the AIZU clone set.

Produces a small LoRA adapter (a few MB) that is later converted to GGUF and
applied *on top of your existing* `qwen2.5-coder-7b-instruct-q4_k_m.gguf` at
inference (see the `gguf_lora` backend in run_quantization.py). That keeps the
evaluation base byte-identical to the Q4_K_M model you already benchmarked, so
the metric delta isolates the fine-tuning lift at the same quantization.

Method (QLoRA):
  * The base model is loaded in 4-bit NF4 (bitsandbytes) and FROZEN.
  * Only the LoRA adapter matrices are trained (bf16), so we are adapting a
    quantized model, not full-precision weights.
  * Loss is computed on the assistant answer only (prompt tokens masked).

Requires a CUDA GPU. Run on a free GPU node (this cannot run CPU-only):
    env -u PYTHONPATH finetune_venv/bin/python train_lora.py

See exp_notes.md for venv/setup and the full end-to-end command sequence.
"""
import argparse
import json
from pathlib import Path


def load_jsonl(path: str) -> list[dict]:
    with open(path) as f:
        return [json.loads(line) for line in f]


def main():
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--base-model", default="Qwen/Qwen2.5-Coder-7B-Instruct",
                    help="HF model to adapt. MUST be the same model your GGUF "
                         "Q4_K_M is a quant of, so the adapter shapes match.")
    ap.add_argument("--train-file", default="finetune_data/aizu_train.jsonl")
    ap.add_argument("--val-file", default="finetune_data/aizu_val.jsonl")
    ap.add_argument("--output-dir",
                    default="finetune_models/qwen2.5-coder-7b-aizu-qlora")
    ap.add_argument("--max-len", type=int, default=2048)
    ap.add_argument("--epochs", type=float, default=3.0)
    ap.add_argument("--lr", type=float, default=2e-4)
    ap.add_argument("--batch-size", type=int, default=2)
    ap.add_argument("--grad-accum", type=int, default=8)
    ap.add_argument("--lora-r", type=int, default=16)
    ap.add_argument("--lora-alpha", type=int, default=32)
    ap.add_argument("--lora-dropout", type=float, default=0.05)
    ap.add_argument("--seed", type=int, default=42)
    ap.add_argument("--no-4bit", action="store_true",
                    help="Train on the bf16 base (standard LoRA) instead of "
                         "the 4-bit NF4 base (QLoRA). Adapter still deploys on "
                         "Q4_K_M at inference either way.")
    args = ap.parse_args()

    import torch
    from transformers import (AutoModelForCausalLM, AutoTokenizer,
                              BitsAndBytesConfig, DataCollatorForSeq2Seq,
                              Trainer, TrainingArguments, set_seed)
    from peft import LoraConfig, get_peft_model, prepare_model_for_kbit_training

    set_seed(args.seed)

    tok = AutoTokenizer.from_pretrained(args.base_model)
    if tok.pad_token is None:
        tok.pad_token = tok.eos_token

    # ---- Tokenize: mask the prompt, supervise only the assistant answer ----
    def encode(ex: dict) -> dict:
        msgs = ex["messages"]
        full = tok.apply_chat_template(msgs, tokenize=False,
                                       add_generation_prompt=False)
        prompt = tok.apply_chat_template(msgs[:1], tokenize=False,
                                         add_generation_prompt=True)
        full_ids = tok(full, add_special_tokens=False)["input_ids"]
        prompt_ids = tok(prompt, add_special_tokens=False)["input_ids"]
        answer_len = max(1, len(full_ids) - len(prompt_ids))
        # Left-truncate if too long: the answer sits at the very end, so we keep
        # it supervised and drop the front of the (masked) prompt for the rare
        # oversized pair.
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

    # ---- Load the (quantized) base and attach LoRA ----
    quant_cfg = None
    if not args.no_4bit:
        quant_cfg = BitsAndBytesConfig(
            load_in_4bit=True,
            bnb_4bit_quant_type="nf4",
            bnb_4bit_use_double_quant=True,
            bnb_4bit_compute_dtype=torch.bfloat16,
        )
    model = AutoModelForCausalLM.from_pretrained(
        args.base_model,
        quantization_config=quant_cfg,
        torch_dtype=torch.bfloat16,
        device_map="auto",
    )
    model.config.use_cache = False
    if not args.no_4bit:
        model = prepare_model_for_kbit_training(
            model, use_gradient_checkpointing=True)
    else:
        model.gradient_checkpointing_enable()

    lora_cfg = LoraConfig(
        r=args.lora_r, lora_alpha=args.lora_alpha, lora_dropout=args.lora_dropout,
        bias="none", task_type="CAUSAL_LM",
        target_modules=["q_proj", "k_proj", "v_proj", "o_proj",
                        "gate_proj", "up_proj", "down_proj"],
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
        optim="paged_adamw_8bit" if not args.no_4bit else "adamw_torch",
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

    # Save the adapter (small) + tokenizer for the GGUF conversion step.
    out = Path(args.output_dir)
    model.save_pretrained(out)
    tok.save_pretrained(out)
    print(f"\nAdapter saved to {out}", flush=True)
    print("Next: convert to GGUF with llama.cpp's convert_lora_to_gguf.py, then "
          "evaluate with the `gguf_lora` backend (see exp_notes.md).", flush=True)


if __name__ == "__main__":
    main()
