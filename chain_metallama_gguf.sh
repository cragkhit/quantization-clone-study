#!/usr/bin/env bash
# Run Meta-Llama-3.1-8B GGUF Q3_K_M -> Q4_K_M to 5 rounds each on GPU 7.
# (DeepSeek q4km was already complete, so it is omitted.)
# Round-based output bases match the completed Q2_K naming; the legacy
# "...gguf.csv" single-run files have different names and are left untouched.
set -u
cd /home/chaiyong.rag/quantization-clone-study
source gguf/bin/activate
mkdir -p logs

REPO="bartowski/Meta-Llama-3.1-8B-Instruct-GGUF"
DIR="results/Meta-Llama-3.1-8B-Instruct"

run() {  # $1=hf_file  $2=output_base  $3=logfile
  echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') starting $1"
  CUDA_VISIBLE_DEVICES=7 python run_quantization.py gguf \
    "${REPO}::$1" \
    --tests-dir ocd/tests \
    --output "$2" \
    --rounds 5 2>&1 | tee -a "logs/$3"
  echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') finished $1"
}

run "Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf" \
    "${DIR}/results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf" \
    run_metallama_q3km_5rounds.log

run "Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf" \
    "${DIR}/results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf" \
    run_metallama_q4km_5rounds.log

echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') Meta-Llama-3.1-8B GGUF sweep complete"
