"""
Minimal inference snippets for each quantization method in Table 1.
Each section is independent — run only the one you need.

Pre-quantized Llama-3.1-8B-Instruct models used throughout.
Install dependencies per section before running.
"""

import csv
import itertools
import sys
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

with open("prompt.md") as _f:
    PROMPT_TEMPLATE = _f.read()


# ==============================================================================
# Data loading
# ==============================================================================

@dataclass
class TestCase:
    program: str   # e.g. "bubblesort"
    variant: str   # e.g. "0_orig", "test_0_orig_pg_krakatau"
    filepath: Path
    code: str


def load_test_cases(tests_dir: str = "ocd/tests") -> list[TestCase]:
    cases = []
    root = Path(tests_dir)
    for java_file in sorted(root.rglob("*.java")):
        variant = java_file.parent.name
        program = java_file.parent.parent.name
        cases.append(TestCase(
            program=program,
            variant=variant,
            filepath=java_file,
            code=java_file.read_text(errors="replace"),
        ))
    return cases


def generate_pairs(test_cases: list[TestCase]):
    """Full Cartesian product (n×n = 10,000 pairs). Ground truth is CLONE iff same program."""
    for tc_a, tc_b in itertools.product(test_cases, repeat=2):
        ground_truth = "CLONE" if tc_a.program == tc_b.program else "NON-CLONE"
        yield tc_a, tc_b, ground_truth


def build_prompt(content_a: str, content_b: str, lang: str) -> str:
    return PROMPT_TEMPLATE.format(contentA=content_a, contentB=content_b, lang=lang)


# ==============================================================================
# Model loaders (each returns a callable: (content_a, content_b, lang) -> str)
# ==============================================================================

def _make_transformers_infer(model, tokenizer, max_new_tokens: int = 128):
    import torch

    def infer(content_a: str, content_b: str, lang: str) -> str:
        prompt = build_prompt(content_a, content_b, lang)
        messages = [{"role": "user", "content": prompt}]
        try:
            formatted = tokenizer.apply_chat_template(
                messages, tokenize=False, add_generation_prompt=True
            )
        except (ImportError, Exception):
            # Fallback for environments where jinja2 < 3.1 is installed
            formatted = (
                f"<|begin_of_text|><|start_header_id|>user<|end_header_id|>\n\n"
                f"{prompt}<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
            )
        inputs = tokenizer(formatted, return_tensors="pt").to(model.device)
        with torch.no_grad():
            outputs = model.generate(**inputs, max_new_tokens=max_new_tokens,
                                     pad_token_id=tokenizer.eos_token_id)
        return tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True
        )

    return infer


def _sanitize_hf_name(hf_model: str) -> str:
    """Convert a HuggingFace model ID to a filesystem-safe string."""
    return hf_model.replace("/", "__").replace("::", "_")


def load_original(hf_model: str | None = None):
    """Full-precision BF16. pip install transformers accelerate"""
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

    model_id = hf_model or "meta-llama/Meta-Llama-3.1-8B-Instruct"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
        model_id, torch_dtype=torch.bfloat16, device_map="auto"
    )
    return _make_transformers_infer(model, tokenizer)


def load_gguf(hf_model: str | None = None):
    """GGUF Q4_K_M. pip install llama-cpp-python
    hf_model format: 'repo_id' or 'repo_id::filename.gguf'
    """
    from llama_cpp import Llama

    default_repo = "bartowski/Meta-Llama-3.1-8B-Instruct-GGUF"
    default_file = "Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf"

    if hf_model and "::" in hf_model:
        repo_id, filename = hf_model.split("::", 1)
    else:
        repo_id = hf_model or default_repo
        filename = default_file

    llm = Llama.from_pretrained(
        repo_id=repo_id,
        filename=filename,
        n_gpu_layers=-1,
        n_ctx=8192,
        verbose=False,
    )

    def infer(content_a: str, content_b: str, lang: str) -> str:
        prompt = build_prompt(content_a, content_b, lang)
        output = llm.create_chat_completion(
            messages=[{"role": "user", "content": prompt}],
            max_tokens=128,
        )
        return output["choices"][0]["message"]["content"]

    return infer


def load_aqlm(hf_model: str | None = None):
    """AQLM 2-bit + PV-Tuning. pip install transformers accelerate aqlm[gpu]"""
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch
    from aqlm.inference import QuantizedLinear

    # PyTorch 2.8 + CUDA 12.2: cuBLAS GemmEx fails for the AQLM GEMM path on A100.
    # Force GEMV (custom CUDA kernel, iterates per row) which works correctly.
    _orig_prepare = QuantizedLinear.prepare_matmul_op
    def _patched_prepare(self, input):
        _orig_prepare(self, input)
        self.use_gemv_rule = lambda inp: True
    QuantizedLinear.prepare_matmul_op = _patched_prepare

    model_id = hf_model or "ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
        model_id, dtype=torch.float16, device_map="auto"
    )
    return _make_transformers_infer(model, tokenizer)


