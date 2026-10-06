# 95OS v2.2.0 — Custom Themes, App Reset, 18+ Subject Catalog, & Universal Markdown Engine

**95OS** is the 100% offline, deterministic exam operating system designed to help students systematically move toward their target score, especially 95%.

Version 2.2.0 introduces deep system maintenance capabilities, multi-stream curriculum onboarding, academic personalization with strict visual restraint, and native Markdown (`.md`) portability alongside RFC 4180 CSV.

---

### 🌟 What's New in v2.2.0

#### 1. Operating System Maintenance & App Reset
- **Atomic Local Database Wipe:** Provides a protected reset mechanism in Settings that executes `database.clearAllTables()` across all 14 Room SQLite tables.
- **Preference Clearance:** Reinitializes all DataStore Preferences, clearing active onboarding states, timers, and theme preferences.
- **Safe State Clearance:** Safeguarded with an explicit confirmation dialog (`Reset 95OS Operating System?`) and navigates back to clean Onboarding.

#### 2. Academic Custom Themes (Zero Purple, Zero Heavy Navy)
- **Calm, High-Ergonomics Academic Palettes:** 6 curated themes engineered for serious study and reduced eye strain:
  1. **Warm Obsidian (Default Dark):** Deep warm obsidian `#121211` background, `#1A1A18` surfaces, warm amber `#D99B38` accents, and target cyan `#38BDF8`.
  2. **Paper White (Default Light):** Ivory paper `#F8F8F5` background, `#FFFFFF` cards, restrained amber `#B8781E`, and target cyan `#0284C7`.
  3. **Graphite Chamber (Monochrome Dark):** Minimalist slate-graphite `#141414` workspace, `#1E1E1E` containers, and chalk silver `#B0B0B0` accents.
  4. **Sepia Scholar (Library Warm):** Soft archival parchment `#F4EFE6`, espresso `#28221D` text, and terracotta `#A0522D` highlights.
  5. **Forest Slate (Deep Night Study):** Night moss `#101714` background, deep slate `#17221D` surfaces, and slate moss `#52986F` accents.
  6. **System Mode:** Automatically tracks Android system dark/light configuration.
- **Instant Persistence:** Themes apply immediately without app restart, saved directly to local DataStore preferences.

#### 3. 18+ Subject Catalog & 7-Step Guided Onboarding
- **Multi-Stream Preset Shortcuts:**
  - **PCM Stream:** Physics, Chemistry, Mathematics, English, Computer Science.
  - **PCB Stream:** Physics, Chemistry, Biology, English, Psychology.
  - **Commerce Stream:** Accountancy, Business Studies, Economics, Mathematics, English.
  - **Humanities Stream:** History, Political Science, Geography, Economics, English, Sociology.
- **18 Preloaded Subjects:** Physics, Chemistry, Mathematics, Biology, English, History, Geography, Political Science, Economics, Accountancy, Business Studies, Computer Science, Psychology, Sociology, Environmental Science, Statistics, Philosophy, Hindi.
- **Custom Subject Builder:** Students can add unique local curriculum subjects directly during onboarding.
- **7-Step Setup Blueprint:**
  1. **Welcome:** 95OS Manifesto & Offline Mission.
  2. **Profile:** Student Name & Board Benchmark suggestions (CBSE, ICSE, State Board, Cambridge IGCSE, IB).
  3. **Exam Target:** Target percentage (90%, 95%, 98%) & Exam timeline (30d, 60d, 90d, 180d).
  4. **Subjects:** Stream presets, 18-subject cards with enrolled counter, and custom subject creation.
  5. **Daily Habit:** Daily study commitment (30m–180m) & target exam duration.
  6. **Appearance:** Interactive theme selection with visual color indicator swatches.
  7. **Summary:** Comprehensive review before launching into the 95% Command Center.

#### 4. Universal Markdown (`.md`) Portability Engine
- **Bidirectional Markdown Support:**
  - Export and import Syllabus, Question Banks, Recall Cards, and Mistake Banks directly as clean, human-readable `.md` documents.
  - Formats syllabus hierarchies with Markdown headers (`# Subject`, `## Chapter`) and completion checkboxes (`- [x] Topic [HIGH] [MASTERED]`).
  - Formats Question Banks and Mistake Banks with structured metadata bullets (`- Subject:`, `- Chapter:`, `- Correct Answer:`, `- Marks Lost:`, `- Category:`).
- **Dual Format Switcher in Settings:**
  - Toggle seamlessly between **CSV (RFC 4180)** and **Markdown (.md)** templates and datasets.
  - Live clipboard copying allows one-tap export for offline study notes, printing, or physical revision binders.
  - Robust error diagnostics and atomic Room transaction rollbacks on parsing invalid records.

---

### 🛡️ Core 95OS Invariants
- **100% Offline-First:** Fully functional under Airplane Mode. Zero cloud databases, zero login screens, zero tracking.
- **Room SQLite Integrity:** Foreign keys enforced (`PRAGMA foreign_keys = ON;`).
- **Deterministic Analytics:** Transparent formulas for all scores, readiness metrics, and recovery estimates.
- **Academic Focus:** Strict minimum 44dp touch targets, clean typographic hierarchy, zero purple, zero dark navy.

---

### 📱 Android Application Details
- **Package Name:** `com.os95.app`
- **Version Name:** `2.2.0`
- **Version Code:** `9`
- **Target SDK:** Android 14 (API 34)
- **Min SDK:** Android 8.0 (API 26)

---

### 📥 Downloads
- `95OS-v2.2.0.apk` — Production Release Build (Signed)
- `95OS-v2.2.0-debug.apk` — Debug Testing Build
