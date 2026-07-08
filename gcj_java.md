# GCJ Java Clone Experiments — Run Tracker

Mirrors the full model set from the OCD study, now on the **GCJ Java** clone set.

- **Dataset:** `gcj_java_clones/pairs.csv` (400 pairs, 200 clone / 200 non-clone — 20 problems × 5 Java submissions)
- **Rounds:** 5 per run

> ⚠️ **Dataset resized 96 → 400 pairs (2026-07-08).** Regenerated from a fresh random
> sample, so all prior results (computed on the **old 96-pair** set) are stale. The
> boxes below have been reset and the old `results_gcj_java/` outputs and `.tex`
> summaries cleared; all 24 runs must be re-run against the new `pairs.csv`.
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

- [ ] **Original (BF16)** — `original` · `meta-llama/Meta-Llama-3.1-8B-Instruct` · venv `aqlm_venv310`
- [ ] **GGUF Q2_K** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q2_K.gguf` · venv `gguf`
- [ ] **GGUF Q3_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf` · venv `gguf`
- [ ] **GGUF Q4_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf` · venv `gguf`
- [ ] **AQLM PV 2-bit** — `aqlm` · `ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf` · venv `aqlm_venv310`
- [ ] **HIGGS-GPTQ 3-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit` · venv `higgs_venv`
- [ ] **HIGGS-GPTQ 4-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit` · venv `higgs_venv`
- [ ] **QTIP 2-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-2Bit` · venv `qtip_venv`
- [ ] **QTIP 3-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-3Bit` · venv `qtip_venv`
- [ ] **QTIP 4-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit` · venv `qtip_venv`

## CodeLlama-7b-Instruct-hf

- [ ] **Original (BF16)** — `codellama` · `codellama/CodeLlama-7b-Instruct-hf` · venv `codellama_venv`
- [ ] **GGUF Q2_K** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf` · venv `gguf`
- [ ] **GGUF Q3_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf` · venv `gguf`
- [ ] **GGUF Q4_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf` · venv `gguf`

## DeepSeek-Coder-V2-Lite-Instruct

- [ ] **Original (BF16)** — `deepseek` · `deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct` · venv `deepseek_venv`
- [ ] **GGUF Q4_K_M** — `gguf` · `bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF::DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf` · venv `gguf`

## Qwen2.5-Coder-7B-Instruct

- [ ] **Original (BF16)** — `original` · `Qwen/Qwen2.5-Coder-7B-Instruct` · venv `aqlm_venv310`
- [ ] **GGUF Q2_K** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf` · venv `gguf`
- [ ] **GGUF Q3_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf` · venv `gguf`
- [ ] **GGUF Q4_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf` · venv `gguf`

## Llama-4-Scout-17B-16E-Instruct

- [ ] **Original (BF16)** — `original` · `meta-llama/Llama-4-Scout-17B-16E-Instruct` · venv `llama4_venv`
- [ ] **GGUF Q2_K** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q2_K.gguf` · venv `gguf`
- [ ] **GGUF Q3_K_S** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q3_K_S.gguf` · venv `gguf`
- [ ] **GGUF Q4_K_M** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q4_K_M-00001-of-00002.gguf` (split) · venv `gguf`

---

**Progress: 0 / 24 runs complete.** (Meta-Llama-3.1-8B ×10 + CodeLlama-7b ×4 + DeepSeek-Coder-V2-Lite ×2 + Qwen2.5-Coder-7B ×4 + Llama-4-Scout ×4)
