# Experiment Notes

## QTIP Setup

### Model
- **HuggingFace model:** `relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit`
- **Quantization:** QTIP 4-bit (trellis-based, `quantlut_sym` decode mode, K=4, L=16, V=2)
- **Base model:** `meta-llama/Meta-Llama-3.1-8B-Instruct`

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
  1 2>&1 | tee run_qtip.log
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
- **`meta-llama/Meta-Llama-3.1-8B-Instruct`** — BF16, loaded via `transformers`
- **`meta-llama/Llama-4-Scout-17B-16E-Instruct`** — BF16, loaded via `transformers`

### Dependencies
No separate venv needed — uses the `aqlm_venv` (transformers 5.8.1, accelerate 1.13.0).

```bash
source aqlm_venv/bin/activate
pip install transformers accelerate
```

### Model loading in `load_original`
- `torch_dtype=torch.bfloat16` — loads weights in BF16 to halve memory vs FP32
  (~16 GB for 8B, ~34 GB for Scout 17B with MoE).
- `device_map="auto"` — accelerate distributes layers across available GPUs automatically.
- Tokenizer and chat template come from the model repo directly via `AutoTokenizer`.
- Inference uses `model.generate()` with `max_new_tokens=128`.

### Running the Experiment
```bash
source aqlm_venv/bin/activate
python run_quantization.py original \
  "meta-llama/Meta-Llama-3.1-8B-Instruct" \
  ocd/tests \
  "results/Meta-Llama-3.1-8B-Instruct/results_original_meta-llama__Meta-Llama-3.1-8B-Instruct" \
  1 2>&1 | tee run_original.log

python run_quantization.py original \
  "meta-llama/Llama-4-Scout-17B-16E-Instruct" \
  ocd/tests \
  "results/Llama-4-Scout-17B-16E-Instruct/results_original_meta-llama__Llama-4-Scout-17B-16E-Instruct" \
  1 2>&1 | tee run_original_llama4_scout.log
```

### Notes
- HuggingFace Hub access is required (`huggingface-cli login` or `HF_TOKEN` env var) since
  both models are gated.
- The 80 GB A100 comfortably fits both models in BF16 on a single GPU.

---

## GGUF Setup

### Models run
- **Meta-Llama-3.1-8B-Instruct:** Q2\_K, Q3\_K\_M, Q4\_K\_M from `bartowski/Meta-Llama-3.1-8B-Instruct-GGUF`
- **Llama-4-Scout-17B-16E-Instruct:** Q2\_K, Q3\_K\_S, Q4\_K\_M from `bartowski/Meta-Llama-4-Scout-17B-16E-Instruct-GGUF`

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
  1 2>&1 | tee run_gguf_q4km.log
```

---

## AQLM Setup

### Model
- **HuggingFace model:** `ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf`
- **Quantization:** AQLM 2-bit + PV-Tuning, 1×16 codebook configuration
- **Base model:** `meta-llama/Meta-Llama-3.1-8B-Instruct`

### Virtual Environment
Uses `aqlm_venv` with PyTorch 2.5.1+cu121 and `aqlm[gpu]` 1.1.7.

```bash
source aqlm_venv/bin/activate
CC=gcc-11 CXX=g++-11 python run_quantization.py aqlm \
  "ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf" \
  ocd/tests \
  "results/Meta-Llama-3.1-8B-Instruct/results_aqlm" \
  1 2>&1 | tee run_aqlm.log
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
