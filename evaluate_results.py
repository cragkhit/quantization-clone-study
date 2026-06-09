"""
Evaluate clone-detection results from CSV files produced by run_quantization.py.

For each CSV it:
  - Parses the JSON `response` column to extract the model's answer
  - Maps YES-SIMILAR → CLONE, NO-NOT-SIMILAR → NON-CLONE
  - Handles DONT-KNOW / parse errors via --unknown-as (default: exclude)
  - Prints a confusion matrix plus precision, recall, F1, and MCC
  - Writes a summary CSV with all metrics (default: evaluation_summary.csv)

Usage:
    python evaluate_results.py                          # all results_*.csv in cwd
    python evaluate_results.py results_original*.csv   # specific files
    python evaluate_results.py --unknown-as non-clone  # treat unknowns as NON-CLONE
    python evaluate_results.py --unknown-as clone       # treat unknowns as CLONE
    python evaluate_results.py --unknown-as exclude    # skip unknowns (default)
    python evaluate_results.py --output summary.csv    # custom output CSV name
"""

import argparse
import csv
import glob
import json
import sys
from datetime import datetime
from pathlib import Path


# ---------------------------------------------------------------------------
# Parsing helpers
# ---------------------------------------------------------------------------

def parse_answer(response_str: str) -> str | None:
    """
    Extract the normalised answer from the model's JSON response.
    Returns 'CLONE', 'NON-CLONE', 'DONT-KNOW', or None on parse failure.
    """
    try:
        obj = json.loads(response_str)
        raw = obj.get("answer", "").strip().upper()
    except (json.JSONDecodeError, AttributeError):
        # Try to extract answer with a loose regex fallback
        import re
        m = re.search(r'"answer"\s*:\s*"([^"]+)"', response_str, re.IGNORECASE)
        raw = m.group(1).strip().upper() if m else ""

    if raw in ("YES-SIMILAR", "YES_SIMILAR", "YES"):
        return "CLONE"
    if raw in ("NO-NOT-SIMILAR", "NO_NOT_SIMILAR", "NO"):
        return "NON-CLONE"
    if raw in ("DONT-KNOW", "DONT_KNOW", "UNKNOWN"):
        return "DONT-KNOW"
    return None  # unrecognised / parse error


# ---------------------------------------------------------------------------
# Metrics
# ---------------------------------------------------------------------------

def confusion_matrix_values(y_true: list[str], y_pred: list[str], pos: str = "CLONE"):
    """Return (TP, FP, FN, TN) for binary labels."""
    neg = "NON-CLONE"
    tp = sum(1 for t, p in zip(y_true, y_pred) if t == pos and p == pos)
    fp = sum(1 for t, p in zip(y_true, y_pred) if t == neg and p == pos)
    fn = sum(1 for t, p in zip(y_true, y_pred) if t == pos and p == neg)
    tn = sum(1 for t, p in zip(y_true, y_pred) if t == neg and p == neg)
    return tp, fp, fn, tn


def compute_metrics(tp: int, fp: int, fn: int, tn: int) -> dict:
    precision = tp / (tp + fp) if (tp + fp) else 0.0
    recall    = tp / (tp + fn) if (tp + fn) else 0.0
    f1        = (2 * precision * recall / (precision + recall)
                 if (precision + recall) else 0.0)
    denom = ((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)) ** 0.5
    mcc = (tp * tn - fp * fn) / denom if denom else 0.0
    return {"precision": precision, "recall": recall, "f1": f1, "mcc": mcc}


# ---------------------------------------------------------------------------
# Report
# ---------------------------------------------------------------------------

def fmt_duration(seconds: float) -> str:
    seconds = int(seconds)
    h, rem = divmod(seconds, 3600)
    m, s = divmod(rem, 60)
    return f"{h:02d}:{m:02d}:{s:02d}"


