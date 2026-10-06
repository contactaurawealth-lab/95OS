package com.os95.app.features.papers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PapersScreen(
    viewModel: PapersViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToQuestionBank: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    var showCreateDialog by remember { mutableStateOf(false) }
    var paperTitle by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "PaperPilot Hub",
            subtitle = "${uiState.papers.size} Papers",
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
                    onClick = { showCreateDialog = true }
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
                description = "Create your first practice paper to start measuring exam performance under realistic timing and mark distributions.",
                icon = Icons.Outlined.Description,
                primaryActionLabel = "Create Paper",
                onPrimaryAction = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.papers) { paper ->
                    OS95Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = paper.title,
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = "${paper.durationMinutes} min • Max Marks: ${paper.totalMarks.toInt()}",
                                    style = typography.caption,
                                    color = colors.mutedText
                                )
                            }
                            Text(
                                text = paper.status,
                                style = typography.caption,
                                color = colors.accent
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val defaultSubjectId = uiState.subjects.firstOrNull()?.id ?: "general_subject"
        OS95Dialog(
            title = "Create Practice Paper",
            message = "Enter the paper title (e.g. Unit Test 1 - Calculus):",
            confirmButtonText = "Create",
            onConfirm = {
                if (paperTitle.isNotBlank()) {
                    viewModel.createPaper(
                        subjectId = defaultSubjectId,
                        title = paperTitle,
                        totalMarks = 50f,
                        durationMinutes = 60
                    )
                    paperTitle = ""
                    showCreateDialog = false
                }
            },
            onDismissRequest = {
                showCreateDialog = false
                paperTitle = ""
            }
        )
    }
}
