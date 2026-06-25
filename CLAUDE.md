# Claude Code Instructions

## exp_notes.md maintenance

Keep `exp_notes.md` up to date whenever:
- A new model backend (`load_X` function) is added or materially changed in `run_quantization.py`
- The user explicitly asks for an update

When updating, document:
- The HuggingFace model URL(s) (use markdown links)
- Virtual environment and pip install steps
- The exact `run_quantization.py` command used to run the experiment
- Any non-obvious workarounds (version pins, patches, env vars, etc.)

After updating `exp_notes.md`, commit **only that file** with a short descriptive message.
