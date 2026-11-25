"""Statistical tests for the RQ1 findings.

The script rebuilds the per-pair majority-vote predictions of every
configuration reported in the paper (results/configurations.csv), using the
same answer parser and voting rule as evaluate_results.py with
--unknown-as non-clone, and checks that the recomputed MCC matches the
published summary tables before running any test.

Three analyses are run.

1. Quantized configuration against its own BF16 model (every benchmark).
   Exact McNemar test on per-pair correctness, Holm-corrected within each
   benchmark, and the change in MCC with a 95% paired bootstrap confidence
   interval (pairs resampled with replacement).

2. Observation 2, robustness depends more on the model than on the bit-width.
   (a) Two-way ANOVA on the change in MCC relative to BF16 for the GGUF
       configurations of the models evaluated at 2, 3, and 4 bits, with model
       and bit-width as factors, reporting partial eta squared.
   (b) Logistic regression on per-pair correctness for the same models at
       BF16 and GGUF 2, 3, and 4 bits, fitted with generalised estimating
       equations (GEE) clustered on pair, because every configuration answers
       the same pairs.
       Wald tests compare the model-specific effect of bit-width
       (model x bit-width interaction) with the effect shared by all models
       (bit-width main effect).

3. Observation 6, quantization costs more on cross-language clones.
   For each quantized configuration, d = dMCC(cross-language) - dMCC(Java),
   where dMCC is the change relative to BF16, so d < 0 means that the
   configuration loses more, or gains less, on cross-language clones.
   The script reports a sign test and a Wilcoxon signed-rank test over the
   configurations, the same tests over per-model means (configurations of the
   same model are not independent), a random-intercept mixed model on d with
   model as the group, and a difference-in-differences bootstrap confidence
   interval for each configuration.
   The tests are repeated on the 4-bit configurations.

Usage (from the repository root, Python 3.10 or later with numpy, scipy,
pandas, and statsmodels):

    python rq1_effect_of_quantization/statistical_tests.py
    python rq1_effect_of_quantization/statistical_tests.py --bootstrap 2000 --skip-gee

Results are printed and written as CSV files to
rq1_effect_of_quantization/tables/stats_*.csv.
"""
from __future__ import annotations

import argparse
import csv
import re
import sys
import warnings
from collections import defaultdict
from pathlib import Path

import numpy as np
import pandas as pd
from scipy import stats
from statsmodels.formula.api import mixedlm, ols
from statsmodels.stats.anova import anova_lm
from statsmodels.stats.multitest import multipletests

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(ROOT / "results"))

from evaluate_results import parse_answer  # noqa: E402
from load_results import load as load_published  # noqa: E402

CONFIGURATIONS = ROOT / "results" / "configurations.csv"
TABLES = HERE / "tables"
BENCHMARKS = ("ocd", "gcj_java", "gcj_crosslang")
BENCHMARK_LABEL = {"ocd": "OCD", "gcj_java": "GCJ-Java", "gcj_crosslang": "GCJ cross-language"}
QUANTIZED_BITS = (2, 3, 4)


# ---------------------------------------------------------------------------
# Loading the per-pair predictions
# ---------------------------------------------------------------------------

def majority_vote(benchmark: str, run: str) -> dict:
    """Return {pair_key: (truth_is_clone, pred_is_clone)} for one configuration.

    Mirrors evaluate_results.evaluate_majority_vote with --unknown-as non-clone:
    unparseable and DONT-KNOW answers cast no vote, and ties or pairs without
    a valid vote are predicted as non-clones.
    """
    pattern = re.compile(re.escape(run) + r"_round\d+\.csv$")
    paths = sorted(p for p in (ROOT / "results" / benchmark).rglob("*.csv")
                   if pattern.fullmatch(p.name))
    if not paths:
        raise FileNotFoundError(f"no result files for {benchmark}/{run}")
    truth: dict = {}
    votes: dict = defaultdict(list)
    for path in paths:
        with open(path, newline="", encoding="utf-8") as f:
            for row in csv.DictReader(f):
                key = row.get("pair_id") or (row["program_a"], row["variant_a"],
                                             row["program_b"], row["variant_b"])
                gt = row["ground_truth"].strip().upper()
                if gt in ("CLONE", "NON-CLONE"):
                    truth[key] = gt == "CLONE"
                pred = parse_answer(row["response"])
                if pred in ("CLONE", "NON-CLONE"):
                    votes[key].append(pred)
    result = {}
    for key, is_clone in truth.items():
        v = votes.get(key, [])
        result[key] = (is_clone, v.count("CLONE") > v.count("NON-CLONE"))
    return result


