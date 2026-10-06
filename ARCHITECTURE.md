# 95OS — Technical Architecture & Engineering Specification

## 1. High-Level Architecture Pattern
95OS strictly implements Clean Architecture with Unidirectional Data Flow (UDF) powered by Jetpack Compose and Kotlin Coroutines:
```
Compose UI (Screens, Design Tokens & Components)
       ↓ (User Intents / Events)
StateFlow-Driven ViewModels
       ↓ (Invokes)
Domain Layer (Use Cases, Models & Deterministic Engines)
       ↓ (Mediated by)
Domain Repositories (Interfaces)
       ↓ (Implemented in Data Layer)
Room Database (SQLite) & DataStore Preferences
       ↓
Local Device Storage
```

- **Dependency Injection:** Central, manual DI container (`OS95AppContainer`) hosted on `OS95Application` without heavy reflection or build-time DI bloat.
- **Offline Sovereignty:** Zero cloud dependencies, zero mandatory external APIs, zero remote authentication.

---

## 2. Multi-Level Navigation Architecture

95OS uses structured multi-level navigation boundaries to match the student's mental model (*Where am I? What am I working on? What should I do next?*):

```
App Root
│
├── Root Navigation (Primary Tabs & Drawer)
│   ├── Home (Command Center)
│   ├── Syllabus (Subjects & Mastery)
│   ├── Recall (Active Spaced Repetition)
│   ├── Papers (PaperPilot Hub)
│   ├── Mistakes (Mistake Bank & Lost Marks)
│   └── Progress (95% Engine & Analytics)
│
├── Secondary Navigation (Contextual Hierarchies)
│   ├── Subject Navigation
│   │   ├── Chapters
│   │   ├── Topics
│   │   └── Performance Breakdown
│   │
│   ├── Paper Navigation
│   │   ├── Builder
│   │   ├── Preview / Printable View
│   │   ├── Exam Mode Lockdown
│   │   └── Result Entry & Diagnostics
│   │
│   └── Recall Navigation
│       ├── Due Today Queue
│       ├── Active Review Session (SM-2)
│       └── History & Card Management
│
└── Modal / Utility Navigation
    ├── Focus / Study Timer
    ├── Universal CSV Import & Export
    ├── Settings (Appearance, Study, Exam, Data, Privacy, About)
    └── Confirmation & Diagnostic Dialogs
```

---

## 3. Core Academic Relational Data Model (Room)

All features tie directly back to the shared academic model:
```
Subject (1) ───< Chapter (N) ───< Topic (N)
                                     │
           ┌─────────────────────────┼─────────────────────────┐
           ▼                         ▼                         ▼
      Recall Card              Question Bank                Mistake
           │                         │                         │
           ▼                         ▼                         ▼
     Recall Review             Paper Question              Lost Marks
                                     │
                                     ▼
                                   Paper
                                     │
                                     ▼
                                Exam Result
```

### Database Tables:
1. `students` — Profile, class level, division, exam target.
2. `study_preferences` — Daily target, default exam durations, focus rules.
3. `subjects` — Academic subjects (`id`, `name`, `colorHex`, `createdAt`).
4. `chapters` — Units within a subject (`id`, `subjectId` FK, `name`, `orderIndex`).
5. `topics` — Granular concepts (`id`, `chapterId` FK, `name`, `masteryState`, `examRelevance`, `weaknessScore`).
6. `question_bank` — Question pool (`id`, `subjectId`, `chapterId`, `topicId`, `questionText`, `marks`, `questionType`, `difficulty`).
7. `papers` — Examination papers (`id`, `title`, `subjectId`, `totalMarks`, `durationMinutes`, `status`, `createdAt`).
8. `paper_questions` — Cross-reference join table (`paperId`, `questionId`, `orderIndex`).
9. `exam_results` — Test score diagnostic (`id`, `paperId`, `marksObtained`, `totalMarks`, `timeTakenMinutes`, `completedAt`).
10. `lost_marks` — Granular mark loss breakdown (`id`, `examResultId`, `topicId`, `marksLost`, `lossCategory`, `notes`).
11. `mistakes` — Mistake bank entries (`id`, `subjectId`, `chapterId`, `topicId`, `question`, `studentAnswer`, `correctAnswer`, `lossCategory`, `marksLost`, `isResolved`).
12. `recall_cards` — Active recall items (`id`, `subjectId`, `chapterId`, `topicId`, `prompt`, `expectedAnswer`, `intervalDays`, `easeFactor`, `dueDate`).
13. `recall_reviews` — SM-2 review logs (`id`, `cardId`, `rating`, `reviewTimestamp`).
14. `study_sessions` — Timed study & exam focus logs (`id`, `subjectId`, `chapterId`, `durationMinutes`, `completedAt`).

