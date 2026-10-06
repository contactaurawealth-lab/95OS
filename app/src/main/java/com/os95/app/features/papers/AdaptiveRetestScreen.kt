package com.os95.app.features.papers

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.theme.OS95Theme
import kotlin.math.roundToInt

@Composable
fun AdaptiveRetestScreen(
    viewModel: AdaptiveRetestViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes
    var showAbandonDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = uiState.stage == AdaptiveRetestStage.IN_TEST) {
        showAbandonDialog = true
    }

    if (showAbandonDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            title = {
                Text(
                    text = "Abandon Re-Test Session?",
                    style = typography.sectionTitle,
                    color = colors.warning
                )
            },
            text = {
                Text(
                    text = "Your answers for this diagnostic re-test will not be recorded and unmastered mistakes will remain open. Are you sure you wish to exit?",
                    style = typography.bodySmall,
                    color = colors.primaryText
                )
            },
            confirmButton = {
                OS95Button(
                    text = "Exit Re-Test",
                    onClick = {
                        showAbandonDialog = false
                        viewModel.restart()
                    }
                )
            },
            dismissButton = {
                OS95OutlinedButton(
                    text = "Continue Test",
                    onClick = { showAbandonDialog = false }
                )
            },
            containerColor = colors.surface,
            textContentColor = colors.primaryText
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OS95IconButton(
                onClick = {
                    if (uiState.stage == AdaptiveRetestStage.IN_TEST) {
                        showAbandonDialog = true
                    } else {
                        onNavigateBack()
                    }
                },
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(spacing.minTouchTarget)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "ADAPTIVE RE-TEST",
                    style = typography.caption.copy(letterSpacing = 1.sp),
                    color = colors.accent
                )
                Text(
                    text = when (uiState.stage) {
                        AdaptiveRetestStage.CONFIG -> "Diagnostic Configuration"
                        AdaptiveRetestStage.IN_TEST -> "Active Re-Test Session"
                        AdaptiveRetestStage.SUMMARY -> "Diagnostic Evaluation"
                    },
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
            }
        }

        if (uiState.isLoading) {
            OS95LoadingState(message = "Assembling targeted re-test questions...")
            return
        }

        when (uiState.stage) {
            AdaptiveRetestStage.CONFIG -> {
                ConfigView(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            AdaptiveRetestStage.IN_TEST -> {
                InTestView(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            AdaptiveRetestStage.SUMMARY -> {
                SummaryView(
                    uiState = uiState,
                    onStartRetest = { viewModel.generateRetest() },
                    onReviewMistakes = onNavigateToMistakes,
                    onSkip = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun ConfigView(
    uiState: AdaptiveRetestUiState,
    viewModel: AdaptiveRetestViewModel
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Blueprint Distribution Info Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "TARGETED QUESTION DISTRIBUTION",
                    style = typography.caption,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Automatically structures questions using your real historical performance:",
                    style = typography.body,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 50% Weak Syllabus Areas\n• 30% Recently Missed Questions & Concepts\n• 20% Mixed Active Revision",
                    style = typography.caption.copy(lineHeight = 20.sp),
                    color = colors.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Filter
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "TARGET SUBJECT",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(10.dp))

                // All Subjects Pill
                val isAllSelected = uiState.selectedSubjectId == null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.minTouchTarget)
                        .clip(shapes.small)
                        .background(if (isAllSelected) colors.accent else colors.cardBackground)
                        .border(1.dp, if (isAllSelected) colors.accent else colors.border, shapes.small)
                        .clickable { viewModel.selectSubject(null) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All Subjects (Cumulative Weaknesses)",
                        style = typography.caption,
                        color = if (isAllSelected) colors.surface else colors.primaryText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                uiState.subjects.forEach { sub ->
                    val isSelected = uiState.selectedSubjectId == sub.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.minTouchTarget)
                            .clip(shapes.small)
                            .background(if (isSelected) colors.accent else colors.cardBackground)
                            .border(1.dp, if (isSelected) colors.accent else colors.border, shapes.small)
                            .clickable { viewModel.selectSubject(sub.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sub.name,
                            style = typography.caption,
                            color = if (isSelected) colors.surface else colors.primaryText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Marks & Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OS95Card(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "TOTAL MARKS",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(15f, 25f, 40f).forEach { marks ->
                        val isSelected = uiState.targetMarks == marks
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(spacing.minTouchTarget)
                                .clip(shapes.small)
                                .background(if (isSelected) colors.accentCyan else colors.cardBackground)
                                .border(1.dp, if (isSelected) colors.accentCyan else colors.border, shapes.small)
                                .clickable { viewModel.setTargetMarks(marks) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${marks.toInt()} Marks",
                                style = typography.caption,
                                color = if (isSelected) colors.surface else colors.primaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            OS95Card(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "TIME LIMIT",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(15, 30, 45).forEach { mins ->
                        val isSelected = uiState.durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(spacing.minTouchTarget)
                                .clip(shapes.small)
                                .background(if (isSelected) colors.accentCyan else colors.cardBackground)
                                .border(1.dp, if (isSelected) colors.accentCyan else colors.border, shapes.small)
                                .clickable { viewModel.setDuration(mins) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins} mins",
                                style = typography.caption,
                                color = if (isSelected) colors.surface else colors.primaryText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OS95Button(
            text = "START ADAPTIVE RE-TEST",
            onClick = { viewModel.generateRetest() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InTestView(
    uiState: AdaptiveRetestUiState,
    viewModel: AdaptiveRetestViewModel
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes
    val scrollState = rememberScrollState()

    val questions = uiState.questions
    if (questions.isEmpty()) {
        OS95Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "No questions generated. Check question bank or syllabus.",
                style = typography.body,
                color = colors.primaryText
            )
        }
        return
    }

    val currentIndex = uiState.currentQuestionIndex
    val currentQ = questions.getOrNull(currentIndex) ?: return
    val currentAnswer = uiState.userAnswers[currentIndex] ?: ""
    val awardedMarks = uiState.marksAwardedMap[currentIndex] ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Question Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUESTION ${currentIndex + 1} OF ${questions.size}",
                style = typography.caption,
                color = colors.accent
            )
            Text(
                text = "${currentQ.snapshotMarks} Marks • ${currentQ.snapshotDifficulty}",
                style = typography.caption,
                color = colors.secondaryText
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Question Content Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = currentQ.sectionName,
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentQ.snapshotQuestionText,
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Response Input
        Text(
            text = "Your Solution / Key Steps:",
            style = typography.caption,
            color = colors.secondaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = currentAnswer,
            onValueChange = { viewModel.recordAnswer(currentIndex, it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Write brief response or working...", color = colors.mutedText) },
            shape = shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText
            ),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Self-Scoring Bar
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "SELF-EVALUATION (Marks Awarded):",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val maxM = currentQ.snapshotMarks
                    val stepOptions = listOf(0f, maxM * 0.5f, maxM)
                    stepOptions.forEach { m ->
                        val isSelected = awardedMarks == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.minTouchTarget)
                                .clip(shapes.small)
                                .background(if (isSelected) colors.accent else colors.cardBackground)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, shapes.small)
                                .clickable { viewModel.recordMarksAwarded(currentIndex, m) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${String.format("%.1f", m)}m",
                                style = typography.caption,
                                color = if (isSelected) colors.surface else colors.primaryText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OS95OutlinedButton(
                text = "Previous",
                onClick = { viewModel.previousQuestion() },
                enabled = currentIndex > 0
            )

            if (currentIndex < questions.size - 1) {
                OS95Button(
                    text = "Next Question",
                    onClick = { viewModel.nextQuestion() }
                )
            } else {
                OS95Button(
                    text = "Finish Re-Test",
                    onClick = { viewModel.finishRetest() }
                )
            }
        }
    }
}

@Composable
private fun SummaryView(
    uiState: AdaptiveRetestUiState,
    onStartRetest: () -> Unit,
    onReviewMistakes: () -> Unit,
    onSkip: () -> Unit
) {
    val summary = uiState.completionSummary ?: return
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Results Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "RE-TEST PERFORMANCE",
                    style = typography.caption,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${summary.marksObtained.toInt()} / ${summary.totalMarks.toInt()}",
                            style = typography.heroTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Score Obtained",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${summary.accuracyPercentage.roundToInt()}%",
                            style = typography.heroTitle,
                            color = if (summary.accuracyPercentage >= 75f) colors.success else colors.warning
                        )
                        Text(
                            text = "Accuracy Rate",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mistakes Fixed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${summary.mistakesFixedCount} mistakes resolved (+${summary.marksRecovered.toInt()} marks recovered)",
                        style = typography.body,
                        color = colors.primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Remaining Weak Topics
        if (summary.remainingWeakTopics.isNotEmpty()) {
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REMAINING WEAK TOPICS",
                            style = typography.caption,
                            color = colors.warning
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    summary.remainingWeakTopics.forEach { topicName ->
                        Text(
                            text = "• $topicName",
                            style = typography.body,
                            color = colors.primaryText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recommended Next Action
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "RECOMMENDED ACTION",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary.recommendedNextAction,
                    style = typography.body,
                    color = colors.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3 Actions: START RETEST, REVIEW MISTAKES, SKIP
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OS95Button(
                text = "START RETEST",
                onClick = onStartRetest,
                modifier = Modifier.fillMaxWidth()
            )

            OS95OutlinedButton(
                text = "REVIEW MISTAKES",
                onClick = onReviewMistakes,
                modifier = Modifier.fillMaxWidth()
            )

            OS95OutlinedButton(
                text = "SKIP",
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
