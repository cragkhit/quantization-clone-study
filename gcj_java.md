# GCJ Java Clone Experiments — Run Tracker

Mirrors the full model set from the OCD study, now on the **GCJ Java** clone set.

- **Dataset:** `gcj_java_clones/pairs.csv` (400 pairs, 200 clone / 200 non-clone — 20 problems × 5 Java submissions)
- **Rounds:** 5 per run

- **Results:** `results_gcj_java/<Model>/...`
- **Eval (all models):** `python evaluate_results.py --dataset gcj-java --mode majority-vote`
  (auto-selects `results_gcj_java/` and writes `results_gcj_java/evaluation_summary_gcj_java.csv`)
- **Eval (one model):** `python evaluate_results.py results_gcj_java/<Model>/*.csv --mode majority-vote --output results_gcj_java/evaluation_summary_gcj_java.csv`

Check a box once **all 5 rounds** for that run are complete. Environment setup
(venvs, kernels, per-backend quirks) is documented in `exp_notes.md`.

### Run command template

```bash
<venv>/bin/python run_quantization.py <backend> "<hf_model>" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/<Model>/results_<name>" \
  --rounds 5
```

> **Note:** `run_quantization.py` uses `str | None` syntax, so it needs a
> **Python 3.10+** venv. Use `aqlm_venv310` (not the 3.9 `aqlm_venv`) for the
> plain `original`/`aqlm` Llama-3.1-8B and Qwen runs.

---

## Meta-Llama-3.1-8B-Instruct

- [x] **Original (BF16)** — `original` · `meta-llama/Meta-Llama-3.1-8B-Instruct` · venv `aqlm_venv310` — *Acc 0.6794, F1 0.7586, MCC 0.4582*
- [x] **GGUF Q2_K** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q2_K.gguf` · venv `gguf` — *Acc 0.5400, F1 0.6849, MCC 0.2041*
- [x] **GGUF Q3_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf` · venv `gguf` — *Acc 0.5466, F1 0.6897, MCC 0.2131*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf` · venv `gguf` — *Acc 0.5758, F1 0.7032, MCC 0.2701*
- [x] **GGUF Q4_K_M + AIZU QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/llama3.1-8b-aizu-qlora-F16.gguf` · venv `gguf` — *Acc 0.9425, F1 0.9396, MCC 0.8890* (adapter trained on AIZU384F; +0.619 MCC — see exp_notes.md)
- [x] **AQLM PV 2-bit** — `aqlm` · `ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf` · venv `aqlm_venv310` — *Acc 0.5300, F1 0.6803, MCC 0.1759*
- [x] **HIGGS-GPTQ 3-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit` · venv `higgs_venv` — *Acc 0.6224, F1 0.7259, MCC 0.3390*
- [x] **HIGGS-GPTQ 4-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit` · venv `higgs_venv` — *Acc 0.6429, F1 0.7378, MCC 0.3822*
- [x] **QTIP 2-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-2Bit` · venv `qtip_venv` — *Acc 0.5952, F1 0.7193, MCC 0.2749*
- [x] **QTIP 3-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-3Bit` · venv `qtip_venv` — *Acc 0.6616, F1 0.7486, MCC 0.4214*
- [x] **QTIP 4-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit` · venv `qtip_venv` — *Acc 0.6921, F1 0.7650, MCC 0.4743*

## CodeLlama-7b-Instruct-hf

- [x] **Original (BF16)** — `codellama` · `codellama/CodeLlama-7b-Instruct-hf` · venv `codellama_venv` — *Acc 0.5331, F1 0.6955, MCC 0.0000*
- [x] **GGUF Q2_K** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf` · venv `gguf` — *Acc 0.5000, F1 0.6667, MCC 0.0000*
- [x] **GGUF Q3_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf` · venv `gguf` — *Acc 0.5077, F1 0.6734, MCC 0.0000*
- [x] **GGUF Q4_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf` · venv `gguf` — *Acc 0.5076, F1 0.6723, MCC 0.0508*
- [x] **GGUF Q4_K_M + AIZU QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/codellama-7b-aizu-qlora-F16.gguf` · venv `gguf` — *Acc 0.8800, F1 0.8703, MCC 0.7687* (adapter trained on AIZU384F; rescues the degenerate baseline, +0.718 MCC — see exp_notes.md)

## DeepSeek-Coder-V2-Lite-Instruct

- [x] **Original (BF16)** — `deepseek` · `deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct` · venv `deepseek_venv` — *Acc 0.8925, F1 0.8938, MCC 0.7852*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF::DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf` · venv `gguf` — *Acc 0.9075, F1 0.9059, MCC 0.8155*

## Qwen2.5-Coder-7B-Instruct

