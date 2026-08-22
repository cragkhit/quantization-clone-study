"""Smoke test for Llama-4-Scout QLoRA via Unsloth: load BF16 + on-the-fly 4-bit
quantization, attach a rank-32 LoRA, run one forward+backward pass, report peak
VRAM. See exp_notes.md ("Llama-4-Scout-17B-16E QLoRA Fine-Tuning") for full
context, the unsloth_venv setup, and the two bugs this script's design works
around (pre-quantized checkpoints crash on load; single-GPU OOMs during load).

Last run (2026-08-12, single GPU via CUDA_VISIBLE_DEVICES=<n>): OOM during
weight loading at ~77.6/79.11 GB, PYTORCH_CUDA_ALLOC_CONF=expandable_segments
had no effect. NEXT STEP (untried): pass device_map="auto" to from_pretrained
and run with CUDA_VISIBLE_DEVICES spanning 2 idle GPUs for ~160GB headroom -
verify Unsloth actually threads device_map through to the underlying
transformers from_pretrained call before assuming this works.

Usage:
  CUDA_VISIBLE_DEVICES=<n> HF_HUB_DISABLE_XET=1 HF_HUB_ENABLE_HF_TRANSFER=0 \
    unsloth_venv/bin/python scripts/smoke_llama4_unsloth.py
"""
import os, time
os.environ["HF_HUB_DISABLE_XET"] = "1"
import torch
from unsloth import FastLanguageModel

MODEL = "unsloth/Llama-4-Scout-17B-16E-Instruct"  # BF16, quantize to 4-bit on-the-fly

print(f"[smoke] {time.strftime('%T')} loading {MODEL} ...", flush=True)
t0 = time.time()
model, tokenizer = FastLanguageModel.from_pretrained(
    model_name=MODEL,
    max_seq_length=2048,
    load_in_4bit=True,
    dtype=None,
)
print(f"[smoke] loaded in {time.time()-t0:.0f}s", flush=True)
print(f"[smoke] VRAM after load: {torch.cuda.memory_allocated()/1e9:.1f} GB alloc, "
      f"{torch.cuda.memory_reserved()/1e9:.1f} GB reserved", flush=True)

print(f"[smoke] {time.strftime('%T')} attaching LoRA ...", flush=True)
model = FastLanguageModel.get_peft_model(
    model,
    r=32,
    target_modules=["q_proj", "k_proj", "v_proj", "o_proj",
                     "gate_proj", "up_proj", "down_proj"],
    lora_alpha=32,
    lora_dropout=0,
    bias="none",
    use_gradient_checkpointing="unsloth",
    random_state=42,
)
print(f"[smoke] LoRA attached. VRAM: {torch.cuda.memory_allocated()/1e9:.1f} GB alloc, "
      f"{torch.cuda.memory_reserved()/1e9:.1f} GB reserved", flush=True)

trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
total = sum(p.numel() for p in model.parameters())
print(f"[smoke] trainable params: {trainable:,} / {total:,}", flush=True)

FastLanguageModel.for_training(model)
prompt = ("Compare the two Java code snippets and decide if they are functional "
          "clones. Respond with JSON {\"answer\": ...}.\n\nCode A:\npublic class A "
          "{ public static void main(String[] a) { System.out.println(1+1); } }\n\n"
          "Code B:\npublic class B { public static void main(String[] a) { int x=1+1; "
          "System.out.println(x); } }")
inputs = tokenizer(prompt, return_tensors="pt").to(model.device)
labels = inputs["input_ids"].clone()

print(f"[smoke] {time.strftime('%T')} forward+backward pass ...", flush=True)
t0 = time.time()
out = model(**inputs, labels=labels)
out.loss.backward()
print(f"[smoke] fwd+bwd done in {time.time()-t0:.1f}s, loss={out.loss.item():.4f}", flush=True)
print(f"[smoke] PEAK VRAM: {torch.cuda.max_memory_allocated()/1e9:.1f} GB alloc, "
      f"{torch.cuda.max_memory_reserved()/1e9:.1f} GB reserved", flush=True)
print("[smoke] DONE - SUCCESS", flush=True)
