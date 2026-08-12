# Models Used in This Study

All models used in the quantization / code-clone-detection study, with their
HuggingFace locations (`account/model-name`). Grouped by model family; each family
lists its full-precision base plus every quantized checkpoint evaluated.

Quantization methods: **GGUF** (Q2_K / Q3_K_M / Q4_K_M via llama.cpp), **QTIP**
4-bit, **HIGGS-GPTQ** 3/4-bit, **AQLM** 2-bit, **FP8**. "Self-quantized" means we
produced the GGUF ourselves from the BF16 base (no community GGUF existed);
those have no HF repo and live locally under `models/`.

---

## Llama-3.1-8B-Instruct

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct) |
| GGUF (Q2_K / Q3_K_M / Q4_K_M) | [`bartowski/Meta-Llama-3.1-8B-Instruct-GGUF`](https://huggingface.co/bartowski/Meta-Llama-3.1-8B-Instruct-GGUF) |
| QTIP 4-bit | [`relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit`](https://huggingface.co/relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit) |
| HIGGS-GPTQ 4-bit | [`ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit`](https://huggingface.co/ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit) |
| HIGGS-GPTQ 3-bit | [`ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit`](https://huggingface.co/ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit) |
| AQLM 2-bit (PV, 1×16) | [`ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf`](https://huggingface.co/ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf) |

## Llama-4-Scout-17B-16E-Instruct

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`meta-llama/Llama-4-Scout-17B-16E-Instruct`](https://huggingface.co/meta-llama/Llama-4-Scout-17B-16E-Instruct) |
| GGUF (Q2_K / Q3_K_S / Q4_K_M) | [`bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF`](https://huggingface.co/bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF) |

## CodeLlama-7b-Instruct

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`codellama/CodeLlama-7b-Instruct-hf`](https://huggingface.co/codellama/CodeLlama-7b-Instruct-hf) |
| GGUF (Q2_K / Q3_K_M / Q4_K_M) | [`QuantFactory/CodeLlama-7b-Instruct-hf-GGUF`](https://huggingface.co/QuantFactory/CodeLlama-7b-Instruct-hf-GGUF) |

## Qwen2.5-Coder-7B-Instruct

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`Qwen/Qwen2.5-Coder-7B-Instruct`](https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct) |
| GGUF, community (Q2_K / Q3_K_M / Q4_K_M) | [`Qwen/Qwen2.5-Coder-7B-Instruct-GGUF`](https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct-GGUF) |
| GGUF, self-quantized (`-self`) | local — quantized from the BF16 base above (`models/Qwen2.5-Coder-7B-Instruct-*.gguf`) |

## Qwen3-Coder-30B-A3B (30B MoE, ~3B active)

| Variant | HuggingFace location |
| --- | --- |
| FP8 (native, served via vLLM) | [`Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8`](https://huggingface.co/Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8) |
| FP8 → GGUF Q4_K_M (community) | [`ijohn07/Qwen3-Coder-30B-A3B-Instruct-FP8-Q4_K_M-GGUF`](https://huggingface.co/ijohn07/Qwen3-Coder-30B-A3B-Instruct-FP8-Q4_K_M-GGUF) |
| BF16 base (for self-GGUF + LoRA) | [`Qwen/Qwen3-Coder-30B-A3B-Instruct`](https://huggingface.co/Qwen/Qwen3-Coder-30B-A3B-Instruct) — stored under `models/Qwen3-Coder-30B-A3B-Instruct/` |
| GGUF, self-quantized (Q2_K / Q3_K_M / Q4_K_M) | local — quantized from the BF16 base (`models/Qwen3-Coder-30B-A3B-Instruct-*.gguf`) |

## Qwen3.6-27B (Qwen3.5-family multimodal / VLM, run text-only)

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`Qwen/Qwen3.6-27B`](https://huggingface.co/Qwen/Qwen3.6-27B) |
| GGUF, self-quantized (Q2_K / Q3_K_M / Q4_K_M) | local — quantized from the BF16 base (`models/Qwen3.6-27B-*.gguf`); no community GGUF exists yet |

## DeepSeek-Coder-V2-Lite-Instruct

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct`](https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct) |
| GGUF (Q4_K_M) | [`bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF`](https://huggingface.co/bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF) |

## aya-expanse-8b (Cohere / Command-R architecture)

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`CohereLabs/aya-expanse-8b`](https://huggingface.co/CohereLabs/aya-expanse-8b) |
| GGUF (Q2_K / Q3_K_M / Q4_K_M) | [`bartowski/aya-expanse-8b-GGUF`](https://huggingface.co/bartowski/aya-expanse-8b-GGUF) |

## Codestral-22B-v0.1

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`mistralai/Codestral-22B-v0.1`](https://huggingface.co/mistralai/Codestral-22B-v0.1) |
| GGUF (Q2_K / Q3_K_M / Q4_K_M) | [`bartowski/Codestral-22B-v0.1-GGUF`](https://huggingface.co/bartowski/Codestral-22B-v0.1-GGUF) |

## cogito-v1-preview-llama-8B (Llama-3.1-8B fine-tune)

| Variant | HuggingFace location |
| --- | --- |
| Full precision (BF16) | [`deepcogito/cogito-v1-preview-llama-8B`](https://huggingface.co/deepcogito/cogito-v1-preview-llama-8B) |
| GGUF (Q2_K / Q3_K_M / Q4_K_M) | [`cortexso/cogito-v1`](https://huggingface.co/cortexso/cogito-v1) (GGUF-only repo) |

---

## Fine-tuned (LoRA / QLoRA on AIZU) — adapters are local

These adapters were trained in-house on the AIZU384F clone set and are **not** on
HuggingFace; each is derived from a base model listed above. Adapters live under
`finetune_models/`.

| Base model | HuggingFace base | Adapter (local) |
| --- | --- | --- |
| Qwen2.5-Coder-7B-Instruct | [`Qwen/Qwen2.5-Coder-7B-Instruct`](https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct) | `finetune_models/qwen2.5-coder-7b-aizu-qlora` (+ `-aizuCL-` cross-language variant) |
| Llama-3.1-8B-Instruct | [`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct) | `finetune_models/llama3.1-8b-aizu-qlora` |
| CodeLlama-7b-Instruct | [`codellama/CodeLlama-7b-Instruct-hf`](https://huggingface.co/codellama/CodeLlama-7b-Instruct-hf) | `finetune_models/codellama-7b-aizu-qlora` |
| Qwen3-Coder-30B-A3B-Instruct | [`Qwen/Qwen3-Coder-30B-A3B-Instruct`](https://huggingface.co/Qwen/Qwen3-Coder-30B-A3B-Instruct) | `finetune_models/qwen3-coder-30b-a3b-aizu-lora` |
| Qwen3.6-27B | [`Qwen/Qwen3.6-27B`](https://huggingface.co/Qwen/Qwen3.6-27B) | `finetune_models/qwen3.6-27b-aizu-lora` |

---

## Local storage

All models above are **downloaded and present on disk** (verified 2026-08-12). HF-hub
repos live in `~/.cache/huggingface/hub/models--*/`; self-quantized GGUFs and the
Qwen3-Coder BF16 base live under `models/`; LoRA adapters under `finetune_models/`.

Also cached but **not part of the documented study** (exploratory / superseded
downloads): `relaxml/Llama-3.1-8b-Instruct-QTIP-{2Bit,3Bit}` (only 4-bit was used),
`unsloth/Llama-4-Scout-17B-16E-Instruct{,-unsloth-bnb-4bit}`, `Qwen/Qwen2.5-Coder-7B`
(base, non-instruct), `Qwen/Qwen2.5-0.5B-Instruct`, `codellama/CodeLlama-7b-hf`, and
empty ref-only caches for `deepseek-ai/DeepSeek-Coder-V2-Instruct` and
`meta-llama/CodeLlama-7b-hf`.
