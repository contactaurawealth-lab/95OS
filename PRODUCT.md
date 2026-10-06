# 95OS — Product Vision & Architecture Manifesto

## 1. Product Identity
- **Product Name:** 95OS
- **Tagline:** The Offline Exam Operating System
- **Core Closed-Loop Philosophy:**
  ```
  Syllabus → Learn → Recall → Practice → Result → Lost Marks → Mistakes → Revision → Re-test → Score Progress
  ```

---

## 2. What 95OS Is — And What It Is Not
- **What 95OS Is:**
  - An exam-performance operating system engineered for one singular purpose: systematically moving a student toward their target score, especially 95%.
  - A closed-loop system where every test result identifies lost marks, categorizes mistakes, routes them into active recall, and generates targeted re-tests.
  - A 100% offline-first, private, local academic instrument.
- **What 95OS Is NOT:**
  - NOT a generic productivity to-do list.
  - NOT a generic note-taking application.
  - NOT an isolated flashcard or trivia quiz toy.
  - NOT an AI tutor or cloud dashboard.
  - NOT a gamified distraction tool.

---

## 3. Core Architectural Pillars
1. **The Closed-Loop Exam System:**
   Every study activity connects directly to the student's academic syllabus and exam targets. Lost marks are not forgotten; they are cataloged and systematically eliminated.
2. **Deterministic Intelligence:**
   Calculations, gaps, and readiness metrics are based on transparent academic models rather than probabilistic or hallucinated outputs.
3. **Offline & Privacy Sovereignty:**
   All syllabus hierarchies, question banks, test attempts, mistake logs, and analytics reside strictly inside the local SQLite/Room database on the device.
4. **Physicality in Exam Simulation:**
   High-stakes examinations are taken on paper with ink. 95OS simulates realistic exam conditions via PaperPilot, printable examination papers, and foreground focus modes.
5. **Separation of Optional AI:**
   Core workflows never depend on network or AI. Any future AI capability is strictly isolated, opt-in, and BYOK.

---

## 4. Academic Data Hierarchy
Everything connects to the core academic hierarchy:
```
Subject
  └── Chapter
        └── Topic
              ├── Recall Card
              ├── Question
              ├── Paper
              ├── Result
              ├── Mistake
              └── Study Session
```

---

## 5. Target User
- Students preparing for high-stakes competitive, board, entrance, and university exams (e.g. CBSE, ICSE, JEE, NEET, SAT, AP, GCSE, A-Levels, University Finals).
- Aiming for mastery and top-percentile scores (target 95%+).

---

## 6. Advanced Exam Intelligence Engine
1. **Time-to-Marks Intelligence:** Deterministic prioritization ($W \times E \times I \times C$) directing the student's next 30/60/90/120 minutes to highest-yield chapters.
2. **Adaptive Re-Test:** Targeted re-examinations distributing 50% weak areas, 30% recent mistakes, and 20% mixed revision.
3. **Exam Readiness Simulator:** Realistic simulated exam environment with countdown, jump navigation, anti-accidental submission, and composite multi-factor readiness score.
4. **Last-7-Days Mode:** Daily countdown curriculum (Day 7 down to Day 1) activating within 7 days of the exam date.
5. **95% Command Center:** Five-second situational awareness dashboard with immediate "WHAT SHOULD I DO NOW?" primary action.

