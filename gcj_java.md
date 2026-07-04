# GCJ Java Clone Experiments — Run Tracker

Mirrors the full model set from the OCD study, now on the **GCJ Java** clone set.

- **Dataset:** `gcj_java_clones/pairs.csv` (96 pairs, 48 clone / 48 non-clone)
- **Rounds:** 5 per run
- **Results:** `results_gcj_java/<Model>/...`
- **Eval:** `python evaluate_results.py results_gcj_java/<Model>/*.csv --mode majority-vote --output results_gcj_java/evaluation_summary_gcj_java.csv`

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

- [x] **Original (BF16)** — `original` · `meta-llama/Meta-Llama-3.1-8B-Instruct` · venv `aqlm_venv310` — *Acc 0.6526, F1 0.7402, MCC 0.3985*
- [x] **GGUF Q2_K** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q2_K.gguf` · venv `gguf` — *Acc 0.5104, F1 0.6713, MCC 0.1026*
- [x] **GGUF Q3_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf` · venv `gguf` — *Acc 0.5319, F1 0.6812, MCC 0.1099*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf` · venv `gguf` — *Acc 0.5729, F1 0.6963, MCC 0.2502*
- [x] **AQLM PV 2-bit** — `aqlm` · `ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf` · venv `aqlm_venv310` — *Acc 0.5000, F1 0.6667, MCC 0.0000 (degenerate: predicts all CLONE)*
- [x] **HIGGS-GPTQ 3-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit` · venv `higgs_venv` — *Acc 0.5978, F1 0.7176, MCC 0.2707*
- [x] **HIGGS-GPTQ 4-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit` · venv `higgs_venv` — *Acc 0.6562, F1 0.7402, MCC 0.4093*
- [x] **QTIP 2-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-2Bit` · venv `qtip_venv` — *Acc 0.5730, F1 0.7031, MCC 0.1751*
- [x] **QTIP 3-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-3Bit` · venv `qtip_venv` — *Acc 0.6129, F1 0.7231, MCC 0.3116*
- [x] **QTIP 4-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit` · venv `qtip_venv` — *Acc 0.6596, F1 0.7460, MCC 0.4060*

## CodeLlama-7b-Instruct-hf

- [x] **Original (BF16)** — `codellama` · `codellama/CodeLlama-7b-Instruct-hf` · venv `codellama_venv` — *Acc 0.5393, F1 0.7007, MCC 0.0000 (degenerate: predicts all CLONE)*
- [x] **GGUF Q2_K** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf` · venv `gguf` — *Acc 0.5000, F1 0.6667, MCC 0.0000 (degenerate: predicts all CLONE)*
- [x] **GGUF Q3_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf` · venv `gguf` — *Acc 0.5109, F1 0.6763, MCC 0.0000 (degenerate: predicts all CLONE)*
- [x] **GGUF Q4_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf` · venv `gguf` — *Acc 0.5000, F1 0.6667, MCC 0.0000 (degenerate: predicts all CLONE)*

## DeepSeek-Coder-V2-Lite-Instruct

- [ ] **Original (BF16)** — `deepseek` · `deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct` · venv `deepseek_venv`
- [ ] **GGUF Q4_K_M** — `gguf` · `bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF::DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf` · venv `gguf`

## Qwen2.5-Coder-7B-Instruct

- [ ] **Original (BF16)** — `original` · `Qwen/Qwen2.5-Coder-7B-Instruct` · venv `aqlm_venv310`
- [ ] **GGUF Q2_K** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf` · venv `gguf`
- [ ] **GGUF Q3_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf` · venv `gguf`
- [ ] **GGUF Q4_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf` · venv `gguf`

## Llama-4-Scout-17B-16E-Instruct

- [x] **Original (BF16)** — `original` · `meta-llama/Llama-4-Scout-17B-16E-Instruct` · venv `llama4_venv` — *done: Acc 0.9688, F1 0.9684, MCC 0.9377*
- [ ] **GGUF Q2_K** — `gguf` · `bartowski/Meta-Llama-4-Scout-17B-16E-Instruct-GGUF` (Q2_K) · venv `gguf`
- [ ] **GGUF Q3_K_S** — `gguf` · `bartowski/Meta-Llama-4-Scout-17B-16E-Instruct-GGUF` (Q3_K_S) · venv `gguf`
- [ ] **GGUF Q4_K_M** — `gguf` · `bartowski/Meta-Llama-4-Scout-17B-16E-Instruct-GGUF` (Q4_K_M) · venv `gguf`

---

**Progress: 15 / 24 runs complete.** (Meta-Llama-3.1-8B ×10 + CodeLlama-7b ×4 + Llama-4-Scout Original)
