#!/usr/bin/env bash
# Run the Codestral-22B-v0.1 GGUF quants on the GCJ Java clone set
# (gcj_java_clones/pairs.csv, 400 pairs, 5 rounds each), sequentially on ONE idle
# GPU. Each run continues past a failure; run_quantization.py resumes from
# existing round CSVs, so re-running is safe. The Original (BF16) run is done
# separately via codestral_venv (needs sentencepiece/protobuf for the SP tokenizer).
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_java/Codestral-22B-v0.1

# Guard against a duplicate GCJ Codestral GGUF run (keys on the output file stem).
if pgrep -af "run_quantization.py" | grep -q "Codestral-22B-v0.1-GGUF"; then
  echo "[chain] ABORT: a GCJ Codestral GGUF run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-4}
export CC=gcc-11 CXX=g++-11

PAIRS=gcj_java_clones/pairs.csv
OUTDIR=results_gcj_java/Codestral-22B-v0.1
REPO=bartowski/Codestral-22B-v0.1-GGUF
declare -a SUMMARY=()

run() {  # $1=hf_model  $2=output_base_name  $3=logfile
  echo "[chain] $(date '+%F %T') START  gguf :: $1"
  gguf/bin/python run_quantization.py gguf "$1" \
    --pairs-file "$PAIRS" \
    --output "$OUTDIR/$2" \
    --rounds 5 2>&1 | tee "logs/$3"
  local rc=${PIPESTATUS[0]}
  if [ "$rc" -eq 0 ]; then
    echo "[chain] $(date '+%F %T') OK     gguf :: $1"
    SUMMARY+=("OK      $2")
  else
    echo "[chain] $(date '+%F %T') FAILED(rc=$rc) gguf :: $1"
    SUMMARY+=("FAILED  $2")
  fi
}

run "$REPO::Codestral-22B-v0.1-Q2_K.gguf" \
    "results_gguf_bartowski__Codestral-22B-v0.1-GGUF_Codestral-22B-v0.1-Q2_K.gguf" \
    run_codestral_gguf_q2k_gcj_java.log
run "$REPO::Codestral-22B-v0.1-Q3_K_M.gguf" \
    "results_gguf_bartowski__Codestral-22B-v0.1-GGUF_Codestral-22B-v0.1-Q3_K_M.gguf" \
    run_codestral_gguf_q3km_gcj_java.log
run "$REPO::Codestral-22B-v0.1-Q4_K_M.gguf" \
    "results_gguf_bartowski__Codestral-22B-v0.1-GGUF_Codestral-22B-v0.1-Q4_K_M.gguf" \
    run_codestral_gguf_q4km_gcj_java.log

echo "[chain] $(date '+%F %T') Codestral-22B GGUF GCJ Java sweep complete"
printf '[chain] %s\n' "${SUMMARY[@]}"
