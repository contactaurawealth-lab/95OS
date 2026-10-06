# 95OS v2.3.0 — Academic Printing, Database Sovereignty, Formula Vault, Exam Day Protocol & Procedural Acoustics

**95OS** is the 100% offline, deterministic exam operating system engineered to help students systematically move toward their target score, especially 95%.

Version 2.3.0 is a milestone release delivering complete academic data sovereignty, physical exam printouts, visual consistency analytics, formula reference vaulting, zero-panic exam morning protocols, and procedural offline cognitive audio.

---

### 🌟 What's New in v2.3.0

#### 1. Full Database Sovereignty Backup & Restore (.95os)
- **Local SQLite Export/Import:** Export the entire raw Room SQLite database into a `.95os` binary backup using Android's Storage Access Framework (SAF).
- **WAL Flush & Validation:** Executes `PRAGMA wal_checkpoint(FULL);` prior to export, and performs SQLite binary header validation (`SQLite format 3`) prior to import.
- **Safe Atomic Replacement:** Backs up the current database as `.old` before swapping, ensuring zero risk of student data corruption.
- **Zero Cloud Dependence:** Complete offline data portability allowing seamless transfers between devices without accounts, servers, or internet.

#### 2. Session Navigation Safety & BackHandler Protection
- **Session Abort Confirmation:** Jetpack Compose `BackHandler` guards active sessions in `ExamSimulatorScreen`, `AdaptiveRetestScreen`, and `FocusScreen`.
- **Zero Accidental Progress Loss:** Dedicated modal dialogs prevent accidental swipes or back-presses from terminating running exams, timed re-tests, or focus sessions.

#### 3. Syllabus Batch Mastery Progression
- **High-Throughput Chapter Updates:** Added "Revise All" and "Master All" batch actions to Chapter headers in `SyllabusScreens.kt`.
- **Atomic Bulk Operations:** Batch updates all child topics in a chapter to `REVISED` or `MASTERED` state via an optimized SQLite query.

#### 4. Printable Revision Documents & Physical Remediation Sheets
- **PDF & Markdown Generators:** Generates clean, printer-ready A4 PDFs and structured Markdown documents for offline study binders:
  1. **Mistake Remediation & Lost Marks Analysis Sheet:** Detailed analysis grouped by root-cause loss category (Conceptual, Calculation, Misread, Careless, Time Shortage) with marks lost and corrective actions.
  2. **High-Yield Forgetting Flash Sheet:** Active recall retention radar export grouping topics by decay risk (`CRITICAL`, `AT_RISK`, `WATCH`, `STABLE`).

#### 5. Longitudinal Study Consistency & Mastery Heatmap
- **70-Day (10-Week) Study Grid:** Interactive visual heatmap in `ProgressScreen` inspired by academic consistency matrices.
- **Volume Classification:** Color-coded focus duration blocks (0m, 1–30m, 31–60m, 61–120m, 120m+) with consecutive streak tracking.

#### 6. Target Score Sensitivity ("What-If") Calculator
- **Dynamic Marks Sensitivity:** Interactive "What-If" slider in `ProgressScreen` powered by `Target95Engine`.
- **Real-Time Projection:** Allows students to simulate the exact impact of recovering 1 to 15 marks on their projected percentage and remaining gap to 95%.

#### 7. Formula & Key Definition Vault
- **High-Yield Equation Management:** Dedicated Formula Vault (`FormulaEntity`, `FormulaDao`, `FormulaRepository`, `FormulaVaultScreen`, `FormulaVaultViewModel`).
- **Subject & Chapter Filtering:** Rapid filtering across subjects, chapters, and exam relevance levels (`HIGH`, `MEDIUM`, `LOW` yield).
- **Monospace Display & Bookmarking:** High-contrast monospace display boxes, explanation notes, and one-tap star bookmarking for quick pre-exam review.

#### 8. "T-Minus 3 Hours" Exam Day Protocol
- **Zero-Panic Readiness Hub:** Specialized pre-exam operational suite with three modules:
  1. **Logistics Checklist:** Physical verification for admit card, photo ID, tested pens, analog wristwatch, transparent pouch, and geometry kit.
  2. **T-Minus Pacing Timeline:** Paced milestones from T-3:00 nutrition to T-0:15 deskside preparation.
  3. **Cognitive Priming Anchors:** Includes an interactive Box Breathing (4-4-4-4) visualizer, First-Pass Paper Scan protocol (10 min), strict Time-to-Mark budgeting, and the Final 15-Minute Audit.

#### 9. 100% Offline Procedural Focus Audio Generator
- **Zero-Asset Audio Synthesis:** Real-time procedural PCM audio generation using Android `AudioTrack` (16-bit mono 44.1 kHz).
- **0 KB Disk Footprint:** 100% offline synthesis without MP3/WAV files, fully operational under Airplane Mode.
- **Three Academic Acoustic Modes:**
  - **Brownian Noise:** Deep, low-frequency rumble for intense mathematical problem-solving.
  - **Pink Noise:** Balanced 1/f spectral density for cognitive endurance and memory consolidation.
  - **Analog Exam Clock:** 1 Hz mechanical pulse simulating an exam hall environment.

---

### 🛡️ Core 95OS Invariants Verified
- **100% Offline-First:** Fully functional under Airplane Mode. Zero cloud databases, zero login screens, zero tracking.
- **Room SQLite Integrity:** Foreign keys enforced (`PRAGMA foreign_keys = ON;`). Version 3 schema with destructive migration fallback.
- **Deterministic Analytics:** Transparent mathematical models for all scores, readiness metrics, and recovery estimates.
- **Design Restraint:** Strict 44dp minimum touch targets, clean typographic hierarchy, **Zero Purple**, **Zero Dark Navy**.
- **95/95 Unit Tests Passing:** 100% successful test suite across all engines, repositories, and UI models.
