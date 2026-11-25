"""RQ1 figure: change in MCC relative to BF16 on GCJ-Java and GCJ cross-language.

Reads the majority-vote summaries in ../tables/ through results/load_results.py
and writes rq1_xlang_bar.pdf and .png next to this script.
Run from anywhere: python rq1_effect_of_quantization/figures/plot_cross_language.py
"""
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / "results"))
from load_results import load  # noqa: E402

# label in the figure -> (model, technique of the quantized variants)
ROWS = {
    "aya-expanse-8B":            ("aya-expanse-8b", "GGUF"),
    "CodeLlama-7B":              ("CodeLlama-7b-Instruct", "GGUF"),
    "Codestral-22B":             ("Codestral-22B-v0.1", "GGUF"),
    "cogito-8B":                 ("cogito-v1-preview-llama-8B", "GGUF"),
    "DeepSeek-Coder-V2-Lite":    ("DeepSeek-Coder-V2-Lite-Instruct", "GGUF"),
    "Llama-3.1-8B (GGUF)":       ("Llama-3.1-8B-Instruct", "GGUF"),
    "Llama-3.1-8B (AQLM)":       ("Llama-3.1-8B-Instruct", "AQLM"),
    "Llama-3.1-8B (HIGGS-GPTQ)": ("Llama-3.1-8B-Instruct", "HIGGS-GPTQ"),
    "Llama-3.1-8B (QTIP)":       ("Llama-3.1-8B-Instruct", "QTIP"),
    "Llama-4-Scout-17B-16E":     ("Llama-4-Scout-17B-16E", "GGUF"),
    "Qwen2.5-Coder-7B":          ("Qwen2.5-Coder-7B-Instruct", "GGUF"),
    "Qwen3.6-27B":               ("Qwen3.6-27B", "GGUF"),
}


def series(data, model, technique):
    """[BF16, 2-bit, 3-bit, 4-bit] MCC, with None for bit-widths that were not evaluated."""
    get = lambda t, b: data[(model, t, b)]["mcc"] if (model, t, b) in data else None
    return [get("BF16", 16)] + [get(technique, b) for b in (2, 3, 4)]


java, xlang = load("gcj_java"), load("gcj_crosslang")
DATA = {label: (series(java, *key), series(xlang, *key)) for label, key in ROWS.items()}
SERIES = [("GCJ-Java", "#2a78d6", ""), ("GCJ cross-language", "#eb6834", "//")]
INK, MUTED, GRID = "#0b0b0b", "#52514e", "#e4e3df"

plt.rcParams.update({"font.size": 8, "font.family": "serif", "hatch.color": "white", "hatch.linewidth": 0.6})
models = list(DATA)
y = np.arange(len(models))[::-1]
h = 0.36
fig, axes = plt.subplots(1, 3, figsize=(7.0, 3.6), sharey=True, sharex=True)

for b, ax in enumerate(axes, start=1):
    for k, (name, colour, hatch) in enumerate(SERIES):
        for yi, m in zip(y, models):
            vals = DATA[m][k]
            off = h / 2 + 0.02 if k == 0 else -(h / 2 + 0.02)
            if vals[b] is None:
                if k == 0:
                    ax.text(0.01, yi, "n/a", va="center", ha="left", fontsize=6, color=MUTED)
                continue
            ax.barh(yi + off, vals[b] - vals[0], h, color=colour, hatch=hatch, edgecolor="white",
                    linewidth=0.5, label=name if (b == 1 and yi == y[0]) else None)
    ax.axvline(0, color=MUTED, linewidth=0.8)
    ax.set_title(f"{b + 1}-bit", fontsize=8, color=INK)
    ax.set_xlim(-0.7, 0.25)
    ax.set_xticks([-0.6, -0.4, -0.2, 0, 0.2])
    ax.xaxis.grid(True, color=GRID, linewidth=0.5)
    ax.set_axisbelow(True)
    for s in ("top", "right", "left"):
        ax.spines[s].set_visible(False)
    ax.spines["bottom"].set_color(MUTED)
    ax.tick_params(colors=MUTED, length=2)
    ax.tick_params(axis="y", length=0)

axes[0].set_yticks(y, models)
axes[0].tick_params(axis="y", colors=INK, labelsize=7)
axes[1].set_xlabel(r"$\Delta$ MCC relative to BF16", fontsize=8, color=INK)
handles, labels = axes[0].get_legend_handles_labels()
fig.legend(handles, labels, loc="upper center", ncol=2, frameon=False, bbox_to_anchor=(0.6, 1.03), fontsize=7)
fig.tight_layout(rect=(0, 0, 1, 0.94))
out = Path(__file__).with_name("rq1_xlang_bar.pdf")
fig.savefig(out, bbox_inches="tight")
fig.savefig(out.with_suffix(".png"), dpi=200, bbox_inches="tight")
for m, v in DATA.items():
    print(f"{m:28s} {v}")
