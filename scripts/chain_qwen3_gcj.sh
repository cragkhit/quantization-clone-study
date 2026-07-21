#!/usr/bin/env bash
# Sequential Qwen3-Coder-30B-A3B runs across the two GCJ pair sets, one at a time
# on a single GPU. Running them sequentially avoids the concurrent-disk-IO
# contention seen when the ~30GB FP8 model is read by multiple vLLM processes at
# once. Each run_quantization call auto-resumes from existing round CSVs.
#
#   Usage: GPU=<n> setsid bash chain_qwen3_gcj.sh > logs/chain_qwen3_gcj.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-0}
mkdir -p logs

FP8="Qwen/Qwen3-Coder-30B-A3B-Instruct-FP8"
GGUF="ijohn07/Qwen3-Coder-30B-A3B-Instruct-FP8-Q4_K_M-GGUF::qwen3-coder-30b-a3b-instruct-fp8-q4_k_m.gguf"

echo "[chain] 1/4 FP8 (vLLM) GCJ-Java  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True \
  vllm_venv/bin/python run_quantization.py qwen3fp8 "$FP8" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8 \
  --rounds 5 > logs/run_qwen3fp8_gcj_java_5rounds.log 2>&1

echo "[chain] 2/4 FP8 (vLLM) GCJ-XLang  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU PYTORCH_CUDA_ALLOC_CONF=expandable_segments:True \
  vllm_venv/bin/python run_quantization.py qwen3fp8 "$FP8" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output results_gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8 \
  --rounds 5 > logs/run_qwen3fp8_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/4 GGUF Q4_K_M (llama.cpp) GCJ-Java  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU gguf/bin/python run_quantization.py gguf "$GGUF" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8_q4km_gguf \
  --rounds 5 > logs/run_qwen3fp8_q4km_gguf_gcj_java_5rounds.log 2>&1

echo "[chain] 4/4 GGUF Q4_K_M (llama.cpp) GCJ-XLang  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU gguf/bin/python run_quantization.py gguf "$GGUF" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output results_gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct-FP8/results_qwen3_coder_30B_a3b_fp8_q4km_gguf \
  --rounds 5 > logs/run_qwen3fp8_q4km_gguf_gcj_crosslang_5rounds.log 2>&1

echo "[chain] all 4 done  $(date '+%F %T')"
