# Experiment Notes

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
- **[`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct)** — BF16, loaded via `transformers`
- **[`meta-llama/Llama-4-Scout-17B-16E-Instruct`](https://huggingface.co/meta-llama/Llama-4-Scout-17B-16E-Instruct)** — BF16, loaded via `transformers`

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
  --rounds 1 2>&1 | tee run_higgs.log
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
  --rounds 1 2>&1 | tee run_higgs_3bit.log
```

---

## AQLM Setup

### Model
- **HuggingFace model:** [`ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf`](https://huggingface.co/ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf)
- **Quantization:** AQLM 2-bit + PV-Tuning, 1×16 codebook configuration
- **Base model:** [`meta-llama/Meta-Llama-3.1-8B-Instruct`](https://huggingface.co/meta-llama/Meta-Llama-3.1-8B-Instruct)

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

---

## CodeLlama-7b-Instruct-hf Setup

### Models run
- **[`codellama/CodeLlama-7b-Instruct-hf`](https://huggingface.co/codellama/CodeLlama-7b-Instruct-hf)** — BF16, full precision
- **[`QuantFactory/CodeLlama-7b-Instruct-hf-GGUF`](https://huggingface.co/QuantFactory/CodeLlama-7b-Instruct-hf-GGUF)** — Q2\_K, Q3\_K\_M, Q4\_K\_M

### Full-precision (BF16)

Uses `aqlm_venv` (standard `transformers` + `accelerate`). The `codellama` backend
in `run_quantization.py` detects the instruct variant from the model ID and wraps the
prompt in `[INST] ... [/INST]` instead of a chat template. `max_new_tokens=256`.

```bash
source aqlm_venv/bin/activate
python run_quantization.py codellama \
  "codellama/CodeLlama-7b-Instruct-hf" \
  --tests-dir ocd/tests \
  --output results/CodeLlama-7b-Instruct-hf/results_codellama__CodeLlama-7b-Instruct-hf \
  --rounds 1 2>&1 | tee run_codellama.log
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
  --rounds 1 2>&1 | tee run_codellama_gguf_q4km.log

# Q3_K_M
python run_quantization.py gguf \
  "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
  --tests-dir ocd/tests \
  --output "results/CodeLlama-7b-Instruct-hf-GGUF/results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
  --rounds 1 2>&1 | tee run_codellama_gguf_q3km.log

# Q2_K
python run_quantization.py gguf \
  "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
  --tests-dir ocd/tests \
  --output "results/CodeLlama-7b-Instruct-hf-GGUF/results_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
  --rounds 1 2>&1 | tee run_codellama_gguf_q2k.log
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
  --rounds 1 2>&1 | tee run_qwen_original.log
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
  --rounds 1 2>&1 | tee run_qwen_q4km.log

# Q3_K_M
python run_quantization.py qwen \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q3km \
  --rounds 1 2>&1 | tee run_qwen_q3km.log

# Q2_K
python run_quantization.py qwen \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q2k \
  --rounds 1 2>&1 | tee run_qwen_q2k.log
```

---

## DeepSeek-Coder-V2-Lite-Instruct Setup

### Models run
- **[`deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct`](https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct)** — BF16, full precision (`deepseek` backend)
- **[`bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF`](https://huggingface.co/bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF)** — Q4\_K\_M (`gguf` backend)

### Full-precision (BF16)

Uses `aqlm_venv`. The `deepseek` backend passes `trust_remote_code=True` because
the DeepSeek-Coder-V2 architecture ships custom model code in the HuggingFace repo
(e.g. `configuration_deepseek.py`). Transformers will warn about downloading remote
code on first load — this is expected.

```bash
source aqlm_venv/bin/activate
python run_quantization.py deepseek \
  "deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct" \
  --tests-dir ocd/tests \
  --output results/DeepSeek-Coder-V2-Lite-Instruct/results_deepseek_coder_v2_lite \
  --rounds 1 2>&1 | tee run_deepseek.log
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
  --rounds 1 2>&1 | tee run_deepseek_q4km.log
```

### Notes
- The GGUF context warning (`n_ctx_seq (8192) < n_ctx_train (163840)`) is harmless;
  our prompts are well within 8192 tokens.
- `trust_remote_code=True` is only needed for the BF16 path; the GGUF file is
  self-contained and does not execute model-repo Python code.
