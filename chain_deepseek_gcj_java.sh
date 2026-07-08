#!/usr/bin/env bash
# Run the DeepSeek-Coder-V2-Lite-Instruct model set on the GCJ Java clone set
# (gcj_java_clones/pairs.csv, 96 pairs, 5 rounds each), sequentially on ONE idle
# GPU. Continues past a failure; run_quantization.py resumes from existing round
# CSVs, so re-running is safe.
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_java/DeepSeek-Coder-V2-Lite-Instruct

# Guard against a duplicate GCJ DeepSeek run (keys on the output dir).
if pgrep -af "run_quantization.py" | grep -q "results_gcj_java/DeepSeek-Coder-V2-Lite-Instruct"; then
  echo "[chain] ABORT: a GCJ DeepSeek run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-1}
export CC=gcc-11 CXX=g++-11

PAIRS=gcj_java_clones/pairs.csv
OUTDIR=results_gcj_java/DeepSeek-Coder-V2-Lite-Instruct
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
run deepseek_venv deepseek "deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct" \
    "results_deepseek_coder_v2_lite" \
    run_gcj_deepseek_original.log

# --- GGUF Q4_K_M (bartowski) -----------------------------------------------
run gguf gguf "bartowski/DeepSeek-Coder-V2-Lite-Instruct-GGUF::DeepSeek-Coder-V2-Lite-Instruct-Q4_K_M.gguf" \
    "results_deepseek_coder_v2_lite_q4km" \
    run_gcj_deepseek_gguf_q4km.log

echo "[chain] $(date '+%F %T') DeepSeek-Coder-V2-Lite GCJ Java sweep complete"
printf '[chain] %s\n' "${SUMMARY[@]}"
