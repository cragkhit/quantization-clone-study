#!/usr/bin/env python3
"""Build a LoRA fine-tuning set from the SimilBench AIZU384F clone benchmark.

The AIZU384F benchmark (AIZU Online Judge submissions) is a *different source*
than the GCJ Google-Code-Jam test set, so training on it and testing on
`gcj_java_clones/pairs.csv` is a clean cross-dataset setup with no problem
overlap.

Each training example mirrors inference exactly:
  * user turn   = the `prompt.md` template filled via `build_prompt()`
                  (same text `run_quantization.py` sends at eval time)
  * assistant   = the JSON object `evaluate_results.py` parses, i.e.
                  {"answer": "YES-SIMILAR"|"NO-NOT-SIMILAR", "explanation": ...}

Ground truth: truth/AIZU384F.csv has columns `Truth,fileA,fileB,lang` with
Truth in {T (clone), F (non-clone)}. All 15 languages are used (per the study
decision); the per-pair `lang` fills the prompt's {lang} slot just like the
monolingual eval path.

Augmentation: each pair is emitted in both orders (A,B) and (B,A) to remove
position bias, doubling 384 -> 768 examples. A small stratified slice is held
out as a validation set for loss monitoring (the real test is GCJ-Java).

Outputs (JSONL, one {"messages": [...]} object per line):
  finetune_data/aizu_train.jsonl
  finetune_data/aizu_val.jsonl
"""
import argparse
import csv
import json
import random
from collections import Counter
from pathlib import Path

from run_quantization import build_prompt

# SimilBench csv lang label -> the token the GCJ eval path would use. We lower-
# case so the Java prompts read "... the java code snippets ..." exactly like
# the GCJ-Java test prompts (pairs.csv carries lang="java").
LANG_LABEL = {
    "C": "c", "C++": "c++", "C#": "c#", "D": "d", "Go": "go",
    "Haskell": "haskell", "Java": "java", "JavaScript": "javascript",
    "Kotlin": "kotlin", "OCaml": "ocaml", "PHP": "php", "Python": "python",
    "Ruby": "ruby", "Rust": "rust", "Scala": "scala",
}

# The answer text `evaluate_results.parse_answer` maps to CLONE / NON-CLONE.
ANSWER = {"T": "YES-SIMILAR", "F": "NO-NOT-SIMILAR"}
# Generic, label-consistent explanations. Only the `answer` field affects the
# metrics; the explanation keeps the output valid JSON and on-format.
EXPLANATION = {
    "T": "The two snippets implement the same task and produce the same result.",
    "F": "The two snippets address different tasks and do not produce the same result.",
}


def build_example(code_a: str, code_b: str, lang: str, truth: str) -> dict:
    prompt = build_prompt(code_a, code_b, lang)
    answer = json.dumps(
        {"answer": ANSWER[truth], "explanation": EXPLANATION[truth]},
        ensure_ascii=False,
    )
    return {
        "messages": [
            {"role": "user", "content": prompt},
            {"role": "assistant", "content": answer},
        ],
        "meta": {"lang": lang, "truth": truth},  # provenance for stratify/audit
    }


def main():
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--truth", default="finetune_data/SimilBench-main/truth/AIZU384F.csv")
    ap.add_argument("--files-dir", default="finetune_data/SimilBench-main/data/AIZU384F")
    ap.add_argument("--out-dir", default="finetune_data")
    ap.add_argument("--val-frac", type=float, default=0.15,
                    help="Fraction held out for validation (stratified by truth+lang).")
    ap.add_argument("--no-swap", action="store_true",
                    help="Disable (A,B)->(B,A) order augmentation.")
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()

    rng = random.Random(args.seed)
    files_dir = Path(args.files_dir)
    code_cache: dict[str, str] = {}

    def read_code(fname: str) -> str:
        if fname not in code_cache:
            code_cache[fname] = (files_dir / fname).read_text(errors="replace")
        return code_cache[fname]

    rows = list(csv.DictReader(open(args.truth)))

    # ---- Stratified train/val split at the *pair* level (both order-swapped
    #      copies of a pair go to the same split, so no leakage across split) ----
    strata: dict[tuple, list[int]] = {}
    for i, r in enumerate(rows):
        strata.setdefault((r["Truth"], r["lang"]), []).append(i)
    val_idx: set[int] = set()
    for _key, idxs in strata.items():
        idxs = idxs[:]
        rng.shuffle(idxs)
        n_val = round(len(idxs) * args.val_frac)
        val_idx.update(idxs[:n_val])

    train_ex, val_ex = [], []
    for i, r in enumerate(rows):
        lang = LANG_LABEL.get(r["lang"], r["lang"].lower())
        truth = r["Truth"].strip().upper()
        ca, cb = read_code(r["fileA"]), read_code(r["fileB"])
        orders = [(ca, cb)] if args.no_swap else [(ca, cb), (cb, ca)]
        bucket = val_ex if i in val_idx else train_ex
        for x, y in orders:
            bucket.append(build_example(x, y, lang, truth))

    rng.shuffle(train_ex)
    rng.shuffle(val_ex)

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    def dump(path: Path, examples: list[dict]):
        with open(path, "w") as f:
            for ex in examples:
                f.write(json.dumps(ex, ensure_ascii=False) + "\n")

    dump(out_dir / "aizu_train.jsonl", train_ex)
    dump(out_dir / "aizu_val.jsonl", val_ex)

    def summarize(name, ex):
        t = Counter(e["meta"]["truth"] for e in ex)
        print(f"  {name:6s}: {len(ex):4d} examples  (clone T={t['T']}, non-clone F={t['F']})")

    print(f"AIZU384F -> LoRA set  (val_frac={args.val_frac}, "
          f"swap_aug={'off' if args.no_swap else 'on'})")
    print(f"  source pairs: {len(rows)}  |  files: {len(code_cache)}")
    summarize("train", train_ex)
    summarize("val", val_ex)
    print(f"  wrote {out_dir}/aizu_train.jsonl, {out_dir}/aizu_val.jsonl")


if __name__ == "__main__":
    main()
