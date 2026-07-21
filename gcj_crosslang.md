# GCJ Cross-Language Clone Experiments — Run Tracker

Same 24-model set as the [GCJ Java tracker](gcj_java.md), now on the
**cross-language** clone set (GCJ^CL).

- **Dataset:** `gcj_crosslang_clones/pairs.csv` (384 pairs, 192 clone / 192 non-clone; true pairs are same-problem cross-language, false pairs are different-problem + different-language)
- **Rounds:** 5 per run
- **Results:** `results_gcj_crosslang/<Model>/...`
- **Prompt language:** filled per pair from `lang1`/`lang2`, e.g. "Compare the two Java and C++ code snippets." (harness `--pairs-file` mode)
- **Eval (all models):** `python evaluate_results.py --dataset gcj-cross-language --mode majority-vote`
  (auto-selects `results_gcj_crosslang/` and writes `results_gcj_crosslang/evaluation_summary_gcj_crosslang.csv`)
- **Eval (one model):** `python evaluate_results.py results_gcj_crosslang/<Model>/*.csv --mode majority-vote --output results_gcj_crosslang/evaluation_summary_gcj_crosslang.csv`

Check a box once **all 5 rounds** for that run are complete. Runs are launched
as per-family chains, one GPU each, in parallel:
`chain_<family>_gcj_crosslang.sh` (metallama→GPU1, codellama→GPU2,
deepseek→GPU3, qwen→GPU5, scout_gguf→GPU6).

---

## Meta-Llama-3.1-8B-Instruct

- [x] **Original (BF16)** — `original` · `meta-llama/Meta-Llama-3.1-8B-Instruct` · venv `aqlm_venv310` — *Acc 0.6107, F1 0.7224, MCC 0.3247*
- [x] **GGUF Q2_K** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::...Q2_K.gguf` · venv `gguf` — *Acc 0.5130, F1 0.6714, MCC 0.0973*
- [x] **GGUF Q3_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::...Q3_K_M.gguf` · venv `gguf` — *Acc 0.5317, F1 0.6845, MCC 0.1587*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::...Q4_K_M.gguf` · venv `gguf` — *Acc 0.5654, F1 0.6971, MCC 0.2646*
- [x] **GGUF Q4_K_M + AIZU QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/llama3.1-8b-aizu-qlora-F16.gguf` · venv `gguf` — *Acc 0.9375, F1 0.9358, MCC 0.8762* (adapter trained on monolingual AIZU384F; +0.612 MCC — see exp_notes.md)
- [x] **AQLM PV 2-bit** — `aqlm` · `ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf` · venv `aqlm_venv310` — *Acc 0.5104, F1 0.6713, MCC 0.1026*
- [x] **HIGGS-GPTQ 3-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit` · venv `higgs_venv` — *Acc 0.5409, F1 0.6882, MCC 0.1910*
- [x] **HIGGS-GPTQ 4-bit** — `higgs` · `ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit` · venv `higgs_venv` — *Acc 0.5699, F1 0.6998, MCC 0.2458 (re-run after transient round-1 CUDA assert)*
- [x] **QTIP 2-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-2Bit` · venv `qtip_venv` — *Acc 0.5039, F1 0.6678, MCC 0.0298*
- [x] **QTIP 3-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-3Bit` · venv `qtip_venv` — *Acc 0.5726, F1 0.7033, MCC 0.2693*
- [x] **QTIP 4-bit** — `qtip` · `relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit` · venv `qtip_venv` — *Acc 0.5957, F1 0.7154, MCC 0.3035*

## CodeLlama-7b-Instruct-hf

- [x] **Original (BF16)** — `codellama` · `codellama/CodeLlama-7b-Instruct-hf` · venv `codellama_venv` — *Acc 0.5631, F1 0.7205, MCC 0.0000 (degenerate: predicts all CLONE)*
- [x] **GGUF Q2_K** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf` · venv `gguf` — *Acc 0.5000, F1 0.6667, MCC 0.0000 (degenerate)*
- [x] **GGUF Q3_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf` · venv `gguf` — *Acc 0.5220, F1 0.6859, MCC 0.0000 (degenerate)*
- [x] **GGUF Q4_K_M** — `gguf` · `QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf` · venv `gguf` — *Acc 0.5120, F1 0.6772, MCC 0.0000 (degenerate)*
- [x] **GGUF Q4_K_M + AIZU QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/codellama-7b-aizu-qlora-F16.gguf` · venv `gguf` — *Acc 0.8750, F1 0.8681, MCC 0.7541* (adapter trained on monolingual AIZU384F; rescues degenerate baseline 0.0000→0.7541 — see exp_notes.md)

