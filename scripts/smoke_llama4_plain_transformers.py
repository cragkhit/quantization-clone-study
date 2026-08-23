"""Smoke test: load Llama-4-Scout via PLAIN transformers (no Unsloth loader
involved at all) with device_map="auto" across 2 GPUs + BitsAndBytesConfig
4-bit, to isolate whether Unsloth's loading wrapper/patches are the reason
device_map/max_memory/the unsloth_zoo device_map_planner all failed to
actually balance the load across 2 GPUs (see exp_notes.md, "Llama-4-Scout
QLoRA Fine-Tuning" - attempts #2 and #3 both produced a sound-looking plan
but still OOM'd one GPU during real weight loading).

If this loads and balances correctly, hand the already-sharded model to
Unsloth's FastLanguageModel.get_peft_model for the LoRA-attach step only
(get_peft_model has no hard requirement that the model was Unsloth-loaded -
confirmed by reading unsloth/models/vision.py's get_peft_model source).

Usage:
  CUDA_VISIBLE_DEVICES=<n>,<m> HF_HUB_DISABLE_XET=1 HF_HUB_ENABLE_HF_TRANSFER=0 \
    unsloth_venv/bin/python scripts/smoke_llama4_plain_transformers.py
"""
import os, time
os.environ["HF_HUB_DISABLE_XET"] = "1"
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer, BitsAndBytesConfig

MODEL = "unsloth/Llama-4-Scout-17B-16E-Instruct"

n_gpus = torch.cuda.device_count()
max_memory = {i: "70GiB" for i in range(n_gpus)}
print(f"[smoke] {n_gpus} visible GPUs, max_memory={max_memory}", flush=True)

quant_cfg = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_use_double_quant=True,
    bnb_4bit_compute_dtype=torch.bfloat16,
)

print(f"[smoke] {time.strftime('%T')} loading {MODEL} via plain transformers ...", flush=True)
t0 = time.time()
tokenizer = AutoTokenizer.from_pretrained(MODEL)
model = AutoModelForCausalLM.from_pretrained(
    MODEL,
    quantization_config=quant_cfg,
    device_map="auto",
    max_memory=max_memory,
    torch_dtype=torch.bfloat16,
)
print(f"[smoke] loaded in {time.time()-t0:.0f}s", flush=True)
print(f"[smoke] hf_device_map: {model.hf_device_map}", flush=True) if hasattr(model, "hf_device_map") else None
for i in range(n_gpus):
    print(f"[smoke] cuda:{i} VRAM: {torch.cuda.memory_allocated(i)/1e9:.1f} GB alloc, "
          f"{torch.cuda.memory_reserved(i)/1e9:.1f} GB reserved", flush=True)

print(f"[smoke] {time.strftime('%T')} attaching LoRA via Unsloth on the pre-loaded model ...", flush=True)
from unsloth import FastLanguageModel
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
print(f"[smoke] LoRA attached.", flush=True)
for i in range(n_gpus):
    print(f"[smoke] cuda:{i} VRAM: {torch.cuda.memory_allocated(i)/1e9:.1f} GB alloc, "
          f"{torch.cuda.memory_reserved(i)/1e9:.1f} GB reserved", flush=True)

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
for i in range(n_gpus):
    print(f"[smoke] cuda:{i} PEAK VRAM: {torch.cuda.max_memory_allocated(i)/1e9:.1f} GB alloc, "
          f"{torch.cuda.max_memory_reserved(i)/1e9:.1f} GB reserved", flush=True)
print("[smoke] DONE - SUCCESS", flush=True)
