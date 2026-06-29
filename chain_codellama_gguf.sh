#!/usr/bin/env bash
# Run CodeLlama GGUF Q2_K -> Q3_K_M -> Q4_K_M to 5 rounds each on GPU 7.
# Output bases match the EXISTING files exactly (note Q2_K lacks the
# "gguf_" prefix that Q3/Q4 have) so run_quantization resumes in place.
set -u
cd /home/chaiyong.rag/quantization-clone-study
source gguf/bin/activate

REPO="QuantFactory/CodeLlama-7b-Instruct-hf-GGUF"
DIR="results/CodeLlama-7b-Instruct-hf"

run() {  # $1=hf_file  $2=output_base  $3=logfile
  echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') starting $1"
  CUDA_VISIBLE_DEVICES=7 python run_quantization.py gguf \
    "${REPO}::$1" \
    --tests-dir ocd/tests \
    --output "$2" \
    --rounds 5 2>&1 | tee -a "$3"
  echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') finished $1"
}

run "CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
    "${DIR}/results_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
    run_codellama_gguf_q2k_5rounds.log

run "CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
    "${DIR}/results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
    run_codellama_gguf_q3km_5rounds.log

run "CodeLlama-7b-Instruct-hf.Q4_K_M.gguf" \
    "${DIR}/results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q4_K_M.gguf" \
    run_codellama_gguf_q4km_5rounds.log

echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') CodeLlama GGUF sweep complete"