---

## 4. Centralized, Versioned, Shareable (CVS) Foundation
Features such as Marks Gap Planner, Forgetting Radar, and Previous Paper Analyzer are designed as CVS-compatible modules. Their computation operates directly over the shared relational entities rather than screen-isolated silos, ensuring total data interoperability and universal CSV portability.

---

## 5. Universal CSV Engine Architecture

The Universal CSV Engine is a shared offline system infrastructure supporting 10 distinct academic datasets:

```
CSV Engine
├── CsvParser (RFC 4180 compliant: quotes, commas, escapes "", multiline, Unicode)
├── CsvValidator (Required columns, types, enums, positive marks)
├── CsvExporter (RFC 4180 serialization, downloadable templates)
├── Duplicate Detector (Exact ID duplicate, Normalized content duplicate)
├── Relationship Resolver (Hierarchy validation: CREATE_MISSING, FAIL_ON_MISSING, SKIP_ROW)
├── Import Preview (Non-destructive validation breakdown before any DB mutation)
├── Import Executor (Atomic Room transaction with complete rollback on unexpected failure)
└── Error Reporter (Row-level and column-level actionable feedback)
```

### Supported Datasets:
1. `QUESTIONS` — Question pool with type, difficulty, marks, answer, source.
2. `SUBJECTS` — Top-level academic subjects.
3. `CHAPTERS` — Units within a subject.
4. `TOPICS` — Granular concepts with mastery and relevance.
5. `SYLLABUS` — Combined multi-level hierarchy.
6. `RECALL_CARDS` — Active spaced repetition flashcards.
7. `PAPERS` — Examination paper definitions.
8. `RESULTS` — Historical exam attempts and diagnostic scores.
9. `MISTAKES` — Catalog of lost marks and resolved status.
10. `STUDY_SESSIONS` — Timed study and focus logs.

---

## 6. PaperPilot Generation & Snapshot Architecture

### 6.1 Constraint Satisfaction Solver
Exam paper generation satisfies strict mathematical constraints without floating-point precision drift:
- Marks are scaled by 10 to perform exact integer subset-sum branch-and-bound backtracking, guaranteeing `sum(q.marks) == targetMarks`.
- When an exact solution cannot be formed from the pool, an explainable failure result is returned with concrete recovery options (relax difficulty, add chapters, add question types, reduce marks, or import more questions).
- Question selection prioritizes weak topics (weakness score adds up to +80 score boost) and respects repetition policies (`STRICTLY_NEW`, `AVOID_RECENT`, `ALLOW_REPETITION`).
- Selected questions are partitioned into authentic academic sections: Section A (MCQ / Objective, 1 mark), Section B (Short Answer, 2-3 marks), Section C (Long Answer, 4-5 marks), and Section D (Advanced / Numerical, 6+ marks).

### 6.2 Paper Snapshot Immutability Invariant
Once a paper is generated and finalized, it becomes **strictly immutable**:
- `PaperQuestionEntity` contains snapshot columns (`snapshotQuestionText`, `snapshotMarks`, `snapshotQuestionType`, `snapshotDifficulty`, `snapshotAnswer`, `snapshotChapterId`, `snapshotTopicId`).
- `questionId` references `question_bank` with `onDelete = ForeignKey.SET_NULL`.
- If an author or student later edits or deletes questions from the Question Bank, all finalized and historical papers remain 100% intact, readable, and printable.

