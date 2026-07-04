#!/usr/bin/env python3
"""Prepare a Java semantic-clone pair dataset from GCJ2-4lang (gcj4.pkl).

Process (per the study protocol):
  * Randomly sample 16 problems from the GCJ dataset.
  * For each problem, randomly sample 3 Java submissions  -> 48 unique files.
  * TRUE pairs  = all within-problem pairs of the extracted submissions
                  (C(3,2)=3 per problem x 16 = 48), same language (Java).
  * FALSE pairs = for each true pair, its first submission paired with a
                  randomly extracted submission from a DIFFERENT problem,
                  same language (48). Partners are drawn only from the
                  already-extracted 48 files, so the file set stays at 48.
  => 96 pairs over 48 unique files.

Outputs:
  gcj_java_clones/files/<index>.java   the 48 extracted source files
  gcj_java_clones/pairs.csv            the 96 labeled pairs
  gcj_java_clones/files_meta.csv       metadata for the 48 files
"""
import os
import pickle
import random
from itertools import combinations

import pandas as pd

SEED = 42
N_PROBLEMS = 16
N_SUBS = 3
LANG = "java"
PKL = "gcj4.pkl"
OUTDIR = "gcj_java_clones"

random.seed(SEED)


def main():
    df = pickle.load(open(PKL, "rb"))
    jd = df[df["lan"] == LANG].copy()

    # A "problem" is a unique (year, round, task); build a stable string id.
    jd["problem"] = (
        jd["year"].astype(str) + "_" + jd["round"].astype(str) + "_" + jd["task"].astype(str)
    )

    # Only keep problems with at least N_SUBS Java submissions.
    counts = jd.groupby("problem").size()
    eligible = sorted(counts[counts >= N_SUBS].index)
    assert len(eligible) >= N_PROBLEMS, (
        f"only {len(eligible)} eligible problems, need {N_PROBLEMS}"
    )

    problems = sorted(random.sample(eligible, N_PROBLEMS))

    # Sample N_SUBS submissions per problem. Use 'index' as the unique file id.
    extracted = {}  # problem -> list of file-id ints
    rows = []
    for p in problems:
        pool = jd[jd["problem"] == p]
        picks = pool.sample(n=N_SUBS, random_state=random.randint(0, 2**31 - 1))
        extracted[p] = list(picks["index"])
        for _, r in picks.iterrows():
            rows.append(r)
    files_meta = pd.DataFrame(rows)

    # ---- TRUE pairs: within-problem combinations ----
    true_pairs = []
    for p in problems:
        for a, b in combinations(extracted[p], 2):
            true_pairs.append((a, b))
    assert len(true_pairs) == N_PROBLEMS * 3, len(true_pairs)

    # ---- FALSE pairs: first submission of each true pair vs a random
    #      extracted submission from a different problem ----
    problem_of = {fid: p for p, fids in extracted.items() for fid in fids}
    false_pairs = []
    for a, _b in true_pairs:
        pa = problem_of[a]
        candidates = [fid for p, fids in extracted.items() if p != pa for fid in fids]
        c = random.choice(candidates)
        false_pairs.append((a, c))
    assert len(false_pairs) == len(true_pairs)

    # ---- Write source files ----
    os.makedirs(os.path.join(OUTDIR, "files"), exist_ok=True)
    id2code = {int(r["index"]): r["flines"] for _, r in files_meta.iterrows()}
    for fid, code in id2code.items():
        with open(os.path.join(OUTDIR, "files", f"{fid}.java"), "w") as f:
            f.write(code if isinstance(code, str) else "".join(code))

    # ---- Write pairs.csv ----
    def rec(pid, a, b, label):
        return {
            "pair_id": pid,
            "label": label,  # 1 = clone (true), 0 = non-clone (false)
            "lang": LANG,
            "file1": f"{a}.java",
            "file2": f"{b}.java",
            "problem1": problem_of[a],
            "problem2": problem_of[b],
        }

    records = []
    pid = 0
    for a, b in true_pairs:
        records.append(rec(pid, a, b, 1)); pid += 1
    for a, b in false_pairs:
        records.append(rec(pid, a, b, 0)); pid += 1
    pairs = pd.DataFrame(records)
    pairs.to_csv(os.path.join(OUTDIR, "pairs.csv"), index=False)

    files_meta_out = files_meta[["index", "problem", "lan", "file", "lines"]].rename(
        columns={"index": "file_id"}
    )
    files_meta_out.to_csv(os.path.join(OUTDIR, "files_meta.csv"), index=False)

    # ---- Summary + sanity checks ----
    true_set = {frozenset(p) for p in true_pairs}
    false_overlap = sum(1 for a, b in false_pairs if frozenset((a, b)) in true_set)
    cross_prob_false = all(problem_of[a] != problem_of[b] for a, b in false_pairs)
    n_files = len({fid for pr in pairs[["file1", "file2"]].values for fid in pr})

    print(f"problems sampled : {N_PROBLEMS}")
    print(f"unique files     : {len(id2code)}  (referenced in pairs: {n_files})")
    print(f"true  pairs      : {len(true_pairs)}")
    print(f"false pairs      : {len(false_pairs)}")
    print(f"total pairs      : {len(pairs)}")
    print(f"false pairs cross-problem : {cross_prob_false}")
    print(f"false pairs colliding with a true pair : {false_overlap}")
    print(f"\nwrote {OUTDIR}/pairs.csv, {OUTDIR}/files_meta.csv, {OUTDIR}/files/*.java")


if __name__ == "__main__":
    main()
