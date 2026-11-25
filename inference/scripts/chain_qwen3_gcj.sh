#!/usr/bin/env bash
# Sequential Qwen3-Coder-30B-A3B runs across the two GCJ pair sets, one at a time
# on a single GPU. Running them sequentially avoids the concurrent-disk-IO
# contention seen when the ~30GB FP8 model is read by multiple vLLM processes at
# once. Each run_quantization call auto-resumes from existing round CSVs.
#
#   Usage: GPU=<n> setsid bash chain_qwen3_gcj.sh > results/logs/chain_qwen3_gcj.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-0}
mkdir -p results/logs

FP8="Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8"
GGUF="ijohn07/Qwen3-Coder-30B-A3B-Instruct-FP8-Q4_K_M-GGUF::qwen3-coder-30b-a3b-instruct-fp8-q4_k_m.gguf"

echo "[chain] 1/4 FP8 (vLLM) GCJ-Java  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True \
  vllm_venv/bin/python inference/run_quantization.py qwen3fp8 "$FP8" \
  --pairs-file benchmarks/gcj_java/pairs.csv \
  --output results/gcj_java/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8 \
  --rounds 5 > results/logs/run_qwen3fp8_gcj_java_5rounds.log 2>&1

echo "[chain] 2/4 FP8 (vLLM) GCJ-XLang  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True \
  vllm_venv/bin/python inference/run_quantization.py qwen3fp8 "$FP8" \
  --pairs-file benchmarks/gcj_crosslang/pairs.csv \
  --output results/gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8 \
  --rounds 5 > results/logs/run_qwen3fp8_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/4 GGUF Q4_K_M (llama.cpp) GCJ-Java  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU gguf/bin/python inference/run_quantization.py gguf "$GGUF" \
  --pairs-file benchmarks/gcj_java/pairs.csv \
  --output results/gcj_java/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8_q4km_gguf \
  --rounds 5 > results/logs/run_qwen3fp8_q4km_gguf_gcj_java_5rounds.log 2>&1

echo "[chain] 4/4 GGUF Q4_K_M (llama.cpp) GCJ-XLang  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU gguf/bin/python inference/run_quantization.py gguf "$GGUF" \
  --pairs-file benchmarks/gcj_crosslang/pairs.csv \
  --output results/gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8_q4km_gguf \
  --rounds 5 > results/logs/run_qwen3fp8_q4km_gguf_gcj_crosslang_5rounds.log 2>&1

echo "[chain] all 4 done  $(date '+%F %T')"