def print_report(csv_path: str, y_true: list[str], y_pred: list[str],
                 n_excluded: int, run_seconds: float | None) -> dict:
    """Print a formatted report to stdout and return a metrics dict."""
    tp, fp, fn, tn = confusion_matrix_values(y_true, y_pred)
    m = compute_metrics(tp, fp, fn, tn)

    total = tp + fp + fn + tn
    accuracy = (tp + tn) / total if total else 0.0

    label_w = 15
    sep = "─" * 52

    duration_str = fmt_duration(run_seconds) if run_seconds is not None else "N/A"

    print(f"\n{'═' * 52}")
    print(f"  File : {Path(csv_path).name}")
    print(f"  Rows : {total + n_excluded}  "
          f"(evaluated: {total}, excluded: {n_excluded})")
    print(f"  Time : {duration_str}")
    print(f"{'═' * 52}")

    # Confusion matrix
    print(f"\n  Confusion Matrix  (positive = CLONE)\n")
    print(f"  {'':>{label_w}}  {'Pred CLONE':>12}  {'Pred NON-CLONE':>14}")
    print(f"  {sep}")
    print(f"  {'True CLONE':>{label_w}}  {tp:>12,}  {fn:>14,}")
    print(f"  {'True NON-CLONE':>{label_w}}  {fp:>12,}  {tn:>14,}")
    print(f"  {sep}")

    # Metrics
    print(f"\n  {'Metric':<14}  {'Value':>8}")
    print(f"  {'─'*26}")
    print(f"  {'Accuracy':<14}  {accuracy:>8.4f}")
    print(f"  {'Precision':<14}  {m['precision']:>8.4f}")
    print(f"  {'Recall':<14}  {m['recall']:>8.4f}")
    print(f"  {'F1-Score':<14}  {m['f1']:>8.4f}")
    print(f"  {'MCC':<14}  {m['mcc']:>8.4f}")
    print()

    return {
        "file": Path(csv_path).name,
        "rows_total": total + n_excluded,
        "rows_evaluated": total,
        "rows_excluded": n_excluded,
        "TP": tp,
        "FP": fp,
        "FN": fn,
        "TN": tn,
        "accuracy": round(accuracy, 4),
        "precision": round(m["precision"], 4),
        "recall": round(m["recall"], 4),
        "f1": round(m["f1"], 4),
        "mcc": round(m["mcc"], 4),
        "run_time": duration_str,
    }


# ---------------------------------------------------------------------------
# Per-file evaluation
# ---------------------------------------------------------------------------

_TS_FMT = "%Y-%m-%d %H:%M:%S"


