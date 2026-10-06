package com.os95.app.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95ProgressBar
import com.os95.app.core.ui.component.OS95TextField
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.core.ui.theme.ThemeMode

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()

    val stepProgress = (uiState.step.ordinal + 1).toFloat() / OnboardingStep.values().size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Step progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "95OS SETUP",
                style = typography.caption.copy(letterSpacing = 1.2.sp),
                color = colors.accent
            )
            Text(
                text = "Step ${uiState.step.ordinal + 1} of ${OnboardingStep.values().size}",
                style = typography.caption,
                color = colors.mutedText
            )
        }

        Spacer(modifier = Modifier.height(spacing.s))
        OS95ProgressBar(progress = stepProgress)
        Spacer(modifier = Modifier.height(spacing.l))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            when (uiState.step) {
                OnboardingStep.WELCOME -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "95OS",
                            style = typography.heroTitle.copy(fontSize = 38.sp, lineHeight = 44.sp),
                            color = colors.accentCyan
                        )
                        Text(
                            text = "The Offline Exam Operating System",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "«Syllabus → Learn → Recall → Practice → Result → Lost Marks → Mistakes → Revision → Re-test → Score Progress»",
                            style = typography.body,
                            color = colors.secondaryText
                        )

                        OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Engineered for Serious Academic Mastery",
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = "• 100% Offline Under Airplane Mode — Zero cloud telemetry, no account logins, no remote trackers.\n" +
                                        "• Local SQLite Sovereignty — Questions, mistakes, test results, and syllabus stay solely on your physical device.\n" +
                                    "• Deterministic Intelligence — Transparent academic math, not probabilistic AI hallucinations.\n" +
                                    "• Closed-Loop Elimination of Errors — Turn every lost mark into an active recall card and targeted adaptive re-test.",
                                    style = typography.bodySmall,
                                    color = colors.secondaryText
                                )
                            }
                        }
                    }
                }

                OnboardingStep.PROFILE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Student Identity",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Enter your details to calibrate your local academic profile.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )

                        OS95TextField(
                            value = uiState.studentName,
                            onValueChange = { viewModel.setStudentName(it) },
                            label = "Student Name",
                            placeholder = "e.g. Alex Gandhi"
                        )

                        OS95TextField(
                            value = uiState.gradeLevel,
                            onValueChange = { viewModel.setGradeLevel(it) },
                            label = "Examination / Grade",
                            placeholder = "e.g. CBSE 12 / JEE / NEET / SAT / AP / GCSE"
                        )

                        Text(
                            text = "Popular Academic Benchmarks",
                            style = typography.caption,
                            color = colors.mutedText
                        )

                        val commonExams = listOf("CBSE Grade 12", "ICSE Grade 10", "JEE Main & Adv", "NEET Medical", "SAT / AP Exams", "University Finals")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            commonExams.chunked(2).forEach { rowExams ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowExams.forEach { ex ->
                                        val isSelected = uiState.gradeLevel == ex
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(if (isSelected) colors.cardBackground else colors.surface)
                                                .border(1.dp, if (isSelected) colors.accent else colors.border)
                                                .clickable { viewModel.setGradeLevel(ex) }
                                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Text(text = ex, style = typography.caption, color = if (isSelected) colors.accent else colors.primaryText)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OnboardingStep.EXAM_TARGET -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Target Score & Timeline",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Establish the deterministic benchmark for the 95OS Marks Gap Planner.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )

                        OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${uiState.targetScore.toInt()}%",
                                    style = typography.statNumber.copy(fontSize = 52.sp),
                                    color = colors.accentCyan
                                )
                                Text(
                                    text = "Target Examination Benchmark",
                                    style = typography.caption,
                                    color = colors.mutedText
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(90f, 95f, 98f).forEach { target ->
                                val isSelected = uiState.targetScore == target
                                if (isSelected) {
                                    OS95Button(
                                        text = "${target.toInt()}%",
                                        onClick = { viewModel.setTargetScore(target) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    OS95OutlinedButton(
                                        text = "${target.toInt()}%",
                                        onClick = { viewModel.setTargetScore(target) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Exam Timeline (Days Remaining)",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30 to "30d Sprint", 60 to "60d Midterm", 90 to "90d Term", 180 to "180d Finals").forEach { (days, label) ->
                                val isSelected = uiState.targetDaysAhead == days
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) colors.cardBackground else colors.surface)
                                        .border(1.dp, if (isSelected) colors.accent else colors.border)
                                        .clickable { viewModel.setTargetDaysAhead(days) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = typography.caption,
                                        color = if (isSelected) colors.accent else colors.primaryText
                                    )
                                }
                            }
                        }
                    }
                }

                OnboardingStep.SUBJECTS -> {
                    var newSubject by remember { mutableStateOf("") }
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Academic Subjects",
                                    style = typography.heroTitle,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = "Choose from 18+ academic subjects or add custom ones.",
                                    style = typography.bodySmall,
                                    color = colors.secondaryText
                                )
                            }
                            Text(
                                text = "${uiState.selectedSubjects.size} Enrolled",
                                style = typography.sectionTitle,
                                color = colors.accentCyan
                            )
                        }

                        // Stream Quick Presets
                        Text(
                            text = "Stream Quick Presets",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("PCM" to "Science PCM", "PCB" to "Medical PCB", "COMMERCE" to "Commerce", "HUMANITIES" to "Humanities").forEach { (preset, label) ->
                                OS95OutlinedButton(
                                    text = label,
                                    onClick = { viewModel.applyStreamPreset(preset) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Stream Filter Pills
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("ALL" to "All (18)", "SCIENCE" to "STEM", "COMMERCE" to "Commerce", "HUMANITIES" to "Humanities", "LANGUAGES" to "Languages").forEach { (filterKey, label) ->
                                val isSelected = uiState.selectedStreamFilter == filterKey
                                Box(
                                    modifier = Modifier
                                        .background(if (isSelected) colors.cardBackground else colors.surface)
                                        .border(1.dp, if (isSelected) colors.accent else colors.border)
                                        .clickable { viewModel.setStreamFilter(filterKey) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(text = label, style = typography.caption, color = if (isSelected) colors.accent else colors.secondaryText)
                                }
                            }
                        }

                        // Filtered Subjects Grid
                        val filteredSubjects = if (uiState.selectedStreamFilter == "ALL") {
                            OnboardingViewModel.ALL_SUBJECTS
                        } else {
                            OnboardingViewModel.ALL_SUBJECTS.filter { it.stream == uiState.selectedStreamFilter }
                        }

                        filteredSubjects.chunked(2).forEach { rowSubjects ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowSubjects.forEach { sub ->
                                    val isSelected = uiState.selectedSubjects.contains(sub.name)
                                    val subjectColor = try { Color(android.graphics.Color.parseColor(sub.colorHex)) } catch (_: Exception) { colors.accent }
                                    OS95Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.toggleSubject(sub.name) },
                                        backgroundColor = if (isSelected) colors.cardBackground else colors.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(subjectColor)
                                                )
                                                Text(
                                                    text = sub.name,
                                                    style = typography.bodySmall,
                                                    color = if (isSelected) colors.accent else colors.primaryText
                                                )
                                            }
                                            if (isSelected) {
                                                Text(text = "✓", style = typography.caption, color = colors.accentCyan)
                                            }
                                        }
                                    }
                                }
                                if (rowSubjects.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Custom Subject Entry
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OS95TextField(
                                value = newSubject,
                                onValueChange = { newSubject = it },
                                label = "Add Custom Subject",
                                placeholder = "e.g. Sanskrit / Astronomy",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            OS95Button(
                                text = "Add",
                                onClick = {
                                    if (newSubject.isNotBlank()) {
                                        viewModel.addCustomSubject(newSubject)
                                        newSubject = ""
                                    }
                                }
                            )
                        }
                    }
                }

                OnboardingStep.DAILY_HABIT -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Daily Habit & Pacing",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Configure your focus time quotas for Time-to-Marks intelligence.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )

                        Text(
                            text = "Daily Dedicated Study Commitment",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 90, 120, 180).forEach { mins ->
                                val isSelected = uiState.dailyStudyMinutes == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) colors.cardBackground else colors.surface)
                                        .border(1.dp, if (isSelected) colors.accent else colors.border)
                                        .clickable { viewModel.setDailyStudyMinutes(mins) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        style = typography.bodySmall,
                                        color = if (isSelected) colors.accent else colors.primaryText
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Default Mock Exam Duration",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(45, 60, 90, 120, 180).forEach { dur ->
                                val isSelected = uiState.defaultExamDurationMinutes == dur
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) colors.cardBackground else colors.surface)
                                        .border(1.dp, if (isSelected) colors.accent else colors.border)
                                        .clickable { viewModel.setDefaultExamDurationMinutes(dur) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${dur}m",
                                        style = typography.bodySmall,
                                        color = if (isSelected) colors.accent else colors.primaryText
                                    )
                                }
                            }
                        }

                        OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
                            Text(
                                text = "95OS uses these parameters to schedule high-yield revision slots and run accurate exam simulation pacing tests without exhausting your study stamina.",
                                style = typography.bodySmall,
                                color = colors.secondaryText
                            )
                        }
                    }
                }

                OnboardingStep.APPEARANCE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Visual Theme",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Select a calm, distraction-free palette for extended exam study.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.userSelectableThemes.chunked(2).forEach { rowThemes ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowThemes.forEach { mode ->
                                        val selected = uiState.themeMode == mode
                                        OS95Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.setThemeMode(mode) },
                                            backgroundColor = if (selected) colors.cardBackground else colors.surface
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = mode.displayName,
                                                        style = typography.bodySmall,
                                                        color = if (selected) colors.accent else colors.primaryText
                                                    )
                                                    if (selected) {
                                                        Text(text = "●", style = typography.caption, color = colors.accentCyan)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (mode.isDarkTheme) "Dark Canvas" else if (mode == ThemeMode.SYSTEM) "Auto Detect" else "Light Paper",
                                                    style = typography.caption,
                                                    color = colors.mutedText
                                                )
                                            }
                                        }
                                    }
                                    if (rowThemes.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                OnboardingStep.SUMMARY -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Blueprint Ready",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Your offline exam operating system is configured and calibrated.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )

                        OS95Card(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Student Profile", style = typography.caption, color = colors.mutedText)
                                    Text(text = uiState.studentName.ifBlank { "Alex" }, style = typography.bodySmall, color = colors.primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Academic Target", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${uiState.targetScore.toInt()}% Target", style = typography.sectionTitle, color = colors.accentCyan)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Exam Timeline", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${uiState.targetDaysAhead} Days Remaining", style = typography.bodySmall, color = colors.accent)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Enrolled Subjects", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${uiState.selectedSubjects.size} Subjects", style = typography.bodySmall, color = colors.primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Daily Commitment", style = typography.caption, color = colors.mutedText)
                                    Text(text = "${uiState.dailyStudyMinutes} min / day", style = typography.bodySmall, color = colors.primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Visual Theme", style = typography.caption, color = colors.mutedText)
                                    Text(text = uiState.themeMode.displayName, style = typography.bodySmall, color = colors.primaryText)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Persistence Mode", style = typography.caption, color = colors.mutedText)
                                    Text(text = "100% Offline SQLite (Local)", style = typography.caption, color = colors.success)
                                }
                            }
                        }

                        OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
                            Text(
                                text = "Subjects enrolled: ${uiState.selectedSubjects.joinToString(", ")}",
                                style = typography.caption,
                                color = colors.secondaryText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.m))

        // Navigation controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.step != OnboardingStep.WELCOME) {
                OS95OutlinedButton(
                    text = "Back",
                    onClick = { viewModel.previousStep() }
                )
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

            if (uiState.step == OnboardingStep.SUMMARY) {
                OS95Button(
                    text = "Launch 95OS",
                    onClick = { viewModel.completeOnboarding(onComplete) }
                )
            } else {
                OS95Button(
                    text = "Continue",
                    onClick = { viewModel.nextStep() }
                )
            }
        }
    }
}
