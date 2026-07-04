#!/usr/bin/env python3
"""Prepare a CROSS-LANGUAGE semantic-clone pair dataset (GCJ^CL) from GCJ2-4lang.

Process (per the study protocol):
  * Randomly sample 16 problems, each solved in all 4 languages.
  * For each problem, randomly sample 3 submissions per language
      (4 langs x 3 = 12 submissions/problem) -> 192 unique files.
  * TRUE pairs (cross-language, same problem, label=1): for each submission
      index i in {0,1,2}, connect the 4 languages in a ring
      java -> cpp -> py -> php -> java, pairing the same-index submissions.
      => 4 edges x 3 indices = 12 true pairs/problem x 16 = 192.
      Every file participates in exactly 2 true pairs, and all submissions to a
      GCJ problem are semantic clones, so every such pair is a genuine clone.
  * FALSE pairs (label=0): one per file (== "each submission of the true pairs"),
      paired with a random extracted submission from a DIFFERENT problem AND a
      DIFFERENT language. => 192 false pairs.
  => 384 pairs over 192 unique files.

Outputs:
  gcj_crosslang_clones/files/<index>.<ext>   the 192 extracted source files
  gcj_crosslang_clones/pairs.csv             the 384 labeled pairs
  gcj_crosslang_clones/files_meta.csv        metadata for the 192 files
"""
import os
import pickle
import random

import pandas as pd

SEED = 42
N_PROBLEMS = 16
N_SUBS = 3
LANGS = ["java", "cpp", "py", "php"]  # ring order for true pairs
EXT = {"java": "java", "cpp": "cpp", "py": "py", "php": "php"}
PKL = "gcj4.pkl"
OUTDIR = "gcj_crosslang_clones"

random.seed(SEED)


def main():
    df = pickle.load(open(PKL, "rb"))
    df = df.copy()
    df["problem"] = (
        df["year"].astype(str) + "_" + df["round"].astype(str) + "_" + df["task"].astype(str)
    )

    # Eligible problems: >= N_SUBS submissions in EVERY language.
    per = df.groupby(["problem", "lan"]).size().unstack(fill_value=0)
    eligible = sorted(per.index[(per[LANGS] >= N_SUBS).all(axis=1)])
    assert len(eligible) >= N_PROBLEMS, (
        f"only {len(eligible)} fully-4-lang problems, need {N_PROBLEMS}"
    )
    problems = sorted(random.sample(eligible, N_PROBLEMS))

    # Sample N_SUBS submissions per (problem, language). picks[p][lang] = [ids].
    picks = {}
    meta_rows = []
    for p in problems:
        picks[p] = {}
        for lan in LANGS:
            pool = df[(df["problem"] == p) & (df["lan"] == lan)]
            chosen = pool.sample(n=N_SUBS, random_state=random.randint(0, 2**31 - 1))
            picks[p][lan] = list(chosen["index"])
            meta_rows.extend(chosen.to_dict("records"))
    files_meta = pd.DataFrame(meta_rows)

    problem_of = {}
    lang_of = {}
    for p in problems:
        for lan in LANGS:
            for fid in picks[p][lan]:
                problem_of[fid] = p
                lang_of[fid] = lan

    # ---- TRUE pairs: same-index language ring, cross-language ----
    true_pairs = []
    for p in problems:
        for i in range(N_SUBS):
            for k in range(len(LANGS)):
                l1, l2 = LANGS[k], LANGS[(k + 1) % len(LANGS)]
                true_pairs.append((picks[p][l1][i], picks[p][l2][i]))
    assert len(true_pairs) == N_PROBLEMS * N_SUBS * len(LANGS) == 192, len(true_pairs)

    # ---- FALSE pairs: one per file; different problem AND different language --
    all_files = list(problem_of.keys())
    false_pairs = []
    for f in all_files:
        cand = [g for g in all_files if problem_of[g] != problem_of[f] and lang_of[g] != lang_of[f]]
        false_pairs.append((f, random.choice(cand)))
    assert len(false_pairs) == 192

    # ---- Write source files ----
    os.makedirs(os.path.join(OUTDIR, "files"), exist_ok=True)
    id2code = {int(r["index"]): (r["flines"], r["lan"]) for _, r in files_meta.iterrows()}
    for fid, (code, lan) in id2code.items():
        with open(os.path.join(OUTDIR, "files", f"{fid}.{EXT[lan]}"), "w") as fh:
            fh.write(code if isinstance(code, str) else "".join(code))

    def fname(fid):
        return f"{fid}.{EXT[lang_of[fid]]}"

    def rec(pid, a, b, label):
        return {
            "pair_id": pid,
            "label": label,  # 1 = clone (true, cross-language), 0 = non-clone
            "file1": fname(a),
            "file2": fname(b),
            "lang1": lang_of[a],
            "lang2": lang_of[b],
            "problem1": problem_of[a],
            "problem2": problem_of[b],
        }

    records, pid = [], 0
    for a, b in true_pairs:
        records.append(rec(pid, a, b, 1)); pid += 1
    for a, b in false_pairs:
        records.append(rec(pid, a, b, 0)); pid += 1
    pairs = pd.DataFrame(records)
    os.makedirs(OUTDIR, exist_ok=True)
    pairs.to_csv(os.path.join(OUTDIR, "pairs.csv"), index=False)

    files_meta[["index", "problem", "lan", "file", "lines"]].rename(
        columns={"index": "file_id"}
    ).to_csv(os.path.join(OUTDIR, "files_meta.csv"), index=False)

    # ---- Summary + sanity checks ----
    true_all_cross_lang = all(lang_of[a] != lang_of[b] for a, b in true_pairs)
    true_all_same_prob = all(problem_of[a] == problem_of[b] for a, b in true_pairs)
    false_all_diff_prob = all(problem_of[a] != problem_of[b] for a, b in false_pairs)
    false_all_diff_lang = all(lang_of[a] != lang_of[b] for a, b in false_pairs)
    files_in_true = {f for pr in true_pairs for f in pr}
    n_files = len({fid for pr in pairs[["file1", "file2"]].values for fid in pr})

    print(f"problems sampled            : {N_PROBLEMS}")
    print(f"unique files                : {len(id2code)}  (referenced in pairs: {n_files})")
    print(f"true  pairs                 : {len(true_pairs)}")
    print(f"false pairs                 : {len(false_pairs)}")
    print(f"total pairs                 : {len(pairs)}")
    print(f"true pairs all cross-language & same-problem : {true_all_cross_lang and true_all_same_prob}")
    print(f"false pairs all diff-problem & diff-language  : {false_all_diff_prob and false_all_diff_lang}")
    print(f"every file participates in a true pair         : {len(files_in_true) == len(id2code)}")
    print(f"\nwrote {OUTDIR}/pairs.csv, {OUTDIR}/files_meta.csv, {OUTDIR}/files/*")


if __name__ == "__main__":
    main()