def load_benchmark(benchmark: str) -> tuple[pd.DataFrame, np.ndarray, dict]:
    """Return (configurations, y_true, {(model, technique, bits): y_pred})."""
    with open(CONFIGURATIONS, newline="") as f:
        confs = [c for c in csv.DictReader(f)
                 if c["benchmark"] == benchmark and c["in_paper"] == "1"]
    preds = {}
    keys = y_true = None
    for c in confs:
        mv = majority_vote(benchmark, c["run"])
        if keys is None:
            keys = sorted(mv, key=str)
            y_true = np.array([mv[k][0] for k in keys])
        if set(mv) != set(keys) or any(mv[k][0] != t for k, t in zip(keys, y_true)):
            raise ValueError(f"{benchmark}/{c['run']} has a different pair set or ground truth")
        preds[(c["model"], c["technique"], int(c["bits"]))] = np.array([mv[k][1] for k in keys])
    table = pd.DataFrame([{"model": m, "technique": t, "bits": b} for (m, t, b) in preds])
    return table, y_true, preds


# ---------------------------------------------------------------------------
# Metrics
# ---------------------------------------------------------------------------

def mcc_from_counts(tp, fp, fn, tn):
    """MCC from (arrays of) confusion counts, 0 when undefined, as in evaluate_results."""
    tp, fp, fn, tn = (np.asarray(x, dtype=float) for x in (tp, fp, fn, tn))
    denom = np.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn))
    with np.errstate(invalid="ignore", divide="ignore"):
        return np.where(denom > 0, (tp * tn - fp * fn) / denom, 0.0)


def mcc(y_true: np.ndarray, y_pred: np.ndarray) -> float:
    tp = np.sum(y_true & y_pred)
    fp = np.sum(~y_true & y_pred)
    fn = np.sum(y_true & ~y_pred)
    tn = np.sum(~y_true & ~y_pred)
    return float(mcc_from_counts(tp, fp, fn, tn))


def bootstrap_mcc(y_true, y_pred, idx):
    """MCC on each bootstrap sample, where idx has shape (B, n)."""
    t = y_true[idx]
    p = y_pred[idx]
    return mcc_from_counts((t & p).sum(1), (~t & p).sum(1), (t & ~p).sum(1), (~t & ~p).sum(1))


def bootstrap_indices(n: int, n_boot: int, seed: int, chunk: int = 500):
    """Yield bootstrap index chunks of shape (<=chunk, n), reproducible for a seed."""
    rng = np.random.default_rng(seed)
    done = 0
    while done < n_boot:
        size = min(chunk, n_boot - done)
        yield rng.integers(0, n, size=(size, n))
        done += size


def paired_delta_bootstrap(y_true, pred_a, pred_b, n_boot, seed) -> np.ndarray:
    """Bootstrap distribution of MCC(b) - MCC(a), resampling pairs jointly."""
    out = [bootstrap_mcc(y_true, pred_b, idx) - bootstrap_mcc(y_true, pred_a, idx)
           for idx in bootstrap_indices(len(y_true), n_boot, seed)]
    return np.concatenate(out)


def percentile_ci(samples: np.ndarray, level: float = 0.95) -> tuple[float, float]:
    alpha = (1 - level) / 2
    lo, hi = np.quantile(samples, [alpha, 1 - alpha])
    return float(lo), float(hi)


def mcnemar_exact(correct_a: np.ndarray, correct_b: np.ndarray) -> tuple[int, int, float]:
    """Exact McNemar test. Returns (b, c, p) with b = only A correct, c = only B correct."""
    b = int(np.sum(correct_a & ~correct_b))
    c = int(np.sum(~correct_a & correct_b))
    p = 1.0 if b + c == 0 else stats.binomtest(min(b, c), b + c, 0.5).pvalue
    return b, c, float(p)


# ---------------------------------------------------------------------------
# Data checks
# ---------------------------------------------------------------------------