---

## 7. Closed-Loop Offline Pipeline Architecture

95OS connects every academic touchpoint into a deterministic closed loop:
```
CSV Import
    ↓
Academic Data (Subjects, Chapters, Topics)
    ↓
Question Bank (Pool with marks, types, difficulty)
    ↓
Syllabus Mapping (Linked to curriculum units)
    ↓
Paper Builder (Target marks, duration, chapters, difficulty)
    ↓
Question Selection Engine (Knapsack solver + weakness prioritization)
    ↓
Exam Paper (Sections A–D, structured layout)
    ↓
Printable / PDF (A4 format with candidate metadata & instructions)
    ↓
Physical Exam (Distraction-free fullscreen timer & exit warning)
    ↓
Result Entry (Question-level marks obtained & loss categorization)
    ↓
Performance Data (Overall percentage & marks trend)
    ↓
Mistakes / Weak Topics (Mistake Bank entries + Topic weakness score updated)
    ↓
Future Paper Generation (Adaptive weighting surfaces previous weak areas)
```
All components operate 100% offline under Room SQLite with zero network calls and zero external telemetry.

---

## 8. Marks Recovery Engine Architecture

The Marks Recovery Engine is a unified, deterministic domain layer (`MarksRecoveryEngine.kt`) serving five interconnected recovery features from a single source of truth:

```
                      Marks Recovery Engine
                                │
        ┌───────────────┬───────┴───────┬───────────────┐
        ▼               ▼               ▼               ▼
   Marks Gap     Forgetting Radar  Paper Analyzer  Recovery Score
    Planner             │               │               │
        │               └───────┬───────┘               │
        ▼                       ▼                       ▼
Top Opportunities       15-Minute Rescue          Regained Marks
  Prioritization              Mode                  Report
```

### 8.1 Marks Gap Planner
- **Objective:** Compute exact percentage and mark distance from target (default 95.0%) and project recoverable marks from active mistakes.
- **Formulas:**
  $$\text{Percentage Gap} = \max(0, \text{Target \%} - \text{Current \%})$$
  $$\text{Marks Needed} = \max(0, \text{Total Marks} \times \frac{\text{Target \%} - \text{Current \%}}{100})$$
  $$\text{Potential Recoverable Marks} = \sum_{m \in \text{Unresolved Mistakes}} m.\text{marksLost}$$
