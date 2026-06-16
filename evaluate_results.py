"""
Evaluate clone-detection results from CSV files produced by run_quantization.py.

For each CSV it:
  - Parses the JSON `response` column to extract the model's answer
  - Maps YES-SIMILAR → CLONE, NO-NOT-SIMILAR → NON-CLONE
  - Handles DONT-KNOW / parse errors via --unknown-as (default: exclude)
  - Prints a confusion matrix plus precision, recall, F1, and MCC
  - Writes a summary CSV with all metrics (default: evaluation_summary.csv)

Usage:
    python evaluate_results.py                                      # all results/**/*.csv, individual mode
    python evaluate_results.py --mode majority-vote                # majority vote across _roundN files
    python evaluate_results.py results/Meta-Llama-3.1-8B-Instruct/*.csv  # specific folder
    python evaluate_results.py results/Meta-Llama-3.1-8B-Instruct/results_aqlm_round1.csv  # single file
    python evaluate_results.py --unknown-as non-clone              # treat unknowns as NON-CLONE
    python evaluate_results.py --unknown-as clone                  # treat unknowns as CLONE
    python evaluate_results.py --unknown-as exclude                # skip unknowns (default)
    python evaluate_results.py --output summary.csv                # custom output CSV name
"""

import argparse
import csv
import glob
import json
import re
import sys
from collections import defaultdict
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
        key = next((k for k in obj if k.lower() == "answer"), None)
        raw = obj[key].strip().upper() if key else ""
    except (json.JSONDecodeError, AttributeError):
        # Try to extract answer with a loose regex fallback
        import re
        m = re.search(r'"answer"\s*:\s*"([^"]+)"', response_str, re.IGNORECASE)
        raw = m.group(1).strip().upper() if m else ""

    if not raw:
        # Plain-text fallback: handles both
        #   ANSWER: YES-SIMILAR          (unquoted)
        #   ANSWER: "YES-SIMILAR"        (quoted)
        m = re.search(r'\bANSWER\s*:\s*"?([A-Z_-]+)"?', response_str, re.IGNORECASE)
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


