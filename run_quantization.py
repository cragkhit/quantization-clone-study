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

def _make_transformers_infer(model, tokenizer, max_new_tokens: int = 128,
                             warmup: bool = False):
    import torch

    if warmup:
        # Pay CUDA kernel compilation/autotuning cost before the real experiment.
        # Run two short generations: one single-token (warms bs=1 kernel path)
        # and one ~16-token sequence (warms the prefill path).
        print("Warming up CUDA kernels...", flush=True)
        _bos = torch.tensor([[model.config.bos_token_id]], device=model.device)
        with torch.no_grad():
            model.generate(_bos, max_new_tokens=2,
                           pad_token_id=tokenizer.eos_token_id)
            model.generate(
                torch.cat([_bos] * 1, dim=0).expand(1, 16),
                max_new_tokens=2, pad_token_id=tokenizer.eos_token_id)
        print("Warmup complete.", flush=True)

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
    return hf_model.replace("/", "_").replace("::", "_")


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
    """AQLM 2-bit + PV-Tuning. pip install transformers accelerate aqlm[gpu]
    Run with: CC=gcc-11 CXX=g++-11 python run_quantization.py aqlm
    """
    import os
    import math
    os.environ.setdefault("CC", "gcc-11")
    os.environ.setdefault("CXX", "g++-11")

    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch
    import aqlm.inference_kernels.kernel_selector as _ks
    import aqlm.inference as _aqlm_inf
    import aqlm.inference_kernels as _aqlm_ik
    from aqlm.inference_kernels.dequantization import dequantize_gemm as _dequant_gemm

    # The GEMM path (optimize_for_training=True, used for prefill of long sequences)
    # calls code1x16_matmat_dequant which hits a cuBLAS FP16 GemmEx error on this
    # PyTorch 2.8 + A100 setup. Route it through dequantize_gemm (dequantize weights
    # then F.linear) which avoids the broken cuBLAS path.
    # The GEMV path (optimize_for_training=False, used for single-token generation)
    # uses the native code1x16_matmat CUDA kernel, which is fast and correct.
    _orig_get_fwd = _ks.get_forward_pass_kernel
    def _patched_get_fwd(codebooks, optimize_for_training):
        if optimize_for_training:
            return _dequant_gemm
        return _orig_get_fwd(codebooks, optimize_for_training)
    _ks.get_forward_pass_kernel = _patched_get_fwd
    _aqlm_ik.get_forward_pass_kernel = _patched_get_fwd
    _aqlm_inf.get_forward_pass_kernel = _patched_get_fwd

    model_id = hf_model or "ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
        model_id, dtype=torch.float16, device_map="auto"
    )

    # The AQLM checkpoint stores lm_head as a regular Linear (not quantized),
    # but the architecture registers it as QuantizedLinear — causing random
    # codebooks/codes to be initialized and garbage output.
    # Fix: reload lm_head.weight from the checkpoint and install a proper Linear.
    import torch.nn as nn
    from safetensors import safe_open
    from huggingface_hub import hf_hub_download
    lm_head_weight = None
    try:
        shard_path = hf_hub_download(repo_id=model_id, filename="model.safetensors")
        with safe_open(shard_path, framework="pt") as _sf:
            if "lm_head.weight" in _sf.keys():
                lm_head_weight = _sf.get_tensor("lm_head.weight")
    except Exception:
        pass
    if lm_head_weight is not None:
        out_features, in_features = lm_head_weight.shape
        _new_lm_head = nn.Linear(in_features, out_features, bias=False,
                                 dtype=torch.float16, device=model.device)
        _new_lm_head.weight = nn.Parameter(lm_head_weight.to(model.device))
        model.lm_head = _new_lm_head
        print(f"Replaced QuantizedLinear lm_head with nn.Linear ({lm_head_weight.shape})")

    return _make_transformers_infer(model, tokenizer)


def load_higgs(hf_model: str | None = None):
    """HIGGS-GPTQ quantized model. pip install gptqmodel transformers accelerate"""
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

    model_id = hf_model or "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
        model_id, device_map="auto", torch_dtype=torch.float16
    )
    return _make_transformers_infer(model, tokenizer)


