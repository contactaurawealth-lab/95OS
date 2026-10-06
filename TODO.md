# Now
- [ ] Phase 3: Time-to-Marks Intelligence & Efficiency Metrics
- [ ] Phase 3: Adaptive Re-Test & Mistake Paper Generator

# Next
- [ ] Phase 3: Exam Readiness Simulator
- [ ] Phase 3: Last-7-Days Mode & 95% Command Center

# Later
- [ ] Phase 3: Time-to-Marks Intelligence & Efficiency Metrics
- [ ] Phase 3: Adaptive Re-Test & Mistake Paper Generator
- [ ] Phase 3: Exam Readiness Simulator
- [ ] Phase 3: Last-7-Days Mode & 95% Command Center

# Bugs
- [ ] (No open bugs)

# Completed
- [x] Phase 3: Marks Gap Planner (CVS-Compatible) — Deterministic percentage & marks gap from 95% target, recoverable marks from active mistakes, and prioritization ranking ($M_{\text{lost}} \times F_{\text{recent}} \times W \times R \times E$)
- [x] Phase 3: Forgetting Radar (CVS-Compatible) — SM-2 spaced repetition decay risk classifier (`CRITICAL`, `AT_RISK`, `WATCH`, `STABLE`) with one-tap jump to Recall sessions
- [x] Phase 3: Previous Paper Analyzer (CVS-Compatible) — Historical exam performance trends (`IMPROVING`, `DECLINING`, `STABLE`), per-chapter accuracy, question-type breakdown, and repeated weakness detection across $\ge 2$ distinct papers
- [x] Phase 3: 15-Minute Rescue Mode — Time-budgeted rapid intervention sessions (5, 10, 15, 20, 30m) balancing High-Risk Recall (40%), Mistake Remediation (35%), and High-Yield Concept Review (25%)
- [x] Phase 3: Recovery Score — Quantifies marks regained on re-tests ($\text{Previous Lost} - \text{Current Lost}$) with assessment comparability normalization for exams of differing lengths, category breakdown, and topic recovery list
- [x] Phase 3: Full closed-loop navigation wiring across Home, Recall, Papers, Mistakes, and Progress tabs in `OS95NavHost`
- [x] Phase 3: Unit & integration test suite (62/62 passing) covering all 5 recovery features and complete 10-phase end-to-end integration scenario
- [x] Phase 2: Universal CSV multi-entity bulk import/export engine across all 10 datasets with RFC 4180 parsing, row-level validation, duplicate detection, missing hierarchy handling, and atomic Room transactions
- [x] Phase 2: PaperPilot blueprint builder and constraint-based paper generation engine (exact marks integer knapsack solver, adaptive weak topic prioritization, repetition filters, and section partitioning)
- [x] Phase 2: Paper snapshot immutability guaranteeing finalized papers never mutate or break when questions in the bank are edited or deleted
- [x] Phase 2: Offline printable exam paper text formatter & Android PdfDocument generator with instructions, candidate metadata, and right-aligned marks
- [x] Phase 2: Real Exam Mode fullscreen timer with distraction block and exit confirmation dialog
- [x] Phase 2: Question-level exam result entry dialog with loss categorization (Concept, Calculation, Misread, Careless, Time Shortage)
- [x] Phase 2: Closed-loop pipeline linkage to Mistake Bank creation, topic weakness score boosting, and adaptive future paper generation
- [x] Phase 2: Question Bank Engine & Management UI with subject/difficulty filters and search
- [x] Phase 2: Active Recall session presets (Blitz 5, Focused 10, Deep 20, All Due) & Mistake Bank auto-linkage ("To Recall")
- [x] Codebase inspection & complete 95OS architecture mapping
- [x] Standalone 95OS project initialized in `/root/95OS` with namespace `com.os95.app`
- [x] Product Documentation Suite: `AGENTS.md`, `PRODUCT.md`, `FEATURES.md`, `ARCHITECTURE.md`, `TODO.md`
- [x] Global Design System: `OS95Theme`, `OS95Colors` (calm academic palette, no purple, no navy-heavy), `OS95Typography`, `OS95Shapes`, `OS95Spacing` (44dp touch targets)
- [x] Reusable Design Components: `OS95Button`, `OS95OutlinedButton`, `OS95IconButton`, `OS95Card`, `OS95Dialog`, `OS95TextField`, `OS95TopBar`, `OS95BottomBar`
- [x] State Components: `OS95EmptyState` (with explanation, primary & secondary actions), `OS95ErrorState` (student-friendly recovery), `OS95LoadingState`, `OS95ProgressBar`
- [x] Local Offline Data Foundation: `OS95Database` (Room SQLite, foreign keys enforced) with 14 relational entities and DAOs
- [x] Academic Hierarchy Foundation: `SubjectEntity` → `ChapterEntity` → `TopicEntity` with 4-stage mastery states and exam relevance weighting
- [x] Spaced Repetition Foundation: `SM2Engine` mathematical implementation & `RecallCardEntity` / `RecallReviewEntity`
- [x] Lost Marks & Mistake Foundation: `MistakeEntity` & `LostMarksEntity` with diagnostic loss categories
- [x] PaperPilot Data Foundation: `PaperEntity`, `PaperQuestionEntity`, `QuestionBankEntity`, `ExamResultEntity`
- [x] Deterministic 95% Analytics Foundation: `Target95Engine` metrics and gap calculation
- [x] Universal CSV Foundation: `UniversalCsvProcessor`, templates, row-level validation, and error reporting
- [x] Multi-Level Navigation Architecture: `OS95Destinations`, `OS95NavHost`, mobile bottom bar, and expanded navigation rail
- [x] Home Shell: Central command center with Exam Target badge, Today's priorities, Syllabus progress, Recent papers, and Marks trend
- [x] Syllabus Shell: Subject, Chapter, and Topic tracking with interactive mastery pills
- [x] Recall Shell: Due today count, SM-2 interactive review session runner, rating buttons, and retention logs
- [x] Papers Shell: PaperPilot hub with paper list, exam duration/marks overview, and creation dialog
- [x] Mistakes Shell: Mistake Bank with category badges, lost marks stats, and resolution flow
- [x] Progress Shell: 95% Target Engine analytics, target projection, gap analysis, and syllabus coverage
- [x] Focus Shell: Study timer with 25m, 50m, 90m exam simulation presets and session persistence
- [x] Settings Shell: Appearance, Study targets, Exam duration, Data portability, Offline privacy info, and About 95OS
- [x] 6-Step Initialization Flow: Welcome → Profile → Subjects → Target (95%) → Appearance → Summary → Home
- [x] Offline Sovereignty: Zero cloud databases, zero telemetry, minimum access permissions (`POST_NOTIFICATIONS`, `VIBRATE`)
- [x] Testing Foundation: Comprehensive unit tests passing (`SM2EngineTest`, `Target95EngineTest`, `UniversalCsvProcessorTest`, `NavigationDestinationTest`, `ThemePersistenceTest`, `OnboardingStepTransitionTest`)
- [x] Gradle Build Verification: Build succeeded cleanly (`./gradlew testDebugUnitTest`)