## DeepSeek-Coder-V2-Lite-Instruct

- [x] **Original (BF16)** — `deepseek` · `deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct` · venv `deepseek_venv` — *Acc 0.8828, F1 0.8794, MCC 0.7669*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF::DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf` · venv `gguf` — *Acc 0.8724, F1 0.8672, MCC 0.7471*

## Qwen2.5-Coder-7B-Instruct

- [x] **Original (BF16)** — `original` · `Qwen/Qwen2.5-Coder-7B-Instruct` · venv `aqlm_venv310` — *Acc 0.8073, F1 0.7628, MCC 0.6630 (precision 0.99)*
- [x] **GGUF Q2_K** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf` · venv `gguf` — *Acc 0.8982, F1 0.8908, MCC 0.8044 (best Qwen)*
- [x] **GGUF Q3_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf` · venv `gguf` — *Acc 0.8333, F1 0.8012, MCC 0.7044*
- [x] **GGUF Q4_K_M** — `qwen` · `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf` · venv `gguf` — *Acc 0.8516, F1 0.8267, MCC 0.7339*
- [x] **GGUF Q4_K_M + AIZU QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/qwen2.5-coder-7b-aizu-qlora-F16.gguf` · venv `gguf` — *Acc 0.9557, F1 0.9563, MCC 0.9118* (adapter trained on monolingual AIZU384F; transfers to cross-language — see exp_notes.md)
- [x] **GGUF Q4_K_M + AIZU-CL QLoRA (fine-tuned)** — `gguf_lora` · Q4_K_M base + `finetune_models/qwen2.5-coder-7b-aizuCL-qlora-F16.gguf` · venv `gguf` — *Acc 0.9583, F1 0.9588, MCC 0.9169* (adapter trained on cross-language AIZU324CLF; ~tie with monolingual adapter — see exp_notes.md)

## Llama-4-Scout-17B-16E-Instruct

- [x] **Original (BF16)** — `original` · `meta-llama/Llama-4-Scout-17B-16E-Instruct` · venv `llama4_venv` *(run separately on GPUs 3,4,5,7)* — *Acc 0.9714, F1 0.9711, MCC 0.9428*
- [x] **GGUF Q2_K** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q2_K.gguf` · venv `gguf` — *Acc 0.9714, F1 0.9710, MCC 0.9430*
- [x] **GGUF Q3_K_S** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q3_K_S.gguf` · venv `gguf` — *Acc 0.9688, F1 0.9683, MCC 0.9380*
- [x] **GGUF Q4_K_M** — `gguf` · `bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF::...Q4_K_M-00001-of-00002.gguf` (split) · venv `gguf` — *Acc 0.9661, F1 0.9655, MCC 0.9329*

## aya-expanse-8b

- [x] **Original (BF16)** — `original` · `CohereLabs/aya-expanse-8b` · venv `aqlm_venv310` — *Acc 0.7995, F1 0.8031, MCC 0.5994* (0 excluded)

## cogito-v1-preview-llama-8B

- [x] **GGUF Q2_K** — `gguf` · `cortexso/cogito-v1::cogito-v1-preview-llama-8b-q2_k.gguf` · venv `gguf` — *Acc 0.5681, F1 0.6771, MCC 0.1787* (2 excluded)
- [x] **GGUF Q3_K_M** — `gguf` · `cortexso/cogito-v1::cogito-v1-preview-llama-8b-q3_k_m.gguf` · venv `gguf` — *Acc 0.5937, F1 0.7105, MCC 0.2911* (5 excluded)
- [x] **GGUF Q4_K_M** — `gguf` · `cortexso/cogito-v1::cogito-v1-preview-llama-8b-q4_k_m.gguf` · venv `gguf` — *Acc 0.8407, F1 0.8578, MCC 0.7009* (1 excluded)

---

**Progress: 24 / 24 runs complete.** ✅ (Meta-Llama-3.1-8B ×10 + CodeLlama-7b ×4 + DeepSeek-Coder-V2-Lite ×2 + Qwen2.5-Coder-7B ×4 + Llama-4-Scout ×4)

**Additional models** (beyond the original OCD-mirrored set): aya-expanse-8b BF16 (added 2026-07-16); Qwen2.5-Coder-7B Q4_K_M + AIZU QLoRA fine-tuned adapter (added 2026-07-18, MCC 0.7339→0.9118), and its cross-language-trained variant AIZU-CL (added 2026-07-19, MCC 0.9169 — ~tie); cogito-v1-preview-llama-8B GGUF Q2_K/Q3_K_M/Q4_K_M (no BF16 run; steep bit-width sensitivity, MCC 0.1787/0.2911/0.7009).
