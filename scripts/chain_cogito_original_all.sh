#!/usr/bin/env bash
# Sequential deepcogito/cogito-v1-preview-llama-8B (BF16, `original` backend)
# runs across all three datasets on a single GPU. Quick GCJ pair-sets first,
# long OCD n×n last. Each run_quantization call auto-resumes from existing
# round CSVs.
#
# Note: the community GGUF quants already in this study (Q2_K/Q3_K_M/Q4_K_M)
# come from cortexso/cogito-v1, which is GGUF-only (no safetensors) - hence no
# prior BF16 baseline. deepcogito/cogito-v1-preview-llama-8B is the original
# full-precision release (standard LlamaForCausalLM, ungated), used here.
#
#   Usage: GPU=<n> setsid bash scripts/chain_cogito_original_all.sh \
#            > logs/chain_cogito_original_all.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-0}
mkdir -p logs

MODEL="deepcogito/cogito-v1-preview-llama-8B"
PY=aqlm_venv310/bin/python

echo "[chain] 1/3 GCJ-Java (400 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output results_gcj_java/cogito-v1-preview-llama-8B/results_original_deepcogito__cogito-v1-preview-llama-8B \
  --rounds 5 > logs/run_cogito_original_gcj_java_5rounds.log 2>&1

echo "[chain] 2/3 GCJ-XLang (384 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output results_gcj_crosslang/cogito-v1-preview-llama-8B/results_original_deepcogito__cogito-v1-preview-llama-8B \
  --rounds 5 > logs/run_cogito_original_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/3 OCD (10k pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --tests-dir ocd/tests \
  --output results/cogito-v1-preview-llama-8B/results_original_deepcogito__cogito-v1-preview-llama-8B \
  --rounds 5 > logs/run_cogito_original_ocd_5rounds.log 2>&1

echo "[chain] all 3 done  $(date '+%F %T')"