def check_against_published(benchmark, y_true, preds) -> None:
    published = load_published(benchmark)
    for key, y_pred in preds.items():
        ours = round(mcc(y_true, y_pred), 3)
        theirs = published[key]["mcc"]
        if abs(ours - theirs) > 0.0011:
            raise ValueError(f"{benchmark} {key}: recomputed MCC {ours} != published {theirs}")


# ---------------------------------------------------------------------------
# Analysis 1: each quantized configuration against its BF16 model
# ---------------------------------------------------------------------------

def against_bf16(data: dict, n_boot: int, seed: int) -> pd.DataFrame:
    rows = []
    for benchmark, (table, y_true, preds) in data.items():
        bench_rows = []
        for i, (model, tech, bits) in enumerate(sorted(preds)):
            if tech == "BF16":
                continue
            base = preds[(model, "BF16", 16)]
            quant = preds[(model, tech, bits)]
            b, c, p = mcnemar_exact(base == y_true, quant == y_true)
            boot = paired_delta_bootstrap(y_true, base, quant, n_boot, seed + i)
            lo, hi = percentile_ci(boot)
            bench_rows.append({
                "benchmark": benchmark, "model": model, "technique": tech, "bits": bits,
                "mcc_bf16": mcc(y_true, base), "mcc_quant": mcc(y_true, quant),
                "delta_mcc": mcc(y_true, quant) - mcc(y_true, base),
                "ci_low": lo, "ci_high": hi,
                "only_bf16_correct": b, "only_quant_correct": c,
                "odds_ratio": (c / b) if b else np.inf,
                "p_mcnemar": p,
            })
        df = pd.DataFrame(bench_rows)
        df["p_holm"] = multipletests(df["p_mcnemar"], method="holm")[1]
        df["significant_holm"] = df["p_holm"] < 0.05
        rows.append(df)
    return pd.concat(rows, ignore_index=True)


# ---------------------------------------------------------------------------
# Analysis 2: model against bit-width (Observation 2)
# ---------------------------------------------------------------------------

def full_gguf_models(table: pd.DataFrame) -> list[str]:
    """Models evaluated at BF16 and at GGUF 2, 3, and 4 bits."""
    gguf = table[table.technique == "GGUF"].groupby("model")["bits"].apply(set)
    return sorted(m for m, b in gguf.items() if set(QUANTIZED_BITS) <= b)


def anova_delta(data: dict) -> pd.DataFrame:
    rows = []
    for benchmark, (table, y_true, preds) in data.items():
        models = full_gguf_models(table)
        records = [{"model": m, "bits": b,
                    "delta": mcc(y_true, preds[(m, "GGUF", b)]) - mcc(y_true, preds[(m, "BF16", 16)])}
                   for m in models for b in QUANTIZED_BITS]
        df = pd.DataFrame(records)
        fit = ols("delta ~ C(model) + C(bits)", data=df).fit()
        aov = anova_lm(fit, typ=2)
        ss_resid = aov.loc["Residual", "sum_sq"]
        ss_total = aov["sum_sq"].sum()
        for term, label in (("C(model)", "model"), ("C(bits)", "bit-width")):
            ss = aov.loc[term, "sum_sq"]
            rows.append({
                "benchmark": benchmark, "factor": label, "n_models": len(models),
                "df": int(aov.loc[term, "df"]), "df_resid": int(aov.loc["Residual", "df"]),
                "F": aov.loc[term, "F"], "p": aov.loc[term, "PR(>F)"],
                "partial_eta_sq": ss / (ss + ss_resid), "eta_sq": ss / ss_total,
            })
    return pd.DataFrame(rows)


