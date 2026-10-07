package com.os95.app.features.papers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Refresh
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.ui.component.OS95DropdownSelector
import com.os95.app.core.ui.component.OS95MenuAction
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
    var dialogSubjectId by remember { mutableStateOf("") }
    if (dialogSubjectId.isBlank() && uiState.subjects.isNotEmpty()) {
        dialogSubjectId = uiState.subjects.first().id
    }
    val dialogChaptersFlow = remember(dialogSubjectId) {
        if (dialogSubjectId.isNotBlank()) viewModel.getChaptersForSubject(dialogSubjectId)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val dialogChapters by dialogChaptersFlow.collectAsState(initial = emptyList())
    var dialogChapterId by remember { mutableStateOf("") }
    LaunchedEffect(dialogChapters) {
        if (dialogChapterId !in dialogChapters.map { it.id }) {
            dialogChapterId = dialogChapters.firstOrNull()?.id ?: ""
        }
    }
    var selectedQuestionType by remember { mutableStateOf("SHORT_ANSWER") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Question Bank",
            subtitle = "${uiState.filteredQuestions.size} Questions",
            onBack = onNavigateBack,
            overflowActions = listOfNotNull(
                if (onNavigateToCsvImport != null) {
                    OS95MenuAction(
                        label = "Import CSV",
                        icon = Icons.Outlined.FileDownload,
                        onClick = onNavigateToCsvImport
                    )
                } else null,
                OS95MenuAction(
                    label = "Reset All Filters",
                    icon = Icons.Outlined.Refresh,
                    onClick = {
                        viewModel.setSubjectFilter(null)
                        viewModel.setDifficultyFilter(null)
                        viewModel.setSearchQuery("")
                    }
                )
            ),
            actions = {
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add Question",
                    onClick = { showAddDialog = true }
                )
            }
        )

        // Search Bar & Dropdown Selectors
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val allSubjectsList: List<SubjectEntity?> = listOf(null) + uiState.subjects
                val selectedSubject = uiState.subjects.find { it.id == uiState.selectedSubjectId }

                OS95DropdownSelector(
                    label = "Subject",
                    selectedValue = selectedSubject,
                    items = allSubjectsList,
                    itemLabel = { it?.name ?: "All Subjects" },
                    onItemSelected = { viewModel.setSubjectFilter(it?.id) },
                    modifier = Modifier.weight(1f)
                )

                val difficulties = listOf(null, "EASY", "MEDIUM", "HARD")
                OS95DropdownSelector(
                    label = "Difficulty",
                    selectedValue = uiState.selectedDifficulty,
                    items = difficulties,
                    itemLabel = {
                        when (it) {
                            "EASY" -> "Easy"
                            "MEDIUM" -> "Medium"
                            "HARD" -> "Hard"
                            else -> "All Levels"
                        }
                    },
                    onItemSelected = { viewModel.setDifficultyFilter(it) },
                    modifier = Modifier.weight(1f)
                )
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
        val hasSubjects = uiState.subjects.isNotEmpty()
        val hasChapters = dialogChapters.isNotEmpty()
        val canSave = hasSubjects && hasChapters && questionText.isNotBlank() && dialogChapterId.isNotBlank()

        OS95Dialog(
            title = "Add Question to Bank",
            confirmButtonText = "Save Question",
            onConfirm = {
                if (canSave) {
                    val marksVal = marksText.toFloatOrNull() ?: 2.0f
                    viewModel.addQuestion(
                        subjectId = dialogSubjectId,
                        chapterId = dialogChapterId,
                        text = questionText.trim(),
                        marks = marksVal,
                        difficulty = selectedDifficulty,
                        questionType = selectedQuestionType
                    )
                    questionText = ""
                    showAddDialog = false
                }
            },
            onDismissRequest = {
                showAddDialog = false
                questionText = ""
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!hasSubjects) {
                        Text(
                            text = "Please create at least one Subject and Chapter in Syllabus first before adding questions.",
                            style = typography.bodySmall,
                            color = colors.warning
                        )
                    } else if (!hasChapters) {
                        Text(
                            text = "No chapters found for this subject. Please add a chapter in Syllabus first to link questions.",
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

                        // Question Text
                        OS95TextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            label = "Question Text",
                            placeholder = "Enter question or problem statement...",
                            singleLine = false
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Marks
                        OS95TextField(
                            value = marksText,
                            onValueChange = { marksText = it },
                            label = "Marks",
                            placeholder = "e.g. 2.0"
                        )

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Difficulty Chips
                        Text(
                            text = "Difficulty",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                                val isSel = selectedDifficulty == diff
                                Surface(
                                    modifier = Modifier
                                        .clip(shapes.small)
                                        .clickable { selectedDifficulty = diff },
                                    shape = shapes.small,
                                    color = if (isSel) colors.accent else colors.surface,
                                    border = BorderStroke(1.dp, if (isSel) colors.accent else colors.border)
                                ) {
                                    Text(
                                        text = diff,
                                        style = typography.caption,
                                        color = if (isSel) colors.surface else colors.primaryText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.m))

                        // Type Chips
                        Text(
                            text = "Question Type",
                            style = typography.caption,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("MCQ", "SHORT_ANSWER", "LONG_ANSWER").forEach { type ->
                                val isSel = selectedQuestionType == type
                                Surface(
                                    modifier = Modifier
                                        .clip(shapes.small)
                                        .clickable { selectedQuestionType = type },
                                    shape = shapes.small,
                                    color = if (isSel) colors.accent else colors.surface,
                                    border = BorderStroke(1.dp, if (isSel) colors.accent else colors.border)
                                ) {
                                    Text(
                                        text = type.replace("_", " "),
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
}
