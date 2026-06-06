"""
Evaluate clone-detection results from CSV files produced by run_quantization.py.

For each CSV it:
  - Parses the JSON `response` column to extract the model's answer
  - Maps YES-SIMILAR → CLONE, NO-NOT-SIMILAR → NON-CLONE
  - Handles DONT-KNOW / parse errors via --unknown-as (default: exclude)
  - Prints a confusion matrix plus precision, recall, F1, and MCC

Usage:
    python evaluate_results.py                          # all results_*.csv in cwd
    python evaluate_results.py results_original*.csv   # specific files
    python evaluate_results.py --unknown-as non-clone  # treat unknowns as NON-CLONE
    python evaluate_results.py --unknown-as clone       # treat unknowns as CLONE
    python evaluate_results.py --unknown-as exclude    # skip unknowns (default)
"""

import argparse
import csv
import glob
import json
import sys
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

def print_report(csv_path: str, y_true: list[str], y_pred: list[str],
                 n_excluded: int) -> None:
    tp, fp, fn, tn = confusion_matrix_values(y_true, y_pred)
    m = compute_metrics(tp, fp, fn, tn)

    total = tp + fp + fn + tn
    accuracy = (tp + tn) / total if total else 0.0

    label_w = 15
    sep = "─" * 52

    print(f"\n{'═' * 52}")
    print(f"  File : {Path(csv_path).name}")
    print(f"  Rows : {total + n_excluded}  "
          f"(evaluated: {total}, excluded: {n_excluded})")
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


# ---------------------------------------------------------------------------
# Per-file evaluation
# ---------------------------------------------------------------------------

def evaluate_file(csv_path: str, unknown_as: str) -> None:
    y_true: list[str] = []
    y_pred: list[str] = []
    n_excluded = 0

    with open(csv_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
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
        return

    print_report(csv_path, y_true, y_pred, n_excluded)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

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
    args = parser.parse_args()

    paths = args.files or sorted(glob.glob("results_*.csv"))
    if not paths:
        print("No result CSV files found.", file=sys.stderr)
        sys.exit(1)

    print(f"Unknown/ambiguous responses: treated as '{args.unknown_as}'")

    for path in paths:
        if not Path(path).exists():
            print(f"File not found: {path}", file=sys.stderr)
            continue
        evaluate_file(path, args.unknown_as)


if __name__ == "__main__":
    main()
