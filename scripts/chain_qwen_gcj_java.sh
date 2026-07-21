#!/usr/bin/env bash
# Run the Qwen2.5-Coder-7B-Instruct model set on the GCJ Java clone set
# (gcj_java_clones/pairs.csv, 96 pairs, 5 rounds each), sequentially on ONE idle
# GPU. Continues past a failure; run_quantization.py resumes from existing round
# CSVs, so re-running is safe.
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_java/Qwen2.5-Coder-7B-Instruct

# Guard against a duplicate GCJ Qwen run (keys on the output dir).
if pgrep -af "run_quantization.py" | grep -q "results_gcj_java/Qwen2.5-Coder-7B-Instruct"; then
  echo "[chain] ABORT: a GCJ Qwen run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-1}
export CC=gcc-11 CXX=g++-11

PAIRS=gcj_java_clones/pairs.csv
OUTDIR=results_gcj_java/Qwen2.5-Coder-7B-Instruct
declare -a SUMMARY=()

run() {  # $1=venv  $2=backend  $3=hf_model  $4=output_base_name  $5=logfile
  echo "[chain] $(date '+%F %T') START  $2 :: $3"
  "$1/bin/python" run_quantization.py "$2" "$3" \
    --pairs-file "$PAIRS" \
    --output "$OUTDIR/$4" \
    --rounds 5 2>&1 | tee "logs/$5"
  local rc=${PIPESTATUS[0]}
  if [ "$rc" -eq 0 ]; then
    echo "[chain] $(date '+%F %T') OK     $2 :: $3"
    SUMMARY+=("OK      $4")
  else
    echo "[chain] $(date '+%F %T') FAILED(rc=$rc) $2 :: $3"
    SUMMARY+=("FAILED  $4")
  fi
}

# --- Original (BF16) -------------------------------------------------------
run aqlm_venv310 original "Qwen/Qwen2.5-Coder-7B-Instruct" \
    "results_qwen2.5_coder_7B_original" \
    run_gcj_qwen_original.log

# --- GGUF (qwen backend defaults to Qwen2.5-Coder-7B-Instruct-GGUF) ---------
run gguf qwen "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q2_k.gguf" \
    "results_qwen2.5_coder_7B_q2k" \
    run_gcj_qwen_q2k.log
run gguf qwen "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q3_k_m.gguf" \
    "results_qwen2.5_coder_7B_q3km" \
    run_gcj_qwen_q3km.log
run gguf qwen "Qwen/Qwen2.5-Coder-7B-Instruct-GGUF::qwen2.5-coder-7b-instruct-q4_k_m.gguf" \
    "results_qwen2.5_coder_7B_q4km" \
    run_gcj_qwen_q4km.log

echo "[chain] $(date '+%F %T') Qwen2.5-Coder-7B GCJ Java sweep complete"
printf '[chain] %s\n' "${SUMMARY[@]}"
