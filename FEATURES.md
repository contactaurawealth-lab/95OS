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

## 2. Planned Advanced Features (Architecture & Contracts Established)

1. **Time-to-Marks Intelligence:**
   Measures efficiency of study minutes converted into recoverable marks.
2. **Adaptive Re-Test:**
   Automatically constructs focused re-tests composed of prior mistakes and adjacent topic questions.
3. **Exam Readiness Simulator:**
   Simulates expected exam score using local historical performance and topic mastery data.
4. **Last-7-Days Mode:**
   High-stakes final sprint UI prioritizing only high-yield topics and critical mistakes.
5. **95% Command Center:**
   The ultimate decision engine answering: *"What should I do right now to move closer to 95%?"*

