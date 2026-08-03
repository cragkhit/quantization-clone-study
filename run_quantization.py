"""
Clone-detection inference runner for quantized LLMs.

Supports nine model backends (original, gguf, qwen, qwen3fp8, deepseek, aqlm, higgs, qtip, codellama) and runs
every n×n pair of Java code snippets from the OCD test suite, saving results
to per-round CSV files that can be evaluated with evaluate_results.py.

Usage:
    python run_quantization.py <model> [hf_model] [--tests-dir DIR]
                               [--output BASE] [--rounds N]
    python run_quantization.py --help

See exp_notes.md for per-backend setup instructions and known workarounds.
"""

import csv
import itertools
import sys
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

_PROMPT_TEMPLATE: str | None = None


def _get_prompt_template() -> str:
    global _PROMPT_TEMPLATE
    if _PROMPT_TEMPLATE is None:
        _PROMPT_TEMPLATE = (Path(__file__).parent / "prompt.md").read_text()
    return _PROMPT_TEMPLATE


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
    """Full Cartesian product (n×n = 10,000 pairs). Ground truth is CLONE iff same program.

    Yields (tc_a, tc_b, ground_truth, pair_id, pair_lang). pair_id and pair_lang
    are None here, so the resume key falls back to the
    (program_a, variant_a, program_b, variant_b) 4-tuple and the prompt uses the
    experiment-wide language.
    """
    for tc_a, tc_b in itertools.product(test_cases, repeat=2):
        ground_truth = "CLONE" if tc_a.program == tc_b.program else "NON-CLONE"
        yield tc_a, tc_b, ground_truth, None, None


# Map GCJ language codes to human-readable names used in the prompt.
_LANG_NAMES = {"java": "Java", "cpp": "C++", "py": "Python", "php": "PHP"}


def _pair_lang(row: dict) -> str | None:
    """Human-readable language for a pair, or None if the CSV carries no language.

    - `lang1`/`lang2` columns (cross-language set): "Java and Python", or just
      "Java" when the two sides share a language.
    - single `lang` column (monolingual set): that language's name.
    """
    def name(code):
        return _LANG_NAMES.get(str(code).strip().lower(), str(code).strip())

    if row.get("lang1") and row.get("lang2"):
        a, b = name(row["lang1"]), name(row["lang2"])
        return a if a == b else f"{a} and {b}"
    if row.get("lang"):
        return name(row["lang"])
    return None


def load_pairs_file(pairs_csv: str, files_dir: str | None = None):
    """Load an explicit list of pairs from a CSV (e.g. gcj_java_clones/pairs.csv).

    Expected columns: pair_id, label (1=clone/0=non-clone), file1, file2,
    problem1, problem2, and either `lang` or `lang1`/`lang2`. Source code is read
    from files_dir (default: a `files/` folder next to the CSV). Yields
    (tc_a, tc_b, ground_truth, pair_id, pair_lang); pair_id makes each row's
    resume key unique even if a (file1, file2) pair repeats, and pair_lang lets
    the prompt name the actual language(s) of each pair.
    """
    pairs_path = Path(pairs_csv)
    fdir = Path(files_dir) if files_dir else pairs_path.parent / "files"
    code_cache: dict[str, str] = {}

    def _case(fname: str, problem: str) -> TestCase:
        if fname not in code_cache:
            code_cache[fname] = (fdir / fname).read_text(errors="replace")
        return TestCase(program=problem, variant=Path(fname).stem,
                        filepath=fdir / fname, code=code_cache[fname])

    with open(pairs_path, newline="") as f:
        for row in csv.DictReader(f):
            gt = "CLONE" if str(row["label"]).strip() == "1" else "NON-CLONE"
            tc_a = _case(row["file1"], row["problem1"])
            tc_b = _case(row["file2"], row["problem2"])
            yield tc_a, tc_b, gt, str(row["pair_id"]), _pair_lang(row)


def build_prompt(content_a: str, content_b: str, lang: str) -> str:
    return _get_prompt_template().format(contentA=content_a, contentB=content_b, lang=lang)


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
                _bos.expand(1, 16),
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


