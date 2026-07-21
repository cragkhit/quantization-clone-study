#!/usr/bin/env bash
# Run the full Meta-Llama-3.1-8B-Instruct model set on the GCJ Java clone set
# (gcj_java_clones/pairs.csv, 96 pairs, 5 rounds each), sequentially on ONE idle
# GPU. Sequential + single-GPU is required for HIGGS (FLUTE template tuning
# mis-selects under contention). CC/CXX=gcc-11 and FLUTE_NUM_SMS=108 are set
# globally; non-HIGGS backends simply ignore them.
#
# Each run continues past a failure (a broken backend must not block the rest);
# run_quantization.py resumes from existing round CSVs, so re-running is safe.
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs results_gcj_java/Meta-Llama-3.1-8B-Instruct

# Guard against a duplicate GCJ Meta-Llama run (keys on the output dir, so it
# neither self-matches this script nor matches unrelated OCD runs).
if pgrep -af "run_quantization.py" | grep -q "results_gcj_java/Meta-Llama-3.1-8B-Instruct"; then
  echo "[chain] ABORT: a GCJ Meta-Llama run_quantization.py process is already active."
  exit 1
fi

export CUDA_VISIBLE_DEVICES=${CUDA_VISIBLE_DEVICES:-1}
export CC=gcc-11 CXX=g++-11 FLUTE_NUM_SMS=108

PAIRS=gcj_java_clones/pairs.csv
OUTDIR=results_gcj_java/Meta-Llama-3.1-8B-Instruct
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

# --- GGUF (bartowski) ------------------------------------------------------
run gguf gguf "bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q2_K.gguf" \
    "results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q2_K.gguf" \
    run_gcj_metallama_gguf_q2k.log
run gguf gguf "bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf" \
    "results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q3_K_M.gguf" \
    run_gcj_metallama_gguf_q3km.log
run gguf gguf "bartowski/Meta-Llama-3.1-8B-Instruct-GGUF::Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf" \
    "results_gguf_bartowski__Meta-Llama-3.1-8B-Instruct-GGUF_Meta-Llama-3.1-8B-Instruct-Q4_K_M.gguf" \
    run_gcj_metallama_gguf_q4km.log

# --- Original (BF16) -------------------------------------------------------
run aqlm_venv310 original "meta-llama/Meta-Llama-3.1-8B-Instruct" \
    "results_original_meta-llama__Meta-Llama-3.1-8B-Instruct" \
    run_gcj_metallama_original.log

# --- AQLM PV 2-bit ---------------------------------------------------------
run aqlm_venv310 aqlm "ISTA-DASLab/Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf" \
    "results_ISTA-DASLab__Meta-Llama-3.1-8B-Instruct-AQLM-PV-2Bit-1x16-hf" \
    run_gcj_metallama_aqlm_2bit.log

# --- QTIP 2/3/4-bit --------------------------------------------------------
run qtip_venv qtip "relaxml/Llama-3.1-8b-Instruct-QTIP-2Bit" \
    "results_relaxml_Llama-3.1-8b-Instruct-QTIP-2Bit" \
    run_gcj_metallama_qtip_2bit.log
run qtip_venv qtip "relaxml/Llama-3.1-8b-Instruct-QTIP-3Bit" \
    "results_relaxml_Llama-3.1-8b-Instruct-QTIP-3Bit" \
    run_gcj_metallama_qtip_3bit.log
run qtip_venv qtip "relaxml/Llama-3.1-8b-Instruct-QTIP-4Bit" \
    "results_relaxml_Llama-3.1-8b-Instruct-QTIP-4Bit" \
    run_gcj_metallama_qtip_4bit.log

# --- HIGGS-GPTQ 3/4-bit ----------------------------------------------------
run higgs_venv higgs "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit" \
    "results_higgs_llama3.1_8B_3bit" \
    run_gcj_metallama_higgs_3bit.log
run higgs_venv higgs "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit" \
    "results_higgs_llama3.1_8B_4bit" \
    run_gcj_metallama_higgs_4bit.log

echo "[chain] $(date '+%F %T') Meta-Llama-3.1-8B GCJ Java sweep complete"
printf '[chain] %s\n' "${SUMMARY[@]}"
