# 95OS — The Offline Exam Operating System

> **«Syllabus → Learn → Recall → Practice → Result → Lost Marks → Mistakes → Revision → Re-test → Score Progress»**

95OS is a 100% offline-first, deterministic exam-performance operating system designed around one singular objective: helping a serious student systematically close their knowledge gaps and move toward their target score, especially **95%**.

---

## 🏛️ Core Principles & Architecture

1. **100% Offline-First Invariant:**
   - Functions completely under Airplane Mode (Zero Wi-Fi, Zero Cellular, Zero Bluetooth).
   - No login screens, no cloud telemetry, no analytics, no remote database dependencies.
2. **Local Data Sovereignty:**
   - Academic records reside exclusively on the student's device in local Room (SQLite) storage.
3. **Deterministic Academic Algorithms:**
   - No hallucinations, no black-box AI dependencies.
   - Transparent mathematical models for retention risk, recovery potential, priority scoring, and exam readiness.
4. **Calm, High-Ergonomics Instrument:**
   - Designed for deep concentration: Warm Obsidian (`#121110`), Warm Paper (`#F8F8F5`), Graphite, and subtle Amber/Cyan accents.
   - Strictly zero purple, zero dark-navy overload, zero children's gamification or streak stress. Minimum 44dp touch targets throughout.

---

## ⚡ What's New in v2.1.0

### 1. Advanced Exam Intelligence Engine (Features 6–10)
- **Time-to-Marks Intelligence:**
  - Evaluates student weakness, chapter exam weightage, recovery potential, and confidence factors ($W \times E \times I \times C$).
  - Answers *"What is the best use of my next 30/60/90/120 minutes?"* with ranked high-impact chapters.
- **Adaptive Re-Test:**
  - Automatically synthesizes targeted diagnostic tests after exams.
  - Strict distribution: **50% Weak Areas**, **30% Recently Incorrect**, **20% Mixed Revision**.
  - Interactive self-scoring runner that directly resolves entries in the Mistake Bank upon demonstrating mastery.
- **Exam Readiness Simulator:**
  - Realistic timed examination environment matching syllabus blueprints.
  - Live countdown timer, 1-tap question jump navigation palette, "Mark for Review" toggling, accidental submission protection.
  - Multi-factor readiness index evaluating recent scores, simulation accuracy, topic coverage, pacing efficiency, and mistake health.
- **Last-7-Days Mode:**
  - High-intensity final-week sprint dashboard that auto-activates when the exam is $\le 7$ days away.
  - Daily countdown curriculum (Day 7 down to Day 1) featuring *"Today's 3 Most Important Tasks"*, high-weightage priority chapters, and 15-minute spaced recall sprints.
- **95% Command Center:**
  - Redesigned home dashboard giving complete situational awareness within **5 seconds**.
  - Top 95% Target metrics (Current Predicted %, Target %, Gap %, Readiness %, Days Remaining).
  - Primary *"WHAT SHOULD I DO NOW?"* recommendation with 1-tap direct navigation.

### 2. Marks Recovery Engine (Features 1–5)
- **Marks Gap Planner:** Real-time projection against target score with prioritized recovery opportunities.
- **Forgetting Radar:** Memory decay prediction based on SuperMemo SM-2 intervals and ratings (Stable, Watch, At Risk, Critical).
- **Previous Paper Analyzer:** Longitudinal pattern analysis across past tests, pinpointing recurrent loss categories.
- **15-Minute Rescue Mode:** Rapid emergency remediation triage targeting highest-yield quick wins.
- **Recovery Score:** Composite metric measuring actual marks recovered across repeated assessments.

### 3. Core Academic Foundation
- **Syllabus Hierarchy:** Subject $\to$ Chapter $\to$ Topic with 4-stage mastery state machine.
- **SuperMemo SM-2 Recall Engine:** Offline flashcards with Ease Factor, Interval, and repetition tracking.
- **PaperPilot:** Offline exam generator and question bank manager with CSV import/export support.
- **Mistake Bank:** Diagnostic error tracking categorizing Careless, Calculation, Forgotten, and Conceptual errors.

---

## 🛠️ Build & Installation

### Prerequisites
- Android SDK 34 (Android 14)
- Java 17+
- Android Gradle Plugin 8.7+

### Compiling from Source
```bash
# Clone the repository
git clone https://github.com/contactaurawealth-lab/95OS.git
cd 95OS

# Run unit tests
./gradlew testDebugUnitTest

# Assemble Release and Debug APKs
./gradlew assembleRelease assembleDebug
```

Compiled APKs will be located in:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License
Private & Proprietary. All rights reserved.
