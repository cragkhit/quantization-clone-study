"""RQ1 figure: MCC of each quantization technique at equal bit-width (Llama-3.1-8B-Instruct).

Reads the majority-vote summaries in ../tables/ through results/load_results.py
and writes rq1_technique_bar.pdf and .png next to this script.
Run from anywhere: python rq1_effect_of_quantization/figures/plot_technique.py
"""
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / "results"))
from load_results import load  # noqa: E402

MODEL = "Llama-3.1-8B-Instruct"
BENCHMARKS = [("ocd", "OCD"), ("gcj_java", "GCJ-Java"), ("gcj_crosslang", "GCJ cross-language")]
TECHNIQUES = ["AQLM", "GGUF", "HIGGS-GPTQ", "QTIP"]
BITS = [2, 3, 4]

data = [load(b) for b, _ in BENCHMARKS]
BF16 = [d[(MODEL, "BF16", 16)]["mcc"] for d in data]
# technique -> {bits: [MCC per benchmark]}, only for the bit-widths that were evaluated
RESULTS = {
    t: {b: [d[(MODEL, t, b)]["mcc"] for d in data] for b in BITS if (MODEL, t, b) in data[0]}
    for t in TECHNIQUES
}

COLOURS = {"AQLM": "#2a78d6", "GGUF": "#eb6834", "HIGGS-GPTQ": "#1baf7a", "QTIP": "#eda100"}
HATCHES = {"AQLM": "", "GGUF": "//", "HIGGS-GPTQ": "\\\\", "QTIP": ".."}
INK, MUTED, GRID = "#0b0b0b", "#52514e", "#e4e3df"

plt.rcParams.update({"font.size": 8, "font.family": "serif", "hatch.color": "white", "hatch.linewidth": 0.6})
fig, axes = plt.subplots(1, 3, figsize=(7.0, 2.4), sharey=True)
width = 0.2

for d, ax in enumerate(axes):
    for i, bits in enumerate(BITS):
        present = [t for t in RESULTS if bits in RESULTS[t]]
        offsets = (np.arange(len(present)) - (len(present) - 1) / 2) * (width + 0.02)
        for t, off in zip(present, offsets):
            ax.bar(i + off, RESULTS[t][bits][d], width, color=COLOURS[t], hatch=HATCHES[t], edgecolor="white",
                   linewidth=0.5, label=t if (d == 0 and i == 0) or (d == 0 and t == "HIGGS-GPTQ" and i == 1) else None)
    ax.axhline(BF16[d], color=MUTED, linestyle="--", linewidth=0.9, zorder=0)
    ax.text(-0.4, BF16[d] + 0.01, f"BF16 ({BF16[d]:.3f})", ha="left", va="bottom", fontsize=6, color=MUTED)
    ax.set_title(BENCHMARKS[d][1], fontsize=8, color=INK)
    ax.set_xticks(range(len(BITS)), [f"{b}-bit" for b in BITS])
    ax.set_ylim(0, 0.56)
    ax.yaxis.grid(True, color=GRID, linewidth=0.5)
    ax.set_axisbelow(True)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    for s in ("left", "bottom"):
        ax.spines[s].set_color(MUTED)
    ax.tick_params(colors=MUTED, length=2)

axes[0].set_ylabel("MCC")
handles, labels = axes[0].get_legend_handles_labels()
order = [labels.index(t) for t in RESULTS]
fig.legend([handles[i] for i in order], [labels[i] for i in order], loc="upper center", ncol=4,
           frameon=False, bbox_to_anchor=(0.5, 1.04), fontsize=7)
fig.tight_layout(rect=(0, 0, 1, 0.92))
out = Path(__file__).with_name("rq1_technique_bar.pdf")
fig.savefig(out, bbox_inches="tight")
fig.savefig(out.with_suffix(".png"), dpi=200, bbox_inches="tight")
print("BF16:", BF16)
for t, r in RESULTS.items():
    print(t, r)
