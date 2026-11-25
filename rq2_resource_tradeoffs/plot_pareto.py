"""RQ2 figures: approximate weight memory vs. MCC and the Pareto front on GCJ-Java and GCJ cross-language.

The weight memory of a configuration is estimated as N x b / 8 bytes, where N is
the number of parameters and b the nominal bit-width (Equation 1 in the paper).
Reads the majority-vote summaries through results/load_results.py and writes
figures/rq2_pareto.pdf and figures/rq2_pareto_xlang.pdf (and .png), and prints
the configurations on each front.
Run from anywhere: python rq2_resource_tradeoffs/plot_pareto.py
"""
import sys
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent / "results"))
from load_results import load  # noqa: E402

# total parameters in billions (nominal sizes, Llama-4-Scout counts all experts)
PARAMS = {
    "aya-expanse-8b": 8.03, "CodeLlama-7b-Instruct": 6.74, "Codestral-22B-v0.1": 22.2,
    "cogito-v1-preview-llama-8B": 8.03, "DeepSeek-Coder-V2-Lite-Instruct": 15.7,
    "Llama-3.1-8B-Instruct": 8.03, "Llama-4-Scout-17B-16E": 109.0,
    "Qwen2.5-Coder-7B-Instruct": 7.62, "Qwen3.6-27B": 27.0,
}
SHORT = {
    "aya-expanse-8b": "aya-expanse-8B", "CodeLlama-7b-Instruct": "CodeLlama-7B",
    "Codestral-22B-v0.1": "Codestral-22B", "cogito-v1-preview-llama-8B": "cogito-8B",
    "DeepSeek-Coder-V2-Lite-Instruct": "DeepSeek-Coder-V2-Lite", "Llama-3.1-8B-Instruct": "Llama-3.1-8B",
    "Llama-4-Scout-17B-16E": "Llama-4-Scout", "Qwen2.5-Coder-7B-Instruct": "Qwen2.5-Coder-7B",
    "Qwen3.6-27B": "Qwen3.6-27B",
}
GGUF_NAME = {2: "Q2_K", 3: "Q3_K_M", 4: "Q4_K_M"}
# label offsets (points) for the BF16 models of the front models, to avoid overlaps
ORIGINAL_LABEL = {"Codestral-22B BF16": (0, -9, "center"), "Qwen3.6-27B BF16": (6, 0, "left"),
                  "cogito-8B BF16": (0, -9, "center")}
# label offsets (points) for front configurations that lie close together
FRONT_LABEL = {"Qwen2.5-Coder-7B Q2_K": (-6, -8, "right"), "aya-expanse-8B Q2_K": (-6, 8, "right"),
               "Qwen2.5-Coder-7B Q3_K_M": (-6, -8, "right"), "aya-expanse-8B Q3_K_M": (-6, 8, "right"),
               "Qwen2.5-Coder-7B Q4_K_M": (6, -20, "left")}


def configurations(benchmark):
    """[(label, approximate weight memory in GB, MCC)] for every configuration in the paper."""
    points = []
    for (model, technique, bits), metrics in load(benchmark).items():
        if technique == "BF16":
            config = "BF16"
        elif technique == "GGUF":
            config = "Q3_K_S" if model == "Llama-4-Scout-17B-16E" and bits == 3 else GGUF_NAME[bits]
        else:
            config = f"{technique} {bits}-bit"
        points.append((f"{SHORT[model]} {config}", PARAMS[model] * bits / 8, metrics["mcc"]))
    return points


def pareto_front(points):
    """Configurations that no other configuration beats on both memory and MCC."""
    front, best = [], -1.0
    for p in sorted(points, key=lambda p: (p[1], -p[2])):
        if p[2] > best:
            front.append(p)
            best = p[2]
    return front


def plot(benchmark, title, outname):
    points = configurations(benchmark)
    front = pareto_front(points)
    on_front = {p[0] for p in front}

    FRONT, OTHER, ORIGINAL = "#2a78d6", "#8d8c87", "#e34948"
    INK, MUTED, GRID = "#0b0b0b", "#52514e", "#e4e3df"
    plt.rcParams.update({"font.size": 8, "font.family": "serif"})
    fig, ax = plt.subplots(figsize=(5.2, 3.2))
    ax.set_xscale("log")
    front_models = {p[0].rsplit(" ", 1)[0] for p in front}
    for name, mem, mcc in points:
        if name in on_front:
            continue
        if name.endswith(" BF16") and name.rsplit(" ", 1)[0] in front_models:
            ax.scatter(mem, mcc, s=30, color=ORIGINAL, edgecolor="white", linewidth=0.6, zorder=3)
            dx, dy, ha = ORIGINAL_LABEL.get(name, (6, 0, "left"))
            ax.annotate(name, (mem, mcc), xytext=(dx, dy), textcoords="offset points",
                        ha=ha, va="center", fontsize=6.5, color=INK)
        else:
            ax.scatter(mem, mcc, s=24, color="white", edgecolor=OTHER, linewidth=1.1, zorder=2)
    for name, mem, mcc in front:
        ax.scatter(mem, mcc, s=40, color=FRONT, edgecolor="white", linewidth=0.6, zorder=3)
        dx, dy, ha = FRONT_LABEL.get(name, (-6, 8 if mcc > 0.85 else 0, "right"))
        ax.annotate(name, (mem, mcc), xytext=(dx, dy), textcoords="offset points",
                    ha=ha, va="center", fontsize=6.5, color=INK, fontweight="bold")
    ax.scatter([], [], s=40, color=FRONT, label="Pareto front")
    ax.scatter([], [], s=30, color=ORIGINAL, edgecolor="white", linewidth=0.6, label="Original (BF16)")
    ax.scatter([], [], s=24, color="white", edgecolor=OTHER, linewidth=1.1, label="Other configurations")
    ax.set_xlim(0.2, 300)
    ax.set_xticks([1, 2, 4, 8, 16, 32, 64, 128, 256], ["1", "2", "4", "8", "16", "32", "64", "128", "256"])
    ax.minorticks_off()
    ax.set_ylim(-0.03, 1.03)
    ax.set_xlabel("Approximate weight memory (GB, log scale)")
    ax.set_ylabel(f"MCC ({title})")
    ax.grid(True, color=GRID, linewidth=0.5)
    ax.set_axisbelow(True)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    for s in ("left", "bottom"):
        ax.spines[s].set_color(MUTED)
    ax.tick_params(colors=MUTED, length=2)
    ax.legend(loc="lower center", bbox_to_anchor=(0.5, 1.02), ncol=3, frameon=False, fontsize=7,
              handletextpad=0.3, columnspacing=1.2)
    fig.tight_layout()
    out = HERE / "figures" / outname
    out.parent.mkdir(exist_ok=True)
    fig.savefig(out, bbox_inches="tight")
    fig.savefig(out.with_suffix(".png"), dpi=200, bbox_inches="tight")
    print(title, len(points), "configurations")
    for p in front:
        print("front:", p)


plot("gcj_java", "GCJ-Java", "rq2_pareto.pdf")
plot("gcj_crosslang", "GCJ cross-language", "rq2_pareto_xlang.pdf")
