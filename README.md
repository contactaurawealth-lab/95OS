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

## ⚡ What's New in v2.3.0

### 1. Database Sovereignty & Recovery Engine
- **Raw SQLite Backup & Restore (.95os):** Full offline database backup and restore via Storage Access Framework with WAL flushing and binary header validation.
- **Session Navigation Safety:** Jetpack Compose `BackHandler` protections for active simulator, retest, and focus sessions.
- **Syllabus Batch Mastery Progression:** "Revise All" and "Master All" chapter-level one-tap batch actions.
- **Printable Revision Documents:** Generates clean, printer-ready A4 PDF and Markdown exports for Mistake Remediation Sheets and Forgetting Flash Sheets.
- **Longitudinal Study Consistency Heatmap:** 70-day (10-week) visual study grid in Progress tracking daily volume and streaks.
- **Target Score Sensitivity Calculator:** Dynamic slider simulating projected score and marks gap impact in real time.

### 2. Exam Day Protocol & Formula Reference
- **Formula & Key Definition Vault:** Offline vault for high-yield equations, laws, and definitions with subject filtering and monospace formatting.
- **"T-Minus 3 Hours" Exam Day Protocol:** Pre-exam logistics checklist, departure/arrival timeline, and 5-card cognitive priming with interactive 4-4-4-4 box breathing.
- **100% Offline Procedural Focus Audio Generator:** Real-time PCM audio synthesis using Android `AudioTrack` (Brownian noise, Pink noise, and Analog Exam Clock Tick) with 0 KB asset files.

### 3. Advanced Exam Intelligence Engine (Features 6–10)
- **Time-to-Marks Intelligence:** Evaluates student weakness, chapter exam weightage, recovery potential, and confidence factors ($W \times E \times I \times C$). Answers *"What is the best use of my next 30/60/90/120 minutes?"*.
- **Adaptive Re-Test:** Targeted diagnostic tests with 50% weak areas, 30% recently missed, and 20% mixed revision.
- **Exam Readiness Simulator:** Timed simulation with countdown timer, question jump palette, and multi-factor readiness score.
- **Last-7-Days Mode:** Final-week countdown sprint with daily priority tasks and adaptive schedule adjustment.
- **95% Command Center:** Primary dashboard providing 5-second situational awareness and a single recommended action.

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
