#!/usr/bin/env bash
# Evaluate our self-quantized Qwen3-Coder-30B-A3B GGUFs (made from the BF16 base
# via llama.cpp; Q2_K/Q3_K_M/Q4_K_M) across all three datasets — GCJ Java (400),
# GCJ cross-language (384), OCD (10k) — 5 rounds each, sequentially on ONE idle
# GPU. Six quick GCJ runs first, then the three long OCD n×n runs. Each run
# auto-resumes from existing round CSVs. These are the BF16-sourced counterparts
# to the community FP8-derived Qwen3 GGUF already in the study.
#
#   Usage: CUDA_VISIBLE_DEVICES=5 setsid bash scripts/chain_qwen3_bf16gguf_all.sh \
#            > logs/chain_qwen3_bf16gguf_all.log 2>&1 < /dev/null &
cd /home/chaiyong.rag/quantization-clone-study
MODELDIR=results/Qwen3-Coder-30B-A3B-Instruct
mkdir -p logs \
  results_gcj_java/Qwen3-Coder-30B-A3B-Instruct \
  results_gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct \
  "$MODELDIR"

if pgrep -af "run_quantization.py gguf" | grep -q "models/Qwen3-Coder-30B-A3B-Instruct-Q"; then
  echo "[chain] ABORT: a Qwen3 BF16-GGUF run is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-5}
GGUFDIR=models
declare -a SUMMARY=()

run() {  # $1=quant tag  $2=dataset tag  $3...=run_quantization args
  local quant="$1"; shift
  local dtag="$1"; shift
  local model="$GGUFDIR/Qwen3-Coder-30B-A3B-Instruct-$quant.gguf"
  echo "[chain] $(date '+%F %T') START  $dtag :: $quant"
  gguf/bin/python run_quantization.py gguf "$model" \
    "$@" --rounds 5 2>&1 | tee "logs/run_qwen3_bf16gguf_${quant,,}_${dtag}.log"
  local rc=${PIPESTATUS[0]}
  if [ "$rc" -eq 0 ]; then SUMMARY+=("OK      $dtag $quant")
  else SUMMARY+=("FAILED  $dtag $quant"); fi
  echo "[chain] $(date '+%F %T') $([ $rc -eq 0 ] && echo OK || echo FAILED) $dtag :: $quant"
}

# Six quick GCJ runs first.
for Q in Q2_K Q3_K_M Q4_K_M; do
  STEM="results_gguf_Qwen3-Coder-30B-A3B-Instruct-$Q"
  run "$Q" gcj_java \
    --pairs-file gcj_java_clones/pairs.csv \
    --output "results_gcj_java/Qwen3-Coder-30B-A3B-Instruct/$STEM"
  run "$Q" gcj_crosslang \
    --pairs-file gcj_crosslang_clones/pairs.csv \
    --output "results_gcj_crosslang/Qwen3-Coder-30B-A3B-Instruct/$STEM"
done

# Three long OCD runs last.
for Q in Q2_K Q3_K_M Q4_K_M; do
  STEM="results_gguf_Qwen3-Coder-30B-A3B-Instruct-$Q"
  run "$Q" ocd \
    --tests-dir ocd/tests \
    --output "$MODELDIR/$STEM"
done

echo "[chain] $(date '+%F %T') Qwen3-Coder-30B-A3B BF16-GGUF sweep complete (9 runs)"
printf '[chain] %s\n' "${SUMMARY[@]}"
