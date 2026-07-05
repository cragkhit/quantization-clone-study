#!/usr/bin/env bash
# Run the CodeLlama-7b-Instruct-hf model set on the GCJ cross-language clone set
# (gcj_crosslang_clones/pairs.csv, 96 pairs, 5 rounds each), sequentially on ONE idle
# GPU. Each run continues past a failure; run_quantization.py resumes from
# existing round CSVs, so re-running is safe.
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_crosslang/CodeLlama-7b-Instruct-hf

# Guard against a duplicate GCJ CodeLlama run (keys on the output dir).
if pgrep -af "run_quantization.py" | grep -q "results_gcj_crosslang/CodeLlama-7b-Instruct-hf"; then
  echo "[chain] ABORT: a GCJ CodeLlama run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=2
export CC=gcc-11 CXX=g++-11

PAIRS=gcj_crosslang_clones/pairs.csv
OUTDIR=results_gcj_crosslang/CodeLlama-7b-Instruct-hf
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
run codellama_venv codellama "codellama/CodeLlama-7b-Instruct-hf" \
    "results_codellama__CodeLlama-7b-Instruct-hf" \
    run_gcx_codellama_original.log

# --- GGUF (QuantFactory) ---------------------------------------------------
run gguf gguf "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
    "results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q2_K.gguf" \
    run_gcx_codellama_gguf_q2k.log
run gguf gguf "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
    "results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q3_K_M.gguf" \
    run_gcx_codellama_gguf_q3km.log
run gguf gguf "QuantFactory/CodeLlama-7b-Instruct-hf-GGUF::CodeLlama-7b-Instruct-hf.Q4_K_M.gguf" \
    "results_gguf_QuantFactory__CodeLlama-7b-Instruct-hf-GGUF_CodeLlama-7b-Instruct-hf.Q4_K_M.gguf" \
    run_gcx_codellama_gguf_q4km.log

echo "[chain] $(date '+%F %T') CodeLlama-7b GCJ cross-language sweep complete"
printf '[chain] %s\n' "${SUMMARY[@]}"
