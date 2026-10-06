# Now
- [ ] User feedback review and continuous feature refinement

# Next
- [ ] Longitudinal student study analytics & extended reporting

# Later
- [ ] Optional BYOK AI sidecar assistance (strictly offline-isolated)

# Bugs
- [ ] (No open bugs)

# Completed
- [x] Full Database Sovereignty Backup & Restore Manager (`.95os` raw SQLite export/import with WAL checkpointing, binary header validation, and atomic swap)
- [x] Session Navigation Safety: Jetpack Compose `BackHandler` protections for active sessions in `ExamSimulatorScreen`, `AdaptiveRetestScreen`, and `FocusScreen` with exit confirmation modals
- [x] Syllabus Batch Mastery Progression: Chapter-level "Revise All" and "Master All" batch actions with optimized Room SQL execution
- [x] Printable Revision Documents: `RevisionDocumentGenerator` producing printer-ready A4 PDF and Markdown sheets for Mistake Remediation and Forgetting Flash Sheet
- [x] Longitudinal Study Consistency & Mastery Heatmap: 70-day (10-week) visual study grid in `ProgressScreen`
- [x] Target Score Sensitivity ("What-If") Calculator: Dynamic slider simulating projected score and marks gap impact in `Target95Engine` and `ProgressScreen`
- [x] Formula & Key Definition Vault: Database persistence (`FormulaEntity`, `FormulaDao`), repository, and UI (`FormulaVaultScreen`) with subject filtering, search, and bookmarking
- [x] "T-Minus 3 Hours" Exam Day Protocol: Zero-panic exam morning suite with gear checklist, arrival timeline, and 5-card cognitive priming with interactive box breathing
- [x] 100% Offline Procedural Focus Audio Generator: `OfflineAcousticEngine` with real-time PCM synthesis for Brownian noise, Pink noise, and Analog Exam Clock Tick (0 KB external assets)
- [x] Final Assembly & Test Suite: 95/95 unit tests passing cleanly (`testDebugUnitTest`) and APK compilation verified (`assembleDebug`)
- [x] Application Reset Feature: Atomic Room `clearAllTables()` and DataStore preferences wipe with confirmation dialog and navigation redirect to Onboarding
- [x] Academic Custom Themes: 6 themes (`SYSTEM`, `WARM_OBSIDIAN`, `PAPER_WHITE`, `GRAPHITE_CHAMBER`, `SEPIA_SCHOLAR`, `FOREST_SLATE`) adhering strictly to NO PURPLE and NO DARK NAVY rules
- [x] 18+ Subject Catalog & 7-Step Onboarding: Multi-stream presets (`PCM`, `PCB`, `Commerce`, `Humanities`), custom subject addition, board benchmarks, and structured step transitions
- [x] Universal Markdown (`.md`) Portability Engine: Bidirectional parsing and export for Syllabus, Question Banks, Recall Cards, and Mistakes with dual format toggle and clipboard support
- [x] Verification: Unit tests (78/78 passing) & assembleDebug build clean verification
- [x] Phase 3: Feature 6 — Time-to-Marks Intelligence: Calculates empirical study yield per hour, prioritizes highest-impact chapters using $W \times E \times I \times C$ priority score, and displays "Best use of your next X minutes" with potential gain %
- [x] Phase 3: Feature 7 — Adaptive Re-Test: Structured 50% weak areas, 30% recently missed concepts, and 20% mixed revision diagnostic re-tests with self-scoring, automatic mistake resolution upon scoring $\ge 90\%$, and decision guidance (`START RETEST`, `REVIEW MISTAKES`, `SKIP`)
- [x] Phase 3: Feature 8 — Exam Readiness Simulator: Formal examination simulation with countdown timer, 1..N question status jump grid, "Mark for Review", accidental submission prevention dialog, and deterministic composite **Exam Readiness: XX%** report
- [x] Phase 3: Feature 9 — Last-7-Days Mode: Final-week exam sprint mode active $\le 7$ days before exam date with Day 7 $\to$ Day 1 curriculum, "Today's 3 Most Important Tasks", and adaptive daily plan recalculation
- [x] Phase 3: Feature 10 — 95% Command Center: Primary dashboard delivering 5-second situational awareness (Top 95% TARGET card with predicted %, target %, marks gap, readiness %, days remaining), Middle "WHAT SHOULD I DO NOW?" card with single high-priority action and instant launch, quick-access engine hub, and daily execution tracking
- [x] Phase 3: Unit test suite for `AdvancedExamEngine` verifying all 5 algorithms and formulas
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