- **Prioritization Formula:**
  $$\text{Priority Score} = M_{\text{lost}} \times F_{\text{recent}} \times W \times R \times E$$
  - $M_{\text{lost}}$: Total marks lost on topic.
  - $F_{\text{recent}}$: Recent frequency factor ($1.0 + 0.2 \times \min(N_{\text{mistakes}}, 5)$).
  - $W$: Topic weakness score ($\max(0.2, \text{weaknessScore})$).
  - $R$: Recoverability factor by dominant loss category (Calculation Error / Careless: 1.0; Forgotten / Didn't Know: 0.9; Time Management: 0.8; Concept Error: 0.7).
  - $E$: Exam relevance weighting ($\text{HIGH} = 1.5, \text{MEDIUM} = 1.0, \text{LOW} = 0.7$).
- **Priority Thresholds:** Score $\ge 10.0 \to \text{VERY\_HIGH}$; $\ge 5.0 \to \text{HIGH}$; $\ge 2.0 \to \text{MEDIUM}$; $< 2.0 \to \text{LOW}$.

### 8.2 Forgetting Radar (SM-2 Retention Decay Risk)
Evaluates SM-2 card intervals, review history, and overdue delta ($\Delta_{\text{days}} = \frac{\text{now} - \text{dueDate}}{86400000}$):
- `CRITICAL`: $\Delta_{\text{days}} > 3.0$ days overdue, or new card overdue ($\text{repetitions} = 0 \land \Delta_{\text{days}} > 0$), or ease factor $< 1.6$.
- `AT_RISK`: $\Delta_{\text{days}} \in [1.0, 3.0]$ days overdue, or interval $\le 3$ days, or topic weakness $\ge 0.7$.
- `WATCH`: Due in $0..3$ days ($\Delta_{\text{days}} \in [-3.0, 0]$), or ease factor $< 2.0$, or topic in `LEARNING` / `NOT_STARTED`.
- `STABLE`: Due $> 3$ days in the future with healthy ease factor $\ge 2.3$ and `MASTERED` mastery state.

### 8.3 Previous Paper Analyzer
Extracts longitudinal performance across PaperPilot and manual papers:
- **Trend Detection:** With $\le 3$ papers, trend delta is $\text{Score}_{\text{last}} - \text{Score}_{\text{first}}$. With $\ge 4$ papers, trend delta is $\text{Average}_{\text{recent 3}} - \text{Average}_{\text{overall}}$.
  - $\Delta \ge +2.0\% \to \text{IMPROVING}$; $\Delta \le -2.0\% \to \text{DECLINING}$; otherwise $\to \text{STABLE}$.
- **Repeated Weakness Invariant:** Explicitly isolates topics where marks were lost across $\ge 2$ distinct examination papers, surfacing entrenched conceptual or careless errors.
- **Diagnostics:** Per-chapter accuracy and question-type success rates mapped against exam paper question snapshots.

### 8.4 15-Minute Rescue Mode
Generates rapid, time-budgeted study blocks:
- **Block 1 (Recall):** 40% of duration ($\ge 2$ mins) targeting highest-risk cards from Forgetting Radar.
- **Block 2 (Mistakes):** 35% of duration ($\ge 2$ mins) eliminating repeated calculation/careless mistakes.
- **Block 3 (High-Yield Concept):** 25% of duration ($\ge 1$ min) reviewing key formulas/concepts from top Marks Gap opportunity.
- Variable time settings supported: 5m, 10m, 15m, 20m, 30m. Sum of block durations strictly equals total duration.

### 8.5 Recovery Score & Assessment Comparability
Measures marks regained from previous exam weaknesses:
- **Normalization Formula (Different Total Marks):**
  $$\text{Normalized Previous Lost} = \left(\frac{\text{Previous Lost Marks}}{\text{Previous Total Marks}}\right) \times \text{Current Total Marks}$$
  $$\text{Recovered Marks} = \text{Normalized Previous Lost} - \text{Current Lost Marks}$$
- Breaks down recovered marks by mistake category and per-topic delta ($\text{Previous Lost on Topic} - \text{Current Lost on Topic}$).

---

## 9. Advanced Exam Intelligence System Architecture (Features 6–10)

The Advanced Exam Intelligence layer (`AdvancedExamEngine.kt` + `OfflineAdvancedExamRepository.kt`) builds directly on top of Room tables (`question_bank`, `papers`, `paper_questions`, `mistakes`, `exam_results`, `study_sessions`, `topics`, `chapters`, `subjects`) and DataStore preferences without cloud servers or artificial generation.

```
                  Advanced Exam Intelligence Engine
                                 │
     ┌──────────────────┬────────┼────────┬──────────────────┐
     ▼                  ▼        ▼        ▼                  ▼
6. Time-to-Marks   7. Adaptive  8. Exam  9. Last-7-Days  10. 95% Command
   Intelligence       Re-Test   Readiness     Mode           Center
                                Simulator
```

### 9.1 Time-to-Marks Intelligence
- **Goal:** Predict exact study time return on target exam marks.
- **Priority Formula:**
  $$\text{Priority Score} = \text{Weakness} \times \text{Exam Weightage} \times \text{Improvement Potential} \times \text{Confidence Factor}$$
  - $\text{Weakness} \in [0.1, 1.0]$: Topic weakness score + mistake frequency penalty.
  - $\text{Exam Weightage} \in [0.7, 1.5]$: Syllabus relevance of constituent topics ($\text{HIGH} \to 1.5, \text{LOW} \to 0.7$).
  - $\text{Improvement Potential} \in [0.5, 1.0]$: Ratio of recoverable mistake categories (careless, calculation vs deep conceptual gaps).
  - $\text{Confidence Factor} \in [0.2, 1.0]$: Evidence scaling by previous question tests ($0.3 + 0.07 \times \min(N, 10)$).
- **Study Time Projection:**
  $$\text{Estimated Hours Needed} = \frac{\text{Active Lost Marks}}{\text{Expected Yield per Hour}}$$
- **Ethical Safeguard:** Explicit labeling of all projections as deterministic empirical estimates. No score guarantees.

### 9.2 Adaptive Re-Test
- **Target Distribution Standard:**
  - **50% Weak Syllabus Areas:** Prioritizes topics with unresolved mistakes and $\text{weaknessScore} \ge 0.5$.
  - **30% Recently Missed Questions / Concepts:** Directly samples questions failed in previous paper runs.
  - **20% Mixed Active Revision:** Balanced cross-topic review to prevent skill regression on mastered areas.
- **Automatic Mistake Resolution:** Scoring $\ge 90\%$ on retest questions automatically resolves corresponding Mistake Bank entries (`isResolved = true`) and credits recovered marks.
- **Post-Retest Decision Tree:**
  - $\ge 85\%$ accuracy: "Mastery demonstrated. Proceed to full Exam Readiness Simulation."
  - $65\% - 84\%$ accuracy: "Moderate improvement. Clear remaining weak concepts in Active Recall."
  - $< 65\%$ accuracy: "High error rate. Re-study chapter concepts before next attempt."
  - Three standardized user actions: `START RETEST`, `REVIEW MISTAKES`, `SKIP`.

### 9.3 Exam Readiness Simulator
- **Configurable Examination Standard:** Subject selection, time limits (30–180m), total marks, and difficulty calibrations.
- **Accidental Submission Prevention:** Interactive modal displaying answered, marked for review, and unanswered tallies before finalizing scores.
- **Active In-Exam Runner:** Real-time countdown timer, 1..N question status jump grid, "Mark for Review" toggling, and structured sectioning.
- **Deterministic Exam Readiness Formula:**
  $$\text{Readiness Score} = (S_{\text{recent}} \times 0.35) + (A_{\text{sim}} \times 0.25) + (C_{\text{syllabus}} \times 0.20) + (T_{\text{discipline}} \times 0.10) + (M_{\text{clean}} \times 0.10)$$
  - $S_{\text{recent}}$ (35%): Longitudinal average of recent tests.
  - $A_{\text{sim}}$ (25%): Immediate simulation accuracy percentage.
  - $C_{\text{syllabus}}$ (20%): Percentage of syllabus topics with `masteryState = MASTERED`.
  - $T_{\text{discipline}}$ (10%): Pacing diagnostic penalizing finishes that are abnormally early ($< 40\%$ allocated time) or time runouts.
  - $M_{\text{clean}}$ (10%): Unresolved mistake cleanliness factor ($100 - \min(30, N_{\text{mistakes}} \times 2.5)$).

### 9.4 Last-7-Days Mode
- **Final-Week Sprint:** Automatically active or configurable when target exam is $\le 7$ days away.
- **Day 7 $\to$ Day 1 Curriculum:**
  - Day 7 to Day 2: Priority Chapter Revision + Adaptive Re-Test / Mistake Bank resolution + Spaced Recall Sprint.
  - Day 1: Final review of core formulas, low-friction recall, and high-yield summary notes.
- **"Today's 3 Most Important Tasks":** Highest-yield daily items with immediate checkbox completion state and adaptive rescheduling of subsequent days.

### 9.5 95% Command Center
- **5-Second Situational Awareness:**
  - 95% TARGET: Current predicted %, Target %, Marks gap, Exam readiness %, Days remaining.
  - "WHAT SHOULD I DO NOW?": Single primary recommended action based on deterministic highest weakness, with estimated minutes and reason.
  - One-tap launch: `START ACTION` or `ALTERNATIVE`.
  - Direct integration tiles for Time-to-Marks, Adaptive Re-Test, Exam Simulator, and Last-7-Days Mode.


