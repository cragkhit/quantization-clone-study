#!/usr/bin/env python3
"""Build a LoRA fine-tuning set from the SimilBench AIZU324CLF *cross-language*
clone benchmark.

Companion to prepare_aizu_finetune.py (which handles the monolingual AIZU384F
set). Here every pair is cross-language: fileA and fileB are different-language
solutions to the same AIZU Online Judge problem. Ground truth:
truth/AIZU324CLF.csv has columns `Truth,fileA,fileB,lang` with Truth in
{T (clone), F (non-clone)} and lang always "CL". 324 balanced pairs (162/162).

The csv `lang` column is uninformative ("CL"), so each pair's prompt language is
derived from the file extensions and phrased "LangA and LangB" — matching how the
GCJ cross-language test is prompted (run_quantization._pair_lang, e.g.
"Java and Python"). Training on AIZU cross-language pairs is a clean cross-dataset
setup for the GCJ^CL test (disjoint sources, no problem overlap).

Each example mirrors inference exactly:
  * user turn = the prompt.md template (via build_prompt) with the derived lang
  * assistant = {"answer": "YES-SIMILAR"|"NO-NOT-SIMILAR", "explanation": ...},
                the JSON evaluate_results.parse_answer reads. Loss on answer only.

Augmentation: each pair emitted in both orders (A,B)+(B,A). A small stratified
slice (by Truth + language-pair) is held out for val loss monitoring.

Outputs (JSONL, one {"messages": [...]} object per line):
  finetune_data/aizu_cl_train.jsonl
  finetune_data/aizu_cl_val.jsonl
"""
import argparse
import csv
import json
import random
from collections import Counter
from pathlib import Path

from run_quantization import build_prompt

# File extension -> display language name. Capitalised names + "A and B" phrasing
# mirror run_quantization._pair_lang used to prompt the GCJ cross-language test.
EXT_TO_LANG = {
    "c": "C", "cpp": "C++", "cs": "C#", "d": "D", "go": "Go", "hs": "Haskell",
    "java": "Java", "js": "JavaScript", "kt": "Kotlin", "ml": "OCaml",
    "php": "PHP", "py": "Python", "rb": "Ruby", "rs": "Rust", "scala": "Scala",
}

ANSWER = {"T": "YES-SIMILAR", "F": "NO-NOT-SIMILAR"}
EXPLANATION = {
    "T": "The two snippets implement the same task and produce the same result.",
    "F": "The two snippets address different tasks and do not produce the same result.",
}


def ext_lang(fname: str) -> str:
    ext = fname.rsplit(".", 1)[-1].lower()
    return EXT_TO_LANG.get(ext, ext)


def pair_lang(file_a: str, file_b: str) -> str:
    """"LangA and LangB" from the two file extensions (or a single name if equal)."""
    a, b = ext_lang(file_a), ext_lang(file_b)
    return a if a == b else f"{a} and {b}"


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
        "meta": {"lang": lang, "truth": truth},
    }


def main():
    ap = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--truth", default="finetune_data/SimilBench-main/truth/AIZU324CLF.csv")
    ap.add_argument("--files-dir", default="finetune_data/SimilBench-main/data/AIZU324CLF")
    ap.add_argument("--out-dir", default="finetune_data")
    ap.add_argument("--out-prefix", default="aizu_cl",
                    help="Output basename: <prefix>_train.jsonl / <prefix>_val.jsonl")
    ap.add_argument("--val-frac", type=float, default=0.15,
                    help="Fraction held out for validation (stratified by truth+lang-pair).")
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
    langs = [pair_lang(r["fileA"], r["fileB"]) for r in rows]

    # ---- Stratified train/val split at the *pair* level (both order-swapped
    #      copies of a pair go to the same split, so no leakage across split) ----
    strata: dict[tuple, list[int]] = {}
    for i, r in enumerate(rows):
        strata.setdefault((r["Truth"], langs[i]), []).append(i)
    val_idx: set[int] = set()
    for _key, idxs in strata.items():
        idxs = idxs[:]
        rng.shuffle(idxs)
        n_val = round(len(idxs) * args.val_frac)
        val_idx.update(idxs[:n_val])

    train_ex, val_ex = [], []
    for i, r in enumerate(rows):
        truth = r["Truth"].strip().upper()
        ca, cb = read_code(r["fileA"]), read_code(r["fileB"])
        orders = [(ca, cb)] if args.no_swap else [(ca, cb), (cb, ca)]
        bucket = val_ex if i in val_idx else train_ex
        for x, y in orders:
            bucket.append(build_example(x, y, langs[i], truth))

    rng.shuffle(train_ex)
    rng.shuffle(val_ex)

    out_dir = Path(args.out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    def dump(path: Path, examples: list[dict]):
        with open(path, "w") as f:
            for ex in examples:
                f.write(json.dumps(ex, ensure_ascii=False) + "\n")

    train_path = out_dir / f"{args.out_prefix}_train.jsonl"
    val_path = out_dir / f"{args.out_prefix}_val.jsonl"
    dump(train_path, train_ex)
    dump(val_path, val_ex)

    def summarize(name, ex):
        t = Counter(e["meta"]["truth"] for e in ex)
        print(f"  {name:6s}: {len(ex):4d} examples  (clone T={t['T']}, non-clone F={t['F']})")

    print(f"AIZU324CLF (cross-language) -> LoRA set  (val_frac={args.val_frac}, "
          f"swap_aug={'off' if args.no_swap else 'on'})")
    print(f"  source pairs: {len(rows)}  |  files: {len(code_cache)}")
    print(f"  language pairs: {dict(Counter(langs))}")
    summarize("train", train_ex)
    summarize("val", val_ex)
    print(f"  wrote {train_path}, {val_path}")


if __name__ == "__main__":
    main()
