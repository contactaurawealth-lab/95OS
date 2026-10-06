package com.os95.app.features.papers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.os95.app.domain.model.ExamReadinessResult
import kotlin.math.roundToInt

@Composable
fun ExamSimulatorScreen(
    viewModel: ExamSimulatorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRetest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

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
                    if (uiState.stage == SimulatorStage.IN_EXAM) {
                        viewModel.showSubmitDialog(true)
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
                    text = "EXAM READINESS SIMULATOR",
                    style = typography.caption.copy(letterSpacing = 1.sp),
                    color = colors.accent
                )
                Text(
                    text = when (uiState.stage) {
                        SimulatorStage.CONFIG -> "Official Examination Setup"
                        SimulatorStage.IN_EXAM -> "Timed Exam Runner"
                        SimulatorStage.RESULT -> "Exam Readiness Report"
                    },
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
            }
        }

        if (uiState.isLoading) {
            OS95LoadingState(message = "Simulating exam environment...")
            return
        }

        when (uiState.stage) {
            SimulatorStage.CONFIG -> {
                SimulatorConfigView(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            SimulatorStage.IN_EXAM -> {
                SimulatorInExamView(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            SimulatorStage.RESULT -> {
                SimulatorResultView(
                    uiState = uiState,
                    onNavigateToRetest = onNavigateToRetest,
                    onDone = onNavigateBack
                )
            }
        }

        // Accidental Submission Prevention Dialog
        if (uiState.showConfirmSubmitDialog) {
            val totalQ = uiState.questions.size
            val answeredQ = uiState.userAnswers.count { it.value.isNotBlank() }
            val markedQ = uiState.markedForReviewIndices.size
            val unansweredQ = totalQ - answeredQ

            AlertDialog(
                onDismissRequest = { viewModel.showSubmitDialog(false) },
                title = {
                    Text(
                        text = "Submit Simulated Exam?",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Please verify your completion status before final scoring:",
                            style = typography.body,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "• Answered: $answeredQ of $totalQ questions",
                            style = typography.caption,
                            color = colors.success
                        )
                        Text(
                            text = "• Marked for Review: $markedQ questions",
                            style = typography.caption,
                            color = colors.warning
                        )
                        if (unansweredQ > 0) {
                            Text(
                                text = "• Unanswered: $unansweredQ questions (will score 0)",
                                style = typography.caption,
                                color = colors.error
                            )
                        }
                    }
                },
                confirmButton = {
                    OS95Button(
                        text = "Submit Now",
                        onClick = { viewModel.confirmSubmit() }
                    )
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showSubmitDialog(false) }) {
                        Text(text = "Keep Working", color = colors.primaryText)
                    }
                },
                containerColor = colors.surface
            )
        }
    }
}

