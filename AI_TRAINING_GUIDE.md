# 🧠 EVENTLOOP AI – Architecture, Training & Fine-Tuning Guide

This guide explains how the **EventLoop AI Copilot & Event Budget Optimizer** is architected, how it avoids hallucinations, and how it is trained and fine-tuned for institutional procurement and logistics optimization.

---

## 🏗️ 1. Why EventLoop AI is Not a "Toy Chatbot" (3-Tier Production Architecture)

Standard AI chatbots hallucinate prices and guess inventory availability.  
**EventLoop AI uses a 3-Tier Enterprise Architecture:**

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Tier 3: Generative AI Agent Layer                     │
│  • Natural Language Intent Extraction (regex + semantic entity parser)      │
│  • Multi-turn Dialogue Management & Conversational Advice                   │
│  • Pluggable API: Google Gemini 1.5 / OpenAI GPT-4o / Ollama Local Llama 3  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Tool Calls / Queries
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                  Tier 2: Institutional Domain Knowledge (RAG)               │
│  • Campus Procurement Thresholds & Tender Rules                             │
│  • Inter-Department Lending Agreements (CS, ECE, Mechanical, Cultural)      │
│  • Safety Verification Policies (15A Electrical spools, fire retardancy)   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Constraints & DB Queries
┌──────────────────────────────────────▼──────────────────────────────────────┐
│           Tier 1: Deterministic Multi-Constraint Sourcing Solver (Java)     │
│  • Exact Knapsack Optimization & Mathematical Cost Accounting               │
│  • Live SQLite Database Querying (719+ verified assets)                     │
│  • 10-Point Compatibility Verification Gates                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 🔑 Key Advantage:
- **Zero Hallucination:** The dollar/rupee math is calculated by **Tier 1 (Java Solver)** directly from verified SQLite records. The LLM only translates requirements and explains the strategy in natural human language!

---

## 💰 2. How the Event Budget Optimizer Works

When an organizer submits:
```text
Budget: ₹50,000
Requirements: 20 chairs, 3 projectors, 10 cables, 5 banners, 200 badges
```

The system dynamically synthesizes **3 competing procurement strategies**:

### 🔴 Plan A — Buy Everything New (Max Convenience / High Drain)
- **Cost:** ~₹48,500 (97% budget spent, ₹1,500 left)
- **Strategy:** Procures 100% brand-new retail items from market suppliers.
- **Lead Time:** 7 Days
- **Feasibility:** 65/100 (High expenditure, zero campus reuse).

### 🔵 Plan B — Reuse + Repair + Rent (Balanced Hybrid)
- **Cost:** ~₹17,800 (35% budget spent, ₹32,200 preserved)
- **Strategy:** Reuses warehouse chairs & cables, pays minor workshop repair for broken projectors/mics, and rents deficit items from approved campus vendors.
- **Lead Time:** 3 Days
- **Feasibility:** 88/100.

### 🟢 Plan C — Reuse + Borrow + Strategic Buy (Max Economy / Recommended)
- **Cost:** ~₹14,600 (29% budget spent, ₹35,400 preserved)
- **Strategy:** 
  1. 100% Free Campus Reuse of banquet tables, chairs, and cables from central warehouse.
  2. Inter-department MoU loans: Borrow 2 high-lumen projectors from CS Lab 3 & ECE Dept (₹0).
  3. Repurposes existing metal standee frames (reprinting flex skin only for ₹350 instead of buying new ₹1,200 frames).
  4. Only purchases non-reusable custom attendee delegate badges (negotiated bulk @ ₹32).
- **Lead Time:** 2 Days
- **Feasibility:** 96/100.

---

## 🎓 3. How to Train & Fine-Tune the AI Model

To fine-tune an open-source model (e.g. **Llama-3-8B-Instruct** or **Mistral-7B**) or OpenAI model for EventLoop, use the supervised instruction format (`train.jsonl`).

### Sample Training Pairs (`data/train.jsonl`):

```json
{"messages": [{"role": "system", "content": "You are EventLoop AI, an institutional procurement optimizer for universities. Your mission is to minimize procurement expense by prioritizing campus asset reuse, inter-department borrowing, and repairs over new purchases."}, {"role": "user", "content": "We are hosting an AI symposium with ₹50,000 budget. Need 20 chairs, 3 projectors, 10 extension cables, 5 banners, and 200 delegate badges. Give me options."}, {"role": "assistant", "content": "Analyzing campus inventory and procurement constraints. I have generated 3 scenarios:\n- Plan A (Buy All): ₹48,500 (Preserves 3% of budget)\n- Plan B (Hybrid): ₹17,800 (Preserves 64% of budget)\n- Plan C (Max Economy): ₹14,600 (Preserves 71% of budget, saving ₹35,400)\nRecommendation: Adopt Plan C by borrowing 2 projectors from CS Lab 3 and reusing metal standee frames."}]}
```

```json
{"messages": [{"role": "system", "content": "You are EventLoop AI. Enforce the mandatory Verification Required Gate strictly."}, {"role": "user", "content": "Can I reserve extension cable EL-ELE-101 for tomorrow's concert?"}, {"role": "assistant", "content": "Reservation Blocked ⚠️. Resource EL-ELE-101 has an EXPIRED physical safety audit. High-amperage cables without periodic insulation testing pose electrical fire hazards. Please request the Store Manager to physically certify this cable before booking."}]}
```

---

## 🔌 4. How to Connect Google Gemini or OpenAI (Optional)

EventLoop has an intelligent offline reasoning engine built-in. If you want to connect live cloud LLMs:

### Option A: Google Gemini 1.5 Flash (Free Tier)
Set the environment variable in your terminal or cloud host (Render):
```bash
set GEMINI_API_KEY=AIzaSy...
```

### Option B: Local Private AI with Ollama (100% Free & Offline)
1. Download Ollama from [https://ollama.ai](https://ollama.ai).
2. Run Llama 3 locally:
   ```bash
   ollama run llama3
   ```
3. EventLoop automatically queries `http://localhost:11434/api/generate`!

---

## 🎙️ 5. Evaluator / Viva Defense Questions

**Q1: How does your AI ensure budget numbers are accurate and not hallucinated?**  
*Answer:* "We separate the reasoning layers: the mathematical optimization is handled by `BudgetOptimizerService.java` using deterministic inventory formulas, while the natural language interface parses intent and communicates the rationale."

**Q2: What optimization algorithm is used?**  
*Answer:* "A multi-choice Knapsack algorithm with heuristic priority weights: `Reuse (Weight: 1.0) > Borrow (0.9) > Repair (0.7) > Rent (0.5) > Buy (0.1)` subject to deadline and condition rating constraints."

**Q3: Can this work for other institutions?**  
*Answer:* "Yes, the benchmark prices and institutional rules in `BudgetOptimizerService` are configurable and can be adapted to hospitals, NGOs, corporate offices, and universities."
