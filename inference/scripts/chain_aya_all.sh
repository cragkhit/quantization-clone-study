#!/usr/bin/env bash
# Sequential CohereLabs/aya-expanse-8b (BF16, `original` backend) runs across all
# three datasets on a single GPU. Quick GCJ pair-sets first, long OCD n×n last.
# Each run_quantization call auto-resumes from existing round CSVs.
#
#   Usage: GPU=<n> setsid bash chain_aya_all.sh > results/logs/chain_aya_all.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-1}
mkdir -p results/logs

MODEL="CohereLabs/aya-expanse-8b"
PY=aqlm_venv310/bin/python

echo "[chain] 1/3 GCJ-Java (400 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY inference/run_quantization.py original "$MODEL" \
  --pairs-file benchmarks/gcj_java/pairs.csv \
  --output results/gcj_java/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5 > results/logs/run_aya_gcj_java_5rounds.log 2>&1

echo "[chain] 2/3 GCJ-XLang (384 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY inference/run_quantization.py original "$MODEL" \
  --pairs-file benchmarks/gcj_crosslang/pairs.csv \
  --output results/gcj_crosslang/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5 > results/logs/run_aya_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/3 OCD (10k pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY inference/run_quantization.py original "$MODEL" \
  --tests-dir benchmarks/ocd/tests \
  --output results/ocd/aya-expanse-8b/results_original_CohereLabs__aya-expanse-8b \
  --rounds 5 > results/logs/run_aya_ocd_5rounds.log 2>&1

echo "[chain] all 3 done  $(date '+%F %T')"
