# 95OS — UI / UX & System Design Fix Implementation Prompt

> **Instruction for Implementation Agent:**
> Do not blindly implement every recommendation. Validate each issue against the current code and rendered UI before changing it.
> Prioritize fixes strictly by severity: **P0 → P1 → P2 → P3**.
> Always address root causes rather than cosmetic symptoms.
> Preserve existing offline architecture and product identity (100% offline, Room SQLite sovereignty, clean UDF with Jetpack Compose, no purple, no navy-heavy themes, 44dp minimum touch targets).
> Verify all changes with unit tests (`./gradlew testDebugUnitTest`) and APK compilation (`./gradlew assembleDebug`).

---

## 1. Execution Order & Actionable Plan

### Phase 1: Critical P0 Blockers (Restore Core User Flows)

1. **Fix Syllabus Manual Creation Dialogs (`SyllabusScreens.kt`):**
   - **Root Cause:** `OS95Dialog` is invoked using the 2-argument convenience overload that lacks a content lambda.
   - **Action:**
     - In `SyllabusScreen`, supply `content = { OS95TextField(value = newSubjectName, onValueChange = { newSubjectName = it }, label = "Subject Name", placeholder = "e.g. Mathematics") }` inside `showAddSubjectDialog`.
     - In `SubjectDetailScreen`, supply `content = { OS95TextField(value = newChapterName, onValueChange = { newChapterName = it }, label = "Chapter Name", placeholder = "e.g. Differentiation") }` inside `showAddChapterDialog`.
     - In `ChapterDetailScreen`, supply content with `OS95TextField(value = newTopicName, onValueChange = { newTopicName = it }, label = "Topic Title")` and a relevance selector (`HIGH`, `MEDIUM`, `LOW`) inside `showAddTopicDialog`.
     - Verify that clicking "Create" successfully saves into `SyllabusRepository` and updates the UI.

2. **Fix Question Bank & Mistake Bank Manual Creation Dialogs:**
   - **Files:** `QuestionBankScreen.kt`, `MistakesScreen.kt`
   - **Root Cause:** Missing text fields and hardcoded invalid foreign keys (`"general_chapter"`).
   - **Action:**
     - In `QuestionBankScreen`, populate `showAddDialog` with inputs for `questionText`, `marksText` (numeric), `selectedDifficulty` pill selector, and a picker for available chapters (preventing foreign key crashes). If no chapters exist, guide the user to create a chapter first.
     - In `MistakesScreen`, populate `showAddDialog` with `OS95TextField` inputs for `mistakeQuestion` and `correctAnswer`, along with a `lossCategory` picker (`CONCEPT_ERROR`, `CALCULATION_ERROR`, `MISREAD_ERROR`, `CARELESS_ERROR`, `TIME_SHORTAGE`).

---

### Phase 2: High-Priority P1 Faults (Data Integrity & Engine Reliability)

3. **Fix HomeViewModel State Loss Race Condition (`HomeViewModel.kt`):**
   - **Root Cause:** Emitting a new `HomeUiState(...)` instance inside `loadDashboardData()` resets `commandCenter`, `rescuePlan`, `potentialRecoverableMarks`, and `criticalTopicsCount` back to null/0.
   - **Action:**
     - Refactor the collector in `HomeViewModel.loadDashboardData()` to merge database emissions using `_uiState.value.copy(...)` so that `commandCenter` and `rescuePlan` are retained when background room database flows emit.

