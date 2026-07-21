#!/usr/bin/env python3
"""Generate a focused LaTeX table for the QLoRA fine-tuning results (RQ5).

Unlike ``evaluate_results.py`` (which reports every model/quantization in one
big per-dataset table), this pairs each fine-tuned adapter with the *same*
Q4_K_M GGUF baseline it was applied on top of, on both GCJ test sets, so the
baseline$\\to$fine-tuned lift is read off directly.

Metrics are computed by reusing ``evaluate_results.evaluate_majority_vote`` (the
same 5-round majority-vote logic), so numbers match the main summaries exactly.

Outputs:
  finetune_results.csv   one row per (model, adapter, dataset): base + FT metrics
  finetune_results.tex   a booktabs LaTeX table (MCC-focused)

Usage:
  python generate_finetune_table.py                 # writes both files
  python generate_finetune_table.py --metric f1     # table keyed on F1 instead of MCC
  python generate_finetune_table.py --tex out.tex --csv out.csv
"""
import argparse
import csv
import glob
import io
import os
from contextlib import redirect_stdout
from pathlib import Path

import evaluate_results as er

# Datasets: (key, results_dir, column header)
DATASETS = [
    ("java",  "results_gcj_java",      "GCJ-Java"),
    ("xlang", "results_gcj_crosslang", "GCJ x-lang"),
]

# One entry per base model. `subdir` is the per-model results folder; `baseline`
# and each adapter `basename` are the run basenames (without the _roundN suffix).
MODELS = [
    {
        "model": "Qwen2.5-Coder-7B",
        "subdir": "Qwen2.5-Coder-7B-Instruct",
        "baseline": "results_qwen2.5_coder_7B_q4km",
        "adapters": [
            ("AIZU384F (mono)",  "results_qwen_q4km_aizu_lora"),
            ("AIZU324CLF (x-l)", "results_qwen_q4km_aizuCL_lora"),
        ],
    },
    {
        "model": "Meta-Llama-3.1-8B",
        "subdir": "Meta-Llama-3.1-8B-Instruct",
        "baseline": "results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf",
        "adapters": [
            ("AIZU384F (mono)", "results_llama3.1_8b_q4km_aizu_lora"),
        ],
    },
    {
        "model": "CodeLlama-7b",
        "subdir": "CodeLlama-7b-Instruct-hf",
        "baseline": "results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q4_K_M.gguf",
        "adapters": [
            ("AIZU384F (mono)", "results_codellama_7b_q4km_aizu_lora"),
        ],
    },
]


def metrics_for(results_dir: str, subdir: str, basename: str) -> dict | None:
    """Majority-vote metrics for one run basename, or None if no files/rows.

    Reuses evaluate_results.evaluate_majority_vote; its console report is
    suppressed so only the returned metrics dict is used.
    """
    paths = sorted(glob.glob(os.path.join(results_dir, subdir, f"{basename}_round*.csv")))
    if not paths:
        return None
    with redirect_stdout(io.StringIO()):
        results = er.evaluate_majority_vote(paths, unknown_as="exclude")
    results = [r for r in results if r]
    return results[0] if results else None


def fmt(v, nd=3):
    return f"{v:.{nd}f}" if isinstance(v, (int, float)) else "---"


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--metric", default="mcc", choices=["mcc", "f1", "accuracy"],
                    help="Metric shown in the LaTeX table (default: mcc). The CSV "
                         "always contains accuracy/precision/recall/f1/mcc.")
    ap.add_argument("--tex", default="summaries/finetune_results.tex")
    ap.add_argument("--csv", default="summaries/finetune_results.csv")
    args = ap.parse_args()

    # ---- Collect metrics: one record per (model, adapter, dataset) ----
    records = []          # flat rows for the CSV
    table = []            # (model, adapter_label, {dskey: (base, ft)}) for LaTeX
    for m in MODELS:
        base_by_ds = {}
        for dskey, rdir, _ in DATASETS:
            base_by_ds[dskey] = metrics_for(rdir, m["subdir"], m["baseline"])

        for adapter_label, adapter_base in m["adapters"]:
            per_ds = {}
            for dskey, rdir, _ in DATASETS:
                base = base_by_ds[dskey]
                ft = metrics_for(rdir, m["subdir"], adapter_base)
                per_ds[dskey] = (base, ft)
                for kind, res in (("baseline_q4km", base), ("finetuned", ft)):
                    if res:
                        records.append({
                            "model": m["model"], "adapter": adapter_label,
                            "dataset": dskey, "kind": kind,
                            "accuracy": res["accuracy"], "precision": res["precision"],
                            "recall": res["recall"], "f1": res["f1"], "mcc": res["mcc"],
                            "rows_excluded": res["rows_excluded"],
                        })
            table.append((m["model"], adapter_label, per_ds))

    # ---- Write CSV ----
    with open(args.csv, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=["model", "adapter", "dataset", "kind",
                                          "accuracy", "precision", "recall", "f1",
                                          "mcc", "rows_excluded"])
        w.writeheader()
        w.writerows(records)
    print(f"CSV written to: {args.csv}  ({len(records)} rows)")

    # ---- Write LaTeX ----
    mk = args.metric
    metric_name = {"mcc": "MCC", "f1": "F1", "accuracy": "Accuracy"}[mk]
    lines = [
        r"% Requires \usepackage{booktabs}.",
        r"\begin{table}[htbp]",
        r"  \centering",
        f"  \\caption{{QLoRA fine-tuning results ({metric_name}, 5-round majority "
        r"vote). Each adapter is applied on top of the same Q4\_K\_M GGUF base; "
        r"``base'' is that base without the adapter, ``+FT'' is with it, and "
        r"$\Delta$ is the lift. Adapters are trained on AIZU (disjoint from GCJ).}",
        r"  \label{tab:finetune_results}",
        r"  \small",
        r"  \begin{tabular}{ll ccc ccc}",
        r"    \toprule",
        r"    & & \multicolumn{3}{c}{\textbf{" + DATASETS[0][2] + r"}}"
        r" & \multicolumn{3}{c}{\textbf{" + DATASETS[1][2] + r"}} \\",
        r"    \cmidrule(lr){3-5}\cmidrule(lr){6-8}",
        r"    \textbf{Model} & \textbf{Fine-tune data}"
        r" & base & +FT & $\Delta$ & base & +FT & $\Delta$ \\",
        r"    \midrule",
    ]
    for model, adapter_label, per_ds in table:
        cells = [model.replace("_", r"\_"), adapter_label.replace("_", r"\_")]
        for dskey, _, _ in DATASETS:
            base, ft = per_ds[dskey]
            bv = base[mk] if base else None
            fv = ft[mk] if ft else None
            delta = (f"$+{fv - bv:.3f}$" if (isinstance(bv, (int, float))
                     and isinstance(fv, (int, float)) and fv >= bv)
                     else (f"${fv - bv:.3f}$" if isinstance(bv, (int, float))
                           and isinstance(fv, (int, float)) else "---"))
            cells += [fmt(bv), r"\textbf{" + fmt(fv) + r"}" if fv is not None else "---", delta]
        lines.append("    " + " & ".join(cells) + r" \\")
    lines += [r"    \bottomrule", r"  \end{tabular}", r"\end{table}", ""]

    with open(args.tex, "w") as f:
        f.write("\n".join(lines))
    print(f"LaTeX table written to: {args.tex}")


if __name__ == "__main__":
    main()
