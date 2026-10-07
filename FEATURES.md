# 95OS — Feature Ecosystem & Traceability Matrix

## 1. Phase 1 Foundation Features (Active Scope)

### 1.1 Command Center (Home Shell)
- **Role:** Central command center communicating current exam context, target score, today's priorities, upcoming exams, syllabus progress, recall due, recent papers, and marks trend.
- **Data Entities:** `StudentProfileEntity`, `StudyPreferencesEntity`, `SubjectEntity`, `TopicEntity`, `PaperEntity`, `ExamResultEntity`.
- **Status:** Phase 1 Foundation Shell.

### 1.2 Syllabus Intelligence (Academic Hierarchy)
- **Role:** Deep academic tree (`Subject → Chapter → Topic`). Allows topic mastery progression (`NOT_STARTED`, `LEARNING`, `REVISED`, `MASTERED`) and exam relevance weighting (`LOW`, `MEDIUM`, `HIGH`).
- **Data Entities:** `SubjectEntity`, `ChapterEntity`, `TopicEntity`.
- **Status:** Phase 1 Foundation Shell & Data Model.

### 1.3 Recall Engine (Active Recall & Spaced Repetition)
- **Role:** Spaced repetition engine rooted in SM-2. Surfaces due cards, quick review sessions, card management, and review history.
- **Data Entities:** `RecallCardEntity`, `RecallReviewEntity`.
- **Status:** Phase 1 Foundation Shell & SM-2 algorithm.

### 1.4 PaperPilot (Exam Paper Architecture)
- **Role:** Full-pipeline offline examination paper generation, physical exam execution, and diagnostic scoring.
- **Components:**
  - **Blueprint Builder:** Subject, chapter, topic, target marks (20/40/50/80/100/Custom), duration (30/45/60/90/120/Custom), difficulty (Easy, Medium, Hard, Mixed), allowed question types, repetition policy (`STRICTLY_NEW`, `AVOID_RECENT`, `ALLOW_REPETITION`), and adaptive weak-topic prioritization toggle.
  - **Constraint Satisfaction Solver:** Deterministic integer-scaled knapsack solver guaranteeing `sum(marks) == requestedMarks`. Explainable failure with relaxation suggestions.
  - **Paper Sections:** Automatically categorizes questions into Section A (MCQ / Objective, 1m), Section B (Short Answer, 2-3m), Section C (Long Answer, 4-5m), and Section D (Advanced / Numerical, 6+m).
  - **Immutable Paper Snapshots:** Finalized papers store verbatim copies of question text, marks, types, and answers in `paper_questions`, preserving paper integrity even if questions in the bank are edited or deleted.
  - **Printable Exam Sheet:** Academic header, Candidate Name, Roll Number, Date, Time Allowed, Maximum Marks, General Instructions, Section demarcations, right-aligned marks `[X Marks]`, and end indicator. Formatted text rendering and Android `PdfDocument` generation.
  - **Real Exam Mode:** Fullscreen distraction-blocking timer with elapsed/remaining countdown, pause toggle, and exit warning dialog.
  - **Diagnostic Result Entry:** Question-level scoring modal recording marks obtained and categorizing lost marks (`CONCEPT_ERROR`, `CALCULATION_ERROR`, `MISREAD_ERROR`, `CARELESS_ERROR`, `TIME_SHORTAGE`).
  - **Closed-Loop Feedback:** Auto-creates `MistakeEntity` records in Mistake Bank, updates `TopicEntity.weaknessScore`, and updates question bank usage counts.
- **Data Entities:** `QuestionBankEntity`, `PaperEntity`, `PaperQuestionEntity`, `ExamResultEntity`, `LostMarksEntity`, `MistakeEntity`, `TopicEntity`.
- **Status:** Phase 2 Production Implementation.

### 1.5 Mistake Bank (Lost Marks & Recovery)
- **Role:** Catalog of lost marks, mistake categorization (Concept Error, Calculation Error, Misread, Careless, Time Shortage, etc.), and recovery tracking.
- **Data Entities:** `MistakeEntity`, `LostMarksEntity`.
- **Status:** Phase 2 Production Implementation.

### 1.6 95% Progress & Analytics Engine
- **Role:** Deterministic progress projection, syllabus coverage, cognitive readiness, and target score gap analysis.
- **Data Entities:** `ExamResultEntity`, `LostMarksEntity`, `TopicEntity`.
- **Status:** Phase 1 Foundation Contracts & Analytics Shell.

