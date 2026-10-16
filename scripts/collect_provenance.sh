#!/usr/bin/env bash
# Collect provenance and gitignored artefacts from the cluster before access ends.
# Prints each command before running it, and keeps going if one step fails.

cd ~/quantization-clone-study || exit 1
mkdir -p provenance

FAILED=()

step() {
  printf '\n\033[1;34m==> %s\033[0m\n' "$1"
}

run() {
  printf '\033[0;36m$ %s\033[0m\n' "$1"
  if bash -c "$1"; then
    printf '\033[0;32m    ok\033[0m\n'
  else
    printf '\033[0;31m    FAILED (exit %s)\033[0m\n' "$?"
    FAILED+=("$1")
  fi
}

step "Anything else ignored or untracked"
run 'git status --ignored --short > provenance/ignored_and_untracked.txt'

step "HF model revisions actually used"
run 'for d in ~/.cache/huggingface/hub/models--*; do echo "$(basename "$d") $(cat "$d/refs/main" 2>/dev/null)"; done > provenance/hf_revisions.txt'

step "Source-build commits"
run 'for r in llama.cpp qtip; do echo "$r $(git -C $r rev-parse HEAD)"; done > provenance/source_commits.txt'
run 'uv pip show --python higgs_venv/bin/python flute-kernel fast-hadamard-transform >> provenance/source_commits.txt'

step "Missing lock files"
run 'uv pip freeze --python finetune_venv/bin/python > requirements/finetune_venv.lock.txt'
run 'uv pip freeze --python unsloth_venv/bin/python > requirements/unsloth_venv.lock.txt'

step "Patched HIGGS quantizer"
run 'cp higgs_venv/lib/python3.10/site-packages/transformers/quantizers/quantizer_higgs.py provenance/'

step "Hardware and toolchain"
run '{ nvidia-smi -q; nvcc --version; gcc-11 --version; uname -a; lscpu; } > provenance/hardware.txt'

step "Sizes before archiving"
run 'du -sh ocd gcj4.pkl finetune_data finetune_models models 2>/dev/null'

step "Archive data and artefacts"
run 'tar czvf ~/qcs_data.tgz ocd gcj4.pkl finetune_data finetune_models provenance requirements'
run 'tar cvf ~/qcs_selfquant.tar models/*Q2_K*.gguf models/*Q3_K_M*.gguf models/*Q4_K_M*.gguf'

step "Summary"
run 'ls -lh ~/qcs_data.tgz ~/qcs_selfquant.tar provenance'
if [ ${#FAILED[@]} -eq 0 ]; then
  printf '\033[1;32mAll steps succeeded.\033[0m\n'
else
  printf '\033[1;31m%d step(s) failed:\033[0m\n' "${#FAILED[@]}"
  printf '  %s\n' "${FAILED[@]}"
fi