def load_qtip(hf_model: str | None = None):
    """QTIP 4-bit. Requires qtip/ repo (Cornell-RelaxML/qtip) and qtip_venv.
    Run with: source qtip_venv/bin/activate && CC=gcc-11 CXX=g++-11 python run_quantization.py qtip
    """
    import os
    import sys
    import subprocess

    os.environ.setdefault("CC", "gcc-11")
    os.environ.setdefault("CXX", "g++-11")

    qtip_dir = Path(__file__).parent / "qtip"
    kernels_dir = qtip_dir / "qtip-kernels"

    # Build CUDA kernel if not already built
    kernel_so = list(kernels_dir.glob("qtip_kernels*.so"))
    if not kernel_so:
        print("Building QTIP CUDA kernel...", flush=True)
        subprocess.run(
            [sys.executable, "setup.py", "build_ext", "--inplace"],
            cwd=kernels_dir, env={**os.environ, "CC": "gcc-11", "CXX": "g++-11"},
            check=True,
        )
        kernel_so = list(kernels_dir.glob("qtip_kernels*.so"))

    # Add repo and kernel dir to sys.path so QTIP modules are importable
    for p in [str(qtip_dir), str(kernels_dir)]:
        if p not in sys.path:
            sys.path.insert(0, p)

    # Load the kernel op (registers torch.ops.quip_lib.*)
    if kernel_so:
        try:
            # Pre-load PyTorch shared libs so the extension's dlopen can find them.
            # Setting LD_LIBRARY_PATH after process start has no effect on the
            # dynamic linker, but ctypes.CDLL calls dlopen which populates the
            # process symbol table for subsequent dlopens.
            import ctypes
            import torch as _torch_for_path
            _torch_lib = Path(_torch_for_path.__file__).parent / "lib"
            for _lib in ["libc10.so", "libtorch.so", "libtorch_cpu.so",
                         "libc10_cuda.so", "libtorch_cuda.so"]:
                _p = _torch_lib / _lib
                if _p.exists():
                    ctypes.CDLL(str(_p))
            import qtip_kernels  # noqa: F401
        except ImportError as e:
            print(f"Warning: QTIP kernel import failed ({e}), falling back to pure-PyTorch path.", flush=True)
            # Force has_kernel=False so BitshiftLinear uses the pure-PyTorch fallback
            import lib.utils.kernel_check as _kc
            _kc.has_kernel = lambda *a, **kw: False

    from lib.utils.unsafe_import import model_from_hf_path
    from lib.linear.quantized_linear import QuantizedLinear as _QtipQL
    from transformers import AutoTokenizer
    import torch

    model_id = hf_model or "relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit"
    print(f"Loading QTIP model: {model_id}", flush=True)
    model, model_str = model_from_hf_path(model_id)

    # Switch every QuantizedLinear to train-fixW mode, which pre-computes
    # the dequantized+Hadamard-rotated weight matrix once and stores it as
    # a plain FP16 tensor (hatW). Subsequent forward passes then do a single
    # cuBLAS matmul instead of on-the-fly trellis decompression, making
    # prefill of long sequences ~5-10x faster at the cost of ~7 GB extra VRAM.
    for _m in model.modules():
        if isinstance(_m, _QtipQL):
            _m.mode = 'train-fixW'
    print("Caching dequantized weights (train-fixW warmup)...", flush=True)
    with torch.no_grad():
        _dummy = torch.tensor([[model.config.bos_token_id]], device=model.device)
        model(_dummy)
    print("Weight cache ready.", flush=True)

    tokenizer = AutoTokenizer.from_pretrained(model_str)
    return _make_transformers_infer(model, tokenizer, max_new_tokens=256, warmup=True)


LOADERS = {
    "original": load_original,
    "gguf":     load_gguf,
    "aqlm":     load_aqlm,
    "higgs":    load_higgs,
    "qtip":     load_qtip,
}


# ==============================================================================
# Experiment runner
# ==============================================================================

def _round_csv_path(base: str, round_num: int) -> str:
    """Return the CSV path for a specific round: {base}_round{N}.csv"""
    return f"{base}_round{round_num}.csv"


def _load_completed_round(round_csv: str) -> set[tuple]:
    """Return set of (program_a, variant_a, program_b, variant_b) done in a round CSV."""
    completed = set()
    p = Path(round_csv)
    if not p.exists():
        return completed
    with open(round_csv, newline="") as f:
        for row in csv.DictReader(f):
            completed.add((row["program_a"], row["variant_a"],
                           row["program_b"], row["variant_b"]))
    return completed


