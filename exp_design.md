# Ordering Ablation: Quantize-then-Fine-tune vs. Fine-tune-then-Quantize

Experimental design for measuring whether the **order** of quantization and
fine-tuning matters for LLM code-clone detection, and by how much as bit-width drops.

> **Key constraint that shapes the whole design:** standard QLoRA (bitsandbytes)
> only supports **4-bit and 8-bit**. You cannot train a LoRA adapter on a 2-bit or
> 3-bit bnb base. So the clean A-vs-B ordering comparison lives at 4-bit; at 2–3 bit
> the arms available are different (see §3).

## 1. Question & hypotheses

**Q:** For code-clone detection, does the order of quantization vs. fine-tuning
matter, and how does the gap behave as bits drop?

- **H1** — At 4-bit, **B (quantize→finetune) ≥ A (finetune→quantize)**: the adapter,
  trained against the quantized frozen base, compensates for quantization error.
- **H2** — **Merging + requantizing** a QLoRA model (B′) loses part of B's advantage,
  because requantization re-introduces the error the adapter learned to cancel.
- **H3** — The A-vs-B gap **widens as bits drop** (2–3 bit), where quantization error
  is larger.

## 2. The arms

Fine-tuning method is held fixed (**LoRA, identical config everywhere**); only the
*order* of quantization relative to fine-tuning changes.

| Arm | Pipeline | Deploy artifact |
| --- | --- | --- |
| **C0** | FP16 base, no fine-tune | — (reference ceiling-ish) |
| **C1** | quantize only, no fine-tune | quantized GGUF — *already have (plain sweeps)* |
| **C2** | FP16 + LoRA, no quantize | merged FP16 — *(fine-tune ceiling)* |
| **A** | FP16 → LoRA → merge → **quantize** | single quantized GGUF |
| **B** | **quantize** → QLoRA → keep adapter separate | quantized base + FP16 adapter — *your `gguf_lora`* |
| **B′** | B → merge → requantize | single quantized GGUF — *(isolates merge cost)* |

## 3. Bit-width feasibility (the crux)

| Bit-width | A (FT→quant) | B (true QLoRA) | B′ (B→merge→requant) |
| --- | --- | --- | --- |
| **Q4_K_M** | ✅ | ✅ bnb-NF4 | ✅ |
| **Q3_K_M** | ✅ | ⚠️ no bnb 3-bit — needs AQLM/HQQ | ✅ (from 4-bit-trained adapter) |
| **Q2_K** | ✅ | ⚠️ no bnb 2-bit — needs AQLM | ✅ |

Consequences:

- The **clean A-vs-B ordering ablation is at 4-bit.**
- At **2–3 bit**, A is the practically important arm; compare it against **C1**
  (zero-shot quant) to show "fine-tune first, then quantize low" recovers accuracy
  *without* low-bit training.
- A true low-bit **B** requires switching that arm to **AQLM** (2-bit + PEFT support)
  — worth it only if H3 is the headline result.

**Confound to note in current B:** your QLoRA trains on **bnb-NF4** but deploys the
adapter on a **llama.cpp Q4_K_M** base — different 4-bit schemes (train/infer
quant-family mismatch). For a clean result, either document this or match the
training and serving quantization.

## 4. Factors held fixed (confound control)

Otherwise you confound "order" with something else:

- **Identical LoRA config** across A and B: rank `r`, `alpha`, dropout,
  `target_modules`, LR, epochs, batch size, **seed**, and the same training split.
- **Same quantization settings** everywhere (no imatrix everywhere, or the same
  imatrix everywhere).
- **Same evaluation**: 5-round majority vote, same prompt, same pairs.

## 5. Datasets, splits, leakage

Keep the existing leakage-safe design: **train on AIZU (SimilBench), test on
GCJ-Java / GCJ-XLang / OCD** — cross-dataset, so no problem/pair overlap by
construction.

- Report all three test sets, but **rank the arms on the balanced GCJ sets**, not
  OCD: OCD's n×n product is heavily imbalanced (~10% clone pairs), which distorts
  MCC (we already saw non-monotonic Q3 > Q4 there for the plain Qwen3 sweep).
- Fix the adapter's training data to **monolingual AIZU384F** for the main ablation
  (already have it); the cross-language **AIZU324CLF** variant is an optional
  secondary factor, not part of the core grid.

## 6. Models

Use the existing QLoRA trio — a clean difficulty spread:

- **CodeLlama-7b** — degenerate baseline (MCC 0.00) → the dramatic-rescue story
- **Qwen2.5-Coder-7B** — strong baseline → the near-ceiling story
- **Llama-3.1-8B** — weak-but-not-degenerate → the middle

## 7. Result table (per model; balanced GCJ-Java shown, repeat for XLang/OCD)

```
                       Q4_K_M      Q3_K_M      Q2_K
  C1  quant, no FT      ....        ....        ....
  A   FT -> quant       ....        ....        ....
  B   QLoRA (sep adpt)  0.955*      (AQLM)      (AQLM)
  B'  QLoRA->merge->req  ....        ....        ....
  ------------------------------------------------------
  C0 FP16 no FT: ....    C2 FP16+LoRA: ....   (bit-independent refs)
```

`*` = already have. Headline comparisons:

- **A vs B** (order effect, at Q4)
- **B vs B′** (merge cost)
- **A/B vs C1** (how much fine-tuning recovers of the quantization loss)

## 8. What we already have vs. need to run

- ✅ **C0, C1** — FP16 and plain-quant sweeps (done for these models).
- ✅ **B at Q4** — the `gguf_lora` results (GCJ-Java + XLang; add OCD if wanted).
- 🔲 **Need:**
  - **C2** — merge adapter → FP16, evaluate (fine-tune ceiling).
  - **A** — merge → `llama-quantize` to Q4/Q3/Q2 (the new GGUF-production pipeline
    does exactly this).
  - **B′** — merge → requantize.

The delta is small because both halves are already built (QLoRA + GGUF production).

## 9. Phased plan

1. **Phase 1** (cheapest, highest signal) — Arm **A at Q4** for the 3 models on
   GCJ-Java + XLang. Compare directly to existing B-Q4 → immediate A-vs-B answer.
2. **Phase 2** — Add **B′-Q4** (merge cost) and **C2** (fine-tune ceiling).
3. **Phase 3** — Extend A to **Q3/Q2** → the fine-tune-then-quantize-low recovery
   curve vs C1.
4. **Phase 4** (optional) — True low-bit **B via AQLM** if H3 needs nailing.

Phase 1 alone is a clean, reportable result.

## 10. Open decisions

- Train/infer quant-family match for B (bnb-NF4 vs llama.cpp K-quants) — accept and
  document, or match?
- Include AIZU324CLF (cross-language training) as a secondary factor, or hold fixed?
- Whether to invest in the AQLM low-bit B arm (Phase 4) for H3.