### 1.7 Universal CSV Engine
- **Role:** Reusable, RFC 4180-compliant import/export engine supporting 10 academic datasets.
- **Dataset Column Specifications:**
  - `QUESTIONS`: `question_id`, `subject`, `chapter`, `topic`, `question_text`, `question_type`, `difficulty`, `marks`, `answer`, `source`
  - `SUBJECTS`: `subject_id`, `name`, `code`, `color_hex`
  - `CHAPTERS`: `chapter_id`, `subject_id`, `name`, `order`
  - `TOPICS`: `topic_id`, `chapter_id`, `name`, `order`, `mastery_state`, `exam_relevance`
  - `SYLLABUS`: `subject`, `chapter`, `topic`, `mastery`, `relevance`
  - `RECALL_CARDS`: `card_id`, `subject`, `chapter`, `topic`, `prompt`, `expected_answer`
  - `PAPERS`: `paper_id`, `title`, `subject`, `total_marks`, `duration_minutes`
  - `RESULTS`: `result_id`, `paper_id`, `marks_obtained`, `total_marks`, `time_taken_minutes`
  - `MISTAKES`: `mistake_id`, `subject`, `chapter`, `topic`, `question`, `loss_category`, `marks_lost`
  - `STUDY_SESSIONS`: `session_id`, `subject`, `chapter`, `duration_minutes`, `type`
- **Import Flow & Validation:**
  - RFC 4180 parsing with quotes, commas, escaped quotes (`""`), multiline text, and Unicode.
  - Pre-import validation for missing columns, numeric positive marks, and enum values.
  - Duplicate detection: Exact ID match and normalized content match with `SKIP`, `REPLACE`, or `KEEP_BOTH` strategies.
  - Relationship validation: Missing hierarchy handling with `CREATE_MISSING` (auto-creates subject/chapter/topic), `FAIL_ON_MISSING`, or `SKIP_ROW`.
  - Non-destructive Import Preview modal displaying detected rows, valid count, duplicate count, errors, and new hierarchy entities.
  - Atomic Room database transaction committing on success or rolling back on failure.
- **Status:** Phase 2 Production Implementation.

### 1.8 Focus Layer (Study Timer & Exam Mode)
- **Role:** Dedicated distraction-free focus timer and foreground exam simulation coordinator.
- **Data Entities:** `StudySessionEntity`, `StudyPreferencesEntity`.
- **Status:** Phase 1 Foundation Shell.

### 1.9 Settings & Data Sovereignty
- **Role:** Appearance, study targets, exam preferences, atomic database backup/restore, import/export, and local privacy transparency.
- **Status:** Phase 1 Foundation Shell.

### 1.10 Marks Recovery Engine (Features 1–5: Closed-Loop Exam Recovery)
- **Role:** Unified offline recovery intelligence turning mistakes, exam results, and recall decay into recoverable marks.
- **Components:**
  1. **Marks Gap Planner:** Computes distance to 95% target score, total marks needed, recoverable marks from active mistakes, and deterministically prioritized recovery opportunities:
     $$\text{Priority Score} = M_{\text{lost}} \times F_{\text{recent}} \times W \times R \times E$$
     Provides actionable guidance per dominant loss category (e.g. calculation, careless, forgotten, concept).
  2. **Forgetting Radar:** Visualizes memory retention risk before exam exposure using SM-2 card intervals, review history, and overdue duration categorized into `CRITICAL`, `AT_RISK`, `WATCH`, and `STABLE`. Allows one-tap jump to active recall sessions.
  3. **Previous Paper Analyzer:** Turns historical exam results into longitudinal trends (`IMPROVING`, `DECLINING`, `STABLE`), per-chapter accuracy, question-type breakdown, and repeated weakness detection across $\ge 2$ distinct papers.
  4. **15-Minute Rescue Mode:** Time-budgeted rapid intervention sessions (5, 10, 15, 20, 30m) balancing High-Risk Recall (40%), Mistake Remediation (35%), and High-Yield Concept Review (25%).
  5. **Recovery Score:** Quantifies marks regained on re-tests ($\text{Previous Lost} - \text{Current Lost}$) with assessment comparability normalization for exams of differing lengths, category breakdown, and topic recovery list.
