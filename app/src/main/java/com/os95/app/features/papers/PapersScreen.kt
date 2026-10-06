package com.os95.app.features.papers

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95Dialog
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TextField
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.domain.model.PaperDifficultyMode
import com.os95.app.domain.model.QuestionResultInput
import com.os95.app.domain.model.RepetitionPolicy
import com.os95.app.domain.pdf.PrintableExamPaperFormatter

@Composable
fun PapersScreen(
    viewModel: PapersViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToQuestionBank: () -> Unit = {},
    onNavigateToAnalysis: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    var showBuilderDialog by remember { mutableStateOf(false) }
    var paperTitleInput by remember { mutableStateOf("") }
    var showPrintableSheet by remember { mutableStateOf(false) }

    // If Physical Exam Mode is currently active, render the dedicated Exam Mode View!
    if (uiState.isExamModeActive && uiState.activePaper != null) {
        ExamModeFullscreenView(
            paper = uiState.activePaper!!,
            remainingSeconds = uiState.examRemainingSeconds,
            totalSeconds = uiState.examTotalSeconds,
            onFinishExam = { viewModel.finishExamMode() },
            onRequestExit = { viewModel.requestExitExam() }
        )

        if (uiState.showExitWarning) {
            OS95Dialog(
                title = "Exam in Progress",
                message = "An active examination timer is running. Are you sure you want to end your exam early?",
                confirmButtonText = "End Exam",
                dismissButtonText = "Keep Writing",
                onConfirm = { viewModel.confirmExitExam() },
                onDismissRequest = { viewModel.cancelExitExam() }
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "PaperPilot Hub",
            subtitle = "${uiState.papers.size} Practice Papers",
            onBack = onNavigateBack,
            actions = {
                OS95IconButton(
                    icon = Icons.Outlined.QuestionAnswer,
                    contentDescription = "Question Bank",
                    onClick = onNavigateToQuestionBank
                )
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Create Paper",
                    onClick = { showBuilderDialog = true }
                )
            }
        )

        if (uiState.isLoading) {
            OS95LoadingState(message = "Loading practice papers...")
            return
        }

        if (uiState.papers.isEmpty()) {
            OS95EmptyState(
                title = "No practice papers yet",
                description = "Build balanced practice examination papers with exact marks, custom time limits, and adaptive weak-topic prioritization.",
                icon = Icons.Outlined.Description,
                primaryActionLabel = "Build Practice Paper",
                onPrimaryAction = { showBuilderDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.papers) { paper ->
                    val paperResult = uiState.results.firstOrNull { it.paperId == paper.id }
                    OS95Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openPaperDetail(paper.id) }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = paper.title,
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = paper.status,
                                    style = typography.caption,
                                    color = colors.accent
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${paper.durationMinutes} min • Max Marks: ${paper.totalMarks.toInt()}",
                                    style = typography.caption,
                                    color = colors.secondaryText
                                )
                                if (paperResult != null) {
                                    val pct = if (paperResult.totalMarks > 0f) {
                                        paperResult.marksObtained / paperResult.totalMarks * 100f
                                    } else 0f
                                    Text(
                                        text = "Scored: ${paperResult.marksObtained.toInt()}/${paperResult.totalMarks.toInt()} (${String.format(Locale.US, "%.0f", pct)}%)",
                                        style = typography.caption,
                                        color = colors.accentCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OS95OutlinedButton(
                                    text = "Start Exam Mode",
                                    icon = Icons.Outlined.PlayArrow,
                                    onClick = { viewModel.startExamMode(paper) },
                                    modifier = Modifier.weight(1f)
                                )
                                OS95OutlinedButton(
                                    text = "Print / PDF",
                                    icon = Icons.Outlined.Print,
                                    onClick = {
                                        viewModel.openPaperDetail(paper.id)
                                        showPrintableSheet = true
                                    }
                                )
                                if (paperResult != null && onNavigateToAnalysis != null) {
                                    OS95OutlinedButton(
                                        text = "Analyze",
                                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                                        onClick = onNavigateToAnalysis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Paper Builder Modal / Dialog ---
    if (showBuilderDialog) {
        val scrollState = rememberScrollState()
        OS95Dialog(
            title = "PaperPilot — Build Paper",
            confirmButtonText = "Generate Paper",
            dismissButtonText = "Cancel",
            onConfirm = {
                viewModel.generatePaper(paperTitleInput)
            },
            onDismissRequest = {
                showBuilderDialog = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
                OS95TextField(
                    value = paperTitleInput,
                    onValueChange = { paperTitleInput = it },
                    label = "Paper Title (e.g. Unit Test — Calculus)"
                )

                // Subject Selector
                Text(text = "Subject:", style = typography.caption, color = colors.mutedText)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.subjects.forEach { sub ->
                        val isSel = uiState.selectedSubjectId == sub.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) colors.cardBackground else colors.surface)
                                .border(1.dp, if (isSel) colors.accent else colors.border)
                                .clickable { viewModel.selectSubject(sub.id) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sub.name,
                                style = typography.caption,
                                color = if (isSel) colors.accent else colors.primaryText
                            )
                        }
                    }
                }

                // Marks Presets
                Text(text = "Total Marks:", style = typography.caption, color = colors.mutedText)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(20f, 40f, 50f, 80f, 100f).forEach { marks ->
                        val isSel = uiState.selectedTotalMarks == marks
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .background(if (isSel) colors.cardBackground else colors.surface)
                                .border(1.dp, if (isSel) colors.accent else colors.border)
                                .clickable { viewModel.setTotalMarks(marks) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${marks.toInt()}m",
                                style = typography.caption,
                                color = if (isSel) colors.accent else colors.secondaryText
                            )
                        }
                    }
                }

                // Duration Presets
                Text(text = "Duration:", style = typography.caption, color = colors.mutedText)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 45, 60, 90, 120).forEach { mins ->
                        val isSel = uiState.selectedDurationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .background(if (isSel) colors.cardBackground else colors.surface)
                                .border(1.dp, if (isSel) colors.accent else colors.border)
                                .clickable { viewModel.setDuration(mins) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                style = typography.caption,
                                color = if (isSel) colors.accent else colors.secondaryText
                            )
                        }
                    }
                }

                // Difficulty Mode
                Text(text = "Difficulty Mode:", style = typography.caption, color = colors.mutedText)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaperDifficultyMode.values().forEach { mode ->
                        val isSel = uiState.selectedDifficultyMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .background(if (isSel) colors.cardBackground else colors.surface)
                                .border(1.dp, if (isSel) colors.accent else colors.border)
                                .clickable { viewModel.setDifficulty(mode) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.displayName.take(8),
                                style = typography.caption,
                                color = if (isSel) colors.accent else colors.secondaryText
                            )
                        }
                    }
                }

                // Adaptive Weighting Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                        .clickable { viewModel.setAdaptiveWeighting(!uiState.adaptiveWeakWeighting) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Adaptive Weak-Topic Prioritization", style = typography.bodySmall, color = colors.primaryText)
                        Text(text = "Allocate extra marks to chapters where marks were lost", style = typography.caption, color = colors.mutedText)
                    }
                    Checkbox(
                        checked = uiState.adaptiveWeakWeighting,
                        onCheckedChange = { viewModel.setAdaptiveWeighting(it) }
                    )
                }

                // Chapters Multi-Select Chips
                if (uiState.chapters.isNotEmpty()) {
                    Text(text = "Selected Chapters (${uiState.selectedChapterIds.size}/${uiState.chapters.size}):", style = typography.caption, color = colors.mutedText)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.chapters.forEach { ch ->
                            val isChecked = uiState.selectedChapterIds.contains(ch.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 44.dp)
                                    .clickable { viewModel.toggleChapter(ch.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleChapter(ch.id) }
                                )
                                Text(text = ch.name, style = typography.bodySmall, color = colors.primaryText)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Generated Candidate Preview Dialog ---
    uiState.generatedPaper?.let { generated ->
        OS95Dialog(
            title = "Generated Paper Preview",
            confirmButtonText = "Finalize & Save Paper",
            dismissButtonText = "Discard",
            onConfirm = {
                viewModel.finalizeGeneratedPaper()
                showBuilderDialog = false
            },
            onDismissRequest = {
                viewModel.clearFeedback()
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = generated.paper.title,
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Text(
                    text = "Total Marks: ${generated.paper.totalMarks.toInt()} • Duration: ${generated.paper.durationMinutes} min • Questions: ${generated.questions.size}",
                    style = typography.bodySmall,
                    color = colors.secondaryText
                )

                generated.adaptiveRationale?.let { rationale ->
                    Text(
                        text = "• $rationale",
                        style = typography.caption,
                        color = colors.accentCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Sections Partitioning:", style = typography.caption, color = colors.mutedText)
                generated.sections.forEach { sec ->
                    Text(
                        text = "• ${sec.name}: ${sec.questions.size} questions (${sec.sectionMarks.toInt()} Marks)",
                        style = typography.caption,
                        color = colors.primaryText
                    )
                }
            }
        }
    }

    // --- Generation Failure Dialog ---
    uiState.generationFailureReason?.let { reason ->
        OS95Dialog(
            title = "Cannot Generate Paper",
            message = reason,
            confirmButtonText = "Got it",
            onConfirm = { viewModel.clearFeedback() },
            onDismissRequest = { viewModel.clearFeedback() }
        ) {
            if (uiState.generationRecoverySuggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Suggestions:", style = typography.caption, color = colors.mutedText)
                    uiState.generationRecoverySuggestions.forEach { sug ->
                        Text(text = "• $sug", style = typography.caption, color = colors.secondaryText)
                    }
                }
            }
        }
    }

    // --- Printable View Sheet ---
    if (showPrintableSheet && uiState.activePaper != null) {
        val formatter = PrintableExamPaperFormatter()
        val subjectName = uiState.subjects.firstOrNull { it.id == uiState.activePaper?.subjectId }?.name ?: "Subject"
        val formatted = formatter.formatFromSnapshots(uiState.activePaper!!, subjectName, uiState.activePaperQuestions)
        val printableText = formatter.renderToPrintableText(formatted)

        OS95Dialog(
            title = "Printable Examination Sheet",
            confirmButtonText = "Close",
            onConfirm = { showPrintableSheet = false },
            onDismissRequest = { showPrintableSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = printableText,
                    style = typography.caption.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                    color = colors.primaryText
                )
            }
        }
    }

    // --- Result Entry Dialog ---
    if (uiState.showResultEntryDialog && uiState.activePaper != null) {
        val paper = uiState.activePaper!!
        var marksObtainedText by remember { mutableStateOf("") }
        val lostMarksMap = remember { mutableStateMapOf<String, Float>() }
        val lossCategoryMap = remember { mutableStateMapOf<String, String>() }

        OS95Dialog(
            title = "Record Exam Result",
            confirmButtonText = "Save Result",
            dismissButtonText = "Cancel",
            onConfirm = {
                val obtained = marksObtainedText.toFloatOrNull() ?: 0f
                val questionInputs = uiState.activePaperQuestions.map { pq ->
                    val lost = lostMarksMap[pq.id] ?: 0f
                    val cat = lossCategoryMap[pq.id] ?: "CARELESS_MISTAKE"
                    QuestionResultInput(
                        questionId = pq.questionId,
                        questionText = pq.snapshotQuestionText,
                        chapterId = pq.snapshotChapterId,
                        topicId = pq.snapshotTopicId,
                        marksAllocated = pq.snapshotMarks,
                        marksLost = lost,
                        lossCategory = cat
                    )
                }
                viewModel.recordDetailedResult(
                    marksObtained = obtained,
                    timeTakenMinutes = paper.durationMinutes,
                    questionResults = questionInputs
                )
            },
            onDismissRequest = { viewModel.dismissResultDialog() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${paper.title} (Max: ${paper.totalMarks.toInt()} Marks)",
                    style = typography.bodySmall,
                    color = colors.mutedText
                )

                OS95TextField(
                    value = marksObtainedText,
                    onValueChange = { marksObtainedText = it },
                    label = "Marks Obtained (e.g. 43)"
                )

                val obtainedVal = marksObtainedText.toFloatOrNull()
                if (obtainedVal != null && paper.totalMarks > 0f) {
                    val pct = (obtainedVal / paper.totalMarks) * 100f
                    Text(
                        text = "Score: ${String.format(Locale.US, "%.1f", pct)}%",
                        style = typography.sectionTitle,
                        color = colors.accentCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Question Mistake Diagnostics (Record marks lost):",
                    style = typography.caption,
                    color = colors.mutedText
                )

                uiState.activePaperQuestions.forEach { pq ->
                    val lostVal = lostMarksMap[pq.id] ?: 0f
                    val hasLost = lostVal > 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${pq.snapshotQuestionText.take(35)}... [${pq.snapshotMarks.toInt()}m]",
                                style = typography.caption,
                                color = colors.primaryText
                            )
                        }
                        Checkbox(
                            checked = hasLost,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    lostMarksMap[pq.id] = 1.0f
                                } else {
                                    lostMarksMap.remove(pq.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExamModeFullscreenView(
    paper: PaperEntity,
    remainingSeconds: Int,
    totalSeconds: Int,
    onFinishExam: () -> Unit,
    onRequestExit: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)
    val isWarning = remainingSeconds <= 600 // 10 minutes warning

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "PHYSICAL EXAM MODE",
                style = typography.caption,
                color = colors.accentCyan
            )
            Spacer(modifier = Modifier.height(spacing.xs))
            Text(
                text = paper.title,
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            Text(
                text = "Maximum Marks: ${paper.totalMarks.toInt()} • Write on Physical Paper with Pen",
                style = typography.caption,
                color = colors.mutedText
            )
        }

        // Center Countdown Timer Card
        OS95Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.cardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeFormatted,
                    style = typography.statNumber.copy(fontSize = 72.sp, lineHeight = 78.sp),
                    color = if (isWarning) colors.error else colors.accent
                )
                Spacer(modifier = Modifier.height(spacing.m))
                if (isWarning) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = "Warning",
                            tint = colors.error,
                            modifier = Modifier.height(18.dp)
                        )
                        Text(
                            text = "Final 10 Minutes: Review your answers",
                            style = typography.caption,
                            color = colors.error
                        )
                    }
                } else {
                    Text(
                        text = "Exam in progress • Offline conditions active",
                        style = typography.caption,
                        color = colors.secondaryText
                    )
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OS95Button(
                text = "Finish Exam & Enter Results",
                icon = Icons.Outlined.Check,
                onClick = onFinishExam,
                modifier = Modifier.fillMaxWidth()
            )
            OS95OutlinedButton(
                text = "End Exam Early",
                icon = Icons.Outlined.Stop,
                onClick = onRequestExit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
