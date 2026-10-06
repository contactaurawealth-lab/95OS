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
- **Role:** Offline generation and tracking of authentic practice examination papers from local question banks.
- **Data Entities:** `QuestionBankEntity`, `PaperEntity`, `PaperQuestionEntity`, `ExamResultEntity`.
- **Status:** Phase 1 Foundation Shell.

### 1.5 Mistake Bank (Lost Marks & Recovery)
- **Role:** Catalog of lost marks, mistake categorization (Concept Error, Calculation Error, Misread, Careless, Time Shortage, etc.), and recovery tracking.
- **Data Entities:** `MistakeEntity`, `LostMarksEntity`.
- **Status:** Phase 1 Foundation Shell.

### 1.6 95% Progress & Analytics Engine
- **Role:** Deterministic progress projection, syllabus coverage, cognitive readiness, and target score gap analysis.
- **Data Entities:** `ExamResultEntity`, `LostMarksEntity`, `TopicEntity`.
- **Status:** Phase 1 Foundation Contracts & Analytics Shell.

### 1.7 Universal CSV Foundation
- **Role:** Centralized, versioned, shareable import/export foundation supporting bulk data across syllabus, questions, cards, and mistakes.
- **Status:** Phase 1 Core Architecture & Processor Interface.

### 1.8 Focus Layer (Study Timer & Exam Mode)
- **Role:** Dedicated distraction-free focus timer and foreground exam simulation coordinator.
- **Data Entities:** `StudySessionEntity`, `StudyPreferencesEntity`.
- **Status:** Phase 1 Foundation Shell.

### 1.9 Settings & Data Sovereignty
- **Role:** Appearance, study targets, exam preferences, atomic database backup/restore, import/export, and local privacy transparency.
- **Status:** Phase 1 Foundation Shell.

---

## 2. Planned Advanced Features (Architecture & Contracts Established in Phase 1)

1. **Marks Gap Planner (CVS-Compatible):**
   Determines Current Score, Target Score (95%), Marks Gap, Recoverable Marks, and Highest-Leverage Topics.
2. **Forgetting Radar (CVS-Compatible):**
   Identifies previously strong topics/cards experiencing memory decay before exams.
3. **Previous Paper Analyzer (CVS-Compatible):**
   Analyzes past-paper results for recurring mistakes, weak chapters, and question patterns.
4. **15-Minute Rescue Mode:**
   High-value short revision session focusing on high-weight vulnerable areas and overdue recall.
5. **Recovery Score:**
   Measures marks recovered from prior mistakes over subsequent re-tests (`Previous Lost - Current Lost`).
6. **Time-to-Marks Intelligence:**
   Measures efficiency of study minutes converted into recoverable marks.
7. **Adaptive Re-Test:**
   Automatically constructs focused re-tests composed of prior mistakes and adjacent topic questions.
8. **Exam Readiness Simulator:**
   Simulates expected exam score using local historical performance and topic mastery data.
9. **Last-7-Days Mode:**
   High-stakes final sprint UI prioritizing only high-yield topics and critical mistakes.
10. **95% Command Center:**
    The ultimate decision engine answering: *"What should I do right now to move closer to 95%?"*
