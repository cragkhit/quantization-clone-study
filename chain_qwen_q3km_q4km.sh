#!/usr/bin/env bash
# Wait for the running Qwen q2k run (PID 897571) to finish, then run
# q3km and q4km to 5 rounds each on GPU 7. Each run_quantization call
# resumes automatically from existing round CSVs.
set -u
cd /home/chaiyong.rag/quantization-clone-study
source gguf/bin/activate

echo "[chain] waiting for q2k (PID 897571) to exit..."
while kill -0 897571 2>/dev/null; do sleep 30; done
echo "[chain] q2k process gone at $(date '+%Y-%m-%d %H:%M:%S'); starting q3km"

CUDA_VISIBLE_DEVICES=7 python run_quantization.py qwen \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q3km \
  --rounds 5 2>&1 | tee -a run_qwen_q3km_5rounds.log

echo "[chain] q3km done at $(date '+%Y-%m-%d %H:%M:%S'); starting q4km"

CUDA_VISIBLE_DEVICES=7 python run_quantization.py qwen \
  "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf" \
  --tests-dir ocd/tests \
  --output results/Qwen2.5-Coder-7B-Instruct/results_qwen2.5_coder_7B_q4km \
  --rounds 5 2>&1 | tee -a run_qwen_q4km_5rounds.log

echo "[chain] q4km done at $(date '+%Y-%m-%d %H:%M:%S'); Qwen GGUF sweep complete"
