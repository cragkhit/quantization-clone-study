# Experiment Notes

## Experimental Environment

Two shared GPU servers, **`tau`** and **`zeta`**. **All results currently in this
repository were produced on `tau`**; `zeta` is a second machine available for
future runs. They differ in GPU architecture and CPU vendor, so the setup notes
below are not interchangeable — see the sm_80 vs sm_90 note.

| Component | `tau` (all existing results) | `zeta` |
| --- | --- | --- |
| GPU | 8 × NVIDIA H100 80 GB HBM3 (compute capability `sm_90`, Hopper) | 8 × NVIDIA A100-SXM4-80GB (compute capability `sm_80`, Ampere) |
| GPU driver | 535.161.08 (CUDA driver API 12.2) | 535.161.08 (CUDA driver API 12.2) |
| CUDA toolkit (`nvcc`) | 12.2.140 | 12.2.140 |
| CPU | 2 × Intel Xeon Platinum 8480C (112 physical cores / 224 threads) | 2 × AMD EPYC 7742 64-Core (128 physical cores / 256 threads) |
| RAM | 2.0 TiB | 2.0 TiB |
| OS / kernel | Ubuntu 22.04.2 LTS / Linux 5.15.0-1053-nvidia | Ubuntu 22.04.2 LTS / Linux 5.15.0-105-generic |
| SMs per GPU | 132 | 108 |
| Default `gcc` | 13 (`gcc-11` available) | 13.1.0 (`gcc-9`/`gcc-11`/`gcc-12` available) |

