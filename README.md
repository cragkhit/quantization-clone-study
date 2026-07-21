# Quantization & Fine-Tuning for LLM Code Clone Detection

A study of how **weight quantization** — and, more recently, **clone-specific
fine-tuning** — affect the ability of instruction-tuned LLMs to perform
**code clone detection**: deciding whether two code snippets are functionally
similar.

The task is framed as pairwise binary classification. For each pair the model is
given a fixed prompt (`prompt.md`) and must answer `YES-SIMILAR`,
`NO-NOT-SIMILAR`, or `DONT-KNOW` as a small JSON object. Runs are repeated for
5 rounds and reduced by majority vote; the headline metric is **MCC** (Matthews
Correlation Coefficient), which is robust on the balanced sets and penalizes the
degenerate "predict-everything-CLONE" failure mode.

## Research questions

- **RQ1** — How does quantization **bit-width** affect performance vs. the
  full-precision baseline?
- **RQ2** — Which quantization **method** (GGUF, AQLM, HIGGS-GPTQ, QTIP) best
  preserves capability at equal bit-width?
- **RQ3** — Is the impact consistent across **languages and clone types**?
- **RQ4** — What are the **resource–performance trade-offs**?
- **RQ5** — How does **fine-tuning on clone-specific data** affect a quantized
  model?

## Pipeline

```
prepare_gcj_*.py / ocd/        →  datasets (code files + labeled pairs)
        │
run_quantization.py <backend>  →  per-round result CSVs (one row per pair)
        │
evaluate_results.py            →  confusion matrix + P/R/F1/MCC, CSV + LaTeX tables
```

1. **`run_quantization.py`** — the inference runner. Loads a model via one of
   several *backends* (see below), runs every pair, and writes
   `..._round{N}.csv`. Resumes automatically (completed rounds/pairs are
   skipped). Two input modes:
   - OCD: the n×n Cartesian product of the `ocd/tests/` Java files.
   - `--pairs-file <csv>`: an explicit labeled pair list (the GCJ sets).
2. **`evaluate_results.py`** — parses the `response` JSON, maps
   `YES-SIMILAR→CLONE` / `NO-NOT-SIMILAR→NON-CLONE`, applies majority vote across
   rounds, and emits a summary CSV plus booktabs **LaTeX** tables. Select a
   dataset with `--dataset {ocd,gcj-java,gcj-cross-language}`.
3. **`prompt.md`** — the single prompt template used identically at every stage.

## Datasets

| Dataset | Dir | Pairs | Language(s) | Clone type |
| --- | --- | --- | --- | --- |
| OCD | `ocd/tests/` | 10,000 (100 files, n×n) | Java | Syntactic / near-miss (compile–decompile, obfuscation) |
| GCJ-Java | `gcj_java_clones/` | 400 (200/200) | Java | Semantic, same-problem (Type-4) |
| GCJ cross-language | `gcj_crosslang_clones/` | 384 (192/192) | Java, C++, Python, PHP | Cross-language semantic |

The GCJ sets are derived from Google Code Jam submissions (`gcj4.pkl`) by
`prepare_gcj_clones.py` and `prepare_gcj_crosslang.py`. Per-run status and
metrics are tracked in **`gcj_java.md`** and **`gcj_crosslang.md`**.

## Models & quantization backends

`run_quantization.py` dispatches to one of these backends (each in its own
virtual environment — see `exp_notes.md`):

| Backend | Format | Notes |
| --- | --- | --- |
| `original`, `deepseek`, `codellama` | BF16 (full precision) | HuggingFace `transformers` |
| `gguf`, `qwen` | GGUF k-quants (Q2_K / Q3_K_M / Q4_K_M …) | llama.cpp |
| `aqlm`, `higgs`, `qtip` | 2/3/4-bit research quantization | AQLM · HIGGS-GPTQ · QTIP |
| `qwen3fp8` | FP8 (MoE) | Qwen3-Coder-30B-A3B via vLLM |
| `qwen36` | Qwen3.6-27B (VLM, text-only) | via transformers |
| `gguf_lora` | GGUF base **+ LoRA adapter** | applies a fine-tuned adapter at inference (RQ5) |