4. **Fix Diagnostic Exam Result Entry (`PapersScreen.kt`):**
   - **Root Cause:** Hardcoded `lostMarksMap[pq.id] = 1.0f` and missing `lossCategoryMap` UI selector.
   - **Action:**
     - In `showResultEntryDialog`, when a question is marked as having lost marks, display an input field for marks lost (defaulting to the question's allocated marks) and a chip/dropdown selector for `LossCategory`.
     - Update `recordDetailedResult` to pass the user's selected category and actual marks lost.

5. **Fix Formula Vault Foreign Key Constraint Crash (`FormulaVaultScreen.kt`):**
   - **Root Cause:** `FormulaEntity` requires a valid `chapterId` FK. Defaulting to `"general_chapter"` causes `SQLiteConstraintException` when no chapters exist.
   - **Action:**
     - Validate that the selected subject has at least one chapter. If `chapters.isEmpty()`, disable saving and display an informative message: *"Please create a chapter under this subject first before vaulting formulas."*

6. **Fix T-Minus 3H Box Breathing Animation Loop (`ExamDayProtocolScreen.kt`):**
   - **Root Cause:** `animateTo` finishes immediately on iterations 2–4 because target scale 1.25f/1.0f has already been reached.
   - **Action:**
     - Run `breathScale.animateTo(1.25f, tween(4000, easing = LinearEasing))` smoothly over the full 4 seconds during Inhale, and `breathScale.animateTo(1.0f, tween(4000, easing = LinearEasing))` over 4 seconds during Exhale, while synchronizing the second counter.

7. **Add Missing User Feedback Banners:**
   - **Files:** `HomeScreen.kt`, `MistakesScreen.kt`
   - **Action:**
     - In `HomeScreen`, render a snackbar or banner when `uiState.feedbackMessage` is present (with dismiss action).
     - In `MistakesScreen`, render a feedback banner when `printFeedbackMessage` is present.
     - In `MistakesScreen` print dialog, ensure `onDismissRequest` only dismisses the dialog without triggering unintended PDF generation.

---

### Phase 3: Medium-Priority P2 Improvements (Ergonomics & Polish)

8. **Fix FocusScreen Vertical Scroll (`FocusScreen.kt`):**
   - Add `.verticalScroll(rememberScrollState())` to the root Column to prevent UI button clipping on smaller viewports (<640dp) and landscape orientation.

9. **Fix Last-7-Days Mode Blank State (`Last7DaysScreen.kt`):**
   - Add an `OS95EmptyState` or `OS95ErrorState` if `uiState.dashboard == null`, offering an action to configure the target exam date.

10. **Fix Recall Screen Empty State (`RecallScreen.kt`):**
    - Show an introductory empty state when `allCards.isEmpty()` explaining how to add or import cards, separate from the "All caught up" state when cards are scheduled for future dates.

11. **Enable Editable Study Targets in Settings (`SettingsScreen.kt`):**
    - Allow students to tap on Target Exam Score and Daily Study Target to adjust their targets via `OS95Dialog`.

12. **Persist Exam Day Checklist Across Orientation Changes (`ExamDayProtocolScreen.kt`):**
    - Use `rememberSaveable` for checked items so rotating the phone does not reset packing progress.

13. **Wire Unused HomeScreen Navigation & Interactive Weakness Items (`HomeScreen.kt`):**
    - Wire `onNavigateToPapers` and `onNavigateToMistakes` into quick-action shortcuts.
    - Make weak chapter items clickable to navigate directly to `ChapterDetail`.

---

### Phase 4: Low-Priority P3 Polish (Design Tokens & Validation)

14. **Design System Consistency & Card Click Ripple:**
    - Refactor direct `androidx.compose.material3.AlertDialog` invocations to `OS95Dialog`.
    - In `OS95Card`, apply `modifier.clip(shapes.medium).clickable(...)` after `Surface` or pass `onClick` to `Surface` directly so ripples match rounded corners.

15. **Onboarding Input Validation (`OnboardingScreen.kt`):**
    - Ensure student name is not empty in Step 2, and require at least one subject to be selected in Step 4 before advancing.

---

## 2. Verification & Validation Protocol

After applying any fix:
1. Run `./gradlew testDebugUnitTest` to ensure all existing and updated tests pass cleanly.
2. Run `./gradlew assembleDebug` to verify compilation and resource packaging.
3. Update `ui-report.md` with resolved status and maintain the audit history.