- **Data Entities:** `ExamResultEntity`, `LostMarksEntity`, `MistakeEntity`, `PaperEntity`, `PaperQuestionEntity`, `RecallCardEntity`, `RecallReviewEntity`, `TopicEntity`, `ChapterEntity`, `SubjectEntity`.
- **Status:** Phase 2 Production Implementation.

---

### 1.11 Advanced Exam Intelligence (Features 6–10)
- **Role:** High-impact study yield intelligence, targeted diagnostic re-tests, realistic examination simulations, final-week sprint coordination, and the primary 95OS situational command dashboard.
- **Components:**
  1. **Time-to-Marks Intelligence (Feature 6):**
     Calculates empirical study time return on marks investment using deterministic priority ranking:
     $$\text{Priority Score} = \text{Weakness} \times \text{Exam Weightage} \times \text{Improvement Potential} \times \text{Confidence Factor}$$
     Outputs "Best use of your next X minutes" with chapter-level potential gains (+XX% gain) and estimated hours needed.
  2. **Adaptive Re-Test (Feature 7):**
     Constructs personalized diagnostic re-tests following the 50% weak areas, 30% recently missed concepts, and 20% mixed revision distribution. Features self-evaluation, automatic mistake resolution upon scoring $\ge 90\%$, and decision guidance with `START RETEST`, `REVIEW MISTAKES`, and `SKIP`.
  3. **Exam Readiness Simulator (Feature 8):**
     Simulates formal examination conditions with configurable duration (30–180m), marks, difficulty, live ticking countdown timer, 1..N question status jump grid, "Mark for Review" toggling, and accidental submission confirmation modals. Computes composite **Exam Readiness: XX%** across recent scores (35%), simulation accuracy (25%), syllabus coverage (20%), time discipline (10%), and mistake factor (10%).
  4. **Last-7-Days Mode (Feature 9):**
     Final-week exam sprint mode active $\le 7$ days before the target date. Provides a Day 7 down to Day 1 curriculum, prioritizing high-yield weak chapters, adaptive re-tests, mistake audits, and recall sprints. Highlights "Today's 3 Most Important Tasks" with immediate completion checkoff.
  5. **95% Command Center (Feature 10):**
     The primary dashboard of 95OS delivering 5-second situational awareness: Top 95% TARGET card (Current predicted %, Target %, Marks gap, Exam readiness %, Days remaining), Middle "WHAT SHOULD I DO NOW?" card with single high-priority action and instant launch, quick-launch engine hub, and daily study execution tracking.
- **Data Entities:** `QuestionBankEntity`, `PaperEntity`, `PaperQuestionEntity`, `ExamResultEntity`, `LostMarksEntity`, `MistakeEntity`, `TopicEntity`, `ChapterEntity`, `SubjectEntity`, `StudySessionEntity`.
- **Status:** Phase 3 Production Implementation.

---

### 1.12 System Maintenance & Customization Architecture
- **Role:** Deep app maintenance, multi-stream curriculum onboarding, academic personalization, and universal Markdown (.md) + CSV data portability.
- **Components:**
  1. **Application Reset Engine:** Atomic SQLite `clearAllTables()` wipe coupled with DataStore preference teardown and backstack clearance to freshly reset the device back to pristine Onboarding. Includes confirmation safeguards.
  2. **Academic Custom Theme System:** 6 curated themes (`SYSTEM`, `WARM_OBSIDIAN`, `PAPER_WHITE`, `GRAPHITE_CHAMBER`, `SEPIA_SCHOLAR`, `FOREST_SLATE`) adhering strictly to the 95OS palette ethos (no purple, no dark-navy-heavy themes, warm obsidian, paper whites, graphite, subtle amber/cyan accents).
  3. **18+ Subject Catalog & 7-Step Onboarding:** 18 predefined subjects across STEM, Commerce, Humanities, and Languages with one-tap stream presets (`PCM`, `PCB`, `Commerce`, `Humanities`), custom subject addition, board benchmarks, and daily study habit commitments.
  4. **Universal Markdown (`.md`) Portability Engine:** Bidirectional parser and exporter for Syllabus, Question Banks, Recall Cards, and Mistake Banks formatted as clean Markdown documents with checkbox states, headers, and bullet metadata, plus dual-format UI toggle and clipboard support.
- **Status:** Production Implementation.

---

