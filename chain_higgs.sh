#!/usr/bin/env bash
# Run HIGGS-GPTQ 3-bit -> 4-bit to 5 rounds each on GPU 7.
# HIGGS requirements (see exp_notes.md):
#   - CUDA_VISIBLE_DEVICES pinned to ONE low-load GPU (FLUTE template tuning
#     mis-selects under contention -> garbage output). GPU 7 is idle.
#   - gcc-11 for fast_hadamard_transform CUDA kernels.
#   - quantizer_higgs.py patch already applied in higgs_venv.
# Each run_quantization call resumes from existing round CSVs.
# -e + pipefail so a failed python run ABORTS the chain instead of silently
# advancing to the next model (which previously left an orphaned run behind).
set -euo pipefail
cd /home/chaiyong.rag/quantization-clone-study
mkdir -p logs

# Guard against launching a second chain while one is already running.
if pgrep -f "run_quantization.py higgs" >/dev/null 2>&1; then
  echo "[chain] ABORT: a HIGGS run is already active; refusing to start a duplicate."
  exit 1
fi

run() {  # $1=model_id  $2=output_base  $3=logfile
  echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') starting $1"
  CUDA_VISIBLE_DEVICES=7 CC=gcc-11 CXX=g++-11 FLUTE_NUM_SMS=108 \
    higgs_venv/bin/python run_quantization.py higgs \
    "$1" \
    --tests-dir ocd/tests \
    --output "$2" \
    --rounds 5 2>&1 | tee -a "logs/$3"
  echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') finished $1"
}

run "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-3bit" \
    "results/Meta-Llama-3.1-8B-Instruct/results_higgs_llama3.1_8B_3bit" \
    run_higgs_3bit_5rounds.log

run "ISTA-DASLab/Llama-3.1-8B-Instruct-HIGGS-GPTQ-4bit" \
    "results/Meta-Llama-3.1-8B-Instruct/results_higgs_llama3.1_8B_4bit" \
    run_higgs_4bit_5rounds.log

echo "[chain] $(date '+%Y-%m-%d %H:%M:%S') HIGGS sweep complete"
