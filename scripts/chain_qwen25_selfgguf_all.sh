#!/usr/bin/env bash
# Evaluate our OWN self-quantized Qwen2.5-Coder-7B GGUFs (Q2_K/Q3_K_M/Q4_K_M, made
# from the BF16 base via llama.cpp; see exp_notes.md) across all three datasets —
# GCJ Java (400), GCJ cross-language (384), OCD (10k) — 5 rounds each, sequentially
# on ONE idle GPU. Six quick GCJ runs first, then the three long OCD n×n runs.
# Output stems carry a "-self" suffix to keep them separate from the existing
# COMMUNITY-GGUF results (results_qwen2.5_coder_7B_q*). Auto-resumes from round CSVs.
#
#   Usage: CUDA_VISIBLE_DEVICES=7 setsid bash scripts/chain_qwen25_selfgguf_all.sh \
#            > logs/chain_qwen25_selfgguf_all.log 2>&1 < /dev/null &
cd /home/chaiyong.rag/quantization-clone-study
MODELDIR=results/Qwen2.5-Coder-7B-Instruct
mkdir -p logs \
  results_gcj_java/Qwen2.5-Coder-7B-Instruct \
  results_gcj_crosslang/Qwen2.5-Coder-7B-Instruct \
  "$MODELDIR"

if pgrep -af "run_quantization.py gguf" | grep -q "models/Qwen2.5-Coder-7B-Instruct-Q"; then
  echo "[chain] ABORT: a Qwen2.5 self-GGUF run is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-7}
GGUFDIR=models
declare -a SUMMARY=()

run() {  # $1=quant tag  $2=dataset tag  $3...=run_quantization args
  local quant="$1"; shift
  local dtag="$1"; shift
  local model="$GGUFDIR/Qwen2.5-Coder-7B-Instruct-$quant.gguf"
  echo "[chain] $(date '+%F %T') START  $dtag :: $quant"
  gguf/bin/python run_quantization.py gguf "$model" \
    "$@" --rounds 5 2>&1 | tee "logs/run_qwen25_selfgguf_${quant,,}_${dtag}.log"
  local rc=${PIPESTATUS[0]}
  if [ "$rc" -eq 0 ]; then SUMMARY+=("OK      $dtag $quant")
  else SUMMARY+=("FAILED  $dtag $quant"); fi
  echo "[chain] $(date '+%F %T') $([ $rc -eq 0 ] && echo OK || echo FAILED) $dtag :: $quant"
}

# Six quick GCJ runs first.
for Q in Q2_K Q3_K_M Q4_K_M; do
  STEM="results_gguf_Qwen2.5-Coder-7B-Instruct-$Q-self"
  run "$Q" gcj_java \
    --pairs-file gcj_java_clones/pairs.csv \
    --output "results_gcj_java/Qwen2.5-Coder-7B-Instruct/$STEM"
  run "$Q" gcj_crosslang \
    --pairs-file gcj_crosslang_clones/pairs.csv \
    --output "results_gcj_crosslang/Qwen2.5-Coder-7B-Instruct/$STEM"
done

# Three long OCD runs last.
for Q in Q2_K Q3_K_M Q4_K_M; do
  STEM="results_gguf_Qwen2.5-Coder-7B-Instruct-$Q-self"
  run "$Q" ocd --tests-dir ocd/tests --output "$MODELDIR/$STEM"
done

echo "[chain] $(date '+%F %T') Qwen2.5-Coder-7B self-GGUF sweep complete (9 runs)"
printf '[chain] %s\n' "${SUMMARY[@]}"
