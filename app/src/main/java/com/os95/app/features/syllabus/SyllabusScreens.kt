package com.os95.app.features.syllabus

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
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
import com.os95.app.domain.model.ExamRelevance
import com.os95.app.domain.model.TopicMasteryState

@Composable
fun SyllabusScreen(
    viewModel: SyllabusViewModel,
    onOpenSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Academic Syllabus",
            subtitle = "${uiState.subjects.size} Subjects",
            actions = {
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add Subject",
                    onClick = { showAddSubjectDialog = true }
                )
            }
        )

        if (uiState.isLoading) {
            OS95LoadingState(message = "Loading syllabus tree...")
        } else if (uiState.subjects.isEmpty()) {
            OS95EmptyState(
                title = "No subjects added",
                description = "Add your academic subjects (e.g. Mathematics, Physics, Chemistry) to establish your syllabus hierarchy.",
                primaryActionLabel = "Add Subject",
                onPrimaryAction = { showAddSubjectDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.subjects) { subject ->
                    OS95Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenSubject(subject.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(spacing.m))
                                Text(
                                    text = subject.name,
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = colors.mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        OS95Dialog(
            title = "Add Subject",
            message = "Enter the name of your subject:",
            confirmButtonText = "Create",
            onConfirm = {
                if (newSubjectName.isNotBlank()) {
                    viewModel.addSubject(newSubjectName)
                    newSubjectName = ""
                    showAddSubjectDialog = false
                }
            },
            onDismissRequest = {
                showAddSubjectDialog = false
                newSubjectName = ""
            }
        )
    }
}

@Composable
fun SubjectDetailScreen(
    subjectId: String,
    viewModel: SyllabusViewModel,
    onBack: () -> Unit,
    onOpenChapter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var newChapterName by remember { mutableStateOf("") }

    LaunchedEffect(subjectId) {
        viewModel.selectSubject(subjectId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = uiState.currentSubject?.name ?: "Subject Chapters",
            subtitle = "${uiState.currentChapters.size} Chapters",
            onBack = onBack,
            actions = {
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add Chapter",
                    onClick = { showAddChapterDialog = true }
                )
            }
        )

        if (uiState.currentChapters.isEmpty()) {
            OS95EmptyState(
                title = "No chapters in this subject",
                description = "Divide this subject into manageable chapters or modules to track topics.",
                primaryActionLabel = "Add Chapter",
                onPrimaryAction = { showAddChapterDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.currentChapters) { chapter ->
                    OS95Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenChapter(chapter.id) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chapter.name,
                                style = typography.sectionTitle,
                                color = colors.primaryText
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = colors.mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddChapterDialog) {
        OS95Dialog(
            title = "Add Chapter",
            message = "Enter chapter or unit name:",
            confirmButtonText = "Create",
            onConfirm = {
                if (newChapterName.isNotBlank()) {
                    viewModel.addChapter(subjectId, newChapterName)
                    newChapterName = ""
                    showAddChapterDialog = false
                }
            },
            onDismissRequest = {
                showAddChapterDialog = false
                newChapterName = ""
            }
        )
    }
}

@Composable
fun ChapterDetailScreen(
    chapterId: String,
    viewModel: SyllabusViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }
    var selectedRelevance by remember { mutableStateOf("MEDIUM") }

    LaunchedEffect(chapterId) {
        viewModel.selectChapter(chapterId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Chapter Topics",
            subtitle = "${uiState.currentTopics.size} Topics",
            onBack = onBack,
            actions = {
                OS95IconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add Topic",
                    onClick = { showAddTopicDialog = true }
                )
            }
        )

        if (uiState.currentTopics.isEmpty()) {
            OS95EmptyState(
                title = "No topics listed",
                description = "Add granular topics and set their exam relevance to track mastery progression toward 95%.",
                primaryActionLabel = "Add Topic",
                onPrimaryAction = { showAddTopicDialog = true }
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OS95OutlinedButton(
                    text = "Mark All Revised",
                    onClick = { viewModel.updateChapterMastery(chapterId, "REVISED") },
                    modifier = Modifier.weight(1f)
                )
                OS95OutlinedButton(
                    text = "Mark All Mastered",
                    onClick = { viewModel.updateChapterMastery(chapterId, "MASTERED") },
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.currentTopics) { topic ->
                    OS95Card(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = topic.name,
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(shapes.pill)
                                        .background(colors.cardBackground)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = topic.examRelevance,
                                        style = typography.caption,
                                        color = if (topic.examRelevance == "HIGH") colors.accent else colors.mutedText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(spacing.m))

                            // Interactive 4-Stage Mastery Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TopicMasteryState.values().forEach { state ->
                                    val isCurrent = topic.masteryState == state.name
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 44.dp)
                                            .clip(shapes.small)
                                            .background(if (isCurrent) colors.accent else colors.cardBackground)
                                            .clickable { viewModel.updateTopicMastery(topic, state.name) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = state.label,
                                            style = typography.caption,
                                            color = if (isCurrent) colors.surface else colors.secondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddTopicDialog) {
        OS95Dialog(
            title = "Add Topic",
            message = "Enter topic title:",
            confirmButtonText = "Create",
            onConfirm = {
                if (newTopicName.isNotBlank()) {
                    viewModel.addTopic(chapterId, newTopicName, selectedRelevance)
                    newTopicName = ""
                    showAddTopicDialog = false
                }
            },
            onDismissRequest = {
                showAddTopicDialog = false
                newTopicName = ""
            }
        )
    }
}
