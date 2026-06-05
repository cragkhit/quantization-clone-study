"""
Minimal inference snippets for each quantization method in Table 1.
Each section is independent — run only the one you need.
 
Pre-quantized Llama-3.1-8B-Instruct models used throughout.
Install dependencies per section before running.
"""
  
with open("prompt.md") as _f:
    PROMPT_TEMPLATE = _f.read()
   
# ==============================================================================
# 1. GGUF  (llama-cpp-python)
#    pip install llama-cpp-python
#    Model: bartowski/Meta-Llama-3.1-8B-Instruct-GGUF  (Q4_K_M shown)
# ==============================================================================
def run_gguf():
    from llama_cpp import Llama
            
    llm = Llama.from_pretrained(
            repo_id="bartowski/Meta-Llama-3.1-8B-Instruct-GGUF",
            filename="Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf",
            n_gpu_layers=-1,   # offload all layers to GPU; set 0 for CPU-only
            verbose=False,
            )
    output = llm(PROMPT_TEMPLATE, max_tokens=128)
    print(output["choices"][0]["text"])


# ==============================================================================
# 2. Original (full-precision BF16, no quantization)
#    pip install transformers accelerate
#    Model: meta-llama/Meta-Llama-3.1-8B-Instruct
# ==============================================================================
def run_original():
    from transformers import AutoTokenizer, AutoModelForCausalLM
    import torch

    model_id = "meta-llama/Meta-Llama-3.1-8B-Instruct"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
            model_id, torch_dtype=torch.bfloat16, device_map="auto"
            )

    messages = [{"role": "user", "content": PROMPT_TEMPLATE}]
    formatted = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
    inputs = tokenizer(formatted, return_tensors="pt").to(model.device)
    outputs = model.generate(**inputs, max_new_tokens=128)
    print(tokenizer.decode(outputs[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True))


# ==============================================================================
# 3. AQLM + PV-Tuning  (transformers native support)
#    pip install transformers accelerate aqlm[gpu]
#    Model: ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf
# ==============================================================================
def run_aqlm():
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

    model_id = "ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf"
    tokenizer = AutoTokenizer.from_pretrained(model_id)
    model = AutoModelForCausalLM.from_pretrained(
            model_id, dtype=torch.float16, device_map="auto"
            )

    messages = [{"role": "user", "content": PROMPT_TEMPLATE}]
    if tokenizer.chat_template:
        formatted = tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
    else:
        # Llama 3.1 Instruct format (AQLM tokenizer omits chat_template)
        formatted = (
            f"<|begin_of_text|><|start_header_id|>user<|end_header_id|>\n\n"
            f"{PROMPT_TEMPLATE}<|eot_id|><|start_header_id|>assistant<|end_header_id|>\n\n"
        )
    inputs = tokenizer(formatted, return_tensors="pt").to(model.device)
    outputs = model.generate(**inputs, max_new_tokens=128)
    print(tokenizer.decode(outputs[0][inputs["input_ids"].shape[1]:], skip_special_tokens=True))


# ==============================================================================
# Run one (comment/uncomment as needed)
# ==============================================================================
if __name__ == "__main__":
#    run_gguf()
#    run_original()
    run_aqlm()

# To run independently:
#   python -c "import run_quantization; run_quantization.run_original()"
#   python -c "import run_quantization; run_quantization.run_aqlm()"
