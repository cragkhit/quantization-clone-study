#!/usr/bin/env bash
# Sequential Qwen/Qwen3.6-27B runs across all three datasets on a single GPU.
# Quick GCJ pair-sets first, long OCD n×n last. Each run_quantization call
# auto-resumes from existing round CSVs.
#
# Qwen3.6-27B is a Qwen3.5-family multimodal (VLM) model run TEXT-ONLY via the
# `qwen36` backend (transformers, not vLLM: vLLM builds new enough for the
# qwen3_5 arch ship CUDA-13 kernels the 535/CUDA-12.2 driver can't run). Needs
# qwen36_venv (transformers 5.14, torch 2.8+cu128). ~8 s/pair on one H100, so
# OCD (50k inferences) is ~4-5 days.
#
#   Usage: GPU=<n> setsid bash scripts/chain_qwen36_all.sh \
#            > logs/chain_qwen36_all.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-4}
mkdir -p logs
export HF_HUB_DISABLE_XET=1

MODEL="Qwen/Qwen3.6-27B"
PY=qwen36_venv/bin/python
BACKEND=qwen36
BASE=results_qwen36_Qwen__Qwen3.6-27B

echo "[chain] 1/3 GCJ-Java (400 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py $BACKEND "$MODEL" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/Qwen3.6-27B/$BASE \
  --rounds 5 > logs/run_qwen36_gcj_java_5rounds.log 2>&1

echo "[chain] 2/3 GCJ-XLang (384 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py $BACKEND "$MODEL" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output results_gcj_crosslang/Qwen3.6-27B/$BASE \
  --rounds 5 > logs/run_qwen36_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/3 OCD (10k pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py $BACKEND "$MODEL" \
  --tests-dir ocd/tests \
  --output results/Qwen3.6-27B/$BASE \
  --rounds 5 > logs/run_qwen36_ocd_5rounds.log 2>&1

echo "[chain] all 3 done  $(date '+%F %T')"