def gee_wald(data: dict) -> pd.DataFrame:
    """Logistic GEE on per-pair correctness, clustered on pair, with Wald tests."""
    import statsmodels.api as sm
    from statsmodels.formula.api import gee

    rows = []
    for benchmark, (table, y_true, preds) in data.items():
        models = full_gguf_models(table)
        frames = []
        for m in models:
            for tech, b in [("BF16", 16)] + [("GGUF", b) for b in QUANTIZED_BITS]:
                frames.append(pd.DataFrame({
                    "pair": np.arange(len(y_true)), "model": m, "bits": str(b),
                    "correct": (preds[(m, tech, b)] == y_true).astype(int)}))
        df = pd.concat(frames, ignore_index=True)

        def fit(formula: str):
            # Every pair is answered by every configuration, so the answers to a
            # pair are correlated. GEE with an exchangeable working correlation
            # and robust standard errors accounts for this.
            return gee(formula, groups="pair", data=df, family=sm.families.Binomial(),
                       cov_struct=sm.cov_struct.Exchangeable()).fit()

        additive = fit("correct ~ C(model) + C(bits)").wald_test_terms(scalar=True).table
        full = fit("correct ~ C(model) * C(bits)").wald_test_terms(scalar=True).table
        tests = [
            ("bit-width shared by all models (bits | model)", additive.loc["C(bits)"]),
            ("model-specific effect of bit-width (model x bits)", full.loc["C(model):C(bits)"]),
            ("model baseline (model | bits)", additive.loc["C(model)"]),
        ]
        for label, t in tests:
            chi2, ddf = float(t["statistic"]), int(t["df_constraint"])
            rows.append({"benchmark": benchmark, "term": label, "n_models": len(models),
                         "pairs": len(y_true), "wald_chi2": chi2, "df": ddf,
                         "chi2_per_df": chi2 / ddf, "p": float(t["pvalue"])})
    return pd.DataFrame(rows)


# ---------------------------------------------------------------------------
# Analysis 3: cross-language against Java (Observation 6)
# ---------------------------------------------------------------------------

def rank_biserial(x: np.ndarray) -> float:
    """Matched-pairs rank-biserial correlation of a set of differences (zeros dropped)."""
    x = x[x != 0]
    if len(x) == 0:
        return 0.0
    ranks = stats.rankdata(np.abs(x))
    w_pos, w_neg = ranks[x > 0].sum(), ranks[x < 0].sum()
    return float((w_pos - w_neg) / (w_pos + w_neg))


def xlang_tests(d: np.ndarray, label: str, unit: str) -> dict:
    """Sign test and Wilcoxon signed-rank test of H0: median d = 0."""
    worse = int(np.sum(d < 0))
    nonzero = int(np.sum(d != 0))
    wil = stats.wilcoxon(d) if nonzero > 0 else None
    return {
        "subset": label, "unit": unit, "n": len(d),
        "worse_on_xlang": worse, "better_on_xlang": int(np.sum(d > 0)),
        "mean_d": float(np.mean(d)), "median_d": float(np.median(d)),
        "p_sign_one_sided": float(stats.binomtest(worse, nonzero, 0.5, alternative="greater").pvalue),
        "p_sign_two_sided": float(stats.binomtest(worse, nonzero, 0.5).pvalue),
        "p_wilcoxon_two_sided": float(wil.pvalue) if wil else np.nan,
        "rank_biserial": rank_biserial(d),
    }


def cross_language(data: dict, n_boot: int, seed: int) -> tuple[pd.DataFrame, pd.DataFrame]:
    _, yj, pj = data["gcj_java"]
    _, yx, px = data["gcj_crosslang"]
    configs = sorted(k for k in pj if k[1] != "BF16" and k in px)
    rows = []
    for i, (model, tech, bits) in enumerate(configs):
        dj = mcc(yj, pj[(model, tech, bits)]) - mcc(yj, pj[(model, "BF16", 16)])
        dx = mcc(yx, px[(model, tech, bits)]) - mcc(yx, px[(model, "BF16", 16)])
        # The two benchmarks have different pairs, so each is resampled on its own,
        # keeping the pairing between the BF16 and quantized predictions within it.
        boot_j = paired_delta_bootstrap(yj, pj[(model, "BF16", 16)], pj[(model, tech, bits)], n_boot, seed + 2 * i)
        boot_x = paired_delta_bootstrap(yx, px[(model, "BF16", 16)], px[(model, tech, bits)], n_boot, seed + 2 * i + 1)
        lo, hi = percentile_ci(boot_x - boot_j)
        rows.append({"model": model, "technique": tech, "bits": bits,
                     "delta_java": dj, "delta_xlang": dx, "d": dx - dj,
                     "ci_low": lo, "ci_high": hi,
                     "ci_excludes_zero": "worse on xlang" if hi < 0 else ("better on xlang" if lo > 0 else "")})
    per_config = pd.DataFrame(rows)

    summary = []
    for label, sub in (("all bit-widths", per_config), ("4 bits", per_config[per_config.bits == 4])):
        summary.append(xlang_tests(sub["d"].to_numpy(), label, "configuration"))
        per_model = sub.groupby("model")["d"].mean().to_numpy()
        summary.append(xlang_tests(per_model, label, "model (mean over its configurations)"))
        if sub["model"].nunique() > 1 and len(sub) > sub["model"].nunique():
            # When the between-model variance is estimated at zero, the fit sits on
            # the boundary and reduces to an ordinary mean of d, which statsmodels
            # reports with warnings. The variance is reported instead.
            with warnings.catch_warnings():
                warnings.simplefilter("ignore")
                fit = mixedlm("d ~ 1", sub, groups=sub["model"]).fit(reml=True)
            lo, hi = fit.conf_int().loc["Intercept"]
            summary.append({"subset": label, "unit": "mixed model, random intercept per model",
                            "n": len(sub), "mean_d": float(fit.params["Intercept"]),
                            "model_variance": float(fit.cov_re.iloc[0, 0]),
                            "ci_low": float(lo), "ci_high": float(hi),
                            "p_mixed_two_sided": float(fit.pvalues["Intercept"])})
        summary.append({"subset": label, "unit": "per-configuration bootstrap CI",
                        "n": len(sub),
                        "ci_worse_on_xlang": int((sub.ci_excludes_zero == "worse on xlang").sum()),
                        "ci_better_on_xlang": int((sub.ci_excludes_zero == "better on xlang").sum())})
    return per_config, pd.DataFrame(summary)