def evaluate_file(csv_path: str, unknown_as: str) -> dict | None:
    """Evaluate one CSV and return its metrics dict, or None if no evaluable rows."""
    y_true: list[str] = []
    y_pred: list[str] = []
    n_excluded = 0
    first_ts: datetime | None = None
    last_ts:  datetime | None = None

    with open(csv_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            ts_raw = row.get("timestamp", "").strip()
            try:
                ts = datetime.strptime(ts_raw, _TS_FMT)
                if first_ts is None:
                    first_ts = ts
                last_ts = ts
            except ValueError:
                pass

            ground_truth = row["ground_truth"].strip().upper()
            predicted = parse_answer(row["response"])

            # Normalise ground truth
            if ground_truth not in ("CLONE", "NON-CLONE"):
                n_excluded += 1
                continue

            # Handle ambiguous predictions
            if predicted is None or predicted == "DONT-KNOW":
                if unknown_as == "exclude":
                    n_excluded += 1
                    continue
                elif unknown_as == "clone":
                    predicted = "CLONE"
                else:  # non-clone
                    predicted = "NON-CLONE"

            y_true.append(ground_truth)
            y_pred.append(predicted)

    if not y_true:
        print(f"\n[{Path(csv_path).name}] No evaluable rows found.")
        return None

    run_seconds = (last_ts - first_ts).total_seconds() if first_ts and last_ts else None
    return print_report(csv_path, y_true, y_pred, n_excluded, run_seconds)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def print_summary_table(rows: list[dict]) -> None:
    if not rows:
        return

    col_headers = ["File", "Total", "Eval", "Excl", "TP", "FP", "FN", "TN",
                   "Accuracy", "Precision", "Recall", "F1", "MCC", "Run Time"]
    col_keys    = ["file", "rows_total", "rows_evaluated", "rows_excluded",
                   "TP", "FP", "FN", "TN",
                   "accuracy", "precision", "recall", "f1", "mcc", "run_time"]
    float_cols  = {"accuracy", "precision", "recall", "f1", "mcc"}

    # Build display strings
    str_rows = []
    for r in rows:
        row = []
        for k in col_keys:
            v = r[k]
            row.append(f"{v:.4f}" if k in float_cols else str(v))
        str_rows.append(row)

    # Column widths: max of header and all values
    widths = [
        max(len(col_headers[i]), *(len(sr[i]) for sr in str_rows))
        for i in range(len(col_headers))
    ]

    def fmt_row(cells):
        return "  " + "  ".join(c.ljust(widths[i]) if i == 0 else c.rjust(widths[i])
                                 for i, c in enumerate(cells))

    sep = "  " + "─" * (sum(widths) + 2 * (len(widths) - 1))
    total_width = len(sep)

    print(f"\n{'═' * total_width}")
    print(f"  {'Summary':^{total_width - 2}}")
    print(f"{'═' * total_width}")
    print(fmt_row(col_headers))
    print(sep)
    for sr in str_rows:
        print(fmt_row(sr))
    print(f"{'═' * total_width}")
    print()


def write_summary_csv(rows: list[dict], output_path: str) -> None:
    if not rows:
        return
    fieldnames = list(rows[0].keys())
    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    print(f"Summary written to: {output_path}")


def write_latex_table(rows: list[dict], output_path: str) -> None:
    if not rows:
        return

    col_headers = ["File", "Total", "Eval", "Excl", "TP", "FP", "FN", "TN",
                   "Accuracy", "Precision", "Recall", "F1", "MCC", "Run Time"]
    col_keys    = ["file", "rows_total", "rows_evaluated", "rows_excluded",
                   "TP", "FP", "FN", "TN",
                   "accuracy", "precision", "recall", "f1", "mcc", "run_time"]
    float_cols  = {"accuracy", "precision", "recall", "f1", "mcc"}

    def escape(s: str) -> str:
        return s.replace("_", r"\_").replace("%", r"\%").replace("&", r"\&")

    col_spec = "l" + "r" * (len(col_headers) - 1)
    header_cells = " & ".join(r"\textbf{" + escape(h) + "}" for h in col_headers)

    lines = [
        r"\begin{table}[htbp]",
        r"  \centering",
        r"  \caption{Clone Detection Evaluation Summary}",
        r"  \label{tab:evaluation_summary}",
        r"  \small",
        f"  \\begin{{tabular}}{{{col_spec}}}",
        r"    \toprule",
        f"    {header_cells} \\\\",
        r"    \midrule",
    ]

    for r in rows:
        cells = []
        for k in col_keys:
            v = r[k]
            if k in float_cols:
                cells.append(f"{v:.4f}")
            elif k == "file":
                cells.append(escape(str(v)))
            else:
                cells.append(str(v))
        lines.append("    " + " & ".join(cells) + r" \\")

    lines += [
        r"    \bottomrule",
        r"  \end{tabular}",
        r"\end{table}",
    ]

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")
    print(f"LaTeX table written to: {output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Evaluate clone-detection CSVs: confusion matrix + metrics."
    )
    parser.add_argument(
        "files",
        nargs="*",
        help="CSV file(s) to evaluate. Defaults to all results_*.csv in cwd.",
    )
    parser.add_argument(
        "--unknown-as",
        choices=["exclude", "clone", "non-clone"],
        default="exclude",
        metavar="MODE",
        help=(
            "How to handle DONT-KNOW / parse-error responses: "
            "'exclude' (default), 'clone', or 'non-clone'."
        ),
    )
    parser.add_argument(
        "--output",
        default="evaluation_summary.csv",
        metavar="FILE",
        help="Output CSV file for the summary table (default: evaluation_summary.csv).",
    )
    parser.add_argument(
        "--latex",
        default=None,
        metavar="FILE",
        help="Write a LaTeX booktabs table to FILE (e.g. evaluation_summary.tex).",
    )
    args = parser.parse_args()

    paths = args.files or sorted(glob.glob("results_*.csv"))
    if not paths:
        print("No result CSV files found.", file=sys.stderr)
        sys.exit(1)

    print(f"Unknown/ambiguous responses: treated as '{args.unknown_as}'")

    summary_rows: list[dict] = []
    for path in paths:
        if not Path(path).exists():
            print(f"File not found: {path}", file=sys.stderr)
            continue
        result = evaluate_file(path, args.unknown_as)
        if result is not None:
            summary_rows.append(result)

    print_summary_table(summary_rows)
    write_summary_csv(summary_rows, args.output)
    if args.latex:
        write_latex_table(summary_rows, args.latex)


if __name__ == "__main__":
    main()