Models evaluated include Meta-Llama-3.1-8B (the one base quantized by **all**
methods), Qwen2.5-Coder-7B, CodeLlama-7b, DeepSeek-Coder-V2-Lite, Llama-4-Scout-17B,
Codestral-22B, aya-expanse-8b, and cogito-v1-preview-llama-8B. See the trackers
for the full grid and MCCs.

## Fine-tuning (RQ5)

A **QLoRA** adapter is trained on an *independent* clone benchmark
([SimilBench](https://github.com/UCL-CREST/SimilBench) **AIZU**, disjoint from
the GCJ test sets) and applied on top of the **same** Q4_K_M GGUF at inference,
so the metric delta isolates the fine-tuning lift at fixed quantization.

```
prepare_aizu_finetune.py      →  finetune_data/aizu_{train,val}.jsonl   (monolingual AIZU384F)
prepare_aizu_cl_finetune.py   →  finetune_data/aizu_cl_{train,val}.jsonl (cross-language AIZU324CLF)
        │
train_lora.py                 →  finetune_models/<name>-qlora/  (LoRA adapter, ~40 MB)
        │
llama.cpp/convert_lora_to_gguf.py  →  <name>-qlora-F16.gguf
        │
run_quantization.py gguf_lora "<repo>::<base>.gguf::<adapter>.gguf"  →  result CSVs
        │
generate_finetune_table.py    →  focused RQ5 LaTeX/CSV (base → +FT MCC lift)
```

Fine-tuning gives large gains at fixed quantization — e.g. on GCJ-Java, MCC
**0.68 → 0.96** (Qwen2.5-Coder-7B) and **0.05 → 0.77** (CodeLlama-7b, rescuing a
degenerate baseline). See `exp_notes.md` for the full results and method notes.

## Repository layout

```
run_quantization.py        inference runner (backends + input modes)
evaluate_results.py        metrics + LaTeX/CSV summary generator
prompt.md                  the clone-detection prompt template
prepare_gcj_*.py           build the GCJ pair datasets from gcj4.pkl
prepare_aizu*_finetune.py  build QLoRA training data from AIZU
train_lora.py              QLoRA fine-tuning (4-bit NF4 base, LoRA adapter)
generate_finetune_table.py focused RQ5 results table
gcj_java.md, gcj_crosslang.md  per-run trackers (status + MCC)
exp_notes.md               per-backend setup, venvs, workarounds, results
summaries/                 generated summary CSVs + LaTeX tables, plus
                           experiment_report.tex (self-contained setup + RQ1–RQ5 write-up)
results*/                  per-round result CSVs (OCD / GCJ-Java / GCJ x-lang)
gcj*_clones/               derived GCJ datasets (source files + pairs.csv)
finetune_data/, finetune_models/   AIZU training data + trained adapters
<backend>_venv/, gguf/     per-backend Python environments
```

## Reproducing

Each backend needs its own environment; `exp_notes.md` documents the exact venv,
pip pins, source builds (GGUF / QTIP / HIGGS kernels are compiled for the target
GPU), and per-backend commands. A typical run + evaluation:

```bash
# One model×quant on GCJ-Java (5 rounds)
<venv>/bin/python run_quantization.py <backend> "<hf_model>" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/<Model>/results_<name>" --rounds 5

# Score every run for a dataset and emit the LaTeX tables
python evaluate_results.py --dataset gcj-java --mode majority-vote --latex
```

> **Note:** `run_quantization.py` uses `str | None` syntax and needs Python 3.10+.
> Runs were executed across two GPU servers (`zeta`, A100/`sm_80`, earlier; `tau`,
> H100/`sm_90`, more recent) — see `exp_notes.md`.
