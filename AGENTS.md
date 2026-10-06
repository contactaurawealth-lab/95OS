# 95OS — Engineering Rules & Agent Guidelines

## 1. Product Identity & Philosophy
- **Product Name:** 95OS
- **Tagline:** The Offline Exam Operating System
- **Core Loop:**
  `Syllabus → Learn → Recall → Practice → Result → Lost Marks → Mistakes → Revision → Re-test → Score Progress`
- **Objective:** Help a student systematically move toward their target score, especially 95%.
- **Core Reality:** 100% offline, deterministic, serious academic instrument.

---

## 2. Architecture Rules
1. **Offline-First Invariant:**
   - The application must function completely under Airplane Mode (No Wi-Fi, No Cellular, No Bluetooth).
   - Core functions must never require login, cloud databases, remote authentication, or external network calls.
2. **Persistence Standard:**
   - Room (SQLite) is the sole primary persistent database.
   - Enforce SQLite foreign keys (`PRAGMA foreign_keys = ON;`).
   - Use DataStore Preferences solely for lightweight app flags and user preferences.
3. **Keep Modules Loosely Coupled & Avoid Overengineering:**
   - Unidirectional Data Flow (UDF): Compose UI observes immutable `StateFlow<UiState>`.
   - ViewModels interact with Domain Use Cases or Repositories.
   - No unnecessary abstractions or duplicate repositories, databases, timers, or services.
   - Prefer simple, direct, maintainable implementations over convoluted frameworks.

---

## 3. AI Rules
1. **AI is NOT part of the core architecture:**
   - The entire exam system, syllabus tracking, paper generation, scoring, and recall engines must work 100% without AI.
2. **Optional & Isolated:**
   - Any future AI integration must be strictly optional, user-controlled, and Bring-Your-Own-Key (BYOK).
   - It must remain completely isolated as a sidecar without ever gating core academic workflows.

---

## 4. UI & Design Rules
1. **Minimal, Calm, Fast & Mobile-First:**
   - Serious exam instrument feel — not a children's gamified toy, social network, or generic AI dashboard.
   - Restrained palette: Warm obsidian, paper whites, graphite, subtle amber / cyan accents.
   - **STRICT PROHIBITION:** No purple. No navy/dark-blue-heavy interface.
   - Avoid excessive gradients and decorative containers without clear functional purpose.
2. **Information Hierarchy & Ergonomics:**
   - 44dp minimum touch targets.
   - Every screen must have an unmistakable primary action.
   - Respect user focus with generous whitespace and clear typography hierarchy.

---

## 5. Data & Privacy Rules
1. **Local Data Sovereignty:**
   - Student academic data is private and sensitive.
   - Default path: Device → Local Room Database → Local Files.
   - No silent telemetry, no hidden trackers, no remote analytics.
2. **Data Integrity:**
   - Never silently discard student data.
   - Validate all imports before writing to persistent tables.
   - Handle empty states, corrupted records, and edge cases gracefully with constructive student feedback.
3. **Minimum Access Principle:**
   - Never request unnecessary permissions (no contacts, location, microphone, wide storage, or camera).
   - Request permissions only when strictly necessary, explain why, and provide fallback.

---

## 6. Development Rules & Workflow
Before implementing any feature:
1. Understand existing code and architecture.
2. Check whether functionality already exists.
3. Extend existing logic rather than rewriting working functionality for stylistic reasons.
4. Update documentation (`ARCHITECTURE.md`, `FEATURES.md`, `PRODUCT.md`).
5. Update canonical `TODO.md`.
6. Implement with clean tests.
7. Verify all unit tests pass: `./gradlew testDebugUnitTest`.
8. Verify clean build: `./gradlew assembleDebug`.
