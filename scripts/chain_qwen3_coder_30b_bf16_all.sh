#!/usr/bin/env bash
# Sequential Qwen3-Coder-30B-A3B-Instruct (BF16, `original` backend) runs across
# all three datasets on a single GPU. Quick GCJ pair-sets first, long OCD n×n
# last. Each run_quantization call auto-resumes from existing round CSVs.
#
# Uses the local checkpoint at models/Qwen3-Coder-30B-A3B-Instruct (already
# downloaded for the self-quantized GGUF sweep) instead of re-pulling from HF -
# same qwen3_moe architecture (Qwen3MoeForCausalLM, standard per-expert
# nn.Linear MoE layout, unlike Llama4's fused-tensor experts), no new backend
# code needed.
#
#   Usage: GPU=<n> setsid bash scripts/chain_qwen3_coder_30b_bf16_all.sh \
#            > logs/chain_qwen3_coder_30b_bf16_all.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-2}
mkdir -p logs

MODEL="models/Qwen3-Coder-30B-A3B-Instruct"
PY=codestral_venv/bin/python
OUTBASE="results_original_Qwen3-Coder-30B-A3B-Instruct"

echo "[chain] 1/3 GCJ-Java (400 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --pairs-file gcj_java_clones/pairs.csv \
  --output "results_gcj_java/Qwen3-Coder-30B-A3B-Instruct/$OUTBASE" \
  --rounds 5 > logs/run_qwen3_coder_30b_bf16_gcj_java_5rounds.log 2>&1

echo "[chain] 2/3 GCJ-XLang (384 pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --pairs-file gcj_crosslang_clones/pairs.csv \
  --output "results_gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct/$OUTBASE" \
  --rounds 5 > logs/run_qwen3_coder_30b_bf16_gcj_crosslang_5rounds.log 2>&1

echo "[chain] 3/3 OCD (10k pairs × 5)  $(date '+%F %T')"
CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py original "$MODEL" \
  --tests-dir ocd/tests \
  --output "results/Qwen3-Coder-30B-A3B-Instruct/$OUTBASE" \
  --rounds 5 > logs/run_qwen3_coder_30b_bf16_ocd_5rounds.log 2>&1

echo "[chain] all 3 done  $(date '+%F %T')"
