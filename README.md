# Fewer Bits, Same Clones? Evaluating LLM Quantization for Code Clone Detection

This repository is the replication package for the paper *Fewer Bits, Same Clones? Evaluating LLM Quantization for Code Clone Detection*, an empirical study of how weight quantization and clone-specific fine-tuning affect the ability of open LLMs to detect code clones.
It contains the benchmarks we built, the inference and fine-tuning code, the raw per-round outputs of every run, and the scripts that turn those outputs into every table and figure in the paper.

The repository follows the structure of the paper.
Each research question has its own folder with the scripts that produce its tables and figures.

## Research questions

| RQ | Question | Folder |
| --- | --- | --- |
| RQ1 | How does quantization affect LLM performance on code clone detection compared with full-precision baselines? (bit-width, quantization technique, single-language and cross-language clones) | [`rq1_effect_of_quantization/`](rq1_effect_of_quantization/) |
| RQ2 | What are the resource-performance trade-offs of quantized LLMs for code clone detection? | [`rq2_resource_tradeoffs/`](rq2_resource_tradeoffs/) |
| RQ3 | How does fine-tuning on clone-specific data affect the performance of quantized LLMs for code clone detection? | [`rq3_fine_tuning/`](rq3_fine_tuning/) |

## Layout

```
prompt.md                       the clone detection prompt used in every run
benchmarks/                     Section 4.4: benchmarks
  prepare_gcj_clones.py           builds GCJ-Java from GCJ2-4lang
  prepare_gcj_crosslang.py        builds GCJ cross-language from GCJ2-4lang
  gcj_java/                       400 labelled pairs over 100 Java files
  gcj_crosslang/                  384 labelled pairs over 192 Java, C++, Python, and PHP files
inference/                      Sections 4.2, 4.3, and 4.6: running the models
  run_quantization.py             inference runner for every model and quantization backend
  scripts/                        the shell scripts that ran each batch of experiments
  requirements/                   pinned environments, one per backend
  provenance/                     model revisions, source commits, patches, and hardware
results/                        raw outputs shared by all RQs
  ocd/  gcj_java/  gcj_crosslang/ per-round result CSVs, one folder per model
  logs/                           console logs of the runs
  configurations.csv              maps each run to its model, technique, and bit-width
  load_results.py                 loads the metrics of the configurations in the paper
rq1_effect_of_quantization/     Section 5.1, Tables 3 to 5, Figures 1 and 2
  evaluate_results.py             computes the metrics and writes the summary tables
  statistical_tests.py            significance tests for the RQ1 findings
  tables/                         evaluation summaries (CSV and LaTeX) per benchmark
  figures/                        plot_technique.py, plot_cross_language.py, and their output
rq2_resource_tradeoffs/         Section 5.2, Figures 3 and 4
  plot_pareto.py                  weight memory, Pareto fronts, and the two figures
  figures/
rq3_fine_tuning/                Section 5.3, Table 6
  prepare_aizu_finetune.py        builds the AIZU384F training set
  prepare_aizu_cl_finetune.py     builds the AIZU324CLF cross-language training set
  train_lora.py                   QLoRA training for the 7B and 8B models
  train_lora_vlm.py               LoRA training for Qwen3.6-27B
  generate_finetune_table.py      baseline and fine-tuned MCC on both GCJ benchmarks
  tables/
docs/                           experiment notes, model list, and run trackers
archive/                        superseded summary tables, kept for reference
```

## From the paper to the files

| Paper | Files | How to regenerate |
| --- | --- | --- |
| Section 4.4, Benchmarks | `benchmarks/` | `python benchmarks/prepare_gcj_clones.py`, `python benchmarks/prepare_gcj_crosslang.py` |
| Section 4.5, Metrics | `rq1_effect_of_quantization/evaluate_results.py` | see below |
| Table 3 (OCD) | `rq1_effect_of_quantization/tables/evaluation_summary_ocd.*` | `evaluate_results.py --dataset ocd` |
| Table 4 (GCJ-Java) | `rq1_effect_of_quantization/tables/evaluation_summary_gcj_java.*` | `evaluate_results.py --dataset gcj-java` |
| Table 5 (GCJ cross-language) | `rq1_effect_of_quantization/tables/evaluation_summary_gcj_crosslang.*` | `evaluate_results.py --dataset gcj-cross-language` |
| Statistical tests for RQ1 | `rq1_effect_of_quantization/tables/stats_*.csv` | `statistical_tests.py` |
| Figure 1, quantization techniques | `rq1_effect_of_quantization/figures/rq1_technique_bar.pdf` | `plot_technique.py` |
| Figure 2, cross-language clones | `rq1_effect_of_quantization/figures/rq1_xlang_bar.pdf` | `plot_cross_language.py` |
| Figures 3 and 4, Pareto fronts | `rq2_resource_tradeoffs/figures/rq2_pareto*.pdf` | `plot_pareto.py` |
| Table 6, fine-tuning | `rq3_fine_tuning/tables/finetune_results.*` | `generate_finetune_table.py` |
| Table A.7, checkpoints | `docs/models.md`, `inference/provenance/hf_revisions.txt` | |

## Reproducing the tables and figures

The raw outputs are included, so the tables and figures can be regenerated without a GPU.
Run all commands from the repository root.
`evaluate_results.py` and `generate_finetune_table.py` need Python 3.10 or later, and the figure scripts need `matplotlib`.