- [x] **Original (BF16)** — `original` · `Qwen/Qwen2.5-Coder-7B-Instruct` · venv `aqlm_venv310` — *Acc 0.7650, F1 0.6928, MCC 0.6005*
- [x] **GGUF Q2_K** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf` · venv `gguf` — *Acc 0.9050, F1 0.8950, MCC 0.8250*
- [x] **GGUF Q3_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf` · venv `gguf` — *Acc 0.7800, F1 0.7179, MCC 0.6236*
- [x] **GGUF Q4_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf` · venv `gguf` — *Acc 0.8175, F1 0.7768, MCC 0.6821*
- [x] **GGUF Q4_K_M + AIZU QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/qwen2.5-coder-7b-aizu-qlora-F16.gguf` · venv `gguf` — *Acc 0.9775, F1 0.9774, MCC 0.9550* (adapter trained on SimilBench AIZU384F; see exp_notes.md)
- [x] **GGUF Q4_K_M + AIZU-CL QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/qwen2.5-coder-7b-aizuCL-qlora-F16.gguf` · venv `gguf` — *Acc 0.9775, F1 0.9776, MCC 0.9550* (adapter trained on cross-language AIZU324CLF; identical to the monolingual adapter here — see exp_notes.md)

## Llama-4-Scout-17B-16E-Instruct

- [x] **Original (BF16)** — `original` · `meta-llama/Llama-4-Scout-17B-16E-Instruct` · venv `llama4_venv` — *Acc 0.9600, F1 0.9585, MCC 0.9223*
- [x] **GGUF Q2_K** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q2_K.gguf` · venv `gguf` — *Acc 0.9500, F1 0.9476, MCC 0.9037*
- [x] **GGUF Q3_K_S** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q3_K_S.gguf` · venv `gguf` — *Acc 0.9525, F1 0.9501, MCC 0.9091*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q4_K_M-00001-of-00002.gguf` (split) · venv `gguf` — *Acc 0.9600, F1 0.9583, MCC 0.9230*

## aya-expanse-8b

- [x] **Original (BF16)** — `original` · `CohereLabs/aya-expanse-8b` · venv `aqlm_venv310` — *Acc 0.8333, F1 0.8398, MCC 0.6696* (4 excluded)

## cogito-v1-preview-llama-8B

- [x] **GGUF Q2_K** — `gguf` · `cortexso/cogito-v1::cogito-v1-preview-llama-8b-q2_k.gguf` · venv `gguf` — *Acc 0.5525, F1 0.6865, MCC 0.2025*
- [x] **GGUF Q3_K_M** — `gguf` · `cortexso/cogito-v1::cogito-v1-preview-llama-8b-q3_k_m.gguf` · venv `gguf` — *Acc 0.6310, F1 0.7310, MCC 0.3701* (7 excluded)
- [x] **GGUF Q4_K_M** — `gguf` · `cortexso/cogito-v1::cogito-v1-preview-llama-8b-q4_k_m.gguf` · venv `gguf` — *Acc 0.8797, F1 0.8884, MCC 0.7680* (1 excluded)

## Codestral-22B-v0.1

- [x] **Original (BF16)** — `original` · `mistralai/Codestral-22B-v0.1` · venv `codestral_venv` — *Acc 0.9375, F1 0.9351, MCC 0.8775*
- [x] **GGUF Q2_K** — `gguf` · `bartowski/Codestral-22B-v0.1-GGUF::Codestral-22B-v0.1-Q2_K.gguf` · venv `gguf` — *Acc 0.9350, F1 0.9350, MCC 0.8700*
- [x] **GGUF Q3_K_M** — `gguf` · `bartowski/Codestral-22B-v0.1-GGUF::Codestral-22B-v0.1-Q3_K_M.gguf` · venv `gguf` — *Acc 0.9350, F1 0.9347, MCC 0.8700*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/Codestral-22B-v0.1-GGUF::Codestral-22B-v0.1-Q4_K_M.gguf` · venv `gguf` — *Acc 0.9400, F1 0.9388, MCC 0.8807*

---

**Progress: 24 / 24 runs complete.** ✅ (Meta-Llama-3.1-8B ×10 + CodeLlama-7b ×4 + DeepSeek-Coder-V2-Lite ×2 + Qwen2.5-Coder-7B ×4 + Llama-4-Scout ×4)

**Additional models** (beyond the original OCD-mirrored set): aya-expanse-8b BF16 (added 2026-07-16); Qwen2.5-Coder-7B Q4_K_M + AIZU QLoRA fine-tuned adapter (added 2026-07-18, MCC 0.6821→0.9550), and its cross-language-trained variant AIZU-CL (added 2026-07-19, MCC 0.9550 — identical on Java); Codestral-22B-v0.1 BF16 + GGUF Q2_K/Q3_K_M/Q4_K_M (added 2026-07-19, MCC 0.8775 BF16, ~0.87–0.88 across quants — near-lossless, Q4_K_M 0.8807 edges out BF16); cogito-v1-preview-llama-8B GGUF Q2_K/Q3_K_M/Q4_K_M (no BF16 run; steep bit-width sensitivity, MCC 0.2025/0.3701/0.7680).
