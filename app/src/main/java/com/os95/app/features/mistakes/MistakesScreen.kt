package com.os95.app.features.mistakes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.os95.app.domain.pdf.RevisionDocumentGenerator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.os95.app.core.database.entity.ChapterEntity
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
import com.os95.app.domain.model.LossCategory

@Composable
fun MistakesScreen(
    viewModel: MistakesViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRecovery: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    var showAddDialog by remember { mutableStateOf(false) }
    var showPrintDialog by remember { mutableStateOf(false) }
    var printFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var mistakeQuestion by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var studentAnswer by remember { mutableStateOf("") }
    var marksLostText by remember { mutableStateOf("1.0") }
    var selectedCategory by remember { mutableStateOf(LossCategory.CARELESS_MISTAKE) }
    var dialogSubjectId by remember(uiState.subjects) {
        mutableStateOf(uiState.subjects.firstOrNull()?.id ?: "")
    }
    var dialogChapters by remember { mutableStateOf<List<ChapterEntity>>(emptyList()) }
    var dialogChapterId by remember { mutableStateOf("") }

    LaunchedEffect(dialogSubjectId) {
        if (dialogSubjectId.isNotBlank()) {
            viewModel.getChaptersForSubject(dialogSubjectId).collect { chapters ->
                dialogChapters = chapters
                if (chapters.isNotEmpty() && (dialogChapterId.isBlank() || chapters.none { it.id == dialogChapterId })) {
                    dialogChapterId = chapters.first().id
                }
            }
        } else {
            dialogChapters = emptyList()
            dialogChapterId = ""
        }
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Mistake Bank",
            subtitle = "${uiState.activeMistakes.size} Active Mistakes",
            onBack = onNavigateBack,
            actions = {
                if (uiState.activeMistakes.isNotEmpty()) {
                    OS95IconButton(
                        icon = Icons.Outlined.Print,
                        contentDescription = "Print Remediation Sheet",
                        onClick = { showPrintDialog = true }
                    )
                }
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Log Mistake",
                    onClick = { showAddDialog = true }
                )
            }
        )

        val feedbackMsg = uiState.feedbackMessage ?: printFeedbackMessage
        if (feedbackMsg != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                color = colors.surface,
                shape = shapes.medium,
                border = BorderStroke(1.dp, colors.accentCyan)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = feedbackMsg,
                        style = typography.bodySmall,
                        color = colors.primaryText,
                        modifier = Modifier.weight(1f)
                    )
                    OS95IconButton(
                        icon = Icons.Outlined.Close,
                        contentDescription = "Dismiss",
                        onClick = {
                            viewModel.clearFeedback()
                            printFeedbackMessage = null
                        }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            OS95LoadingState(message = "Loading mistake logs...")
            return
        }

        if (uiState.activeMistakes.isEmpty()) {
            OS95EmptyState(
                title = "No logged mistakes",
                description = "Record lost marks and exam errors to convert lost marks into recovered score.",
                icon = Icons.Outlined.WarningAmber,
                primaryActionLabel = "Log Mistake",
                onPrimaryAction = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OS95Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.cardBackground
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL LOST MARKS",
                                    style = typography.caption,
                                    color = colors.mutedText
                                )
                                Text(
                                    text = "-${uiState.totalMarksLost.toInt()} marks",
                                    style = typography.statNumber,
                                    color = colors.error
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (onNavigateToRecovery != null) {
                                    OS95OutlinedButton(
                                        text = "Recovery Plan",
                                        onClick = onNavigateToRecovery
                                    )
                                } else {
                                    Text(
                                        text = "Recoverable via re-test",
                                        style = typography.caption,
                                        color = colors.secondaryText
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(spacing.m))
                }

                items(uiState.activeMistakes) { mistake ->
                    OS95Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(shapes.pill)
                                        .background(colors.cardBackground)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = mistake.lossCategory,
                                        style = typography.caption,
                                        color = colors.accent
                                    )
                                }
                                Text(
                                    text = "-${mistake.marksLost.toInt()} marks",
                                    style = typography.caption,
                                    color = colors.error
                                )
                            }

                            Spacer(modifier = Modifier.height(spacing.s))
                            Text(
                                text = mistake.question,
                                style = typography.sectionTitle,
                                color = colors.primaryText
                            )

                            if (mistake.studentAnswer.isNotBlank()) {
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = "Your Answer: ${mistake.studentAnswer}",
                                    style = typography.bodySmall,
                                    color = colors.mutedText
                                )
                            }

                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = "Correct Answer: ${mistake.correctAnswer}",
                                style = typography.bodySmall,
                                color = colors.accentCyan
                            )

                            Spacer(modifier = Modifier.height(spacing.m))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OS95OutlinedButton(
                                    text = "To Recall",
                                    icon = Icons.Outlined.Psychology,
                                    onClick = { viewModel.convertToRecallCard(mistake) }
                                )
                                Spacer(modifier = Modifier.width(spacing.s))
                                OS95Button(
                                    text = "Mark Resolved",
                                    icon = Icons.Outlined.Check,
                                    onClick = { viewModel.resolveMistake(mistake) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val hasSubjects = uiState.subjects.isNotEmpty()
        val hasChapters = dialogChapters.isNotEmpty()
        val canSave = hasSubjects && hasChapters && mistakeQuestion.isNotBlank() && correctAnswer.isNotBlank() && dialogChapterId.isNotBlank()

        OS95Dialog(
            title = "Log Test Mistake",
            confirmButtonText = "Save Mistake",
            onConfirm = {
                if (canSave) {
                    val marksVal = marksLostText.toFloatOrNull() ?: 1.0f
                    viewModel.recordMistake(
                        subjectId = dialogSubjectId,
                        chapterId = dialogChapterId,
                        question = mistakeQuestion.trim(),
                        studentAnswer = studentAnswer.trim(),
                        correctAnswer = correctAnswer.trim(),
                        category = selectedCategory.name,
                        marksLost = marksVal
                    )
                    mistakeQuestion = ""
                    correctAnswer = ""
                    studentAnswer = ""
                    marksLostText = "1.0"
                    showAddDialog = false
                }
            },
            onDismissRequest = {
                showAddDialog = false
                mistakeQuestion = ""
                correctAnswer = ""
                studentAnswer = ""
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!hasSubjects) {
                        Text(
                            text = "Please create at least one Subject and Chapter in Syllabus first before logging mistakes.",
                            style = typography.bodySmall,
                            color = colors.warning
                        )
                    } else if (!hasChapters) {
                        Text(
                            text = "No chapters found for this subject. Please add a chapter in Syllabus first to link mistakes.",
                            style = typography.bodySmall,
                            color = colors.warning
                        )
                    } else {
                        // Subject Selector Chips
                        Text(
                            text = "Subject",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.subjects.forEach { s ->
                                val isSel = dialogSubjectId == s.id
                                Surface(
                                    modifier = Modifier
                                        .clip(shapes.small)
                                        .clickable { dialogSubjectId = s.id },
                                    shape = shapes.small,
                                    color = if (isSel) colors.accent else colors.surface,
                                    border = BorderStroke(1.dp, if (isSel) colors.accent else colors.border)
                                ) {
                                    Text(
                                        text = s.name,
                                        style = typography.caption,
                                        color = if (isSel) colors.surface else colors.primaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Chapter Selector Chips
                        Text(
                            text = "Chapter",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dialogChapters.forEach { c ->
                                val isSel = dialogChapterId == c.id
                                Surface(
                                    modifier = Modifier
                                        .clip(shapes.small)
                                        .clickable { dialogChapterId = c.id },
                                    shape = shapes.small,
                                    color = if (isSel) colors.accent else colors.surface,
                                    border = BorderStroke(1.dp, if (isSel) colors.accent else colors.border)
                                ) {
                                    Text(
                                        text = c.name,
                                        style = typography.caption,
                                        color = if (isSel) colors.surface else colors.primaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Question input
                        OS95TextField(
                            value = mistakeQuestion,
                            onValueChange = { mistakeQuestion = it },
                            label = "Question Text",
                            placeholder = "Enter question or problem description...",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Correct Answer input
                        OS95TextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            label = "Correct Answer / Ideal Working",
                            placeholder = "Enter expected step/solution...",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Student Answer input
                        OS95TextField(
                            value = studentAnswer,
                            onValueChange = { studentAnswer = it },
                            label = "Your Answer (What went wrong)",
                            placeholder = "Enter your answer or skipped step...",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Marks Lost
                        OS95TextField(
                            value = marksLostText,
                            onValueChange = { marksLostText = it },
                            label = "Marks Lost",
                            placeholder = "e.g. 1.0, 2.0, 5.0",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Loss Category Selector Chips
                        Text(
                            text = "Error Category",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LossCategory.values().forEach { cat ->
                                val isSel = selectedCategory == cat
                                Surface(
                                    modifier = Modifier
                                        .clip(shapes.small)
                                        .clickable { selectedCategory = cat },
                                    shape = shapes.small,
                                    color = if (isSel) colors.accent else colors.surface,
                                    border = BorderStroke(1.dp, if (isSel) colors.accent else colors.border)
                                ) {
                                    Text(
                                        text = cat.label,
                                        style = typography.caption,
                                        color = if (isSel) colors.surface else colors.primaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    if (showPrintDialog) {
        val subjectNames = remember(uiState.subjects) {
            uiState.subjects.associate { it.id to it.name }
        }
        val generator = remember { RevisionDocumentGenerator() }
        val markdownText = remember(uiState.activeMistakes, subjectNames) {
            generator.generateMistakeRemediationMarkdown(uiState.activeMistakes, subjectNames)
        }

        OS95Dialog(
            title = "Remediation Sheet Export",
            message = "Generate an offline study sheet of your ${uiState.activeMistakes.size} unmastered mistakes to eliminate recurring errors.",
            confirmButtonText = "Copy Markdown",
            onConfirm = {
                clipboardManager.setText(AnnotatedString(markdownText))
                printFeedbackMessage = "Copied printable remediation sheet to clipboard!"
                showPrintDialog = false
            },
            onDismissRequest = {
                showPrintDialog = false
            },
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "You can copy markdown notes to your clipboard, or save a PDF document to disk for physical printing.",
                        style = typography.caption,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    OS95OutlinedButton(
                        text = "Generate PDF Document",
                        icon = Icons.Outlined.Print,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            try {
                                val pdfFile = generator.generateMistakeRemediationPdf(context, uiState.activeMistakes, subjectNames)
                                printFeedbackMessage = "Exported PDF (${pdfFile.length() / 1024} KB) to cache: ${pdfFile.name}"
                            } catch (e: Exception) {
                                printFeedbackMessage = "PDF Generation failed: ${e.message}"
                            }
                            showPrintDialog = false
                        }
                    )
                }
            }
        )
    }
}