@Composable
private fun SimulatorConfigView(
    uiState: ExamSimulatorUiState,
    viewModel: ExamSimulatorViewModel
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
        // Description Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "EXAM CONDITIONS SIMULATION",
                    style = typography.caption,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Strict examination environment with live countdown, question status jump grid, review marking, and post-exam Readiness score computation.",
                    style = typography.body,
                    color = colors.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Selection
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "SUBJECT",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
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

        // Exam Duration & Marks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OS95Card(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "DURATION",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(30, 60, 90).forEach { mins ->
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

            OS95Card(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "TOTAL MARKS",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    listOf(25f, 50f, 80f).forEach { marks ->
                        val isSelected = uiState.totalMarks == marks
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(spacing.minTouchTarget)
                                .clip(shapes.small)
                                .background(if (isSelected) colors.accentCyan else colors.cardBackground)
                                .border(1.dp, if (isSelected) colors.accentCyan else colors.border, shapes.small)
                                .clickable { viewModel.setTotalMarks(marks) },
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Difficulty Selector
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "DIFFICULTY CALIBRATION",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EASY", "BALANCED", "HARD").forEach { diff ->
                        val isSelected = uiState.difficulty == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.minTouchTarget)
                                .clip(shapes.small)
                                .background(if (isSelected) colors.accent else colors.cardBackground)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, shapes.small)
                                .clickable { viewModel.setDifficulty(diff) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff,
                                style = typography.caption,
                                color = if (isSelected) colors.surface else colors.primaryText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OS95Button(
            text = "START SIMULATION EXAM",
            onClick = { viewModel.startExam() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimulatorInExamView(
    uiState: ExamSimulatorUiState,
    viewModel: ExamSimulatorViewModel
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes
    val scrollState = rememberScrollState()

    val questions = uiState.questions
    val currentIndex = uiState.currentQuestionIndex
    val currentQ = questions.getOrNull(currentIndex) ?: return
    val currentAnswer = uiState.userAnswers[currentIndex] ?: ""
    val isMarkedForReview = uiState.markedForReviewIndices.contains(currentIndex)

    // Formatted Countdown
    val minsLeft = uiState.timeRemainingSeconds / 60
    val secsLeft = uiState.timeRemainingSeconds % 60
    val isTimeWarning = minsLeft < 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Sticky Exam Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapes.medium)
                .background(if (isTimeWarning) colors.error.copy(alpha = 0.15f) else colors.cardBackground)
                .border(1.dp, if (isTimeWarning) colors.error else colors.border, shapes.medium)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = if (isTimeWarning) colors.error else colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%02d:%02d", minsLeft, secsLeft),
                    style = typography.sectionTitle,
                    color = if (isTimeWarning) colors.error else colors.primaryText
                )
            }

            // Mark for Review Toggle Button
            Row(
                modifier = Modifier
                    .height(spacing.minTouchTarget)
                    .clip(shapes.small)
                    .background(if (isMarkedForReview) colors.warning.copy(alpha = 0.2f) else colors.surface)
                    .border(1.dp, if (isMarkedForReview) colors.warning else colors.border, shapes.small)
                    .clickable { viewModel.toggleMarkForReview(currentIndex) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = if (isMarkedForReview) colors.warning else colors.mutedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMarkedForReview) "Marked" else "Review",
                    style = typography.caption,
                    color = if (isMarkedForReview) colors.warning else colors.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question Jump Grid
        Text(
            text = "QUESTION NAVIGATION",
            style = typography.caption,
            color = colors.mutedText
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            questions.forEachIndexed { idx, q ->
                val isAnswered = !uiState.userAnswers[idx].isNullOrBlank()
                val isReview = uiState.markedForReviewIndices.contains(idx)
                val isCurrent = idx == currentIndex

                val chipBg = when {
                    isCurrent -> colors.accent
                    isReview -> colors.warning
                    isAnswered -> colors.accentCyan
                    else -> colors.cardBackground
                }
                val chipText = if (isCurrent || isReview || isAnswered) colors.surface else colors.primaryText

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(shapes.small)
                        .background(chipBg)
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) colors.primaryText else colors.border,
                            shape = shapes.small
                        )
                        .clickable { viewModel.jumpToQuestion(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        style = typography.caption.copy(fontSize = 12.sp),
                        color = chipText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Question Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "QUESTION ${currentIndex + 1} OF ${questions.size}",
                        style = typography.caption,
                        color = colors.accent
                    )
                    Text(
                        text = "${currentQ.snapshotMarks.toInt()} Marks",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentQ.sectionName,
                    style = typography.caption,
                    color = colors.secondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentQ.snapshotQuestionText,
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Answer Input
        Text(
            text = "Student Answer Formulation:",
            style = typography.caption,
            color = colors.secondaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = currentAnswer,
            onValueChange = { viewModel.recordAnswer(currentIndex, it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter working, derivation, or final answer...", color = colors.mutedText) },
            shape = shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText
            ),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Footer Actions
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
                    text = "Submit Exam",
                    onClick = { viewModel.showSubmitDialog(true) }
                )
            }
        }
    }
}

@Composable
private fun SimulatorResultView(
    uiState: ExamSimulatorUiState,
    onNavigateToRetest: () -> Unit,
    onDone: () -> Unit
) {
    val res = uiState.readinessResult ?: return
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // EXAM READINESS: XX% Hero Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.cardBackground)
                .border(2.dp, colors.accentCyan, RoundedCornerShape(12.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "OFFICIAL EXAM READINESS",
                    style = typography.caption,
                    color = colors.accentCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${res.readinessScore.roundToInt()}%",
                    style = typography.heroTitle.copy(fontSize = 44.sp),
                    color = if (res.readinessScore >= 80f) colors.success else colors.primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Score: ${res.score.toInt()} • Accuracy: ${res.accuracy.roundToInt()}%",
                    style = typography.body,
                    color = colors.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostics Breakdown Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "READINESS FACTOR BREAKDOWN",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                res.readinessFactors.forEach { (factor, score) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = factor, style = typography.body, color = colors.primaryText)
                        Text(
                            text = "${score.roundToInt()}%",
                            style = typography.caption,
                            color = colors.accentCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error Diagnostics Card
        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "ERROR DIAGNOSTICS",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Easy Questions Missed: ${res.easyQuestionsMissedCount}",
                    style = typography.body,
                    color = if (res.easyQuestionsMissedCount > 0) colors.error else colors.primaryText
                )
                Text(
                    text = "• Conceptual Errors: ${res.conceptualMistakesCount}",
                    style = typography.body,
                    color = colors.primaryText
                )
                Text(
                    text = "• Calculation & Careless Errors: ${res.calculationMistakesCount + res.carelessMistakesCount}",
                    style = typography.body,
                    color = colors.primaryText
                )
                Text(
                    text = "• Avg Time / Question: ${res.avgTimePerQuestionSeconds.roundToInt()}s",
                    style = typography.body,
                    color = colors.primaryText
                )

                if (res.timeManagementWarning != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠ ${res.timeManagementWarning}",
                        style = typography.caption,
                        color = colors.warning
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weak Chapters Identified
        if (res.weakChapterNames.isNotEmpty()) {
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "WEAK CHAPTERS IDENTIFIED",
                        style = typography.caption,
                        color = colors.warning
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    res.weakChapterNames.forEach { chName ->
                        Text(
                            text = "• $chName",
                            style = typography.body,
                            color = colors.primaryText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Actions
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OS95Button(
                text = "Retest Weaknesses (Adaptive Re-Test)",
                onClick = onNavigateToRetest,
                modifier = Modifier.fillMaxWidth()
            )

            OS95OutlinedButton(
                text = "Done / Return to Dashboard",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
