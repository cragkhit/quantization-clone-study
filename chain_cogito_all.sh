#!/usr/bin/env bash
# Sequential cortexso/cogito-v1 Llama-8b GGUF runs (Q2_K, Q3_K_M, Q4_K_M) across
# all three datasets on a single GPU, via the `gguf` backend (llama-cpp-python).
# cogito-v1-preview-llama-8B is Deep Cogito's Llama-3.1-8B fine-tune; all .gguf
# files live on the repo's `main` branch, so Llama.from_pretrained loads them by
# filename directly (no branch needed). cogito runs in standard (non-thinking)
# mode by default, so the usual max_tokens=128 GGUF path applies.
#
# Order: quick GCJ pair-sets (all 3 quants) first, long OCD n×n last, so the
# comparable pair-set numbers land soonest. Each run_quantization call
# auto-resumes from existing round CSVs.
#
#   Usage: GPU=<n> setsid bash chain_cogito_all.sh > logs/chain_cogito_all.log 2>&1 < /dev/null &
set -u
cd /home/chaiyong.rag/quantization-clone-study
GPU=${GPU:-2}
mkdir -p logs

REPO="cortexso/cogito-v1"
PY=gguf/bin/python
MODELDIR=cogito-v1-preview-llama-8B

# quant short-name -> gguf filename
declare -A QFILE=(
  [Q2_K]=cogito-v1-preview-llama-8b-q2_k.gguf
  [Q3_K_M]=cogito-v1-preview-llama-8b-q3_k_m.gguf
  [Q4_K_M]=cogito-v1-preview-llama-8b-q4_k_m.gguf
)
QUANTS=(Q2_K Q3_K_M Q4_K_M)

declare -a SUMMARY=()

run() {  # $1=hf_model  $2=output_base  $3=logfile
  echo "[chain] $(date '+%F %T') START  gguf :: $1"
  CUDA_VISIBLE_DEVICES=$GPU $PY run_quantization.py gguf "$1" "${@:4}" \
    --output "$2" --rounds 5 > "logs/$3" 2>&1
  local rc=$?
  if [ "$rc" -eq 0 ]; then
    echo "[chain] $(date '+%F %T') OK     $2"; SUMMARY+=("OK      $2")
  else
    echo "[chain] $(date '+%F %T') FAILED(rc=$rc) $2"; SUMMARY+=("FAILED  $2")
  fi
}

# 1/3 GCJ-Java (400 pairs × 5)
for q in "${QUANTS[@]}"; do
  f=${QFILE[$q]}
  run "${REPO}::${f}" \
    "results_gcj_java/${MODELDIR}/results_gguf_cortexso__cogito-v1_${f}" \
    "run_cogito_${q}_gcj_java_5rounds.log" \
    --pairs-file gcj_java_clones/pairs.csv
done

# 2/3 GCJ-XLang (384 pairs × 5)
for q in "${QUANTS[@]}"; do
  f=${QFILE[$q]}
  run "${REPO}::${f}" \
    "results_gcj_crosslang/${MODELDIR}/results_gguf_cortexso__cogito-v1_${f}" \
    "run_cogito_${q}_gcj_crosslang_5rounds.log" \
    --pairs-file gcj_crosslang_clones/pairs.csv
done

# 3/3 OCD (10k pairs × 5)
for q in "${QUANTS[@]}"; do
  f=${QFILE[$q]}
  run "${REPO}::${f}" \
    "results/${MODELDIR}/results_gguf_cortexso__cogito-v1_${f}" \
    "run_cogito_${q}_ocd_5rounds.log" \
    --tests-dir ocd/tests
done

echo "[chain] $(date '+%F %T') cogito-v1 llama-8b GGUF all datasets done"
printf '[chain] %s\n' "${SUMMARY[@]}"