def print_report(label: str, y_true: list[str], y_pred: list[str],
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
    print(f"  File : {label}")
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
        "file": label,
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


def _load_csv_rows(csv_path: str) -> tuple[list[dict], datetime | None, datetime | None]:
    """Read all rows from a CSV, return (rows, first_ts, last_ts)."""
    rows = []
    first_ts: datetime | None = None
    last_ts:  datetime | None = None
    with open(csv_path, newline="", encoding="utf-8") as f:
        for row in csv.DictReader(f):
            ts_raw = row.get("timestamp", "").strip()
            try:
                ts = datetime.strptime(ts_raw, _TS_FMT)
                if first_ts is None:
                    first_ts = ts
                last_ts = ts
            except ValueError:
                pass
            rows.append(row)
    return rows, first_ts, last_ts


def evaluate_file(csv_path: str, unknown_as: str) -> dict | None:
    """Evaluate one CSV and return its metrics dict, or None if no evaluable rows."""
    y_true: list[str] = []
    y_pred: list[str] = []
    n_excluded = 0

    rows, first_ts, last_ts = _load_csv_rows(csv_path)
    for row in rows:
        ground_truth = row["ground_truth"].strip().upper()
        predicted = parse_answer(row["response"])

        if ground_truth not in ("CLONE", "NON-CLONE"):
            n_excluded += 1
            continue

        if predicted is None or predicted == "DONT-KNOW":
            if unknown_as == "exclude":
                n_excluded += 1
                continue
            predicted = "CLONE" if unknown_as == "clone" else "NON-CLONE"

        y_true.append(ground_truth)
        y_pred.append(predicted)

    if not y_true:
        print(f"\n[{Path(csv_path).name}] No evaluable rows found.")
        return None

    run_seconds = (last_ts - first_ts).total_seconds() if first_ts and last_ts else None
    return print_report(Path(csv_path).name, y_true, y_pred, n_excluded, run_seconds)


def _strip_round_suffix(stem: str) -> str:
    """Remove trailing _roundN from a filename stem."""
    return re.sub(r"_round\d+$", "", stem)


def evaluate_majority_vote(paths: list[str], unknown_as: str) -> list[dict | None]:
    """
    Group files by base name (stripping _roundN), apply majority vote across
    rounds for each code pair, then compute metrics per group.
    """
    # Group paths: stem-without-round → list of paths (preserve directory)
    groups: dict[str, list[str]] = defaultdict(list)
    for p in paths:
        stem = Path(p).stem
        key = str(Path(p).parent / _strip_round_suffix(stem))
        groups[key].append(p)

    results = []
    for key in sorted(groups):
        group_paths = sorted(groups[key])
        n_rounds = len(group_paths)
        label = f"{Path(key).name} (majority vote, {n_rounds} round{'s' if n_rounds != 1 else ''})"

        # Accumulate votes and ground truth per pair
        pair_truth:  dict[tuple, str]       = {}
        pair_votes:  dict[tuple, list[str]] = defaultdict(list)
        round_durations: list[float] = []

        for csv_path in group_paths:
            rows, fts, lts = _load_csv_rows(csv_path)
            if fts and lts:
                round_durations.append((lts - fts).total_seconds())
            for row in rows:
                pair_key = (row["program_a"], row["variant_a"],
                            row["program_b"], row["variant_b"])
                gt = row["ground_truth"].strip().upper()
                if gt in ("CLONE", "NON-CLONE"):
                    pair_truth[pair_key] = gt
                pred = parse_answer(row["response"])
                if pred in ("CLONE", "NON-CLONE"):
                    pair_votes[pair_key].append(pred)

        # Resolve majority vote for each pair
        y_true: list[str] = []
        y_pred: list[str] = []
        n_excluded = 0

        for pair_key in pair_truth:
            votes = pair_votes.get(pair_key, [])
            clone_count     = votes.count("CLONE")
            non_clone_count = votes.count("NON-CLONE")

            if clone_count > non_clone_count:
                predicted = "CLONE"
            elif non_clone_count > clone_count:
                predicted = "NON-CLONE"
            else:  # tie or no valid votes
                if unknown_as == "exclude":
                    n_excluded += 1
                    continue
                predicted = "CLONE" if unknown_as == "clone" else "NON-CLONE"

            y_true.append(pair_truth[pair_key])
            y_pred.append(predicted)

        if not y_true:
            print(f"\n[{label}] No evaluable pairs found.")
            results.append(None)
            continue

        run_seconds = sum(round_durations) / len(round_durations) if round_durations else None
        results.append(print_report(label, y_true, y_pred, n_excluded, run_seconds))

    return results


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


def _extract_model_info(filename: str) -> tuple[str, str]:
    """
    Return (group_label, model_name) derived from a results CSV filename.

    Naming conventions handled:
      results_original_{provider}__{model}     → ("Original (FP16)",    model)
      results_gguf_{provider}__{repo}_{f}.gguf → ("GGUF Quantization",  f.gguf)
      results_gguf_{provider}__{repo}           → ("GGUF Quantization",  repo)
      results_gguf_{anything}                   → ("GGUF Quantization",  anything.gguf)
    """
    stem = Path(filename).stem  # strip .csv

    if stem.startswith("results_original_"):
        rest = stem[len("results_original_"):]
        model_name = rest.split("__", 1)[1] if "__" in rest else rest
        return "Original (FP16)", model_name

    if stem.startswith("results_gguf_"):
        rest = stem[len("results_gguf_"):]
        if "__" in rest:
            after_dunder = rest.split("__", 1)[1]
            if after_dunder.endswith(".gguf"):
                # repo_name_gguf_file.gguf — repo uses hyphens only, split at first _
                parts = after_dunder.split("_", 1)
                model_name = parts[1] if len(parts) == 2 else after_dunder
            else:
                model_name = after_dunder
        else:
            model_name = rest + ".gguf"
        return "GGUF Quantization", model_name

    return "Other", stem


def write_latex_table(rows: list[dict], output_path: str) -> None:
    if not rows:
        return

    float_cols = {"accuracy", "precision", "recall", "f1", "mcc"}

    TABLES = [
        {
            "caption": "Clone Detection Evaluation Summary --- Count Statistics",
            "label":   "tab:evaluation_counts",
            "size":    r"\small",
            "resize":  False,
            "headers": ["Model", "Total", "Eval", "Excl", "TP", "FP", "FN", "TN"],
            "keys":    ["file", "rows_total", "rows_evaluated", "rows_excluded",
                        "TP", "FP", "FN", "TN"],
        },
        {
            "caption": "Clone Detection Evaluation Summary --- Performance Metrics",
            "label":   "tab:evaluation_metrics",
            "size":    r"\small",
            "resize":  False,
            "headers": ["Model", "Accuracy", "Precision", "Recall", "F1", "MCC", "Run Time"],
            "keys":    ["file", "accuracy", "precision", "recall", "f1", "mcc", "run_time"],
        },
        {
            "caption": "Clone Detection Evaluation Summary --- Full Results",
            "label":   "tab:evaluation_summary",
            "size":    r"\footnotesize",
            "resize":  True,
            "headers": ["Model", "Total", "Eval", "Excl", "TP", "FP", "FN", "TN",
                        "Accuracy", "Precision", "Recall", "F1", "MCC", "Run Time"],
            "keys":    ["file", "rows_total", "rows_evaluated", "rows_excluded",
                        "TP", "FP", "FN", "TN",
                        "accuracy", "precision", "recall", "f1", "mcc", "run_time"],
        },
    ]

    def escape(s: str) -> str:
        return s.replace("_", r"\_").replace("%", r"\%").replace("&", r"\&")

    # Group rows preserving input order within each group
    grouped: dict[str, list[tuple[str, dict]]] = {}
    group_order: list[str] = []
    for r in rows:
        group, model_name = _extract_model_info(r["file"])
        if group not in grouped:
            grouped[group] = []
            group_order.append(group)
        grouped[group].append((model_name, r))

    preferred = ["Original (FP16)", "GGUF Quantization"]
    group_order.sort(key=lambda g: preferred.index(g) if g in preferred else 99)

    all_lines: list[str] = []

    for tbl in TABLES:
        headers = tbl["headers"]
        keys    = tbl["keys"]
        n_cols  = len(headers)
        col_spec = "l" + "r" * (n_cols - 1)
        header_cells = " & ".join(r"\textbf{" + escape(h) + "}" for h in headers)

        lines = [
            r"\begin{table}[htbp]",
            r"  \centering",
            f"  \\caption{{{tbl['caption']}}}",
            f"  \\label{{{tbl['label']}}}",
            f"  {tbl['size']}",
        ]
        if tbl["resize"]:
            lines.append(r"  \resizebox{\textwidth}{!}{%")
        lines += [
            f"  \\begin{{tabular}}{{{col_spec}}}",
            r"    \toprule",
            f"    {header_cells} \\\\",
            r"    \midrule",
        ]

        for i, group in enumerate(group_order):
            if i > 0:
                lines.append(r"    \midrule")
            lines.append(
                f"    \\multicolumn{{{n_cols}}}{{l}}{{\\textit{{{escape(group)}}}}} \\\\"
            )
            lines.append(r"    \midrule")
            for model_name, r in grouped[group]:
                cells = []
                for k in keys:
                    if k == "file":
                        cells.append(escape(model_name))
                    elif k in float_cols:
                        cells.append(f"{r[k]:.4f}")
                    else:
                        cells.append(str(r[k]))
                lines.append("    " + " & ".join(cells) + r" \\")

        lines.append(r"    \bottomrule")
        lines.append(r"  \end{tabular}")
        if tbl["resize"]:
            lines.append(r"  }")
        lines.append(r"\end{table}")

        all_lines.extend(lines)
        all_lines.append("")  # blank line between tables

    with open(output_path, "w", encoding="utf-8") as f:
        f.write("\n".join(all_lines) + "\n")
    print(f"LaTeX tables written to: {output_path}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Evaluate clone-detection CSVs: confusion matrix + metrics."
    )
    parser.add_argument(
        "files",
        nargs="*",
        help="CSV file(s) to evaluate. Defaults to all results/**/*.csv.",
    )
    parser.add_argument(
        "--mode",
        choices=["individual", "majority-vote"],
        default="individual",
        metavar="MODE",
        help=(
            "Evaluation mode: 'individual' scores each file separately (default); "
            "'majority-vote' groups files by base name (stripping _roundN), "
            "takes the per-pair majority vote across rounds, then scores each group."
        ),
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

    paths = args.files or sorted(glob.glob("results/**/*.csv", recursive=True))
    if not paths:
        print("No result CSV files found.", file=sys.stderr)
        sys.exit(1)

    print(f"Unknown/ambiguous responses: treated as '{args.unknown_as}'")
    print(f"Mode: {args.mode}")

    existing_paths = []
    for path in paths:
        if not Path(path).exists():
            print(f"File not found: {path}", file=sys.stderr)
        else:
            existing_paths.append(path)

    summary_rows: list[dict] = []
    if args.mode == "majority-vote":
        for result in evaluate_majority_vote(existing_paths, args.unknown_as):
            if result is not None:
                summary_rows.append(result)
    else:
        for path in existing_paths:
            result = evaluate_file(path, args.unknown_as)
            if result is not None:
                summary_rows.append(result)

    print_summary_table(summary_rows)
    write_summary_csv(summary_rows, args.output)
    if args.latex:
        write_latex_table(summary_rows, args.latex)


if __name__ == "__main__":
    main()
