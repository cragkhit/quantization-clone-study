# Experiment Notes

## Experimental Environment

All experiments were run on a single shared GPU server:

| Component | Spec |
| --- | --- |
| GPU | 8 × NVIDIA H100 80 GB HBM3 (compute capability `sm_90`) |
| GPU driver | 535.161.08 (CUDA driver API 12.2) |
| CUDA toolkit (`nvcc`) | 12.2.140 |
| CPU | 2 × Intel Xeon Platinum 8480C (112 physical cores / 224 threads) |
| RAM | 2.0 TiB |
| OS / kernel | Ubuntu 22.04.2 LTS / Linux 5.15.0-1053-nvidia |

**GPU usage per run.** Each experiment is pinned to a **single H100** via
`CUDA_VISIBLE_DEVICES=<id>` — an 8B model in BF16 fits comfortably in 80 GB (QTIP's
dequantized weight cache adds ~7 GB). Pinning to one idle GPU also avoids cross-run
contention, which is **mandatory for HIGGS**: its FLUTE kernel mis-tunes its template
under GPU contention and produces garbage output (see [HIGGS-GPTQ Setup](#higgs-gptq-setup)).

**sm_90 note.** The H100 is compute capability `sm_90`. Prebuilt CUDA wheels that lack
sm_90 support must be rebuilt from source — this affects `llama-cpp-python` (GGUF backends)
and the FLUTE kernel (HIGGS); see their setup sections and the source-compiled-components
caveats below. Separately, `nvcc` 12.2 does not support the system's default GCC 13, so
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
