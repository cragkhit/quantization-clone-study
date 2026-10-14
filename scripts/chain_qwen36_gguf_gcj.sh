#!/usr/bin/env bash
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-2}
export LD_LIBRARY_PATH="/cm/shared/apps/cuda12.2/toolkit/12.2.2/targets/x86_64-linux/lib:${LD_LIBRARY_PATH:-}"
PY="env -u PYTHONPATH gguf/bin/python"
BACKEND=qwen36_gguf
MODELDIR=models
i=0
TOTAL=6
for Q in Q2_K Q3_K_M Q4_K_M; do
  i=$((i+1))
  echo "[chain] $i/$TOTAL GCJ-Java $Q  $(date '+%F %T')"
  CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py $BACKEND "$MODELDIR/Qwen3.6-27B-$Q.gguf" \
    --pairs-file gcj_java_clones/pairs.csv \
    --output "results_gcj_java/Qwen3.6-27B/results_gguf_Qwen3.6-27B-$Q" \
    --rounds 5 > "logs/run_qwen36_gguf_${Q}_gcj_java.log" 2>&1
done
for Q in Q2_K Q3_K_M Q4_K_M; do
  i=$((i+1))
  echo "[chain] $i/$TOTAL GCJ-XLang $Q  $(date '+%F %T')"
  CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py $BACKEND "$MODELDIR/Qwen3.6-27B-$Q.gguf" \
    --pairs-file gcj_crosslang_clones/pairs.csv \
    --output "results_gcj_crosslang/Qwen3.6-27B/results_gguf_Qwen3.6-27B-$Q" \
    --rounds 5 > "logs/run_qwen36_gguf_${Q}_gcj_crosslang.log" 2>&1
done
echo "[chain] all $TOTAL runs complete  $(date '+%F %T')"
