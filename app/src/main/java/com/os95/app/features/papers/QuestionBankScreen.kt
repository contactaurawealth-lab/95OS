package com.os95.app.features.papers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Icon
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
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95Dialog
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95TextField
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun QuestionBankScreen(
    viewModel: QuestionBankViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCsvImport: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    var showAddDialog by remember { mutableStateOf(false) }
    var questionText by remember { mutableStateOf("") }
    var marksText by remember { mutableStateOf("2.0") }
    var selectedDifficulty by remember { mutableStateOf("MEDIUM") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Question Bank",
            subtitle = "${uiState.filteredQuestions.size} Questions",
            onBack = onNavigateBack,
            actions = {
                if (onNavigateToCsvImport != null) {
                    OS95IconButton(
                        icon = androidx.compose.material.icons.Icons.Outlined.Description,
                        contentDescription = "Import CSV",
                        onClick = onNavigateToCsvImport
                    )
                }
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add Question",
                    onClick = { showAddDialog = true }
                )
            }
        )

        // Search Bar & Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OS95TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = "Search questions",
                placeholder = "Filter by keywords or concepts..."
            )

            Spacer(modifier = Modifier.height(spacing.s))

            // Difficulty filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val difficulties = listOf(null to "All", "EASY" to "Easy", "MEDIUM" to "Medium", "HARD" to "Hard")
                difficulties.forEach { (diff, label) ->
                    val isSelected = uiState.selectedDifficulty == diff
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 44.dp)
                            .clip(shapes.pill)
                            .background(if (isSelected) colors.accent else colors.cardBackground)
                            .clickable { viewModel.setDifficultyFilter(diff) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = typography.caption,
                            color = if (isSelected) colors.surface else colors.secondaryText
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            OS95LoadingState(message = "Loading questions...")
            return
        }

        if (uiState.filteredQuestions.isEmpty()) {
            OS95EmptyState(
                title = "No questions found",
                description = "Add questions to your local question bank to enable PaperPilot automatic exam paper generation.",
                icon = Icons.Outlined.QuestionAnswer,
                primaryActionLabel = "Add Question",
                onPrimaryAction = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filteredQuestions) { question ->
                    OS95Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(shapes.pill)
                                            .background(colors.cardBackground)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${question.marks.toInt()} marks",
                                            style = typography.caption,
                                            color = colors.accent
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(shapes.pill)
                                            .background(colors.cardBackground)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = question.difficulty,
                                            style = typography.caption,
                                            color = colors.mutedText
                                        )
                                    }
                                }

                                OS95IconButton(
                                    icon = Icons.Outlined.Delete,
                                    contentDescription = "Delete Question",
                                    onClick = { viewModel.deleteQuestion(question) }
                                )
                            }

                            Spacer(modifier = Modifier.height(spacing.s))
                            Text(
                                text = question.questionText,
                                style = typography.body,
                                color = colors.primaryText
                            )

                            if (question.markingScheme.isNotBlank()) {
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    text = "Marking Scheme: ${question.markingScheme}",
                                    style = typography.caption,
                                    color = colors.secondaryText
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
            title = "Add Question to Bank",
            message = "Enter question text and marks distribution:",
            confirmButtonText = "Save Question",
            onConfirm = {
                if (questionText.isNotBlank()) {
                    val marksVal = marksText.toFloatOrNull() ?: 1.0f
                    viewModel.addQuestion(
                        subjectId = defaultSubjectId,
                        chapterId = "general_chapter",
                        text = questionText,
                        marks = marksVal,
                        difficulty = selectedDifficulty,
                        questionType = "SHORT_ANSWER"
                    )
                    questionText = ""
                    showAddDialog = false
                }
            },
            onDismissRequest = {
                showAddDialog = false
                questionText = ""
            }
        )
    }
}