```bash
# Tables 3 to 5: majority vote over five rounds, unparseable answers counted as non-clones
for d in ocd gcj-java gcj-cross-language; do
  python rq1_effect_of_quantization/evaluate_results.py --dataset $d \
    --mode majority-vote --unknown-as non-clone --latex
done

# Figures 1 and 2 (RQ1) and Figures 3 and 4 (RQ2)
python rq1_effect_of_quantization/figures/plot_technique.py
python rq1_effect_of_quantization/figures/plot_cross_language.py
python rq2_resource_tradeoffs/plot_pareto.py

# Table 6 (RQ3)
python rq3_fine_tuning/generate_finetune_table.py

# Statistical tests for RQ1 (needs numpy, scipy, pandas, and statsmodels)
python rq1_effect_of_quantization/statistical_tests.py
```

`statistical_tests.py` rebuilds the per-pair majority-vote predictions with the paper's setting and checks that they reproduce the MCC in Tables 3 to 5 before running any test.
It compares every quantized configuration with its BF16 model (exact McNemar test with Holm correction and a paired bootstrap confidence interval for the change in MCC).
It also tests whether robustness to quantization depends more on the model than on the bit-width (two-way ANOVA on the change in MCC and a logistic GEE on per-pair correctness).
Finally, it tests whether quantization costs more on cross-language clones than on Java clones (sign, Wilcoxon signed-rank, and mixed-model tests, and a difference-in-differences bootstrap per configuration).
With 10,000 bootstrap samples, the default, the run takes about a minute.

The paper counts an answer that cannot be parsed, or a `DONT-KNOW` answer, as a non-clone prediction, so every pair is evaluated.
`evaluate_results.py` defaults to excluding such answers, so pass `--unknown-as non-clone` to reproduce the paper.
`generate_finetune_table.py` uses the paper's setting by default.

## Configurations

`results/configurations.csv` lists every run on each benchmark with its model, technique, bit-width, and whether the paper reports it (`in_paper`).
The paper reports 40 configurations per benchmark: nine full-precision (BF16) models, 25 GGUF models, and six configurations of Llama-3.1-8B-Instruct with AQLM, HIGGS-GPTQ, and QTIP.

The results folders also contain runs that the paper does not report.
These are the community GGUF releases of Qwen2.5-Coder-7B-Instruct (`technique` is `GGUF (community)`), which the paper replaces with GGUF models that we quantized ourselves from the BF16 checkpoint, the Qwen3-Coder-30B-A3B runs, and the fine-tuning runs, which belong to RQ3.
The fine-tuning table also lists Qwen2.5-Coder-7B with adapters on its community Q4_K_M model, which the paper does not report.

## Rerunning the experiments

Rerunning inference needs GPUs and one Python environment per quantization backend.
`inference/run_quantization.py` loads a model through one of the backends below, runs every pair for the given number of rounds, and writes one CSV per round.
It resumes automatically from completed rounds and pairs.

| Backend | Format | Notes |
| --- | --- | --- |
| `original`, `deepseek`, `codellama` | BF16 (full precision) | Hugging Face `transformers` |
| `gguf`, `qwen` | GGUF k-quants (Q2_K, Q3_K_M, Q4_K_M) | llama.cpp |
| `aqlm`, `higgs`, `qtip` | 2-bit, 3-bit, and 4-bit research quantization | AQLM, HIGGS-GPTQ, QTIP |
| `qwen36` | Qwen3.6-27B (text only) | `transformers` |
| `gguf_lora` | GGUF model with a LoRA adapter | applies a fine-tuned adapter at inference (RQ3) |

```bash
# One configuration on GCJ-Java, five rounds
<venv>/bin/python inference/run_quantization.py <backend> "<hf_model>" \
  --pairs-file benchmarks/gcj_java/pairs.csv \
  --output "results/gcj_java/<Model>/results_<name>" --rounds 5
```

`docs/exp_notes.md` documents each backend's environment, version pins, source builds, and exact commands.
`inference/requirements/` holds the pinned environments, and `inference/provenance/` records the model revisions, source commits, local patches, and hardware.
The scripts in `inference/scripts/` are the batches we ran on our GPU servers, so they refer to our server paths and virtual environments.

## Data not included

Some inputs are not redistributed and are ignored by Git.

- `benchmarks/ocd/tests/`: the OCD benchmark (Ragkhitwetsagul et al.).
- `benchmarks/gcj4.pkl`: the [GCJ2-4lang](https://huggingface.co/datasets/lihy11/GCJ2-4lang) dataset, from which `prepare_gcj_*.py` build the GCJ benchmarks. The built benchmarks are included.
- `rq3_fine_tuning/data/`: the AIZU datasets from [SimilBench](https://github.com/UCL-CREST/SimilBench), which `prepare_aizu*_finetune.py` turn into training data.
- `rq3_fine_tuning/models/`: the trained LoRA adapters.
- The model checkpoints, which are downloaded from Hugging Face (see Table A.7 in the paper and `docs/models.md`).

## Licence

This package is released under the [MIT License](LICENSE).
The benchmark programs come from Google Code Jam, the AIZU Online Judge, and OCD, and remain subject to their original terms.
