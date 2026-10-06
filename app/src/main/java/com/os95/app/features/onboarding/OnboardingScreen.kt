package com.os95.app.features.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()

    val stepProgress = (uiState.step.ordinal + 1).toFloat() / OnboardingStep.values().size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Step progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "95OS Setup",
                style = typography.caption.copy(letterSpacing = 1.sp),
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
        Spacer(modifier = Modifier.height(spacing.xl))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            when (uiState.step) {
                OnboardingStep.WELCOME -> {
                    Column(verticalArrangement = Arrangement.Center) {
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
                        Spacer(modifier = Modifier.height(spacing.m))
                        Text(
                            text = "«Syllabus → Learn → Recall → Practice → Result → Lost Marks → Mistakes → Revision → Re-test → Score Progress»",
                            style = typography.body,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.l))
                        OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
                            Text(
                                text = "Engineered for one singular objective: systematically moving you toward your target score, especially 95%+.\n\n100% offline. Zero cloud accounts. Your study data belongs to you.",
                                style = typography.bodySmall,
                                color = colors.primaryText
                            )
                        }
                    }
                }

                OnboardingStep.PROFILE -> {
                    Column {
                        Text(
                            text = "Student Profile",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Tell us how you would like to be addressed.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))
                        OS95TextField(
                            value = uiState.studentName,
                            onValueChange = { viewModel.setStudentName(it) },
                            label = "Your Name",
                            placeholder = "e.g. Alex"
                        )
                        Spacer(modifier = Modifier.height(spacing.m))
                        OS95TextField(
                            value = uiState.gradeLevel,
                            onValueChange = { viewModel.setGradeLevel(it) },
                            label = "Grade or Academic Exam",
                            placeholder = "e.g. Grade 12 / Board Exam / Entrance"
                        )
                    }
                }

                OnboardingStep.SUBJECTS -> {
                    var newSubject by remember { mutableStateOf("") }
                    Column {
                        Text(
                            text = "Academic Subjects",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Select or add the core subjects you are preparing for.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.l))

                        listOf("Mathematics", "Physics", "Chemistry", "Biology", "History", "Computer Science").forEach { sub ->
                            val selected = uiState.selectedSubjects.contains(sub)
                            OS95Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                backgroundColor = if (selected) colors.cardBackground else colors.surface,
                                onClick = { viewModel.toggleSubject(sub) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub,
                                        style = typography.body,
                                        color = if (selected) colors.accent else colors.primaryText
                                    )
                                    Text(
                                        text = if (selected) "Enrolled" else "Tap to add",
                                        style = typography.caption,
                                        color = colors.mutedText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.m))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OS95TextField(
                                value = newSubject,
                                onValueChange = { newSubject = it },
                                label = "Add Custom Subject",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.height(spacing.s))
                        }
                    }
                }

                OnboardingStep.EXAM_TARGET -> {
                    Column {
                        Text(
                            text = "Target Percentage",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "The deterministic benchmark for the 95OS Target Engine.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))

                        OS95Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.cardBackground
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.targetScore.toInt()}%",
                                    style = typography.statNumber.copy(fontSize = 54.sp),
                                    color = colors.accentCyan
                                )
                                Text(
                                    text = "Standard 95OS Mastery Goal",
                                    style = typography.caption,
                                    color = colors.mutedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.l))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                    }
                }

                OnboardingStep.APPEARANCE -> {
                    Column {
                        Text(
                            text = "Visual Theme",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Choose your calm, distraction-free appearance.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))

                        ThemeMode.values().forEach { mode ->
                            val selected = uiState.themeMode == mode
                            OS95Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                backgroundColor = if (selected) colors.cardBackground else colors.surface,
                                onClick = { viewModel.setThemeMode(mode) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = mode.name,
                                        style = typography.body,
                                        color = if (selected) colors.accent else colors.primaryText
                                    )
                                    if (selected) {
                                        Text(
                                            text = "Selected",
                                            style = typography.caption,
                                            color = colors.accentCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                OnboardingStep.SUMMARY -> {
                    Column {
                        Text(
                            text = "Ready to Launch",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Your offline exam operating system is configured.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xl))

                        OS95Card(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Student: ${uiState.studentName.ifBlank { "Student" }}", style = typography.bodySmall, color = colors.primaryText)
                                Text(text = "Target: ${uiState.targetScore.toInt()}%", style = typography.bodySmall, color = colors.accentCyan)
                                Text(text = "Subjects: ${uiState.selectedSubjects.joinToString(", ")}", style = typography.bodySmall, color = colors.secondaryText)
                                Text(text = "Theme: ${uiState.themeMode.name}", style = typography.bodySmall, color = colors.secondaryText)
                                Text(text = "Storage: 100% Local Offline SQLite", style = typography.caption, color = colors.mutedText)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.l))

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