def load_deepseek(hf_model: str | None = None):
    """DeepSeek-Coder-V2-Lite BF16. pip install transformers accelerate"""
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

    model_id = hf_model or "deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct"
    tokenizer = AutoTokenizer.from_pretrained(model_id, trust_remote_code=True)
    model = AutoModelForCausalLM.from_pretrained(
        model_id, torch_dtype=torch.bfloat16, device_map="auto",
        trust_remote_code=True,
    )
    return _make_transformers_infer(model, tokenizer)


def load_gguf(hf_model: str | None = None):
    """GGUF Q4_K_M. pip install llama-cpp-python
    hf_model format: 'repo_id', 'repo_id::filename.gguf', or a local path to a
    '.gguf' file (e.g. a self-quantized model under models/).
    """
    from llama_cpp import Llama

    default_repo = "bartowski/Meta-Llama-3.1-8B-Instruct-GGUF"
    default_file = "Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf"

    if hf_model and hf_model.endswith(".gguf") and Path(hf_model).is_file():
        # Local GGUF file (self-quantized via llama.cpp); load it directly.
        llm = Llama(
            model_path=hf_model,
            n_gpu_layers=-1,
            n_ctx=16384,
            verbose=False,
        )
    else:
        if hf_model and "::" in hf_model:
            repo_id, filename = hf_model.split("::", 1)
        else:
            repo_id = hf_model or default_repo
            filename = default_file

        llm = Llama.from_pretrained(
            repo_id=repo_id,
            filename=filename,
            n_gpu_layers=-1,
            n_ctx=16384,
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


def load_qwen36_gguf(hf_model: str | None = None):
    """Qwen3.6-27B (qwen35 arch) GGUF, self-quantized under models/ (no
    community GGUF exists yet for this brand-new architecture; see
    "Producing our own GGUF quants" in exp_notes.md).

    hf_model format: a local path to a '.gguf' file (default
    'models/Qwen3.6-27B-Q4_K_M.gguf'), or 'repo_id::filename.gguf' if a
    community GGUF ever appears.

    Bypasses create_chat_completion() and builds the ChatML prompt by hand,
    because llama-cpp-python's chat-completion API has no way to pass the
    'enable_thinking' Jinja variable through to the model's own chat
    template. Left at its default, the template opens an unclosed <think>
    block and the model reasons at length before ever emitting the JSON
    verdict -- 1024 generated tokens wasn't even enough to finish thinking on
    a single pair. Manually emitting the closed '<think>\n\n</think>\n\n'
    block the template would produce for enable_thinking=False (mirroring
    load_qwen36's transformers-side enable_thinking=False) skips straight to
    the JSON answer: ~4-5s/pair instead of never finishing.
    """
    from llama_cpp import Llama

    hf_model = hf_model or "models/Qwen3.6-27B-Q4_K_M.gguf"

    if hf_model.endswith(".gguf") and Path(hf_model).is_file():
        llm = Llama(model_path=hf_model, n_gpu_layers=-1, n_ctx=16384, verbose=False)
    elif "::" in hf_model:
        repo_id, filename = hf_model.split("::", 1)
        llm = Llama.from_pretrained(
            repo_id=repo_id, filename=filename,
            n_gpu_layers=-1, n_ctx=16384, verbose=False,
        )
    else:
        raise ValueError(
            f"qwen36_gguf: '{hf_model}' is not a local .gguf file and has no "
            "'repo_id::filename.gguf' separator. Pass a local path under "
            "models/, or repo::file once a community GGUF exists."
        )

    def infer(content_a: str, content_b: str, lang: str) -> str:
        prompt = build_prompt(content_a, content_b, lang)
        full_prompt = (
            f"<|im_start|>user\n{prompt}<|im_end|>\n"
            f"<|im_start|>assistant\n<think>\n\n</think>\n\n"
        )
        output = llm.create_completion(
            full_prompt,
            max_tokens=384,
            stop=["<|im_end|>"],
        )
        return output["choices"][0]["text"]

    return infer


def load_qwen(hf_model: str | None = None):
    """Qwen2.5-Coder GGUF. pip install llama-cpp-python
    hf_model format: 'repo_id::filename.gguf' or omit for default Q4_K_M.
    Default: Qwen/Qwen2.5-Coder-7B-Instruct-GGUF (Q4_K_M)
    """
    from llama_cpp import Llama

    default_repo = "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF"
    default_file = "qwen2.5-coder-7b-instruct-q4_k_m.gguf"

    if hf_model and "::" in hf_model:
        repo_id, filename = hf_model.split("::", 1)
    else:
        repo_id = hf_model or default_repo
        filename = default_file

    llm = Llama.from_pretrained(
        repo_id=repo_id,
        filename=filename,
        n_gpu_layers=-1,
        n_ctx=16384,
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


def load_gguf_lora(hf_model: str | None = None):
    """GGUF base + a LoRA adapter applied at inference (llama.cpp `lora_path`).

    Lets a fine-tuned adapter run on top of an *existing* quantized GGUF, so the
    base weights are byte-identical to a baseline you already evaluated and the
    metric delta isolates the fine-tuning lift at the same quantization.

    hf_model format: 'repo_id::base_file.gguf::/path/to/adapter.gguf'
    Default base: Qwen2.5-Coder-7B-Instruct Q4_K_M. The adapter path is required.
    """
    from llama_cpp import Llama

    default_repo = "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF"
    default_file = "qwen2.5-coder-7b-instruct-q4_k_m.gguf"

    parts = (hf_model or "").split("::")
    if len(parts) == 3:
        repo_id, filename, lora_path = parts
    elif len(parts) == 1 and parts[0]:
        repo_id, filename, lora_path = default_repo, default_file, parts[0]
    else:
        raise ValueError(
            "gguf_lora needs 'repo::base_file.gguf::adapter.gguf' "
            "(or just 'adapter.gguf' to use the default Qwen Q4_K_M base).")

    if not Path(lora_path).exists():
        raise FileNotFoundError(f"LoRA adapter not found: {lora_path}")

    print(f"GGUF base: {repo_id}::{filename}\nLoRA adapter: {lora_path}", flush=True)
    llm = Llama.from_pretrained(
        repo_id=repo_id,
        filename=filename,
        lora_path=lora_path,
        n_gpu_layers=-1,
        n_ctx=16384,
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


def load_qwen3fp8(hf_model: str | None = None):
    """Qwen3-Coder-30B-A3B-Instruct FP8 (fine-grained FP8, MoE) served via vLLM.

    vLLM's FP8-MoE kernels are far faster than transformers here, which is
    decode-latency-bound on this model (~25s/pair, GPU ~23%). Requires vllm_venv
    (vLLM 0.11 / torch 2.8+cu128, compatible with the 535/CUDA-12.2 driver) and
    an H100/sm_90 GPU. Pin one GPU with CUDA_VISIBLE_DEVICES:
      CUDA_VISIBLE_DEVICES=<gpu> vllm_venv/bin/python run_quantization.py qwen3fp8
    """
    from vllm import LLM, SamplingParams

    model_id = hf_model or "Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8"
    # gpu_memory_utilization is kept moderate: the untuned fused-MoE path
    # allocates a large transient workspace during CUDA-graph capture, which
    # OOMs if KV cache claims too much of the 80 GiB card. max_num_seqs is small
    # because inference is issued one pair at a time (no need for high batching).
    llm = LLM(
        model=model_id,
        max_model_len=32768,
        gpu_memory_utilization=0.80,
        max_num_seqs=64,
    )
    # Qwen3-Coder-Instruct recommended decoding settings (non-thinking model).
    # Sampling (temperature > 0) also gives the per-round variation the
    # majority-vote evaluation relies on.
    sampling = SamplingParams(
        temperature=0.7, top_p=0.8, top_k=20,
        repetition_penalty=1.05, max_tokens=256,
    )

    def infer(content_a: str, content_b: str, lang: str) -> str:
        prompt = build_prompt(content_a, content_b, lang)
        messages = [{"role": "user", "content": prompt}]
        outputs = llm.chat(messages, sampling, use_tqdm=False)
        return outputs[0].outputs[0].text

    return infer


def load_aqlm(hf_model: str | None = None):
    """AQLM 2-bit + PV-Tuning. pip install transformers accelerate aqlm[gpu]
    Run with: CC=gcc-11 CXX=g++-11 python run_quantization.py aqlm
    """
    import os
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
    """HIGGS-GPTQ 4-bit quantized model (Hadamard Incoherence + GPTQ).

    Dependencies:
        pip install gptqmodel transformers accelerate tiktoken fast_hadamard_transform flute-kernel

    Default model: ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit

    *** IMPORTANT: run with CUDA_VISIBLE_DEVICES set to a single, low-load GPU. ***
    FLUTE template tuning (_tune) benchmarks 144 kernel variants at load time and is
    non-deterministic under GPU contention. On a congested GPU it can select an
    incompatible template_id, producing garbage output for all inference pairs.
    Pinning to a free GPU (e.g. CUDA_VISIBLE_DEVICES=7) makes selection stable.
    """
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

    model_id = hf_model or "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit"

    try:
        tokenizer = AutoTokenizer.from_pretrained(model_id)
    except (ValueError, OSError):
        base_id = "meta-llama/Meta-Llama-3.1-8B-Instruct"
        print(f"Tokenizer not found in {model_id}, loading from {base_id}", flush=True)
        tokenizer = AutoTokenizer.from_pretrained(base_id)

    model = AutoModelForCausalLM.from_pretrained(
        model_id, device_map="auto", torch_dtype=torch.float16
    )
    return _make_transformers_infer(model, tokenizer)


def load_codellama(hf_model: str | None = None):
    """CodeLlama base or instruct model. pip install transformers accelerate
    Default: codellama/CodeLlama-7b-hf (base model, completion-style prompting)
    For instruct variant: codellama/CodeLlama-7b-Instruct-hf ([INST]...[/INST] format)
    """
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

    model_id = hf_model or "codellama/CodeLlama-7b-hf"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    if tokenizer.pad_token is None:
        tokenizer.pad_token = tokenizer.eos_token

    model = AutoModelForCausalLM.from_pretrained(
        model_id, torch_dtype=torch.bfloat16, device_map="auto"
    )

    is_instruct = "instruct" in model_id.lower()

    def infer(content_a: str, content_b: str, lang: str) -> str:
        prompt = build_prompt(content_a, content_b, lang)
        if is_instruct:
            formatted = f"[INST] {prompt} [/INST]"
        else:
            # Base model: pass prompt as plain completion input
            formatted = prompt

        inputs = tokenizer(formatted, return_tensors="pt").to(model.device)
        with torch.no_grad():
            outputs = model.generate(
                **inputs, max_new_tokens=256,
                pad_token_id=tokenizer.eos_token_id,
            )
        return tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True
        )

    return infer


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


def load_qwen36(hf_model: str | None = None):
    """Qwen3.6-27B — a Qwen3.5-family multimodal (VLM) model, run TEXT-ONLY.

    Served via **transformers**, not vLLM: every vLLM new enough to support the
    `qwen3_5` architecture (>= 0.19) ships CUDA-13 kernels, and this box's driver
    (535 / CUDA 12.2) cannot run them — a vLLM smoke test dies in the flash-attn
    kernel with "CUDA driver version is insufficient". transformers uses torch's
    cu128 kernels, which do work here. Needs `qwen36_venv`
    (transformers >= 5.14, torch 2.8+cu128 with matching torchvision/torchaudio).
    The 27B **dense** model fits one H100 in BF16 (~52 GB); pin a GPU with
    CUDA_VISIBLE_DEVICES.

    Text-only path: the model is loaded with `AutoModelForImageTextToText` but fed
    text-only prompts via the plain tokenizer chat template — no processor/vision
    inputs. Qwen3.6 is a hybrid *thinking* model whose template appends `<think>`
    by default; we pass `enable_thinking=False` so it emits the JSON verdict
    directly (empty think block) instead of a long reasoning trace that would
    blow past max_new_tokens and fail JSON parsing.
    """
    from transformers import AutoModelForImageTextToText, AutoTokenizer
    import torch

    model_id = hf_model or "Qwen/Qwen3.6-27B"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForImageTextToText.from_pretrained(
        model_id, dtype=torch.bfloat16, device_map="auto",
    )
    model.eval()

    def infer(content_a: str, content_b: str, lang: str) -> str:
        prompt = build_prompt(content_a, content_b, lang)
        messages = [{"role": "user", "content": prompt}]
        text = tokenizer.apply_chat_template(
            messages, tokenize=False, add_generation_prompt=True,
            enable_thinking=False,
        )
        inputs = tokenizer(text, return_tensors="pt").to(model.device)
        with torch.no_grad():
            outputs = model.generate(
                **inputs, max_new_tokens=256,
                do_sample=True, temperature=0.7, top_p=0.8, top_k=20,
                pad_token_id=tokenizer.eos_token_id,
            )
        return tokenizer.decode(
            outputs[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True
        )

    return infer


LOADERS = {
    "original":   load_original,
    "qwen36":     load_qwen36,
    "qwen36_gguf": load_qwen36_gguf,
    "gguf":       load_gguf,
    "qwen":       load_qwen,
    "gguf_lora":  load_gguf_lora,
    "qwen3fp8":   load_qwen3fp8,
    "deepseek":   load_deepseek,
    "aqlm":       load_aqlm,
    "higgs":      load_higgs,
    "qtip":       load_qtip,
    "codellama":  load_codellama,
}


# ==============================================================================
# Experiment runner
# ==============================================================================

def _round_csv_path(base: str, round_num: int) -> str:
    """Return the CSV path for a specific round: {base}_round{N}.csv"""
    return f"{base}_round{round_num}.csv"


def _resume_key(pair_id, program_a, variant_a, program_b, variant_b):
    """Unique key for a pair. Uses pair_id when present (explicit --pairs-file
    mode), else the (program_a, variant_a, program_b, variant_b) 4-tuple."""
    return pair_id if pair_id else (program_a, variant_a, program_b, variant_b)


def _load_completed_round(round_csv: str) -> set:
    """Return set of resume keys already done in a round CSV."""
    completed = set()
    p = Path(round_csv)
    if not p.exists():
        return completed
    with open(round_csv, newline="") as f:
        for row in csv.DictReader(f):
            completed.add(_resume_key(row.get("pair_id"), row["program_a"], row["variant_a"],
                                      row["program_b"], row["variant_b"]))
    return completed


_FIELDNAMES = ["program_a", "variant_a", "program_b", "variant_b",
               "ground_truth", "timestamp", "response", "pair_id"]


def _run_round(
    round_num: int,
    rounds: int,
    round_csv: str,
    pairs: list,
    infer,
    lang: str,
    overall_done: int,
    total: int,
) -> int:
    """Write one round of inference results to round_csv, resuming if partial.

    Returns the updated overall_done count.
    """
    completed = _load_completed_round(round_csv)
    pairs_per_round = len(pairs)

    if len(completed) == pairs_per_round:
        print(f"--- Round {round_num}/{rounds}: already complete, skipping ---", flush=True)
        return overall_done

    print(f"--- Round {round_num}/{rounds} "
          f"({len(completed)} done, {pairs_per_round - len(completed)} remaining) ---",
          flush=True)

    Path(round_csv).parent.mkdir(parents=True, exist_ok=True)
    file_exists = Path(round_csv).exists()
    with open(round_csv, "a", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=_FIELDNAMES)
        if not file_exists:
            writer.writeheader()

        for tc_a, tc_b, ground_truth, pair_id, pair_lang in pairs:
            key = _resume_key(pair_id, tc_a.program, tc_a.variant, tc_b.program, tc_b.variant)
            if key in completed:
                continue

            response = infer(tc_a.code, tc_b.code, pair_lang or lang)
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
                "pair_id":      pair_id if pair_id else "",
            })
            f.flush()
            print(f"[{overall_done}/{total}] {ts} | R{round_num} | "
                  f"{tc_a.program}/{tc_a.variant} vs "
                  f"{tc_b.program}/{tc_b.variant} → {ground_truth}", flush=True)

    print(f"Round {round_num} done → {round_csv}", flush=True)
    return overall_done


def run_experiment(
    model_name: str,
    hf_model: str | None = None,
    tests_dir: str = "ocd/tests",
    output_base: str | None = None,
    lang: str = "Java",
    rounds: int = 1,
    pairs_file: str | None = None,
    files_dir: str | None = None,
):
    """Run model_name on all test pairs for the given number of rounds.

    Each round is saved to {output_base}_round{N}.csv. Resumes automatically:
    completed rounds are skipped; partial rounds continue from where they left off.
    """
    if model_name not in LOADERS:
        raise ValueError(f"Unknown model '{model_name}'. Choose from: {list(LOADERS)}")

    if output_base is None:
        if hf_model:
            model_part = hf_model.split("/")[-1].split("::")[0]
            output_base = f"results/{model_part}/results_{_sanitize_hf_name(hf_model)}"
        else:
            output_base = f"results_{model_name}"

    if pairs_file:
        pairs = list(load_pairs_file(pairs_file, files_dir))
        source_desc = f"{len(pairs)} pairs from {pairs_file}"
    else:
        test_cases = load_test_cases(tests_dir)
        pairs = list(generate_pairs(test_cases))
        source_desc = f"{len(test_cases)} files → {len(pairs)} pairs"
    pairs_per_round = len(pairs)

    total_done = sum(len(_load_completed_round(_round_csv_path(output_base, r)))
                     for r in range(1, rounds + 1))
    total = pairs_per_round * rounds
    print(f"Loaded {source_desc} × {rounds} round(s) = {total} total "
          f"({total_done} already done, {total - total_done} remaining)", flush=True)

    if total_done == total:
        print("All rounds already completed. Nothing to do.")
        return

    infer = LOADERS[model_name](hf_model)
    print(f"Model '{model_name}' loaded. Starting inference...", flush=True)

    overall_done = total_done
    for round_num in range(1, rounds + 1):
        overall_done = _run_round(
            round_num, rounds,
            _round_csv_path(output_base, round_num),
            pairs, infer, lang, overall_done, total,
        )

    print(f"All {rounds} round(s) complete. Files: "
          f"{', '.join(_round_csv_path(output_base, r) for r in range(1, rounds + 1))}")


# ==============================================================================
# Entry point
# ==============================================================================
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Run clone-detection inference for a quantized model.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_quantization.py original
  python run_quantization.py gguf bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf
  python run_quantization.py aqlm ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf --rounds 4
  python run_quantization.py qtip relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit --output results/my_run
  python run_quantization.py qwen3fp8 Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8 --rounds 5
  python run_quantization.py codellama
  python run_quantization.py codellama codellama/CodeLlama-7b-Instruct-hf
        """,
    )
    parser.add_argument(
        "model",
        choices=list(LOADERS),
        help="Quantization method / model loader to use.",
    )
    parser.add_argument(
        "hf_model",
        nargs="?",
        default=None,
        help="HuggingFace model ID (default: hardcoded per loader). "
             "GGUF accepts 'repo_id::filename.gguf' to select a specific quant file.",
    )
    parser.add_argument(
        "--tests-dir",
        default="ocd/tests",
        metavar="DIR",
        help="Path to the OCD tests folder (default: ocd/tests). "
             "Ignored when --pairs-file is given.",
    )
    parser.add_argument(
        "--pairs-file",
        default=None,
        metavar="CSV",
        help="Evaluate an explicit pair list (columns: pair_id, label, file1, "
             "file2, problem1, problem2) instead of the n×n OCD product.",
    )
    parser.add_argument(
        "--files-dir",
        default=None,
        metavar="DIR",
        help="Directory holding the source files referenced by --pairs-file "
             "(default: a 'files/' folder next to the pairs CSV).",
    )
    parser.add_argument(
        "--output",
        default=None,
        dest="output_base",
        metavar="BASE",
        help="Base path for result CSV files. Each round is saved as BASE_roundN.csv. "
             "Default: results/<model>/<sanitized_hf_model>.",
    )
    parser.add_argument(
        "--rounds",
        type=int,
        default=1,
        metavar="N",
        help="Number of times to repeat the full experiment (default: 1).",
    )

    args = parser.parse_args()
    run_experiment(args.model, args.hf_model, args.tests_dir, args.output_base,
                   rounds=args.rounds, pairs_file=args.pairs_file,
                   files_dir=args.files_dir)
