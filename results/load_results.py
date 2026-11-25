"""Load the majority-vote metrics of the configurations reported in the paper.

The evaluation summaries in rq1_effect_of_quantization/tables/ name each run by
its raw result-file stem. results/configurations.csv maps every stem to the
model, quantization technique, and bit-width that the paper reports, so the
figure scripts can look up a configuration without parsing file names.

    from load_results import load
    data = load("gcj_java")          # {(model, technique, bits): {"mcc": 0.463, ...}}

Metrics are rounded to three decimals, as in the paper.
"""
import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIGURATIONS = ROOT / "results" / "configurations.csv"
TABLES = ROOT / "rq1_effect_of_quantization" / "tables"
BENCHMARKS = ("ocd", "gcj_java", "gcj_crosslang")
METRICS = ("precision", "recall", "f1", "mcc")


def load(benchmark: str, include_unreported: bool = False) -> dict:
    """Return {(model, technique, bits): metrics} for one benchmark."""
    if benchmark not in BENCHMARKS:
        raise ValueError(f"benchmark must be one of {BENCHMARKS}")
    summary = {}
    with open(TABLES / f"evaluation_summary_{benchmark}.csv", newline="") as f:
        for row in csv.DictReader(f):
            summary[row["file"].split(" (")[0]] = row
    data = {}
    with open(CONFIGURATIONS, newline="") as f:
        for conf in csv.DictReader(f):
            if conf["benchmark"] != benchmark:
                continue
            if conf["in_paper"] != "1" and not include_unreported:
                continue
            row = summary[conf["run"]]
            metrics = {m: round(float(row[m]), 3) for m in METRICS}
            metrics.update(FP=int(row["FP"]), FN=int(row["FN"]))
            data[(conf["model"], conf["technique"], int(conf["bits"]))] = metrics
    return data
