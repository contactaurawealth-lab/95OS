# Now
- [ ] Phase 2: PaperPilot paper blueprint builder and exam generation engine
- [ ] Phase 2: Offline Printable PDF exam paper generator
- [ ] Phase 2: Real Exam Mode foreground timer service & distraction block coordinator

# Next
- [ ] Phase 2: Result recording & diagnostic flow with question-level loss categorization
- [ ] Phase 2: Universal CSV multi-entity bulk import/export execution

# Later
- [ ] Phase 3: Marks Gap Planner (CVS-Compatible)
- [ ] Phase 3: Forgetting Radar (CVS-Compatible)
- [ ] Phase 3: Previous Paper Analyzer (CVS-Compatible)
- [ ] Phase 3: 15-Minute Rescue Mode
- [ ] Phase 3: Recovery Score & Time-to-Marks Intelligence
- [ ] Phase 3: Adaptive Re-Test & Exam Readiness Simulator
- [ ] Phase 3: Last-7-Days Mode & 95% Command Center

# Bugs
- [ ] (No open bugs)

# Completed
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
