#!/usr/bin/env bash
# Run the Llama-4-Scout-17B-16E-Instruct GGUF set on the GCJ Java clone set
# (gcj_java_clones/pairs.csv, 96 pairs, 5 rounds each), sequentially on ONE idle
# GPU. Files are already in the HF cache under the bartowski *-old-GGUF repo;
# Q4_K_M is split into 2 shards (point to shard 1, llama.cpp loads the rest).
# Continues past a failure; run_quantization.py resumes from existing round CSVs.
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_java/Llama-4-Scout-17B-16E-Instruct

if pgrep -af "run_quantization.py" | grep -q "results_gcj_java/Llama-4-Scout-17B-16E-Instruct/results_gguf"; then
  echo "[chain] ABORT: a GCJ Scout GGUF run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-1}

PAIRS=gcj_java_clones/pairs.csv
OUTDIR=results_gcj_java/Llama-4-Scout-17B-16E-Instruct
REPO="bartowski/meta-llama_Llama-4-Scout-17B-16E-Instruct-old-GGUF"
declare -a SUMMARY=()

run() {  # $1=hf_model  $2=output_base_name  $3=logfile
  echo "[chain] $(date '+%F %T') START  gguf :: $1"
  gguf/bin/python run_quantization.py gguf "$1" \
    --pairs-file "$PAIRS" \
    --output "$OUTDIR/$2" \
    --rounds 5 2>&1 | tee "logs/$3"
  local rc=${PIPESTATUS[0]}
  if [ "$rc" -eq 0 ]; then
    echo "[chain] $(date '+%F %T') OK     $2"
    SUMMARY+=("OK      $2")
  else
    echo "[chain] $(date '+%F %T') FAILED(rc=$rc) $2"
    SUMMARY+=("FAILED  $2")
  fi
}

run "${REPO}::meta-llama_Llama-4-Scout-17B-16E-Instruct-Q2_K.gguf" \
    "results_gguf_llama4_scout_Q2_K" run_gcj_scout_gguf_q2k.log

run "${REPO}::meta-llama_Llama-4-Scout-17B-16E-Instruct-Q3_K_S.gguf" \
    "results_gguf_llama4_scout_Q3_K_S" run_gcj_scout_gguf_q3ks.log

run "${REPO}::meta-llama_Llama-4-Scout-17B-16E-Instruct-Q4_K_M/meta-llama_Llama-4-Scout-17B-16E-Instruct-Q4_K_M-00001-of-00002.gguf" \
    "results_gguf_llama4_scout_Q4_K_M" run_gcj_scout_gguf_q4km.log

echo "[chain] $(date '+%F %T') Llama-4-Scout GGUF GCJ Java sweep complete"
printf '[chain] %s\n' "${SUMMARY[@]}"
