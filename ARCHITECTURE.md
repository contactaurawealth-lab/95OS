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