LOADERS = {
    "original": load_original,
    "gguf":     load_gguf,
    "aqlm":     load_aqlm,
}


# ==============================================================================
# Experiment runner
# ==============================================================================

def _load_completed(output_csv: str) -> set[tuple]:
    """Return set of (program_a, variant_a, program_b, variant_b) already in the CSV."""
    completed = set()
    p = Path(output_csv)
    if not p.exists():
        return completed
    with open(output_csv, newline="") as f:
        for row in csv.DictReader(f):
            completed.add((row["program_a"], row["variant_a"],
                           row["program_b"], row["variant_b"]))
    return completed


def run_experiment(
    model_name: str,
    hf_model: str | None = None,
    tests_dir: str = "ocd/tests",
    output_csv: str | None = None,
    lang: str = "Java",
):
    """
    Load all test cases from tests_dir, generate every n×n pair,
    run model_name on each, and write results to output_csv.

    Resumes automatically if output_csv already exists (skips completed pairs).
    Columns: program_a, variant_a, program_b, variant_b, ground_truth, timestamp, response
    """
    if model_name not in LOADERS:
        raise ValueError(f"Unknown model '{model_name}'. Choose from: {list(LOADERS)}")

    if output_csv is None:
        if hf_model:
            output_csv = f"results_{model_name}_{_sanitize_hf_name(hf_model)}.csv"
        else:
            output_csv = f"results_{model_name}.csv"

    test_cases = load_test_cases(tests_dir)
    pairs = list(generate_pairs(test_cases))
    total = len(pairs)

    completed = _load_completed(output_csv)
    remaining = total - len(completed)
    print(f"Loaded {len(test_cases)} files → {total} pairs "
          f"({len(completed)} already done, {remaining} remaining)", flush=True)

    if remaining == 0:
        print("All pairs already completed. Nothing to do.")
        return

    infer = LOADERS[model_name](hf_model)
    print(f"Model '{model_name}' loaded. Starting inference...", flush=True)

    fieldnames = ["program_a", "variant_a", "program_b", "variant_b",
                  "ground_truth", "timestamp", "response"]
    file_exists = Path(output_csv).exists()
    with open(output_csv, "a", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        if not file_exists:
            writer.writeheader()

        done = len(completed)
        for idx, (tc_a, tc_b, ground_truth) in enumerate(pairs, 1):
            key = (tc_a.program, tc_a.variant, tc_b.program, tc_b.variant)
            if key in completed:
                continue

            response = infer(tc_a.code, tc_b.code, lang)
            ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            done += 1
            writer.writerow({
                "program_a":    tc_a.program,
                "variant_a":    tc_a.variant,
                "program_b":    tc_b.program,
                "variant_b":    tc_b.variant,
                "ground_truth": ground_truth,
                "timestamp":    ts,
                "response":     response,
            })
            f.flush()
            print(f"[{done}/{total}] {ts} | {tc_a.program}/{tc_a.variant} vs "
                  f"{tc_b.program}/{tc_b.variant} → {ground_truth}", flush=True)

    print(f"Done. Results saved to {output_csv}")


# ==============================================================================
# Legacy single-call helpers (kept for quick ad-hoc use)
# ==============================================================================

def run_gguf(content_a: str, content_b: str, lang: str):
    print(load_gguf()(content_a, content_b, lang))


def run_original(content_a: str, content_b: str, lang: str):
    print(load_original()(content_a, content_b, lang))


def run_aqlm(content_a: str, content_b: str, lang: str):
    print(load_aqlm()(content_a, content_b, lang))


# ==============================================================================
# Entry point
# Usage: python run_quantization.py <model_name> [hf_model] [tests_dir] [output_csv]
#   model_name : original | gguf | aqlm
#   hf_model   : HuggingFace model ID (default: hardcoded per model type)
#                GGUF supports 'repo_id::filename.gguf' to pick a specific quant
#                e.g. bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf
#   tests_dir  : path to OCD tests folder  (default: ocd/tests)
#   output_csv : where to save results     (default: results_<model_name>_<hf_model>.csv)
# ==============================================================================
if __name__ == "__main__":
    model_name = sys.argv[1] if len(sys.argv) > 1 else "original"
    hf_model   = sys.argv[2] if len(sys.argv) > 2 else None
    tests_dir  = sys.argv[3] if len(sys.argv) > 3 else "ocd/tests"
    output_csv = sys.argv[4] if len(sys.argv) > 4 else None

    run_experiment(model_name, hf_model, tests_dir, output_csv)