### 1.13 Advanced Offline Instrumentation & Exam Protocol
- **Role:** Complete academic data sovereignty, physical exam printouts, visual consistency analytics, formula reference vaulting, zero-panic exam morning protocols, and procedural offline cognitive audio.
- **Components:**
  1. **Full Database Sovereignty Backup & Restore (.95os):** Raw Room SQLite database backup and restore via Storage Access Framework (SAF) with WAL flushing (`PRAGMA wal_checkpoint(FULL);`), binary header verification (`SQLite format 3`), and safe atomic file swap.
  2. **Session Navigation Safety:** Jetpack Compose `BackHandler` protections for active sessions in `ExamSimulatorScreen`, `AdaptiveRetestScreen`, and `FocusScreen` with confirmation modal dialogs preventing accidental progress loss.
  3. **Syllabus Batch Mastery Progression:** Chapter-level "Revise All" and "Master All" batch actions executing atomic Room SQL updates across all child topics.
  4. **Printable Revision Documents:** `RevisionDocumentGenerator` generating standardized A4 PDF and Markdown sheets for Mistake Remediation (root-cause categorized) and High-Yield Forgetting Flash Sheets.
  5. **Longitudinal Study Consistency & Mastery Heatmap:** 70-day (10-week) visual study grid in `ProgressScreen` categorizing daily focus volumes (0m, 1–30m, 31–60m, 61–120m, 120m+) with streak tracking.
  6. **Target Score Sensitivity ("What-If") Calculator:** Dynamic marks sensitivity simulation slider in `ProgressScreen` projecting real-time percentage and marks gap changes from targeted topic recoveries.
  7. **Formula & Key Definition Vault:** Dedicated formula and law management screen (`FormulaVaultScreen`) with subject filtering, monospace equation formatting, explanation notes, and bookmarking.
  8. **"T-Minus 3 Hours" Exam Day Protocol:** Pre-exam morning operational suite with a logistics checklist, departure/arrival timeline, and 5-card cognitive priming with an interactive 4-4-4-4 box breathing engine.
---

### 1.14 Side Bar Navigation Drawer & Calm Dropdown Interaction Suite
- **Role:** Deep, friction-free accessibility across the entire 15-screen academic operating system without cluttering primary bottom navigation or requiring endless dashboard scrolling.
- **Components:**
  1. **Global Side Bar Navigation Drawer (`OS95NavigationDrawer`):**
     - Full-featured `ModalNavigationDrawer` with edge-swipe gestures and `OS95TopBar` hamburger menu activation.
     - Live student profile header featuring Cadet Scholar name, target percentage badge (`95% TARGET`), current target exam, and real-time `● OFFLINE` status indicator.
     - Categorized structural navigation sections:
       - **Core Exam Loop:** Command Center, Syllabus Tracker, Recall Engine, Practice Papers, Mistake Bank, and Score & Radar.
       - **Advanced Exam Engines:** Time-to-Marks Intel, Adaptive Re-Test, Exam Simulator, and Last-7-Days Mode.
       - **High-Yield Vaults:** Formula Vault, T-3H Exam Protocol, and Focus & Acoustics.
       - **System & Sovereignty:** Universal CSV/Markdown Data Portability and Settings & Targets.
     - 44dp minimum touch ergonomics, active destination indicator strips, and responsive support for phones and tablets.
  2. **Calm Obsidian Dropdown Menu System (`OS95Dropdowns`):**
     - `OS95DropdownMenu`: Restrained dark obsidian surface with subtle graphite border and rounded corners matching `OS95Theme`.
     - `OS95DropdownMenuItem`: 44dp touch target with optional leading icon, trailing badge/counter, active check indicator, and destructive styling.
     - `OS95DropdownSelector<T>`: Generic outlined selection box with animated rotating chevron arrow (`0°` to `180°`), placeholder support, and automated popup management.
     - `OS95OverflowMenu`: Standard 3-dot vertical action list seamlessly embedded in `OS95TopBar`.
  3. **Contextual Screen Integrations:**
     - **Question Bank:** Dual dropdown selectors for Subject and Difficulty filtering, plus overflow actions for CSV import and filter reset.
     - **Recall Engine:** Dropdown selector for Spaced Repetition review presets (Blitz, Focused, Deep, All Due) and overflow menu shortcuts.
     - **Focus & Exam Mode:** Dropdown selector for procedural offline acoustic profiles (Mute, Brown Noise, Pink Noise, Exam Clock Tick) and duration presets.
     - **Command Center & Practice Papers:** Instant 3-dot overflow menus providing one-tap access to advanced diagnostic engines and exam builders.
- **Status:** Production Implementation.