# ---------------------------------------------------------------------------
# Reporting
# ---------------------------------------------------------------------------

def show(title: str, df: pd.DataFrame) -> None:
    print(f"\n{'=' * 78}\n{title}\n{'=' * 78}")
    with pd.option_context("display.max_rows", None, "display.width", 200,
                           "display.max_columns", None, "display.float_format", "{:.4g}".format):
        print(df.to_string(index=False))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--bootstrap", type=int, default=10000, help="bootstrap samples (default 10000)")
    parser.add_argument("--seed", type=int, default=2026, help="random seed (default 2026)")
    parser.add_argument("--skip-gee", action="store_true",
                        help="skip the GEE logistic regression, the slowest step")
    args = parser.parse_args()

    data = {}
    for benchmark in BENCHMARKS:
        data[benchmark] = load_benchmark(benchmark)
        check_against_published(benchmark, data[benchmark][1], data[benchmark][2])
        print(f"Loaded {BENCHMARK_LABEL[benchmark]}: {len(data[benchmark][2])} configurations, "
              f"{len(data[benchmark][1])} pairs, MCC matches the published table.")

    TABLES.mkdir(exist_ok=True)

    vs_bf16 = against_bf16(data, args.bootstrap, args.seed)
    vs_bf16.to_csv(TABLES / "stats_vs_bf16.csv", index=False)
    show("1. Quantized configuration vs. its BF16 model (exact McNemar, Holm per benchmark; "
         "dMCC with 95% paired bootstrap CI)", vs_bf16)
    for benchmark, sub in vs_bf16.groupby("benchmark", sort=False):
        print(f"  {BENCHMARK_LABEL[benchmark]}: {int(sub.significant_holm.sum())} of {len(sub)} "
              f"configurations differ significantly from BF16 after Holm correction.")

    aov = anova_delta(data)
    aov.to_csv(TABLES / "stats_obs2_anova.csv", index=False)
    show("2a. Two-way ANOVA on dMCC (GGUF, models at 2, 3, and 4 bits): model vs. bit-width", aov)

    if not args.skip_gee:
        wald = gee_wald(data)
        wald.to_csv(TABLES / "stats_obs2_gee.csv", index=False)
        show("2b. Logistic GEE on per-pair correctness (clustered on pair), Wald tests", wald)

    per_config, summary = cross_language(data, args.bootstrap, args.seed)
    per_config.to_csv(TABLES / "stats_obs6_configs.csv", index=False)
    summary.to_csv(TABLES / "stats_obs6_summary.csv", index=False)
    show("3a. d = dMCC(cross-language) - dMCC(Java) per configuration, with 95% "
         "difference-in-differences bootstrap CI", per_config)
    show("3b. Tests of whether quantization costs more on cross-language clones (d < 0)", summary)

    print(f"\nCSV files written to {TABLES.relative_to(ROOT)}/stats_*.csv")


if __name__ == "__main__":
    main()