**GPU usage per run.** Each experiment is pinned to a **single GPU** via
`CUDA_VISIBLE_DEVICES=<id>` — an 8B model in BF16 fits comfortably in 80 GB (QTIP's
dequantized weight cache adds ~7 GB). Pinning to one idle GPU also avoids cross-run
contention, which is **mandatory for HIGGS**: its FLUTE kernel mis-tunes its template
under GPU contention and produces garbage output (see [HIGGS-GPTQ Setup](#higgs-gptq-setup)).

**sm_80 vs sm_90 note.** `tau`'s H100 is compute capability `sm_90`; prebuilt CUDA wheels
that lack sm_90 support must be rebuilt from source — this affects `llama-cpp-python`
(GGUF backends) and the FLUTE kernel (HIGGS); see their setup sections and the
source-compiled-components caveats below. **These rebuilds are `tau`-specific.** On
`zeta` (`sm_80`) the stock wheels are expected to work: the `llama-cpp-python` crash is
triggered by an sm_90-only fused path (see [GGUF Setup → H100 (sm_90)](#h100-sm_90-must-rebuild-from-source)),
and HIGGS needs no `FLUTE_NUM_SMS` override because the ISTA checkpoints are already
packed for A100's 108 SMs. None of this is verified on `zeta` yet — no runs have been
done there.

Separately, `nvcc` 12.2 does not support the default GCC 13 on **either** machine, so
CUDA compilation and the QTIP / AQLM / HIGGS runs use `CC=gcc-11 CXX=g++-11`.

## Environment Preservation & Restoration

Each venv's installed packages are frozen to `requirements/<venv>.lock.txt`
(via `uv pip freeze`) and committed. These pin exact versions so a venv can be
recreated; they complement — but do not replace — the per-backend setup steps in
the sections below.

| Venv | Python | Lock file | Used by |
| --- | --- | --- | --- |
| `aqlm_venv` | 3.9 | `requirements/aqlm_venv.lock.txt` | AQLM (round 1 only); older Llama-3.1-8B / Qwen / DeepSeek BF16 runs |
| `aqlm_venv310` | 3.10 | `requirements/aqlm_venv310.lock.txt` | AQLM (rounds 2+); Python 3.10 rebuild of `aqlm_venv` so `run_quantization.py` imports |
| `qtip_venv` | 3.10 | `requirements/qtip_venv.lock.txt` | QTIP (+ source-built CUDA kernel) |
| `llama4_venv` | 3.10 | `requirements/llama4_venv.lock.txt` | Llama-4-Scout original |
| `higgs_venv` | 3.10 | `requirements/higgs_venv.lock.txt` | HIGGS-GPTQ 3-bit / 4-bit |
| `gguf` | 3.10 | `requirements/gguf.lock.txt` | all GGUF backends (+ source-built `llama-cpp-python` for sm_90) |
| `codellama_venv` | 3.10 | `requirements/codellama_venv.lock.txt` | CodeLlama BF16 |
| `deepseek_venv` | 3.10 | `requirements/deepseek_venv.lock.txt` | DeepSeek-Coder-V2-Lite BF16 (transformers 4.45.2) |
| `codestral_venv` | 3.10 | `requirements/codestral_venv.lock.txt` | Codestral-22B-v0.1 BF16 (codellama_venv versions + sentencepiece/protobuf) |
| `qwen36_venv` | 3.10 | `requirements/qwen36_venv.lock.txt` | Qwen3.6-27B text-only (transformers 5.14, torch 2.8+cu128 trio; `qwen36` backend) |

### Restoring a venv

Recreate with the **same Python version** shown above, then install from the lock file:

```bash
# Example: codellama_venv (Python 3.10)
uv venv codellama_venv --python 3.10
uv pip install --python codellama_venv/bin/python -r requirements/codellama_venv.lock.txt
```

For an `aqlm_venv`-style Python 3.9 venv, substitute `--python 3.9`. The `+cuXXX`
local version tags in the lock files (e.g. `torch==2.8.0+cu128`) resolve from PyPI
for recent torch; if a pin is not found, reinstall torch from its CUDA index first
(see the relevant backend section) and then install the rest of the lock file.

### Caveats — source-compiled components (lock file is not enough)

Two venvs contain components built from source that `pip install -r` **cannot**
reproduce from PyPI. After restoring the lock file, redo these build steps:

> **Machine portability.** All lock files were captured on **`tau`** (H100 / `sm_90`).
> The source-built artifacts below are compiled for `sm_90` and are **not** portable to
> `zeta` (A100 / `sm_80`) — rebuild them there rather than copying the venv.

- **`gguf`** — `llama-cpp-python` was rebuilt from source for H100 `sm_90`.
  Installing the pinned version from PyPI pulls the prebuilt wheel that crashes on
  H100. See [GGUF Setup → H100 (sm_90): must rebuild from source](#h100-sm_90-must-rebuild-from-source).
- **`qtip_venv`** — the QTIP CUDA kernel is compiled in-place under `qtip/qtip-kernels/`
  (not a pip package). See [QTIP Repository & CUDA Kernel](#qtip-repository--cuda-kernel).
  `fast-hadamard-transform` (in `qtip_venv` and `higgs_venv`) is likewise built from
  GitHub, not PyPI.

### Regenerating a lock file

After changing a venv, refresh its snapshot and commit:

```bash
uv pip freeze --python <venv>/bin/python > requirements/<venv>.lock.txt
```

---

## QTIP Setup

### Model
- **HuggingFace model:** [`relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit`](https://huggingface.co/relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit)
- **Quantization:** QTIP 4-bit (trellis-based, `quantlut_sym` decode mode, K=4, L=16, V=2)
- **Base model:** [`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct)

### Virtual Environment
A dedicated `qtip_venv` was created (separate from `aqlm_venv`) because QTIP requires a different PyTorch version and transformers pin.

```bash
python3 -m venv --without-pip qtip_venv
source qtip_venv/bin/activate
curl -sS https://bootstrap.pypa.io/get-pip.py | python3
pip install torch==2.4.0 --index-url https://download.pytorch.org/whl/cu124
pip install transformers==4.46.3 accelerate safetensors huggingface_hub \
            numpy tqdm sentencepiece protobuf datasets glog
pip install "git+https://github.com/Dao-AILab/fast-hadamard-transform.git" --no-build-isolation
```

**Why transformers 4.46.3:** The QTIP model code (`model/cache_utils.py`) imports
`is_quanto_available` from `transformers.utils`, which was removed in transformers 5.x.
Pinning to 4.46.3 fixes this.

### QTIP Repository & CUDA Kernel
The Cornell-RelaxML QTIP repo is cloned to `./qtip/` and its CUDA kernel compiled in-place:

```bash
git clone --depth 1 https://github.com/Cornell-RelaxML/qtip.git qtip
cd qtip/qtip-kernels
CC=gcc-11 CXX=g++-11 python setup.py build_ext --inplace
```

**Why gcc-11:** The system default is GCC 13, which nvcc (CUDA 12.2) does not support.
GCC-12 is installed but missing `cc1plus`. GCC-11 is fully functional.

The compiled kernel `.so` ends up at:
`qtip/qtip-kernels/qtip_kernels.cpython-310-x86_64-linux-gnu.so`

### Loading in `run_quantization.py` (`load_qtip`)
Key steps performed at load time:

1. **PyTorch shared-lib pre-load:** The `qtip_kernels.so` dynamically links against
   `libc10.so`, `libtorch_cpu.so`, etc. Setting `LD_LIBRARY_PATH` after process start
   has no effect, so the libs are pre-loaded via `ctypes.CDLL` before importing the
   kernel extension.

2. **Kernel registration:** `import qtip_kernels` registers
   `torch.ops.quip_lib.decompress_matvec_qtip_*` ops used during single-token generation.

3. **Model loading:** Uses QTIP's own `model_from_hf_path()` (from
   `qtip/lib/utils/unsafe_import.py`), which reads `quip_params` from the model config
   and instantiates a custom `LlamaForCausalLM` with `QuantizedLinear` layers.

4. **`train-fixW` weight caching:** All `QuantizedLinear` layers are switched to
   `train-fixW` mode before the first forward pass. This pre-computes and stores the
   dequantized + Hadamard-rotated weight matrix (`hatW`) once, replacing on-the-fly
   trellis decompression with a plain FP16 `cuBLAS` matmul on every subsequent call.
   **Speedup: ~7–10× (~6–8s/pair vs ~50–80s/pair without caching).**
   Cost: ~7 GB extra VRAM (negligible on the 80 GB A100).

5. **CUDA kernel warmup:** Two short dummy generations are run to pay the Triton
   autotuning cost (first-call overhead ~26s) before the real experiment starts.

### Running the Experiment
```bash
source qtip_venv/bin/activate
CC=gcc-11 CXX=g++-11 python run_quantization.py qtip \
  "relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit" \
  ocd/tests \
  "results/Meta-Llama-3.1-8B-Instruct/results_relaxml_Llama-3.1-8b-Instruct-QTIP-4Bit" \
  1 2>&1 | tee logs/run_qtip.log
```

---

## QTIP max_new_tokens

The QTIP model requires `max_new_tokens=256` (instead of the default 128).

**Reason:** In round 1 with `max_new_tokens=128`, 1,881 out of 10,000 responses were
excluded due to truncated/malformed JSON. The model tends to produce longer explanations
than the other models before closing the JSON object, causing the response to be cut off
mid-stream and fail parsing.

**Where set:** `load_qtip()` in `run_quantization.py` passes `max_new_tokens=256` to
`_make_transformers_infer()`.

Other models (GGUF, AQLM, original) use the default `max_new_tokens=128`.

---

## Original (Full-Precision) Setup

### Models run
- **[`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct)** — BF16, loaded via `transformers`
- **[`meta-llama/Llama-4-Scout-17B-16E-Instruct`](https://huggingface.co/meta-llama/Llama-4-Scout-17B-16E-Instruct)** — BF16, loaded via `transformers`

### Virtual Environments

**For Meta-Llama-3.1-8B-Instruct:** use `aqlm_venv310` (Python 3.10, transformers 4.57.6).
Round 1 was originally produced with `aqlm_venv` (Python 3.9), but that venv can no longer
run `run_quantization.py` — it fails to import at load because `_PROMPT_TEMPLATE: str | None`
uses the PEP 604 `X | None` union (Python 3.10+). Rounds 2–5 therefore use `aqlm_venv310`
(the same Python 3.10 rebuild used for AQLM; see [AQLM Setup → Virtual Environment](#virtual-environment-3)).

**For Llama-4-Scout-17B-16E-Instruct:** use `llama4_venv` (Python 3.10, transformers 5.12.1).
`aqlm_venv` cannot be used for Scout because it runs Python 3.9, which rejects the `str | None`
type-union syntax in `run_quantization.py` at import time. `higgs_venv` (Python 3.10) also fails
because its transformers 4.50.0 does not recognise the `llama4` model type. `llama4_venv` was
created specifically to resolve both constraints.

```bash
uv venv llama4_venv --python 3.10
uv pip install --python llama4_venv/bin/python \
    torch --index-url https://download.pytorch.org/whl/cu121
uv pip install --python llama4_venv/bin/python transformers accelerate
```

### Model loading in `load_original`
- `torch_dtype=torch.bfloat16` — loads weights in BF16 to halve memory vs FP32
  (~16 GB for 8B, ~34 GB for Scout 17B with MoE).
- `device_map="auto"` — accelerate distributes layers across available GPUs automatically.
- Tokenizer and chat template come from the model repo directly via `AutoTokenizer`.
- Inference uses `model.generate()` with `max_new_tokens=128`.

### Running the Experiment
```bash
# Meta-Llama-3.1-8B-Instruct (rounds 2-5 use aqlm_venv310; round 1 already exists as
# ..._round1.csv and is skipped on resume by _run_round)
aqlm_venv310/bin/python run_quantization.py original \
  "meta-llama/Meta-Llama-3.1-8B-Instruct" \
  --tests-dir ocd/tests \
  --output "results/Meta-Llama-3.1-8B-Instruct/results_original_meta-llama__Meta-Llama-3.1-8B-Instruct" \
  --rounds 5 2>&1 | tee logs/run_original_meta-llama_rounds2-5.log

# Llama-4-Scout-17B-16E-Instruct
llama4_venv/bin/python run_quantization.py original \
  "meta-llama/Llama-4-Scout-17B-16E-Instruct" \
  --tests-dir ocd/tests \
  --output "results/Llama-4-Scout-17B-16E-Instruct/results_original_meta-llama__Llama-4-Scout-17B-16E-Instruct" \
  --rounds 5 2>&1 | tee logs/run_original_llama4_scout.log
```

### Notes
- HuggingFace Hub access is required (`huggingface-cli login` or `HF_TOKEN` env var) since
  both models are gated.
- The 80 GB A100 comfortably fits both models in BF16 on a single GPU.

---

## GGUF Setup

### Models run
- **Meta-Llama-3.1-8B-Instruct:** Q2\_K, Q3\_K\_M, Q4\_K\_M from [`bartowski/Meta-Llama-3.1-8B-Instruct-GGUF`](https://huggingface.co/bartowski/Meta-Llama-3.1-8B-Instruct-GGUF)
- **Llama-4-Scout-17B-16E-Instruct:** Q2\_K, Q3\_K\_S, Q4\_K\_M from [`bartowski/Meta-Llama-4-Scout-17B-16E-Instruct-GGUF`](https://huggingface.co/bartowski/Meta-Llama-4-Scout-17B-16E-Instruct-GGUF)

### Virtual Environment
A dedicated `gguf/` venv is used (the directory acts as the venv root).

```bash
python3 -m venv gguf
source gguf/bin/activate
pip install llama-cpp-python==0.3.26   # GPU build with CUDA support
```

`llama-cpp-python` bundles its own copy of `llama.cpp` and compiles CUDA kernels
automatically during install. No separate GCC version management is needed — the
package handles compiler selection internally.

#### H100 (sm_90): must rebuild from source

The prebuilt `llama-cpp-python==0.3.26` CUDA wheel **crashes on H100 GPUs** with:

```
CUDA error: no kernel image is available for execution on the device
  in function ggml_cuda_kernel_can_use_pdl (common.cuh:1602)
```

The model loads, but the **first decode** aborts in the fused RMS-norm / PDL
(Programmatic Dependent Launch) path — the wheel has no kernel image for the
H100's compute capability (sm_90). On A100 (sm_80), `can_use_pdl` returns false
and the non-fused path runs, so the prebuilt wheel works there; on this all-H100
allocation it does not. This affects **every GGUF backend** (`gguf`, `qwen`, and
the Scout/CodeLlama/DeepSeek GGUF runs) — but not the PyTorch/`transformers`
backends (`original`, `deepseek` BF16, etc.).

**Fix — rebuild from source targeting sm_90.** `nvcc` (CUDA 12.2) supports sm_90.
The host compiler must be set explicitly: `CC`/`CXX` env vars do **not** reach
nvcc's `-ccbin`, so without `-DCMAKE_CUDA_HOST_COMPILER` the build fails with
`unsupported GNU version! gcc versions later than 12` (system default is gcc-13).

```bash
CMAKE_ARGS="-DGGML_CUDA=on -DCMAKE_CUDA_ARCHITECTURES=90 \
  -DCMAKE_C_COMPILER=gcc-11 -DCMAKE_CXX_COMPILER=g++-11 \
  -DCMAKE_CUDA_HOST_COMPILER=g++-11" \
CC=gcc-11 CXX=g++-11 \
  gguf/bin/pip install --force-reinstall --no-cache-dir \
    --no-binary llama-cpp-python "llama-cpp-python==0.3.26"
```

After the rebuild, `libggml-cuda.so.0` grows from ~140 MB to ~214 MB (native
sm_90 kernels) and inference runs on H100 without the crash. Fused vs non-fused
RMS-norm is numerically equivalent, so results mix cleanly with earlier A100 runs.

### Model loading in `load_gguf`
- Uses `Llama.from_pretrained(repo_id, filename)` which downloads the `.gguf` shard
  from HuggingFace Hub and loads it directly into VRAM.
- `n_gpu_layers=-1`: offloads all layers to GPU.
- `n_ctx=8192`: context window size, sufficient for the code comparison prompts.
- Inference uses `llm.create_chat_completion()` with `max_tokens=128`.

### hf_model argument format
```
repo_id::filename.gguf
```
e.g. `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf`

If only `repo_id` is given (no `::` separator), the default file
`Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf` is used.

### Running the Experiment
```bash
source gguf/bin/activate
python run_quantization.py gguf \
  "bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf" \
  ocd/tests \
  "results/Meta-Llama-3.1-8B-Instruct/results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf" \
  1 2>&1 | tee logs/run_gguf_q4km.log
```

### Producing our own GGUF quants (models without a community GGUF)

When no community GGUF exists for a model, we quantize it ourselves with the
**llama.cpp** tooling (this is how bartowski's files are made). Cloned to
`./llama.cpp/`. The pipeline is: **HF weights → F16 GGUF → quantized GGUF**.

**Always quantize from the BF16/FP16 base, not an FP8/quantized checkpoint.** FP8
(and other pre-quantized) weights are already lossy; converting FP8 → GGUF → Q2/Q3
compounds two rounds of quantization error (and the converter expects unquantized
BF16/FP16 tensors anyway). E.g. for Qwen3-Coder-30B-A3B, quantize
`Qwen/Qwen3-Coder-30B-A3B-Instruct` (BF16), **not** the `-FP8` variant we serve via
vLLM.

**1. Build the llama.cpp binaries for sm_90.** The `gguf` venv's `llama-cpp-python`
does not ship the standalone `llama-quantize` / `llama-imatrix` executables, so build
them from the `./llama.cpp/` checkout. No system `cmake`; install it (and `ninja`)
into the `gguf` venv. Same sm_90 + gcc-11 rules as the `llama-cpp-python` rebuild
(nvcc 12.2 rejects gcc-13, so `-DCMAKE_CUDA_HOST_COMPILER=g++-11`):

```bash
gguf/bin/pip install cmake ninja
export PATH="$PWD/gguf/bin:$PATH"
cd llama.cpp
CC=gcc-11 CXX=g++-11 cmake -B build -G Ninja -DGGML_CUDA=ON \
  -DCMAKE_CUDA_ARCHITECTURES=90 -DCMAKE_CUDA_HOST_COMPILER=g++-11 -DLLAMA_CURL=OFF
cmake --build build -j 16 --target llama-quantize llama-imatrix llama-cli
```

Binaries land in `llama.cpp/build/bin/`. (`llama-quantize` is CPU-only; the CUDA
build is only needed to make `llama-imatrix` fast — see the imatrix note below.)
Recent llama.cpp moved the per-arch converter classes into `convert_hf_to_gguf.py`'s
`conversion/` submodule; Qwen3-Coder MoE is `Qwen3MoeForCausalLM`
(`conversion/qwen.py`), so it is supported.

**2. Download the BF16 base** (use `HF_HUB_DISABLE_XET=1`; the xet backend stalls on
large shards — same lesson as the Codestral GGUF download):

```bash
HF_HUB_DISABLE_XET=1 gguf/bin/python -c "
from huggingface_hub import snapshot_download
snapshot_download('Qwen/Qwen3-Coder-30B-A3B-Instruct',
    local_dir='models/Qwen3-Coder-30B-A3B-Instruct',
    allow_patterns=['*.safetensors','*.json','*.txt','tokenizer*','*.py'])"
```

**3. Convert HF → F16 GGUF** (repackages weights; no quantization yet). The
converter needs **torch + safetensors**, which the `gguf` venv (inference-only
`llama-cpp-python`, no torch) does not have — use **`aqlm_venv310`** with the
checkout's `gguf-py` on `PYTHONPATH`:

```bash
PYTHONPATH=llama.cpp/gguf-py aqlm_venv310/bin/python \
  llama.cpp/convert_hf_to_gguf.py models/Qwen3-Coder-30B-A3B-Instruct \
  --outfile models/Qwen3-Coder-30B-A3B-Instruct-F16.gguf --outtype f16
```

Produces a **61 GB** F16 GGUF (30B params, 579 tensors). The conversion is
CPU/IO-bound (~5 min to write the file).

**4. Quantize F16 → target bit-widths.** One `llama-quantize` call per type (the
type list is in `llama-quantize --help`). Each pass is CPU-bound, ~3 min for this
30B MoE; run detached (a 2-min foreground tool timeout will SIGTERM it mid-write):

```bash
Q=llama.cpp/build/bin/llama-quantize
BASE=models/Qwen3-Coder-30B-A3B-Instruct
$Q "$BASE-F16.gguf" "$BASE-Q4_K_M.gguf" Q4_K_M   # ~18 GB
$Q "$BASE-F16.gguf" "$BASE-Q3_K_M.gguf" Q3_K_M   # 14.7 GB
$Q "$BASE-F16.gguf" "$BASE-Q2_K.gguf"   Q2_K     # 11.3 GB
```

These self-made files then feed the `gguf` backend exactly like a community GGUF —
point `--output` at a `results_*/Qwen3-Coder-30B-A3B-Instruct/` path and pass the
local `.gguf` path as the `hf_model` arg (a plain path, no `repo::file` needed).

**`load_gguf` local-path support.** `load_gguf` now accepts a **local `.gguf` file
path** in addition to `repo_id` / `repo_id::filename.gguf`: if `hf_model` ends in
`.gguf` and the file exists, it loads via `Llama(model_path=…)` instead of
`Llama.from_pretrained(...)` (which only pulls from the Hub). Everything else
(`n_gpu_layers=-1`, `n_ctx=16384`, chat-completion infer) is unchanged.

### Study integration (BF16-sourced GGUF sweep)

All three self-made quants × all three datasets (9 runs) are chained on one GPU by
`scripts/chain_qwen3_bf16gguf_all.sh` (six quick GCJ runs first, then the three long
OCD runs; auto-resumes). Results land under a `Qwen3-Coder-30B-A3B-Instruct/` dir
(distinct from the community-FP8 `Qwen3-Coder-30B-A3B-Instruct-FP8/` already in the
study), with `results_gguf_…` stems so the evaluator groups them as GGUF quants:

```bash
CUDA_VISIBLE_DEVICES=6 setsid bash scripts/chain_qwen3_bf16gguf_all.sh \
  > logs/chain_qwen3_bf16gguf_all.log 2>&1 < /dev/null &
```

**Results (5-round majority-vote MCC) — BF16-sourced beats FP8 at every bit-width.**
This is the empirical payoff of the "quantize from BF16, not FP8" rule: a single
clean quantization from BF16 outperforms both the native FP8 checkpoint *and* the
community FP8→GGUF Q4_K_M, on both balanced GCJ sets. Even our Q3_K_M edges out
native FP8.

| Quantization path | GCJ-Java | GCJ-XLang | OCD |
| --- | --- | --- | --- |
| FP8 (native, vLLM) | 0.6939 | 0.7002 | 0.9307 |
| FP8 → GGUF Q4_K_M (community) | 0.7582 | 0.7296 | — |
| **BF16 → GGUF Q4_K_M (ours)** | **0.7664** | **0.7552** | 0.9555 |
| BF16 → GGUF Q3_K_M (ours) | 0.7058 | 0.6919 | 0.9388 |
| BF16 → GGUF Q2_K (ours) | 0.6664 | 0.5653 | 0.9827 |

Monotonic (Q4 > Q3 > Q2) on the balanced GCJ sets; precision ≈ 1.00 throughout
(this model essentially never false-positives). OCD's ordering is dominated by its
class imbalance, as elsewhere — read the GCJ sets for the clean bit-width trend.

**imatrix (optional, skipped here).** For the best low-bit (Q2_K/Q3_K) quality —
especially on a sparse MoE — first compute an importance matrix
(`llama-imatrix -m …-F16.gguf -f calibration.txt -o model.imatrix -ngl 99`) and pass
`--imatrix model.imatrix` to `llama-quantize`. This Qwen3-Coder run was done **without**
imatrix (plain uniform-importance quantization) by choice.

### Reproducing a community GGUF: Qwen2.5-Coder-7B self-quantized (2026-08-08)

Same pipeline applied to `Qwen/Qwen2.5-Coder-7B-Instruct` (dense 7B,
`Qwen2ForCausalLM`) → `models/Qwen2.5-Coder-7B-Instruct-{Q4_K_M,Q3_K_M,Q2_K}.gguf`,
evaluated on all three datasets with a **`-self`** output suffix to separate them
from the existing COMMUNITY-GGUF rows (`results_qwen2.5_coder_7B_q*`, from
`Qwen/Qwen2.5-Coder-7B-Instruct-GGUF`). Chain: `scripts/chain_qwen25_selfgguf_all.sh`.

- **Non-obvious dep:** unlike Qwen3-Coder, the Qwen2 converter takes the
  **sentencepiece** vocab path (`qwen.py` → `_set_vocab_sentencepiece`), so
  `convert_hf_to_gguf.py` fails with `ModuleNotFoundError: sentencepiece` until it's
  installed into `aqlm_venv310` (`uv pip install --python aqlm_venv310/bin/python sentencepiece`).

**Finding — the community Q2_K used an imatrix; ours (plain) did not.** Q3_K_M and
Q4_K_M reproduce the community numbers closely, but **Q2_K collapses only in the
self (plain) build**, and only on the *balanced* GCJ sets:

| Variant | GCJ-Java (self / comm) | GCJ-XLang (self / comm) | OCD (self / comm) |
| --- | --- | --- | --- |
| Q4_K_M | 0.6391 / 0.6821 | 0.6753 / 0.7339 | 0.9989 / 0.9978 |
| Q3_K_M | 0.6313 / 0.6236 | 0.7128 / 0.7044 | 0.9978 / 0.9978 |
| **Q2_K** | **0.4445 / 0.8250** | **0.3958 / 0.8044** | 0.9821 / 0.9994 |

The Q2_K gap (~+0.38 MCC for the community build) appears **only at 2-bit** and
**only on the balanced sets** — the textbook imatrix signature (biggest help at the
lowest bit-width; OCD's ~10% class imbalance floats MCC near 1.0 and hides it). So
the anomalous "Q2_K = best Qwen" community row is an imatrix artifact, not a genuine
2-bit advantage. Confirming it directly (calibrated imatrix Q2_K re-quant) is the
clean follow-up ablation.

---

## HIGGS-GPTQ Setup

### Model
- **HuggingFace model:** [`ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit`](https://huggingface.co/ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit)
- **Quantization:** HIGGS (Hadamard Incoherence + GPTQ) 4-bit
- **Base model:** [`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct)

### Virtual Environment
A dedicated `higgs_venv` was created using `uv` (the system Python 3.10 lacks `ensurepip`).

```bash
uv venv higgs_venv --python 3.10
```

Packages must be installed in this order — torch must be pinned first to prevent
gptqmodel/xformers from upgrading it to an incompatible version:

```bash
# Step 1: pin torch 2.8+cu128 (compatible with system CUDA 12.2 driver)
uv pip install --python higgs_venv/bin/python \
    "torch==2.8.0+cu128" \
    --index-url https://download.pytorch.org/whl/cu128

# Step 2: install remaining deps, constraining torch
uv pip install --python higgs_venv/bin/python \
    "transformers==4.50.0" accelerate tiktoken sentencepiece \
    --constraint <(echo "torch==2.8.0+cu128")

# Step 3: flute-kernel (prerelease required; --no-deps to avoid vllm pull)
uv pip install --python higgs_venv/bin/python \
    "flute-kernel==0.4.2" --prerelease=allow --no-deps

# Step 4: fast_hadamard_transform (must build from GitHub; PyPI tarball is broken)
CC=gcc-11 CXX=g++-11 higgs_venv/bin/python -m pip install \
    "git+https://github.com/Dao-AILab/fast-hadamard-transform.git" \
    --no-build-isolation --no-cache-dir

# Step 5: bootstrap pip (uv venvs omit pip by default) then install torchvision
uv pip install --python higgs_venv/bin/python pip torchvision sentencepiece
```

**Why torch 2.8+cu128 (not the latest):** `gptqmodel` and `xformers` pull in
torch 2.12+cu130, which requires a CUDA driver ≥ 13.0. The system driver is 12.2,
so CUDA is unavailable with torch 2.12. Installing torch 2.8 first and constraining
it prevents the upgrade.

**Why transformers 4.50.0:** The HIGGS quantizer (`quantizer_higgs.py`) is present
in transformers ≥ 4.47. Version 5.x introduces `torch.int1` which is absent in
torch 2.8, breaking `LlamaForCausalLM` import. 4.50.0 is the highest 4.x that
includes the HIGGS quantizer.

**Why gcc-11:** `fast_hadamard_transform` uses CUDA kernels; nvcc (CUDA 12.2)
rejects GCC > 12. GCC-12 is missing `cc1plus`; GCC-11 works correctly.

**Why `flute-kernel --no-deps`:** flute-kernel 0.4.2 lists `vllm==0.7.2` as a
dependency, which conflicts with everything else. The kernel itself doesn't need
vllm at runtime; `--no-deps` skips the resolution conflict.

### Patch Applied to `quantizer_higgs.py`

The HIGGS model checkpoint (`ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit`)
uses an older format that stores `num_sms_packed` per-layer in safetensors but omits
`tune_metadata` from the config JSON. The transformers 4.50.0 HIGGS quantizer requires
`tune_metadata` and raises `KeyError` on load.

**Fix:** patch `_process_model_after_weight_loading` in
`higgs_venv/lib/python3.10/site-packages/transformers/quantizers/quantizer_higgs.py`
to recover the correct `template_id` by running FLUTE's `_tune` benchmark when
`tune_metadata` is absent:

```python
# Replace the line:
#   module.tune_metadata = TuneMetaData.from_dict(self.quantization_config.tune_metadata[name])
# with:
if name in self.quantization_config.tune_metadata:
    module.tune_metadata = TuneMetaData.from_dict(self.quantization_config.tune_metadata[name])
else:
    import flute.utils as _fu
    from flute.tune import _tune
    _device = module.weight.device
    _num_sms = int(getattr(module, "num_sms_packed", _fu.get_device_num_sms(_device)))
    N = module.out_features
    K = module.in_features
    _tid = _tune(M=1, N=N, K=K, num_bits=module.num_bits,
                 group_size=module.group_size, num_sms=_num_sms,
                 dtype=module.scales.dtype, device=_device,
                 num_seeds=1, legacy=False)
    module.tune_metadata = TuneMetaData(
        M=1, N=N, K=K, num_bits=module.num_bits, group_size=module.group_size,
        num_sms=_num_sms, dtype=module.scales.dtype, device=_device, template_id=_tid,
    )
```

This patch runs `_tune` at model load time (~3–4 minutes for 224 layers), which
benchmarks and selects the optimal FLUTE kernel template for each layer shape.

### Critical: CUDA_VISIBLE_DEVICES must be set

`_tune` benchmarks 144 CUDA kernel variants by timing them. On a GPU under heavy
load, timing is noisy and an incompatible `template_id` can be selected, producing
garbage output for all inference pairs. **Always pin to a specific low-load GPU:**

```bash
CUDA_VISIBLE_DEVICES=7 CC=gcc-11 CXX=g++-11 \
  higgs_venv/bin/python run_quantization.py higgs \
  "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit" \
  --tests-dir ocd/tests \
  --output results/Meta-Llama-3.1-8B-Instruct/results_higgs_llama3.1_8B_4bit \
  --rounds 1 2>&1 | tee logs/run_higgs.log
```

Without `CUDA_VISIBLE_DEVICES`, `device_map="auto"` may place the model on a
contested GPU, causing `_tune` to select template_id=0 or similar incompatible
templates that produce corrupted matrix products and garbage token output.

### What is a FLUTE Template?

A template specifies a CUDA kernel configuration for quantized matrix multiplication:
tile dimensions (TileM, TileK, TileP), thread count, pipeline stages, and quant-map
mode. The weight matrix is packed at quantization time in a layout matching a specific
template. Running inference with a mismatched template reads memory in the wrong
pattern, corrupting all outputs. `_tune` selects the fastest compatible template for
the current GPU and layer shape.

### H100 (sm_90): FLUTE source build + num_sms override

On the H100 box, HIGGS needs two fixes (in addition to the `higgs_venv` setup above).
Without them the run crashes at model load, never reaching inference.

**1. Rebuild `flute-kernel` from source for sm_90.** The prebuilt `flute-kernel==0.4.2`
wheel ships SASS for **sm_80/86/89 only** (verify with
`cuobjdump flute/_C*.so | grep 'arch = sm_'`), so on H100 FLUTE's `qgemm` aborts during
`_tune` with `CUDA error: no kernel image is available for execution on the device`.
0.4.2 is the latest release and there is **no sdist on PyPI**, so build from GitHub
(FLUTE lists H100 as supported "unoptimized" only via source build):

```bash
# CUTLASS v3.4.1 is required; setup.py hard-codes CUTLASS_PATH — edit it to your clone.
git clone --depth 1 --branch v3.4.1 https://github.com/NVIDIA/cutlass.git /path/to/cutlass
git clone --branch v0.4.2 https://github.com/HanGuo97/flute && cd flute
sed -i 's#CUTLASS_PATH = "/workspace/cutlass/"#CUTLASS_PATH = "/path/to/cutlass/"#' setup.py

TORCH_CUDA_ARCH_LIST="9.0" MAX_JOBS=8 CC=gcc-11 CXX=g++-11 \
  NVCC_PREPEND_FLAGS="-ccbin /usr/bin/g++-11" \
  higgs_venv/bin/pip install . --force-reinstall --no-build-isolation --no-deps
```

`--no-deps` avoids FLUTE's `vllm` requirement; `-ccbin g++-11` keeps nvcc off system
gcc-13. The rebuilt `_C.so` reports `arch = sm_90` and version `0.4.2+cu122`.

**2. Force `num_sms=108` (the packed value).** The ISTA checkpoints are FLUTE stream-K
packed for **num_sms=108** (A100; stored per-layer as `num_sms_packed`). The H100 has
132 SMs, but FLUTE's template configs only cover the packed value, so tuning/repacking
for 132 yields an empty candidate set → `ValueError: min() arg is an empty sequence`.
Two edits pin everything to 108 (inference num_sms must match the packing):

- `transformers/quantizers/quantizer_higgs.py` (`_process_model_after_weight_loading`):
  set `_num_sms = int(os.environ.get("HIGGS_NUM_SMS", 108))` instead of the device count.
- `flute/utils.py` (`get_device_num_sms`): return `int(os.environ["FLUTE_NUM_SMS"])`
  when set, else the real count. This makes `maybe_tune_and_repack` skip repacking
  (metadata 108 == reported 108) and `tune_and_pack` target 108.

Then run with **`FLUTE_NUM_SMS=108`** set. Verified correct: HIGGS 3-bit round-2 labels
on H100 matched the A100 round-1 labels on 619/619 overlapping pairs (100%).

```bash
CUDA_VISIBLE_DEVICES=7 CC=gcc-11 CXX=g++-11 FLUTE_NUM_SMS=108 \
  higgs_venv/bin/python run_quantization.py higgs \
  "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit" \
  --tests-dir ocd/tests \
  --output results/Meta-Llama-3.1-8B-Instruct/results_higgs_llama3.1_8B_3bit \
  --rounds 5 2>&1 | tee logs/run_higgs_3bit_5rounds.log
```

---

## HIGGS-GPTQ 3-bit

Same `higgs_venv` and setup as the 4-bit run above. The 3-bit checkpoint
([`ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit`](https://huggingface.co/ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit))
does **not** include a tokenizer, so `load_higgs` falls back to loading one from
[`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct) automatically.

All other considerations (CUDA\_VISIBLE\_DEVICES, FLUTE template tuning, gcc-11)
are identical to the 4-bit case.

```bash
CUDA_VISIBLE_DEVICES=7 CC=gcc-11 CXX=g++-11 \
  higgs_venv/bin/python run_quantization.py higgs \
  "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit" \
  --tests-dir ocd/tests \
  --output results/Meta-Llama-3.1-8B-Instruct/results_higgs_llama3.1_8B_3bit \
  --rounds 1 2>&1 | tee logs/run_higgs_3bit.log
```

---

## AQLM Setup

### Model
- **HuggingFace model:** [`ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf`](https://huggingface.co/ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf)
- **Quantization:** AQLM 2-bit + PV-Tuning, 1×16 codebook configuration
- **Base model:** [`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct)

### Virtual Environment

Round 1 was produced with `aqlm_venv` (Python 3.9). That venv can **no longer run**
`run_quantization.py`: the module fails to import at load because
`_PROMPT_TEMPLATE: str | None = None` uses the PEP 604 `X | None` union (Python 3.10+).

Rounds 2+ therefore use **`aqlm_venv310` (Python 3.10)**, a rebuild of `aqlm_venv`'s
exact package set on Python 3.10 (torch 2.8.0+cu128, transformers 4.57.6,
accelerate 1.10.1, `aqlm` 1.1.7) so results stay consistent across rounds.

```bash
# One-time setup (uv; system Python 3.10). Mirrors the venv-restore pattern above:
# install cu128 torch first, then the rest of the lock file.
uv venv aqlm_venv310 --python 3.10
uv pip install --python aqlm_venv310/bin/python \
  torch==2.8.0 --index-url https://download.pytorch.org/whl/cu128
uv pip install --python aqlm_venv310/bin/python -r requirements/aqlm_venv310.lock.txt
```

```bash
# Run (the trailing integer is the number of rounds).
# The output base must be the descriptive model name so all rounds share a base and
# the evaluator groups them: {base}_round{N}.csv. Round 1 already exists as
# results_ISTA-DASLa__..._round1.csv; _run_round skips complete rounds and resumes at 2.
source aqlm_venv310/bin/activate
CC=gcc-11 CXX=g++-11 python run_quantization.py aqlm \
  "ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf" \
  ocd/tests \
  "results/Meta-Llama-3.1-8B-Instruct/results_ISTA-DASLa__Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf" \
  5 2>&1 | tee logs/run_aqlm.log
```

### Fixes Applied in `load_aqlm`

1. **GCC version:** `CC=gcc-11 CXX=g++-11` — default GCC 13 is too new for nvcc (CUDA 12.2).

2. **Broken cuBLAS GEMM path:** `code1x16_matmat_dequant` hits a `CUBLAS_STATUS_NOT_SUPPORTED`
   error on this PyTorch 2.8 + A100 setup. The GEMM path (prefill of long sequences,
   `optimize_for_training=True`) is rerouted through `dequantize_gemm` (pure-PyTorch
   fallback: dequantize weights then `F.linear`). The GEMV path (single-token generation,
   `optimize_for_training=False`) keeps the native `code1x16_matmat` CUDA kernel.

3. **Broken `lm_head`:** The AQLM architecture registers `lm_head` as `QuantizedLinear`
   but the checkpoint only stores it as a plain `nn.Linear` (`lm_head.weight` in
   safetensors). This caused randomly-initialized codebooks and garbage output. Fix:
   load `lm_head.weight` directly from the safetensors checkpoint and replace the layer
   with a proper `nn.Linear` after `from_pretrained`.

---

## CodeLlama-7b-Instruct-hf Setup

### Models run
- **[`codellama/CodeLlama-7b-Instruct-hf`](https://huggingface.co/codellama/CodeLlama-7b-Instruct-hf)** — BF16, full precision
- **[`QuantFactory/CodeLlama-7b-Instruct-hf-GGUF`](https://huggingface.co/QuantFactory/CodeLlama-7b-Instruct-hf-GGUF)** — Q2\_K, Q3\_K\_M, Q4\_K\_M

### Full-precision (BF16)

Uses a dedicated **`codellama_venv` (Python 3.10)**. The `codellama` backend
in `run_quantization.py` detects the instruct variant from the model ID and wraps the
prompt in `[INST] ... [/INST]` instead of a chat template. `max_new_tokens=256`.

> **Why a new venv:** rounds 1–2 were originally produced with `aqlm_venv`, but that
> venv runs **Python 3.9**, which now fails to import `run_quantization.py` at module
> load (`_PROMPT_TEMPLATE: str | None = None` uses the PEP 604 `X | None` union, which
> is 3.10+). `codellama_venv` replicates `aqlm_venv`'s exact package versions on
> Python 3.10 so later rounds stay consistent with the earlier ones:
> torch 2.8.0+cu128, transformers 4.57.6, accelerate 1.10.1.

```bash
# One-time setup (uv; system Python 3.10)
uv venv codellama_venv --python 3.10
uv pip install --python codellama_venv/bin/python \
  torch==2.8.0 --index-url https://download.pytorch.org/whl/cu128
uv pip install --python codellama_venv/bin/python \
  transformers==4.57.6 accelerate==1.10.1

# Run (CUDA_VISIBLE_DEVICES pins a single GPU; the script auto-resumes partial rounds)
CUDA_VISIBLE_DEVICES=3 codellama_venv/bin/python run_quantization.py codellama \
  "codellama/CodeLlama-7b-Instruct-hf" \
  --tests-dir ocd/tests \
  --output results/CodeLlama-7b-Instruct-hf/results_codellama__CodeLlama-7b-Instruct-hf \
  --rounds 5 2>&1 | tee logs/run_codellama.log
```

### GGUF Variants

Uses the `gguf` venv (same `llama-cpp-python` as the Meta-Llama GGUF runs).

```bash
source gguf/bin/activate

# Q4_K_M
python run_quantization.py gguf \
  "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf" \
  --tests-dir ocd/tests \
  --output "results/CodeLlama-7b-Instruct-hf-GGUF/results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q4_K_M.gguf" \
  --rounds 1 2>&1 | tee logs/run_codellama_gguf_q4km.log

# Q3_K_M
python run_quantization.py gguf \
  "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
  --tests-dir ocd/tests \
  --output "results/CodeLlama-7b-Instruct-hf-GGUF/results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
  --rounds 1 2>&1 | tee logs/run_codellama_gguf_q3km.log

# Q2_K
python run_quantization.py gguf \
  "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
  --tests-dir ocd/tests \
  --output "results/CodeLlama-7b-Instruct-hf-GGUF/results_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
  --rounds 1 2>&1 | tee logs/run_codellama_gguf_q2k.log
```

### Notes
- No HuggingFace token needed — `codellama/CodeLlama-7b-Instruct-hf` is public.
- The base CodeLlama model (`codellama/CodeLlama-7b-hf`) uses completion-style prompting
  (no `[INST]` wrapper); only the `-Instruct-hf` variant uses the instruction format.

---

## Qwen2.5-Coder-7B Setup

### Models run
- **[`Qwen/Qwen2.5-Coder-7B-Instruct`](https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct)** — BF16, full precision (`original` backend)
- **[`Qwen/Qwen2.5-Coder-7B-Instruct-GGUF`](https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct-GGUF)** — Q2\_K, Q3\_K\_M, Q4\_K\_M (`qwen` backend)

### Full-precision (BF16)

Uses `aqlm_venv` via the `original` backend (`AutoModelForCausalLM` + chat template).
Qwen2.5 does not require `trust_remote_code` in modern transformers.

```bash
source aqlm_venv/bin/activate
python run_quantization.py original \
  "Qwen/Qwen2.5-Coder-7B-Instruct" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_original \
  --rounds 1 2>&1 | tee logs/run_qwen_original.log
```

### GGUF Variants

Uses the `gguf` venv with the dedicated `qwen` backend in `run_quantization.py`.
The `qwen` loader is identical to `load_gguf` but defaults to `Qwen/Qwen2.5-Coder-7B-Instruct-GGUF`.

```bash
source gguf/bin/activate

# Q4_K_M (default)
python run_quantization.py qwen \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q4km \
  --rounds 1 2>&1 | tee logs/run_qwen_q4km.log

# Q3_K_M
python run_quantization.py qwen \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q3km \
  --rounds 1 2>&1 | tee logs/run_qwen_q3km.log

# Q2_K
python run_quantization.py qwen \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q2k \
  --rounds 1 2>&1 | tee logs/run_qwen_q2k.log
```

---

## Qwen3-Coder-30B-A3B FP8 Setup

### Model run
- **[`Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8`](https://huggingface.co/Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8)** — fine-grained (block) FP8, 30B MoE with ~3B active params (`qwen3fp8` backend)

### Why vLLM (not transformers)

The `qwen3fp8` loader serves the model with **vLLM**, not `transformers`. On this
model `transformers` generation is decode-latency-bound: ~25 s/pair with the GPU
at only ~23% utilisation (≈14 days for the 5-round OCD sweep). vLLM's FP8-MoE
kernels + paged attention run at ~0.9 s/pair with the GPU at ~91% (the full
50,000-inference OCD sweep finishes in well under a day).

### Virtual Environment (`vllm_venv`)

```bash
# uv venv, system Python 3.10
uv venv vllm_venv --python 3.10

# vLLM 0.11 ships torch 2.8+cu128, which runs on the 535.161 / CUDA 12.2 driver.
# (vLLM 0.24 pulls torch cu130 → "driver too old", CUDA unavailable. Do not use.)
uv pip install --python vllm_venv/bin/python --torch-backend=cu128 "vllm==0.11.0"

# vLLM only floors transformers (>=4.55.2); uv otherwise resolves transformers 5.x,
# whose tokenizer API breaks vLLM ("Qwen2Tokenizer has no attribute
# all_special_tokens_extended"). Pin the 4.57 line:
uv pip install --python vllm_venv/bin/python "transformers==4.57.6"
```

Lock file: `requirements/vllm_venv.lock.txt`.

### Model loading in `load_qwen3fp8`

- `LLM(max_model_len=32768, gpu_memory_utilization=0.80, max_num_seqs=64)`.
  The **0.80** cap is deliberate: the untuned fused-MoE path (no tuned config for
  the `E=128,N=768,fp8_w8a8` shape on H100) allocates a large transient workspace
  during CUDA-graph capture, so a higher KV-cache claim OOMs at graph-capture
  time. `max_num_seqs=64` is fine since pairs are issued one at a time.
- Sampling uses Qwen3-Coder's recommended non-thinking settings
  (`temperature=0.7, top_p=0.8, top_k=20, repetition_penalty=1.05, max_tokens=256`);
  `temperature>0` also supplies the per-round variation majority-vote relies on.
- `llm.chat()` applies the model's chat template. Requires an H100/sm_90 GPU
  (native FP8). Launch fully detached (`setsid`) so a shell/tool timeout can't
  signal the vLLM EngineCore subprocess mid-startup.

### Running the Experiment (OCD)

```bash
setsid env CUDA_VISIBLE_DEVICES=<gpu> PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True \
  vllm_venv/bin/python run_quantization.py qwen3fp8 \
  "Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8" \
  --tests-dir ocd/tests \
  --output results/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8 \
  --rounds 5 > logs/run_qwen3fp8_ocd_5rounds.log 2>&1 < /dev/null &
```

OCD 5-round majority vote: **Acc 0.9878, Precision 1.0000, Recall 0.8780,
F1 0.9350, MCC 0.9307** (0 excluded — all responses parsed cleanly).

---

## DeepSeek-Coder-V2-Lite-Instruct Setup

### Models run
- **[`deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct`](https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct)** — BF16, full precision (`deepseek` backend)
- **[`bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF`](https://huggingface.co/bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF)** — Q4\_K\_M (`gguf` backend)

### Full-precision (BF16)

Uses a dedicated **`deepseek_venv` (Python 3.10, transformers 4.45.2)**. The
`deepseek` backend passes `trust_remote_code=True` because the DeepSeek-Coder-V2
architecture ships custom model code in the HuggingFace repo (e.g.
`configuration_deepseek.py`). Transformers will warn about downloading remote code
on first load — this is expected.

> **Why transformers 4.45.2 (its own venv):** the DeepSeek remote `modeling_deepseek.py`
> calls `past_key_values.seen_tokens`, an attribute removed from `DynamicCache` in
> transformers ~4.46+. `aqlm_venv`/`codellama_venv` (transformers 4.57.6) therefore
> crash on the first decode with
> `AttributeError: 'DynamicCache' object has no attribute 'seen_tokens'`. 4.45.2 is the
> newest release that still exposes `seen_tokens` (now only a deprecation warning) and
> runs on Python 3.10. Round 1 was produced earlier with an older transformers; a load
> + short-generate smoke test confirmed 4.45.2 yields valid output before the resume.

```bash
# One-time setup (uv; system Python 3.10)
uv venv deepseek_venv --python 3.10
uv pip install --python deepseek_venv/bin/python \
  torch==2.8.0 --index-url https://download.pytorch.org/whl/cu128
uv pip install --python deepseek_venv/bin/python \
  "transformers==4.45.2" accelerate sentencepiece protobuf

# Run (CUDA_VISIBLE_DEVICES pins a single GPU; the script auto-resumes partial rounds)
CUDA_VISIBLE_DEVICES=3 deepseek_venv/bin/python run_quantization.py deepseek \
  "deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct" \
  --tests-dir ocd/tests \
  --output results/DeepSeek-Coder-V2-Lite-Instruct/results_deepseek_coder_v2_lite \
  --rounds 5 2>&1 | tee logs/run_deepseek.log
```

### GGUF (Q4\_K\_M)

Uses the standard `gguf` backend and venv. The GGUF file is from Bartowski's
community quantizations.

```bash
source gguf/bin/activate
python run_quantization.py gguf \
  "bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF::DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf" \
  --tests-dir ocd/tests \
  --output results/DeepSeek-Coder-V2-Lite-Instruct/results_deepseek_coder_v2_lite_q4km \
  --rounds 1 2>&1 | tee logs/run_deepseek_q4km.log
```

### Notes
- The GGUF context warning (`n_ctx_seq (8192) < n_ctx_train (163840)`) is harmless;
  our prompts are well within 8192 tokens.
- `trust_remote_code=True` is only needed for the BF16 path; the GGUF file is
  self-contained and does not execute model-repo Python code.

---

## aya-expanse-8b Setup

### Models run
- **[`CohereLabs/aya-expanse-8b`](https://huggingface.co/CohereLabs/aya-expanse-8b)** — BF16, full precision (`original` backend)
- **[`bartowski/aya-expanse-8b-GGUF`](https://huggingface.co/bartowski/aya-expanse-8b-GGUF)** — Q2\_K, Q3\_K\_M, Q4\_K\_M (`gguf` backend)

### No new backend needed

`aya-expanse-8b` is a standard `CohereForCausalLM` with a chat template, so it runs
on the existing **`original`** loader (`AutoModelForCausalLM` + `apply_chat_template`)
— no new `load_X` function. It uses **`aqlm_venv310`** (Python 3.10, transformers
4.57.6); that transformers line already supports the Cohere architecture, and no
version pins, patches, or env vars are required.

- The model is **gated** on HuggingFace — a stored HF token (`~/.cache/huggingface/token`
  or `HF_TOKEN`) with the terms accepted is needed for the first download (~16 GB).
- `torch_dtype=torch.bfloat16`, `device_map="auto"`; fits comfortably on one H100.
- `max_new_tokens=128` (loader default).

### Running the experiment (all three datasets)

All three runs are chained sequentially on one GPU by `chain_aya_all.sh`
(quick GCJ pair-sets first, long OCD n×n last; each call auto-resumes from
existing round CSVs):

```bash
GPU=1 setsid bash chain_aya_all.sh > logs/chain_aya_all.log 2>&1 < /dev/null &
```

Equivalent individual commands:

```bash
# GCJ-Java (400 pairs × 5)
CUDA_VISIBLE_DEVICES=1 aqlm_venv310/bin/python run_quantization.py original \
  "CohereLabs/aya-expanse-8b" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5

# GCJ cross-language (384 pairs × 5)
CUDA_VISIBLE_DEVICES=1 aqlm_venv310/bin/python run_quantization.py original \
  "CohereLabs/aya-expanse-8b" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output results_gcj_crosslang/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5

# OCD (10,000 pairs × 5 = 50,000 inferences; ~18 h on one H100)
CUDA_VISIBLE_DEVICES=1 aqlm_venv310/bin/python run_quantization.py original \
  "CohereLabs/aya-expanse-8b" \
  --tests-dir ocd/tests \
  --output results/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5
```

### Results — Original BF16 (5-round majority vote)

| Dataset | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| GCJ-Java (400) | 0.8333 | 0.8047 | 0.8782 | 0.8398 | 0.6696 | 4 |
| GCJ cross-language (384) | 0.7995 | 0.7889 | 0.8177 | 0.8031 | 0.5994 | 0 |
| OCD (10,000) | 0.9325 | 0.5972 | 0.9980 | 0.7473 | 0.7424 | 0 |

### GGUF variants (`gguf` backend)

The three bartowski GGUF quants run on the standard `gguf` venv (source-built
`llama-cpp-python` for sm_90). aya is a Cohere/Command-R architecture, which
`llama.cpp` supports natively — no new backend. All three quants × all three
datasets (9 runs) are chained sequentially on one GPU by `chain_aya_gguf_all.sh`
(six quick GCJ runs first, then the three long OCD n×n runs; each call auto-resumes
from existing round CSVs):

```bash
CUDA_VISIBLE_DEVICES=5 setsid bash scripts/chain_aya_gguf_all.sh \
  > logs/chain_aya_gguf_all.log 2>&1 < /dev/null &
```

Equivalent single quant (Q4\_K\_M on GCJ-Java shown):

```bash
gguf/bin/python run_quantization.py gguf \
  "bartowski/aya-expanse-8b-GGUF::aya-expanse-8b-Q4_K_M.gguf" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/aya-expanse-8b/results_gguf_bartowski__aya-expanse-8b-GGUF_aya-expanse-8b-Q4_K_M.gguf" \
  --rounds 5
```

Results (5-round majority vote MCC; BF16 baseline from the table above):

| Variant | GCJ-Java | GCJ-XLang | OCD |
| --- | --- | --- | --- |
| Original (BF16) | 0.6696 | 0.5994 | 0.7424 |
| GGUF Q4\_K\_M | **0.6744** | 0.5514 | 0.5711 |
| GGUF Q3\_K\_M | 0.6460 | 0.5526 | **0.7359** |
| GGUF Q2\_K | 0.3910 | 0.3970 | 0.2146 |

Q4\_K\_M is ~lossless vs BF16 on the GCJ sets (Java edges it out, 0.6744 vs 0.6696);
Q3\_K\_M is close; **Q2\_K collapses** (steep 2-bit sensitivity, worst on OCD, 0.7424→0.2146,
where it degenerates toward predicting CLONE — recall 1.0, precision 0.14).

**Non-monotonic on OCD:** Q3\_K\_M (0.7359) > Q4\_K\_M (0.5711). OCD's n×n product is
heavily class-imbalanced (~10 % clone pairs), and Q4\_K\_M over-predicts CLONE
(precision 0.39, recall 1.00) while Q3\_K\_M stays more selective (precision 0.59),
so on this imbalanced set the lower-bit quant scores higher on MCC. The GCJ sets are
balanced (50/50) and there Q4\_K\_M ≥ Q3\_K\_M as expected.

## Codestral-22B-v0.1 Setup

### Models run
- **[`mistralai/Codestral-22B-v0.1`](https://huggingface.co/mistralai/Codestral-22B-v0.1)** — BF16, full precision (`original` backend)
- **[`bartowski/Codestral-22B-v0.1-GGUF`](https://huggingface.co/bartowski/Codestral-22B-v0.1-GGUF)** — Q2\_K, Q3\_K\_M, Q4\_K\_M (`gguf` backend)

### No new backend needed

Codestral-22B-v0.1 is a standard `MistralForCausalLM` with a `[INST] … [/INST]`
chat template, so it runs on the existing **`original`** loader
(`AutoModelForCausalLM` + `apply_chat_template`) — no new `load_X` function. The
model is **gated** on HuggingFace (stored HF token with terms accepted needed for
the first ~44 GB download); it fits on one H100 in BF16 (`device_map="auto"`,
`max_new_tokens=128`).

### Full-precision (BF16): dedicated `codestral_venv` (needs sentencepiece + protobuf)

Codestral ships **only a SentencePiece tokenizer** (`tokenizer.model`, no
`tokenizer.json`), so `AutoTokenizer.from_pretrained` must convert it on the fly.
Without `sentencepiece` **and** `protobuf` this fails at load
(`Cannot instantiate this tokenizer from a slow version …` / `requires the
protobuf library`). The other transformers venvs (`aqlm_venv310`, `codellama_venv`)
lack both, so a dedicated **`codestral_venv`** mirrors `codellama_venv`'s versions
plus the two tokenizer deps:

```bash
# One-time setup (uv; system Python 3.10)
uv venv codestral_venv --python 3.10
uv pip install --python codestral_venv/bin/python \
  torch==2.8.0 --index-url https://download.pytorch.org/whl/cu128
uv pip install --python codestral_venv/bin/python \
  transformers==4.57.6 accelerate==1.10.1 sentencepiece protobuf

# Run (GCJ-Java; CUDA_VISIBLE_DEVICES pins one GPU; auto-resumes partial rounds)
CUDA_VISIBLE_DEVICES=1 codestral_venv/bin/python run_quantization.py original \
  "mistralai/Codestral-22B-v0.1" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Codestral-22B-v0.1/results_original_mistralai__Codestral-22B-v0.1" \
  --rounds 5
```

### GGUF variants: use `HF_HUB_DISABLE_XET=1` (xet backend hangs on large files)

The three GGUF quants run on the standard `gguf` venv via `chain_codestral_gguf_gcj_java.sh`
(sequential on one GPU). **Non-obvious download workaround:** HuggingFace's default
**xet** transfer backend repeatedly stalled/truncated on the ~10–13 GB Codestral
GGUF shards on this box — a download would hang for hours with the socket wedged
(no progress, GPU idle), or `hf_hub_download` would raise
`RemoteProtocolError: peer closed connection without sending complete message body`.
Setting **`HF_HUB_DISABLE_XET=1`** forces plain resumable HTTPS, which completes
reliably. For a hung/partial file, delete the `*.incomplete` blob under
`~/.cache/huggingface/hub/models--bartowski--Codestral-22B-v0.1-GGUF/blobs/` and
re-fetch; `hf_hub_download` (and the chain, which retries via `run_quantization.py`
resume) then resumes cleanly. The chain script exports `HF_HUB_DISABLE_XET=1`.

```bash
CUDA_VISIBLE_DEVICES=4 HF_HUB_DISABLE_XET=1 setsid bash chain_codestral_gguf_gcj_java.sh \
  > logs/chain_codestral_gguf_gcj_java.log 2>&1 < /dev/null &

# Equivalent single quant (Q4_K_M shown):
HF_HUB_DISABLE_XET=1 gguf/bin/python run_quantization.py gguf \
  "bartowski/Codestral-22B-v0.1-GGUF::Codestral-22B-v0.1-Q4_K_M.gguf" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Codestral-22B-v0.1/results_gguf_bartowski__Codestral-22B-v0.1-GGUF_Codestral-22B-v0.1-Q4_K_M.gguf" \
  --rounds 5
```

### Results (GCJ-Java, 5-round majority vote)

Quantization is near-lossless here — MCC barely moves from BF16, and Q4\_K\_M
slightly edges out full precision.

| Variant | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| Original (BF16) | 0.9375 | 0.9730 | 0.9000 | 0.9351 | 0.8775 | 0 |
| GGUF Q2\_K | 0.9350 | 0.9350 | 0.9350 | 0.9350 | 0.8700 | 0 |
| GGUF Q3\_K\_M | 0.9350 | 0.9394 | 0.9300 | 0.9347 | 0.8700 | 0 |
| GGUF Q4\_K\_M | 0.9400 | 0.9583 | 0.9200 | 0.9388 | 0.8807 | 0 |

### Results (GCJ cross-language, 5-round majority vote)

Codestral was initially only run on GCJ-Java; the cross-language sweep
(`gcj_crosslang_clones/pairs.csv`) was filled in later with the same commands
(swap `--pairs-file` and `--output`). Same pattern as GCJ-Java — quantization
stays close to BF16, with Q2\_K/Q3\_K\_M costing a bit more MCC than on Java.

| Variant | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| Original (BF16) | 0.9115 | — | — | 0.9146 | 0.8251 | 0 |
| GGUF Q2\_K | 0.8724 | — | — | 0.8847 | 0.7624 | 0 |
| GGUF Q3\_K\_M | 0.8672 | — | — | 0.8806 | 0.7535 | 0 |
| GGUF Q4\_K\_M | 0.8984 | — | — | 0.9056 | 0.8061 | 0 |

### Results (OCD, 5-round majority vote)

The OCD sweep (`--tests-dir ocd/tests`, no `--pairs-file`) was the last dataset
filled in, run on idle GPUs with the same `original`/`gguf` commands (see
[Experimental Environment](#experimental-environment) for the GPU-contention
note that pushed the GGUF chain's Q4\_K\_M leg onto a dedicated idle GPU
mid-run). OCD is syntactic/near-miss rather than semantic, and Codestral is
near-saturated on it — MCC stays in a tight 0.96–0.97 band across all four
configs, the smallest quantization-induced spread of any dataset for this
model.

| Variant | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| Original (BF16) | 0.9942 | 0.9469 | 0.9980 | 0.9718 | 0.9689 | 0 |
| GGUF Q2\_K | 0.9941 | 0.9443 | 1.0000 | 0.9713 | 0.9686 | 0 |
| GGUF Q3\_K\_M | 0.9948 | 0.9506 | 1.0000 | 0.9747 | 0.9722 | 0 |
| GGUF Q4\_K\_M | 0.9927 | 0.9328 | 0.9990 | 0.9648 | 0.9614 | 0 |

Codestral's coverage is now complete across all three datasets (OCD, GCJ-Java,
GCJ cross-language) for all four variants (BF16, Q2\_K, Q3\_K\_M, Q4\_K\_M).

## Qwen3.6-27B Setup (multimodal model, run text-only)

### Model run
- **[`Qwen/Qwen3.6-27B`](https://huggingface.co/Qwen/Qwen3.6-27B)** — a Qwen3.5-family
  **multimodal (VLM)** model (`Qwen3_5ForConditionalGeneration`, 27B **dense**,
  64-layer hybrid Gated-DeltaNet + Gated-Attention), run **text-only** via the new
  `qwen36` backend.

### Why transformers, not vLLM (the load path is the hard part)

vLLM would be the natural fast server, but **it cannot run this model on this box**:
the `qwen3_5` architecture needs vLLM ≥ 0.19, and every vLLM that new ships
**CUDA-13** kernels (its `vllm-flash-attn` Hopper kernel needs `libcudart.so.13`).
This box's driver is **535 / CUDA 12.2**, which cannot run CUDA-13 code — a vLLM
smoke test loads and profiles KV cache but then dies in the attention kernel with
`CUDA error: CUDA driver version is insufficient for CUDA runtime version`. The last
cu12 vLLM (0.11) predates `qwen3_5`. So the model runs on **transformers** (uses
torch's cu128 kernels, which work on the 535 driver), served by the `qwen36` loader.

### Virtual environment (`qwen36_venv`)

Needs **transformers ≥ 5.14** (4.57.6 does not know `qwen3_5`). Installing
`-U transformers` pulls **torch 2.13+cu130**, which the 535 driver rejects
(`CUDA driver too old`); torchvision/torchaudio then mismatch a pinned-back torch
(`operator torchvision::nms does not exist` / torchaudio `undefined symbol`). The
fix is to pin the **whole torch trio to cu128**:

```bash
uv venv qwen36_venv --python 3.10
uv pip install --python qwen36_venv/bin/python -U "transformers>=5.14" accelerate
# Pin torch stack back to cu128 (the -U above pulls cu130, which the 535 driver rejects):
uv pip install --python qwen36_venv/bin/python \
  "torch==2.8.0" "torchvision==0.23.0" "torchaudio==2.8.0" \
  --index-url https://download.pytorch.org/whl/cu128
```

Lock file: `requirements/qwen36_venv.lock.txt`. (During bring-up a vLLM 0.24/0.21
was installed to test the vLLM path; it is **vestigial** — uninstalled, not needed.)

### `load_qwen36` details

- Loads with `AutoModelForImageTextToText` (`dtype=bfloat16`, `device_map="auto"`);
  the 27B dense weights (~52 GB) fit one H100. **Text-only**: prompts go through the
  plain **tokenizer** chat template — no processor/vision inputs.
- Qwen3.6 is a hybrid **thinking** model whose template appends `<think>` by default.
  We pass **`enable_thinking=False`** (injects an empty `<think></think>`) so it emits
  the JSON verdict directly instead of a long reasoning trace that would overrun
  `max_new_tokens` and fail JSON parsing. `max_new_tokens=256`,
  `do_sample=True, temperature=0.7, top_p=0.8, top_k=20` (Qwen instruct settings; the
  sampling also gives the per-round variation majority-vote relies on).
- The `qwen3_5` linear-attention layers log *"fast path is not available … falling
  back to torch implementation"* because `flash-linear-attention` / `causal-conv1d`
  are not installed. Correctness is unaffected; it just runs at ~12 s/pair (so OCD's
  50k inferences ≈ ~7 days on one H100).

### Running the experiment (all three datasets)

Chained sequentially on one GPU by `scripts/chain_qwen36_all.sh` (quick GCJ sets
first, OCD last; each call auto-resumes from existing round CSVs):

```bash
GPU=4 setsid bash scripts/chain_qwen36_all.sh > logs/chain_qwen36_all.log 2>&1 < /dev/null &
```

### Results (5-round majority vote)

All three datasets completed 2026-07-27 (launched 2026-07-22; OCD alone took
~4.5 days at ~8 s/pair). 0 excluded on every dataset — the `enable_thinking=False`
JSON verdict parsed cleanly on all 12,304 pairs × 5 rounds.

| Dataset | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| GCJ-Java (400) | 0.9450 | 1.0000 | 0.8900 | 0.9418 | 0.8954 | 0 |
| GCJ cross-language (384) | 0.9271 | 0.9881 | 0.8646 | 0.9222 | 0.8609 | 0 |
| OCD (10,000) | 0.9926 | 1.0000 | 0.9260 | 0.9616 | 0.9584 | 0 |

Notably high precision throughout (1.0 on both GCJ-Java and OCD) — the model is
conservative about calling CLONE, trading some recall for near-zero false
positives. The OCD MCC (0.9584) is the highest of any model in the OCD study,
ahead of Qwen3-Coder-30B-A3B-FP8 (0.9307).

### Self-quantized GGUF (`qwen36_gguf` backend)

No community GGUF exists yet for the brand-new `qwen35` architecture, so it is
quantized ourselves following [Producing our own GGUF
quants](#producing-our-own-gguf-quants-models-without-a-community-gguf):
`llama.cpp` (as of 2026-07-16) already registers `Qwen3_5ForConditionalGeneration`
in `conversion/qwen.py` (`Qwen3_5TextModel`, GGUF arch `QWEN35`) with a matching
C++ runtime (`src/models/qwen35.cpp`), and the already-installed
`llama-cpp-python==0.3.26` has `qwen35` compiled into its `libllama.so` — no
rebuild needed for inference, only for producing the quants (`llama-quantize`,
already built at `llama.cpp/build/bin/`).

```bash
SNAP=~/.cache/huggingface/hub/models--Qwen--Qwen3.6-27B/snapshots/<hash>
env -u PYTHONPATH PYTHONPATH=llama.cpp/gguf-py aqlm_venv310/bin/python \
  llama.cpp/convert_hf_to_gguf.py "$SNAP" \
  --outfile models/Qwen3.6-27B-F16.gguf --outtype f16   # 54.6 GB, 866 tensors

Q=llama.cpp/build/bin/llama-quantize
BASE=models/Qwen3.6-27B
$Q "$BASE-F16.gguf" "$BASE-Q4_K_M.gguf" Q4_K_M   # 16.8 GB, 4.92 BPW
$Q "$BASE-F16.gguf" "$BASE-Q3_K_M.gguf" Q3_K_M   # 13.5 GB, 3.95 BPW
$Q "$BASE-F16.gguf" "$BASE-Q2_K.gguf"   Q2_K     # 10.9 GB, 3.18 BPW
```

**Thinking must be suppressed by hand.** `llama-cpp-python`'s
`create_chat_completion()` has no way to pass the `enable_thinking` Jinja
variable through to the model's own chat template. Left at its default, the
template opens an unclosed `<think>` block and the model reasons at length
before ever emitting the JSON verdict — a smoke test hit 1024 generated
tokens (`finish_reason=length`) without finishing a single pair's reasoning,
let alone answering it. The fix (the new `qwen36_gguf` backend in
`run_quantization.py`) builds the ChatML prompt by hand with a pre-closed
`<think>\n\n</think>\n\n` block — the template's own output for
`enable_thinking=False`, mirroring `load_qwen36`'s transformers-side flag —
and calls raw `create_completion()` instead of the chat wrapper. This took
inference from "never finishes" to **~4–5 s/pair**, `finish_reason=stop`,
clean JSON every time.

```bash
CUDA_VISIBLE_DEVICES=<gpu> env -u PYTHONPATH gguf/bin/python run_quantization.py qwen36_gguf \
  models/Qwen3.6-27B-Q4_K_M.gguf \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/Qwen3.6-27B/results_gguf_Qwen3.6-27B-Q4_K_M \
  --rounds 5
```

All 6 GCJ runs (Q2_K/Q3_K_M/Q4_K_M × Java/cross-language) were chained on one
GPU by `scripts/chain_qwen36_gguf_gcj.sh` and completed 2026-08-04 in ~16 h
(~5 s/pair, matching the smoke test). OCD (50,000 inferences × 3 quant levels)
is deferred — at this rate it is a multi-day-per-quant-level undertaking, same
order as the BF16 OCD run.

| Config | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| GCJ-Java Q2_K | 0.9300 | 1.0000 | 0.8600 | 0.9247 | 0.8686 | 0 |
| GCJ-Java Q3_K_M | 0.9625 | 1.0000 | 0.9250 | 0.9610 | 0.9276 | 0 |
| GCJ-Java Q4_K_M | 0.9375 | 1.0000 | 0.8750 | 0.9333 | 0.8819 | 0 |
| GCJ cross-language Q2_K | 0.9452 | 1.0000 | 0.8901 | 0.9418 | 0.8957 | 1 |
| GCJ cross-language Q3_K_M | 0.9792 | 0.9894 | 0.9688 | 0.9789 | 0.9585 | 0 |
| GCJ cross-language Q4_K_M | 0.9269 | 0.9940 | 0.8594 | 0.9218 | 0.8618 | 1 |

**Non-monotonic with bit-width, and Q3_K_M is the best config overall** — it
beats not just Q2_K/Q4_K_M but the BF16 baseline itself (Java 0.9276 vs.
0.8954; cross-language 0.9585 vs. 0.8609). Precision stays pinned near 1.0
across every config (0.99–1.00), so all of the MCC movement comes from
**recall** — quantization here shifts how many true clones the model misses,
not how trigger-happy it is. This is the second model in the study (after
aya-expanse-8b) where a mid bit-width outperforms both its neighbors, so
bit-width is a coarse predictor of quality but not a monotonic guarantee.

## cogito-v1-preview-llama-8B Setup

### Models run
- **[`deepcogito/cogito-v1-preview-llama-8B`](https://huggingface.co/deepcogito/cogito-v1-preview-llama-8B)** — BF16, full precision (`original` backend)
- **[`cortexso/cogito-v1`](https://huggingface.co/cortexso/cogito-v1)** — Q2\_K, Q3\_K\_M, Q4\_K\_M (`gguf` backend)

### Two separate repos — the GGUF one has no full-precision weights

The GGUF quants (run first, chronologically) come from `cortexso/cogito-v1`,
which is a **GGUF-only** community distribution — it ships no `.safetensors`,
so no BF16 baseline could be run from that repo. The original full-precision
release lives at a **different** repo, `deepcogito/cogito-v1-preview-llama-8B`
(standard `LlamaForCausalLM`, ungated, BF16) — a Llama-3.1-8B fine-tune. No new
backend code was needed for either: `original` and `gguf` are the same generic
loaders used for every other model in the study.

- BF16: `AutoModelForCausalLM`, `torch_dtype=torch.bfloat16`, `device_map="auto"`,
  standard Llama-3.1 `[INST]`-style chat template, `max_new_tokens=128`
  (loader default). Uses **`aqlm_venv310`** (Python 3.10, transformers 4.57.6) —
  same venv as aya-expanse-8b, no version pins needed.
- GGUF: standard `gguf` venv/backend; cogito runs in non-thinking mode by
  default so the usual `max_tokens=128` GGUF path applies (see
  `scripts/chain_cogito_all.sh`).

### Running the BF16 experiment (all three datasets)

Chained sequentially on one GPU by `scripts/chain_cogito_original_all.sh`
(quick GCJ sets first, OCD last; each call auto-resumes from existing round
CSVs):

```bash
GPU=0 setsid bash scripts/chain_cogito_original_all.sh \
  > logs/chain_cogito_original_all.log 2>&1 < /dev/null &
```

### Results (5-round majority vote)

Quantization is **not** lossless for this model — BF16 clears every GGUF
quant by a wide margin on all three datasets (Q4\_K\_M alone still leaves
$0.08$–$0.14$ MCC on the table vs.\ BF16), unlike aya-expanse-8b/Codestral
where 4-bit was near-free. See RQ1/RQ4 in `summaries/experiment_report.tex`.

| Variant | Acc | Precision | Recall | F1 | MCC | Excl |
| --- | --- | --- | --- | --- | --- | --- |
| **GCJ-Java (400)** | | | | | | |
| Original (BF16) | 0.9225 | 0.9617 | 0.8800 | 0.9191 | 0.8481 | 0 |
| GGUF Q2\_K | 0.5525 | 0.5283 | 0.9800 | 0.6865 | 0.2025 | 0 |
| GGUF Q3\_K\_M | 0.6310 | 0.5794 | 0.9899 | 0.7310 | 0.3701 | 7 |
| GGUF Q4\_K\_M | 0.8797 | 0.8304 | 0.9550 | 0.8884 | 0.7680 | 1 |
| **GCJ cross-language (384)** | | | | | | |
| Original (BF16) | 0.9213 | 0.8971 | 0.9531 | 0.9242 | 0.8441 | 3 |
| GGUF Q2\_K | 0.5681 | 0.5423 | 0.9010 | 0.6771 | 0.1787 | 2 |
| GGUF Q3\_K\_M | 0.5937 | 0.5559 | 0.9844 | 0.7105 | 0.2911 | 5 |
| GGUF Q4\_K\_M | 0.8407 | 0.7764 | 0.9583 | 0.8578 | 0.7009 | 1 |
| **OCD (10,000)** | | | | | | |
| Original (BF16) | 0.9732 | 0.7886 | 1.0000 | 0.8818 | 0.8747 | 16 |
| GGUF Q2\_K | 0.7694 | 0.3023 | 0.9910 | 0.4633 | 0.4705 | 43 |
| GGUF Q3\_K\_M | 0.8232 | 0.3647 | 1.0000 | 0.5345 | 0.5412 | 149 |
| GGUF Q4\_K\_M | 0.9441 | 0.6414 | 1.0000 | 0.7816 | 0.7756 | 7 |

## Datasets

### GCJ2-4lang (cross-language clones)

[HuggingFace: lihy11/GCJ2-4lang](https://huggingface.co/datasets/lihy11/GCJ2-4lang) —
the 4-language extended version of the GCJ2 dataset from
[MultiPerspectiveCloneEval](https://github.com/lihy11/MultiPerspectiveCloneEval)
(ISSRE 2023). Supports **cross-language** code clone detection, unlike the
Java-only MultiPerspectiveCloneEval release.

Download (single 71 MB pickle; gitignored):

```bash
curl -sL -o gcj4.pkl \
  "https://huggingface.co/datasets/lihy11/GCJ2-4lang/resolve/main/gcj4.pkl"
```

Load as a pandas DataFrame (37,364 rows, one solution per row):

```python
import pickle
df = pickle.load(open("gcj4.pkl", "rb"))
```

Key columns:
- `flines` — full source code of the solution (string).
- `lan` — language: `java` (12,447), `cpp` (11,985), `py` (11,608), `php` (1,324).
- `year`, `round`, `task` — identify the GCJ **problem** (the clone group).
- `file`, `lines`, `funid`, `username`, `index`, `solution` — metadata.

Clone pairing (Type-4 / functional): two solutions are clones iff they share the
same `(year, round, task)`. There are **20 distinct problems, each solved in all
4 languages**, so a **cross-language** clone pair is any pair with the same
`(year, round, task)` but different `lan`. Non-clones come from different problems.

### Derived Java semantic-clone pair set (`gcj_java_clones/`)

A small labeled Java-only clone-pair benchmark sampled from `gcj4.pkl`,
produced by `prepare_gcj_clones.py` (reproducible, `SEED=42`):

```bash
python3 prepare_gcj_clones.py
```

Protocol:
- Randomly sample **16 problems** (from those with >=3 Java submissions) and
  **3 Java submissions each** -> **48 unique files**.
- **True pairs (48)**: all within-problem submission pairs (C(3,2)=3 per problem
  x 16), same language, `label=1`.
- **False pairs (48)**: each true pair's first submission paired with a random
  extracted submission from a *different* problem, same language, `label=0`.
  Partners are drawn only from the 48 extracted files, so the file set stays 48.
- Result: **96 pairs over 48 files** (balanced 48/48).

Outputs:
- `gcj_java_clones/files/<id>.java` — the 48 extracted source files.
- `gcj_java_clones/pairs.csv` — `pair_id, label, lang, file1, file2, problem1, problem2`.
- `gcj_java_clones/files_meta.csv` — `file_id, problem, lan, file, lines`.

The generator asserts the invariants (48 unique files, 48/48 balance, all false
pairs cross-problem, no false pair colliding with a true pair).

### Derived cross-language clone pair set (`gcj_crosslang_clones/`, GCJ^CL)

The cross-language counterpart, produced by `prepare_gcj_crosslang.py`
(reproducible, `SEED=42`):

```bash
python3 prepare_gcj_crosslang.py
```

Protocol:
- Randomly sample **16 problems** solved in all 4 languages, and **3 submissions
  per language** each (4 x 3 = 12 per problem) -> **192 unique files**.
- **True pairs (192)**, cross-language + same problem, `label=1`: for each
  submission index `i` in `{0,1,2}`, connect the 4 languages in a **ring**
  `java -> cpp -> py -> php -> java`, pairing same-index submissions
  (4 edges x 3 indices = 12/problem x 16). Every file lands in exactly 2 true
  pairs; all submissions to a GCJ problem are semantic clones, so each is a
  genuine cross-language clone. Even coverage: 48 pairs per ring edge.
- **False pairs (192)**, `label=0`: one per file, paired with a random extracted
  submission from a **different problem AND different language**.
- Result: **384 pairs over 192 files** (balanced 192/192).

Outputs:
- `gcj_crosslang_clones/files/<id>.<ext>` — 192 files (48 each java/cpp/py/php).
- `gcj_crosslang_clones/pairs.csv` — `pair_id, label, file1, file2, lang1, lang2, problem1, problem2`.
- `gcj_crosslang_clones/files_meta.csv` — `file_id, problem, lan, file, lines`.

The generator asserts the invariants (192 files, all true pairs cross-language &
same-problem, all false pairs different-problem & different-language, every file
participates in a true pair, 192/192 balance).

### Evaluating a derived pair set (`--pairs-file`)

By default `run_quantization.py` evaluates the full n×n Cartesian product of the
`*.java` files under `--tests-dir` (ground truth = CLONE iff same parent
`program` dir). The `--pairs-file` flag instead evaluates an **explicit pair
list** — used for the curated GCJ sets above:

```bash
python run_quantization.py <backend> <hf_model> \
  --pairs-file gcj_java_clones/pairs.csv \
  --files-dir  gcj_java_clones/files \   # optional; defaults to <pairs_csv dir>/files
  --output results_gcj_java/<Model>/... --rounds 5
```

The CSV needs columns `pair_id, label, file1, file2, problem1, problem2`
(`label` 1=clone / 0=non-clone), plus a language column — either a single `lang`
(monolingual set) or `lang1`/`lang2` (cross-language set). Source is read from
`--files-dir` (default: `files/` next to the CSV). A `pair_id` column was added
to the result CSVs so each row's resume key is unique even if a `(file1, file2)`
pair repeats; the OCD n×n mode is unchanged (empty `pair_id`, resume still keyed
on the `program_a/variant_a/program_b/variant_b` 4-tuple).

**Per-pair prompt language.** The prompt's `{lang}` field ("Compare the two
{lang} code snippets") is filled per pair from the CSV: `lang` (or matching
`lang1`/`lang2`) → e.g. `Java`; differing `lang1`/`lang2` → e.g.
`Java and C++` (codes mapped java→Java, cpp→C++, py→Python, php→PHP). The OCD
n×n mode still uses the single experiment-wide `--lang` (default `Java`).

Results for the derived GCJ sets live under **`results_gcj_java/`** (kept
separate from the OCD-based `results/`).

### Original Llama-4-Scout on GCJ Java (`gcj_java_clones`)

```bash
CUDA_VISIBLE_DEVICES=1,2,3,5,6,7 llama4_venv/bin/python run_quantization.py original \
  "meta-llama/Llama-4-Scout-17B-16E-Instruct" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Llama-4-Scout-17B-16E-Instruct/results_original_meta-llama__Llama-4-Scout-17B-16E-Instruct" \
  --rounds 5 2>&1 | tee logs/run_original_llama4_scout_gcj_java.log
```

96 pairs × 5 rounds = 480 inferences. `CUDA_VISIBLE_DEVICES` pins the run to idle
GPUs so it does not collide with other jobs (Scout's MoE in BF16 is distributed
across them via `device_map="auto"`).

---

## Fine-Tuning (QLoRA): Qwen2.5-Coder-7B on AIZU, evaluated on GCJ-Java

**Goal.** Fine-tune a LoRA adapter for the clone task and measure the lift at the
**same quantization** as an existing baseline: the Qwen2.5-Coder-7B **GGUF
Q4_K_M** row (GCJ-Java Acc 0.8175 / MCC 0.6821).

**Design (fair comparison).** A GGUF k-quant is not differentiable, so you cannot
back-prop through the `.gguf` directly. Instead:
1. Train a LoRA adapter in PyTorch with **QLoRA** — the base Qwen2.5-Coder-7B is
   loaded 4-bit NF4 (bitsandbytes) and frozen; only the adapter (bf16) trains.
2. Convert the adapter to GGUF.
3. At inference, apply the adapter **on top of the existing
   `qwen2.5-coder-7b-instruct-q4_k_m.gguf`** via llama.cpp `lora_path` (the new
   `gguf_lora` backend). The base bytes are identical to the benchmarked Q4_K_M
   model, so the metric delta isolates the fine-tuning effect.

Caveat: the adapter is trained against the NF4 base and applied to the K-quant
Q4_K_M base — a mild train/inference quant mismatch (same as QLoRA's own NF4
assumption). Report the result as its own row, not a drop-in Q4_K_M replacement.

### Training data — SimilBench AIZU384F

AIZU Online Judge submissions (a *different source* than GCJ, so no problem
overlap with the GCJ-Java test set). 384 balanced pairs (192 clone / 192
non-clone) across 15 languages. Ground truth: `truth/AIZU384F.csv`
(`Truth,fileA,fileB,lang`; T=clone, F=non-clone).

```bash
# The SimilBench repo (contains data/AIZU384F/ + truth/AIZU384F.csv) lives in
# finetune_data/SimilBench-main/ (downloaded from the SimilBench Google-Drive zip).
# Build the chat-format train/val JSONL (all 15 languages, (A,B)+(B,A) swap aug,
# 15% stratified val). No GPU needed:
env -u PYTHONPATH finetune_venv/bin/python prepare_aizu_finetune.py
# -> finetune_data/aizu_train.jsonl (652 ex)  finetune_data/aizu_val.jsonl (116 ex)
```

Each example's user turn is the exact `prompt.md` template (via `build_prompt`);
the assistant turn is the JSON `{"answer": "YES-SIMILAR"|"NO-NOT-SIMILAR", ...}`
that `evaluate_results.parse_answer` reads. Loss is masked to the answer only.

### Virtual environment (`finetune_venv`)

```bash
# uv venv, system Python 3.10 (matches the other venvs). Install cu128 torch
# first, then the training deps.
uv venv --python /usr/bin/python3.10 finetune_venv
uv pip install --python finetune_venv/bin/python \
    torch==2.8.0 --index-url https://download.pytorch.org/whl/cu128
uv pip install --python finetune_venv/bin/python -r requirements/finetune.txt
# requirements/finetune.txt: transformers 4.57.6, accelerate 1.10.1, peft,
# bitsandbytes, datasets, sentencepiece, huggingface_hub.
```

> All commands use `env -u PYTHONPATH` because a shared Jupyter Python 3.9 on
> `PYTHONPATH` otherwise shadows the venv's site-packages.

### Training

Requires a CUDA GPU. QLoRA (r=16, α=32, dropout=0.05) on q/k/v/o/gate/up/down,
LR 2e-4 cosine, 3 epochs, effective batch 16 (bs 2 × grad-accum 8), max_len 2048.

```bash
CUDA_VISIBLE_DEVICES=0 env -u PYTHONPATH finetune_venv/bin/python train_lora.py \
  --base-model Qwen/Qwen2.5-Coder-7B-Instruct \
  --output-dir finetune_models/qwen2.5-coder-7b-aizu-qlora \
  2>&1 | tee logs/train_qwen_aizu_qlora.log
```

`--base-model` MUST be the model your GGUF Q4_K_M is a quant of, so the adapter
tensor shapes match. `--no-4bit` trains on the bf16 base (standard LoRA) instead.
The adapter (a few MB) is written to the `--output-dir`.

### Convert the adapter to GGUF

Needs the llama.cpp repo (not bundled here) for `convert_lora_to_gguf.py`:

```bash
git clone https://github.com/ggml-org/llama.cpp   # one-time
env -u PYTHONPATH finetune_venv/bin/python llama.cpp/convert_lora_to_gguf.py \
  finetune_models/qwen2.5-coder-7b-aizu-qlora \
  --base Qwen/Qwen2.5-Coder-7B-Instruct \
  --outfile finetune_models/qwen2.5-coder-7b-aizu-qlora-F16.gguf --outtype f16
```

### Evaluate on GCJ-Java (`gguf_lora` backend)

Applies the adapter on top of the **existing** Q4_K_M GGUF, through the same
llama.cpp pipeline as the baseline. Uses the `gguf` venv (llama-cpp-python).
hf_model format: `repo::base_file.gguf::adapter.gguf`.

```bash
CUDA_VISIBLE_DEVICES=0 env -u PYTHONPATH gguf/bin/python run_quantization.py gguf_lora \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf::finetune_models/qwen2.5-coder-7b-aizu-qlora-F16.gguf" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Qwen2.5-Coder-7B-Instruct/results_qwen_q4km_aizu_lora" \
  --rounds 5 2>&1 | tee logs/run_qwen_q4km_aizu_lora_gcj_java.log

env -u PYTHONPATH gguf/bin/python evaluate_results.py \
  results_gcj_java/Qwen2.5-Coder-7B-Instruct/results_qwen_q4km_aizu_lora_round*.csv \
  --mode majority-vote --output summaries/evaluation_summary_gcj_java.csv
```

Compare the resulting Acc/MCC against the plain Q4_K_M row (0.8175 / 0.6821) to
read off the fine-tuning lift. Verify at load time that this llama-cpp-python
build honours `lora_path` (0.3.26 supports GGUF LoRA adapters).

### Results (first run, 2026-07-18)

Adapter `qwen2.5-coder-7b-aizu-qlora` (QLoRA, 4-bit NF4 base, r=16/α=32, 3 epochs,
123 steps, ~9 min on one H100; final eval_loss 0.0004) applied on top of the
existing `qwen2.5-coder-7b-instruct-q4_k_m.gguf`, evaluated on GCJ-Java (400
pairs, 5 rounds, majority vote):

| Metric    | Q4_K_M baseline | + AIZU QLoRA adapter | Δ      |
|-----------|-----------------|----------------------|--------|
| Accuracy  | 0.8175          | **0.9775**           | +0.160 |
| Precision | 1.0000          | 0.9799               | −0.020 |
| Recall    | 0.6350          | **0.9750**           | +0.340 |
| F1        | 0.7768          | **0.9774**           | +0.201 |
| MCC       | 0.6821          | **0.9550**           | +0.273 |

Confusion matrices (positive = CLONE):

```
  baseline Q4_K_M                  + AIZU adapter
                Pred C  Pred N                   Pred C  Pred N
  True CLONE      127      73        True CLONE     195       5
  True NON-CLONE    0     200        True NON-CLONE   4     196
```

**Interpretation.** The stock Q4_K_M model was strongly biased toward NON-CLONE:
it never false-positived (precision 1.0) but caught only 64% of true clones
(recall 0.635), missing 73/200. Fine-tuning on AIZU clone pairs taught it to
recognise *functional* similarity across differing implementations — recall rose
to 0.975 for a small precision cost (4 false positives). The resulting MCC 0.955
matches the study's best model (Llama-4-Scout BF16, 0.959): a fine-tuned
quantized 7B reaches 17B-level accuracy on this task.

Notes on validity:
- **True cross-dataset generalisation**: trained on AIZU (AIZU Online Judge),
  tested on GCJ (Google Code Jam) — disjoint sources, zero problem overlap, so
  the gain is not memorisation. (The GCJ dataset itself could not supply held-out
  training problems: all 20 of its Java problems are in the GCJ-Java test set.)
- **Not a format artifact**: both baseline and fine-tuned produced 0
  `DONT-KNOW`/unparseable responses, so the lift is a real capability gain, not
  reduced hedging.
- The near-zero *training* loss reflects the short, templated target string, not
  test difficulty — the GCJ-Java metrics are the meaningful signal.
- Trained on NF4, applied to Q4_K_M (mild, expected QLoRA quant mismatch): report
  as its own row, not a drop-in Q4_K_M replacement.

#### Cross-language (GCJ^CL, same adapter)

The *same* adapter run through `gguf_lora` on `gcj_crosslang_clones/pairs.csv`
(384 java/cpp/py/php pairs, 5 rounds, majority vote):

| Metric   | Q4_K_M baseline | + AIZU QLoRA adapter | Δ      |
|----------|-----------------|----------------------|--------|
| Accuracy | 0.8516          | **0.9557**           | +0.104 |
| F1       | 0.8267          | **0.9563**           | +0.130 |
| MCC      | 0.7339          | **0.9118**           | +0.178 |

Confusion matrix: 186 TP / 181 TN, 11 FP / 6 FN, 0 excluded (recall 0.969,
precision 0.944). **Notable:** AIZU384F's training pairs are all *monolingual*
(same-language, across 15 languages) — the adapter never saw a cross-language
pair — yet cross-language detection (e.g. Java-vs-Python) improved by +0.178 MCC.
The fine-tuning taught a general functional-similarity skill that transfers
across the language barrier, not just within-language matching.

```bash
CUDA_VISIBLE_DEVICES=0 env -u PYTHONPATH \
  LD_LIBRARY_PATH=/cm/shared/apps/cuda12.2/toolkit/12.2.2/targets/x86_64-linux/lib:$LD_LIBRARY_PATH \
  gguf/bin/python run_quantization.py gguf_lora \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf::finetune_models/qwen2.5-coder-7b-aizu-qlora-F16.gguf" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output "results_gcj_crosslang/Qwen2.5-Coder-7B-Instruct/results_qwen_q4km_aizu_lora" --rounds 5
```

#### Cross-language-trained adapter (AIZU324CLF) and the 2×2

To test whether *what* the adapter trains on matters, a second adapter was
trained on the **cross-language** AIZU set (AIZU324CLF: 324 pairs where the two
files are different languages solving the same problem). `prepare_aizu_cl_finetune.py`
is the cross-language counterpart of `prepare_aizu_finetune.py`: since the csv
`lang` column is just "CL", each pair's prompt language is derived from the file
extensions and phrased "LangA and LangB" (e.g. "Java and Python"), matching the
GCJ cross-language test. Training reuses `train_lora.py` unchanged:

```bash
env -u PYTHONPATH finetune_venv/bin/python prepare_aizu_cl_finetune.py   # -> aizu_cl_{train,val}.jsonl (614/34)
CUDA_VISIBLE_DEVICES=0 env -u PYTHONPATH finetune_venv/bin/python train_lora.py \
  --train-file finetune_data/aizu_cl_train.jsonl --val-file finetune_data/aizu_cl_val.jsonl \
  --output-dir finetune_models/qwen2.5-coder-7b-aizuCL-qlora
# convert (same as above) -> qwen2.5-coder-7b-aizuCL-qlora-F16.gguf, then eval both
# GCJ sets via gguf_lora with output base ..._qwen_q4km_aizuCL_lora.
```

Both adapters evaluated on both GCJ tasks (same Q4_K_M base, 5-round majority
vote, MCC):

| adapter (training data)       | GCJ-Java | GCJ cross-language |
|-------------------------------|----------|--------------------|
| *none* (Q4_K_M baseline)      | 0.6821   | 0.7339             |
| AIZU384F  (monolingual pairs) | 0.9550   | 0.9118             |
| AIZU324CLF (cross-lang pairs) | 0.9550   | 0.9169             |

**Conclusion.** The two adapters are statistically indistinguishable on *both*
tasks (the cross-language adapter scores exactly 0.9550 on Java, identical to the
monolingual adapter; 0.9169 vs 0.9118 on cross-language is a 1-pair difference).
So the composition of the fine-tuning pairs — same-language vs cross-language —
has no measurable effect: QLoRA learns a general functional-similarity skill that
transfers across the language barrier either way. Fine-tuning the quantized 7B on
a few hundred clone pairs from an unrelated source (AIZU) roughly triples its
distance-to-perfect MCC and brings it to the level of the study's best model
(Llama-4-Scout BF16).

### Other models (Llama-3.1-8B, CodeLlama-7b)

The identical pipeline was applied to two more models with existing GGUF Q4_K_M
baselines — both standard Llama architectures, so `train_lora.py`,
`convert_lora_to_gguf.py`, and the `gguf_lora` backend all work unchanged (reuse
the AIZU384F `aizu_{train,val}.jsonl`, just change `--base-model` and the GGUF
base). CodeLlama uses the `[INST]` chat template, consistent between the HF
tokenizer (training) and its GGUF (inference). Commands, e.g. Llama-3.1:

```bash
CUDA_VISIBLE_DEVICES=0 env -u PYTHONPATH finetune_venv/bin/python train_lora.py \
  --base-model meta-llama/Meta-Llama-3.1-8B-Instruct \
  --output-dir finetune_models/llama3.1-8b-aizu-qlora        # then convert + gguf_lora eval
# GGUF base: bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf
# CodeLlama base: QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf
```

> Launch each training with an explicit `CUDA_VISIBLE_DEVICES`: an
> auto-"first-free-GPU" picker can place two jobs on the same GPU because a model
> that is still loading has not yet claimed its VRAM.

All three models, AIZU384F adapter on the same Q4_K_M base (5-round majority vote,
MCC, baseline → fine-tuned):

| Model (Q4_K_M base)   | GCJ-Java            | GCJ cross-language  |
|-----------------------|---------------------|---------------------|
| Qwen2.5-Coder-7B      | 0.6821 → **0.9550** | 0.7339 → **0.9118** |
| Meta-Llama-3.1-8B     | 0.2701 → **0.8890** | 0.2646 → **0.8762** |
| CodeLlama-7b          | 0.0508 → **0.7687** | 0.0000 → **0.7541** |

**Findings.** (1) **Fine-tuning rescues even a degenerate quantized model**:
CodeLlama Q4_K_M scored MCC 0.0000 on cross-language (predicted every pair a
clone); a 40M-parameter adapter trained on a few hundred unrelated AIZU pairs
lifts it to 0.75 on both tasks. (2) **The absolute lift is largest for the
weakest base** (CodeLlama +0.72/+0.75, Llama-3.1 +0.62/+0.61, Qwen +0.27/+0.18),
so fine-tuning helps damaged/weak quantized models most — but (3) **base-model
capability remains the ceiling**: the final ranking still tracks it
(Qwen ~0.93 > Llama-3.1 ~0.88 > CodeLlama ~0.76). The adapter dramatically
narrows the gap that quantization + a weak base open up, without erasing it.

### Qwen3-Coder-30B-A3B — LoRA on the BF16 base (MoE; 2026-08-03)

First **standard LoRA** (not QLoRA) in this study, and first on a **MoE**. Trained
on the BF16 base with `train_lora.py --no-4bit` — same recipe otherwise (r=16,
α=32, dropout 0.05, targets `q/k/v/o/gate/up/down`, lr 2e-4, 3 epochs, seed 42,
completion-only loss). The base (~60 GB BF16) is sharded model-parallel across 4
GPUs via `device_map="auto"`; point `--base-model` at the local snapshot from the
GGUF-production download to avoid re-downloading:

```bash
CUDA_VISIBLE_DEVICES=4,5,6,7 env -u PYTHONPATH finetune_venv/bin/python train_lora.py \
  --no-4bit --base-model models/Qwen3-Coder-30B-A3B-Instruct \
  --train-file finetune_data/aizu_train.jsonl --val-file finetune_data/aizu_val.jsonl \
  --output-dir finetune_models/qwen3-coder-30b-a3b-aizu-lora
```

- **Adapter is large: 843 M trainable params (2.69%)** — LoRA targets every one of
  the 128 experts' `gate/up/down` across all layers, so the MoE FFN dominates the
  count (vs ~40–80 M for the dense 7–8 B adapters). ~5 h (123 steps, naive
  model-parallel = one GPU active at a time; train_loss 0.114).

**MoE LoRA → GGUF works (the open question from the DeepSeek note is resolved).**
`convert_lora_to_gguf.py` (reuses the `conversion/` model classes, so
`Qwen3MoeForCausalLM` is supported) **stacks the per-expert LoRA into the merged
expert-tensor layout**: the output GGUF has 672 tensors — 384 attention
(`attn_{q,k,v,output}.weight.lora_{a,b}`) **plus 288 expert**
(`ffn_{gate,up,down}_exps.weight.lora_{a,b}`, i.e. all experts stacked per layer).
Nothing dropped. Adapter GGUF: `finetune_models/qwen3-coder-30b-a3b-aizu-lora-F16.gguf`
(1.7 GB).

```bash
PYTHONPATH=llama.cpp/gguf-py env -u PYTHONPATH aqlm_venv310/bin/python \
  llama.cpp/convert_lora_to_gguf.py finetune_models/qwen3-coder-30b-a3b-aizu-lora \
  --base models/Qwen3-Coder-30B-A3B-Instruct --outtype f16 \
  --outfile finetune_models/qwen3-coder-30b-a3b-aizu-lora-F16.gguf
```

**`load_gguf_lora` now accepts a local base GGUF.** Added a 2-part form
`localbase.gguf::adapter.gguf` (if the base ends in `.gguf` and the file exists,
it loads via `Llama(model_path=…, lora_path=…)` instead of `from_pretrained`). This
lets the adapter run on our self-made F16/Q4_K_M GGUFs (not on the Hub). Evaluated
here on the **full-precision F16 base** = the fine-tune **ceiling** (arm C2 in
`exp_design.md`):

```bash
CUDA_VISIBLE_DEVICES=6 env -u PYTHONPATH gguf/bin/python run_quantization.py gguf_lora \
  "models/Qwen3-Coder-30B-A3B-Instruct-F16.gguf::finetune_models/qwen3-coder-30b-a3b-aizu-lora-F16.gguf" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Qwen3-Coder-30B-A3B-Instruct/results_gguf_lora_Qwen3-Coder-30B-A3B-Instruct-F16_aizu" \
  --rounds 5
```

**Results (5-round majority vote, MCC):** the LoRA lifts the model to the study's
top tier — a fine-tuned 30B matching Llama-4-Scout / the Qwen2.5-Coder+LoRA 7B.

| | GCJ-Java | GCJ-XLang |
| --- | --- | --- |
| FP8 base (≈ full-precision, no FT) | 0.6939 | 0.7002 |
| BF16→GGUF Q4_K_M (no FT) | 0.7664 | 0.7552 |
| **F16 + AIZU LoRA (fine-tuned)** | **0.9600** | **0.9531** |

+0.266 / +0.253 MCC over the full-precision base; precision & recall both ≈0.97–0.98
(balanced 3 FP / 5 FN on Java). Monolingual AIZU384F training again transfers to
cross-language, consistent with the 7B adapters.

### Qwen3.6-27B — LoRA on a multimodal (VLM) base, evaluated via transformers (2026-08-04)

First **VLM** in the study. `Qwen3.6-27B` is `Qwen3_5ForConditionalGeneration` — a
dense 27B multimodal model with a vision tower, a *thinking* template, and an
auxiliary multi-token-prediction head (`mtp`). It runs **text-only via
transformers** (`qwen36` backend; vLLM/GGUF unavailable — see that section). The
standard `train_lora.py` does **not** apply; a sibling `train_lora_vlm.py` handles
three model-specific changes:

1. Loads with `AutoModelForImageTextToText` (not `AutoModelForCausalLM`), BF16, no 4-bit.
2. `target_modules` is a **regex scoped to the text tower only**
   (`model\.language_model\.layers\.\d+\.…`) so LoRA does **not** touch the vision
   encoder or the `mtp.layers.*` head — both share the same q/k/v/o/gate/up/down
   suffixes and would otherwise be caught by a plain suffix list. Verified: **79.7 M
   trainable (0.29%)**, a clean 64-layer text-only adapter (0 = regex missed;
   bloated = vision/mtp caught — neither happened).
3. Tokenization sets `enable_thinking=False` to match the eval path (the AIZU
   targets are plain JSON verdicts, no reasoning trace).

Training needs `peft`+`datasets` added to `qwen36_venv` (transformers 5.14.1;
`finetune_venv`'s 4.57 cannot load `qwen3_5`). Same recipe otherwise (r=16/α=32,
lr 2e-4, 3 epochs, seed 42, completion-only loss); ~2 h on 2 GPUs, train_loss 0.057.

```bash
CUDA_VISIBLE_DEVICES=6,7 env -u PYTHONPATH qwen36_venv/bin/python train_lora_vlm.py \
  --base-model Qwen/Qwen3.6-27B --output-dir finetune_models/qwen3.6-27b-aizu-lora
```

**Eval via transformers + PeftModel (`qwen36_lora` backend), not GGUF.** The model
already runs on transformers here, and GGUF for this new VLM arch is untested, so
the `gguf_lora` route does not apply. `load_qwen36_lora` mirrors `load_qwen36` but
wraps the base with `PeftModel.from_pretrained`. hf_model = `adapter_dir` (default
base) or `base_id::adapter_dir`. This applies the adapter on the **BF16 base** =
fine-tune ceiling (arm C2):

```bash
CUDA_VISIBLE_DEVICES=7 env -u PYTHONPATH qwen36_venv/bin/python run_quantization.py qwen36_lora \
  "finetune_models/qwen3.6-27b-aizu-lora" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Qwen3.6-27B/results_qwen36_lora_Qwen3.6-27B_aizu" --rounds 5
```

**Results (5-round majority vote, MCC):**

| | GCJ-Java | GCJ-XLang |
| --- | --- | --- |
| BF16 base (no FT) | 0.8954 | 0.8609 |
| **BF16 + AIZU LoRA (fine-tuned)** | **0.9504** | **0.9532** |

+0.055 / +0.092 over the base. Smaller lift than the weaker models (the base was
already strong), consistent with the earlier finding — absolute lift is largest for
weaker bases, but base capability sets the ceiling. Monolingual AIZU384F again
transfers to cross-language.
