#!/usr/bin/env bash
# Sequential CohereLabs/aya-expanse-8b (BF16, `original` backend) runs across all
# three datasets on a single GPU. Quick GCJ pair-sets first, long OCD n×n last.
# Each run_quantization call auto-resumes from existing round CSVs.
#
#   Usage: GPU=<n> setsid bash chain_aya_all.sh > logs/chain_aya_all.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-1}
mkdir -p logs

MODEL="CohereLabs/aya-expanse-8b"
PY=aqlm_venv310/bin/python

echo "[chain] 1/3 GCJ-Java (400 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5 > logs/run_aya_gcj_java_5rounds.log 2>&1

echo "[chain] 2/3 GCJ-XLang (384 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output results_gcj_crosslang/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5 > logs/run_aya_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/3 OCD (10k pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --tests-dir ocd/tests \
  --output results/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5 > logs/run_aya_ocd_5rounds.log 2>&1

echo "[chain] all 3 done  $(date '+%F %T')"
