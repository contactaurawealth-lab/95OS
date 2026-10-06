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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95Dialog
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme

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
        val defaultSubjectId = uiState.subjects.firstOrNull()?.id ?: "general_subject"
        OS95Dialog(
            title = "Log Test Mistake",
            message = "Enter question text and correct answer:",
            confirmButtonText = "Save",
            onConfirm = {
                if (mistakeQuestion.isNotBlank() && correctAnswer.isNotBlank()) {
                    viewModel.recordMistake(
                        subjectId = defaultSubjectId,
                        question = mistakeQuestion,
                        studentAnswer = "",
                        correctAnswer = correctAnswer,
                        category = "CARELESS_MISTAKE",
                        marksLost = 1.0f
                    )
                    mistakeQuestion = ""
                    correctAnswer = ""
                    showAddDialog = false
                }
            },
            onDismissRequest = {
                showAddDialog = false
                mistakeQuestion = ""
                correctAnswer = ""
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
            title = "Print Remediation Sheet",
            message = "Generate an offline study sheet of your ${uiState.activeMistakes.size} unmastered mistakes to eliminate recurring errors.",
            confirmButtonText = "Copy Markdown",
            onConfirm = {
                clipboardManager.setText(AnnotatedString(markdownText))
                printFeedbackMessage = "Copied printable remediation sheet to clipboard!"
                showPrintDialog = false
            },
            onDismissRequest = {
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