def run_experiment(
    model_name: str,
    hf_model: str | None = None,
    tests_dir: str = "ocd/tests",
    output_base: str | None = None,
    lang: str = "Java",
    rounds: int = 1,
):
    """
    Load all test cases from tests_dir, generate every n×n pair, and run model_name
    for the specified number of rounds. Each round is saved to its own CSV:
        {output_base}_round1.csv, {output_base}_round2.csv, ...

    Resumes automatically: completed rounds are skipped; partial rounds continue
    from where they left off.
    Columns: program_a, variant_a, program_b, variant_b, ground_truth, timestamp, response
    """
    if model_name not in LOADERS:
        raise ValueError(f"Unknown model '{model_name}'. Choose from: {list(LOADERS)}")

    if output_base is None:
        if hf_model:
            model_part = hf_model.split("/")[-1].split("::")[0]
            output_base = f"results/{model_part}/results_{_sanitize_hf_name(hf_model)}"
        else:
            output_base = f"results_{model_name}"

    test_cases = load_test_cases(tests_dir)
    pairs = list(generate_pairs(test_cases))
    pairs_per_round = len(pairs)

    # Check overall resume state across all rounds
    total_done = sum(len(_load_completed_round(_round_csv_path(output_base, r)))
                     for r in range(1, rounds + 1))
    total = pairs_per_round * rounds
    print(f"Loaded {len(test_cases)} files → {pairs_per_round} pairs × {rounds} round(s) = {total} total "
          f"({total_done} already done, {total - total_done} remaining)", flush=True)

    if total_done == total:
        print("All rounds already completed. Nothing to do.")
        return

    infer = LOADERS[model_name](hf_model)
    print(f"Model '{model_name}' loaded. Starting inference...", flush=True)

    fieldnames = ["program_a", "variant_a", "program_b", "variant_b",
                  "ground_truth", "timestamp", "response"]

    overall_done = total_done
    for round_num in range(1, rounds + 1):
        round_csv = _round_csv_path(output_base, round_num)
        completed = _load_completed_round(round_csv)

        if len(completed) == pairs_per_round:
            print(f"--- Round {round_num}/{rounds}: already complete, skipping ---", flush=True)
            continue

        print(f"--- Round {round_num}/{rounds} "
              f"({len(completed)} done, {pairs_per_round - len(completed)} remaining) ---", flush=True)

        Path(round_csv).parent.mkdir(parents=True, exist_ok=True)
        file_exists = Path(round_csv).exists()
        with open(round_csv, "a", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            if not file_exists:
                writer.writeheader()

            for tc_a, tc_b, ground_truth in pairs:
                key = (tc_a.program, tc_a.variant, tc_b.program, tc_b.variant)
                if key in completed:
                    continue

                response = infer(tc_a.code, tc_b.code, lang)
                ts = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                overall_done += 1
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
                print(f"[{overall_done}/{total}] {ts} | R{round_num} | "
                      f"{tc_a.program}/{tc_a.variant} vs "
                      f"{tc_b.program}/{tc_b.variant} → {ground_truth}", flush=True)

        print(f"Round {round_num} done → {round_csv}", flush=True)

    print(f"All {rounds} round(s) complete. Files: "
          f"{', '.join(_round_csv_path(output_base, r) for r in range(1, rounds + 1))}")


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
# Usage: python run_quantization.py <model_name> [hf_model] [tests_dir] [output_base] [rounds]
#   model_name  : original | gguf | aqlm | higgs | qtip
#   hf_model    : HuggingFace model ID (default: hardcoded per model type)
#                 GGUF supports 'repo_id::filename.gguf' to pick a specific quant
#                 e.g. bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf
#   tests_dir   : path to OCD tests folder   (default: ocd/tests)
#   output_base : base name for result files  (default: results_<model_name>[_<hf_model>])
#                 each round is saved as {output_base}_round{N}.csv
#   rounds      : number of times to repeat the full experiment (default: 1)
# ==============================================================================
if __name__ == "__main__":
    model_name  = sys.argv[1] if len(sys.argv) > 1 else "original"
    hf_model    = sys.argv[2] if len(sys.argv) > 2 else None
    tests_dir   = sys.argv[3] if len(sys.argv) > 3 else "ocd/tests"
    output_base = sys.argv[4] if len(sys.argv) > 4 else None
    rounds      = int(sys.argv[5]) if len(sys.argv) > 5 else 1

    run_experiment(model_name, hf_model, tests_dir, output_base, rounds=rounds)
