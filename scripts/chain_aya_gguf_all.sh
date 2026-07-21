#!/usr/bin/env bash
# Run the aya-expanse-8b GGUF quants (Q2_K, Q3_K_M, Q4_K_M) across all three
# datasets — GCJ Java (400 pairs), GCJ cross-language (384 pairs), OCD (10k pairs)
# — 5 rounds each, sequentially on ONE idle GPU. Quick GCJ pair-sets first, long
# OCD n×n last. Each run continues past a failure; run_quantization.py resumes
# from existing round CSVs, so re-running is safe. The Original (BF16) baseline is
# already done via the `original` backend (aqlm_venv310).
#
#   Usage: CUDA_VISIBLE_DEVICES=5 setsid bash chain_aya_gguf_all.sh \
#            > logs/chain_aya_gguf_all.log 2>&1 < /dev/null &
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_java/aya-expanse-8b results_gcj_crosslang/aya-expanse-8b results/aya-expanse-8b

# Guard against a duplicate aya GGUF run (keys on the repo name in the arg list).
if pgrep -af "run_quantization.py" | grep -q "aya-expanse-8b-GGUF"; then
  echo "[chain] ABORT: an aya GGUF run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-5}
REPO=bartowski/aya-expanse-8b-GGUF
declare -a SUMMARY=()

run() {  # $1=quant_tag (Q2_K|Q3_K_M|Q4_K_M)  $2=dataset_tag  $3=extra args...
  local quant="$1"; shift
  local dtag="$1"; shift
  local hf="$REPO::aya-expanse-8b-$quant.gguf"
  local outbase="results_gguf_bartowski__aya-expanse-8b-GGUF_aya-expanse-8b-$quant.gguf"
  echo "[chain] $(date '+%F %T') START  $dtag :: $quant"
  gguf/bin/python run_quantization.py gguf "$hf" \
    "$@" \
    --rounds 5 2>&1 | tee "logs/run_aya_gguf_${quant,,}_${dtag}.log"
  local rc=${PIPESTATUS[0]}
  if [ "$rc" -eq 0 ]; then
    echo "[chain] $(date '+%F %T') OK     $dtag :: $quant"
    SUMMARY+=("OK      $dtag $quant")
  else
    echo "[chain] $(date '+%F %T') FAILED(rc=$rc) $dtag :: $quant"
    SUMMARY+=("FAILED  $dtag $quant")
  fi
}

# All six quick GCJ runs first (both trackers fill fast), then the three long OCD n×n runs.
for Q in Q2_K Q3_K_M Q4_K_M; do
  OB="results_gguf_bartowski__aya-expanse-8b-GGUF_aya-expanse-8b-$Q.gguf"
  run "$Q" gcj_java \
    --pairs-file gcj_java_clones/pairs.csv \
    --output "results_gcj_java/aya-expanse-8b/$OB"
  run "$Q" gcj_crosslang \
    --pairs-file gcj_crosslang_clones/pairs.csv \
    --output "results_gcj_crosslang/aya-expanse-8b/$OB"
done

for Q in Q2_K Q3_K_M Q4_K_M; do
  OB="results_gguf_bartowski__aya-expanse-8b-GGUF_aya-expanse-8b-$Q.gguf"
  run "$Q" ocd \
    --tests-dir ocd/tests \
    --output "results/aya-expanse-8b/$OB"
done

echo "[chain] $(date '+%F %T') aya-expanse-8b GGUF sweep complete (9 runs)"
printf '[chain] %s\n' "${SUMMARY[@]}"
