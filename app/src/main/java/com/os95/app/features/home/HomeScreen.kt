package com.os95.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95ProgressBar
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToRecall: () -> Unit,
    onNavigateToPapers: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()

    if (uiState.isLoading) {
        OS95LoadingState(message = "Initializing 95OS Command Center...")
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Top Command Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "95OS Command Center",
                    style = typography.caption.copy(letterSpacing = 1.sp),
                    color = colors.accent
                )
                Text(
                    text = uiState.studentName,
                    style = typography.heroTitle,
                    color = colors.primaryText
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OS95IconButton(
                    icon = Icons.Outlined.Timer,
                    contentDescription = "Focus Timer",
                    onClick = onNavigateToFocus
                )
                OS95IconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    onClick = onNavigateToSettings
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.l))

        // Target Card (Target Score + Gap)
        OS95Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.cardBackground
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EXAM TARGET",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = "${uiState.targetPercentage.toInt()}%",
                            style = typography.statNumber,
                            color = colors.accentCyan
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MARKS GAP",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = if (uiState.metrics.currentScorePercentage > 0f) {
                                "${"%.1f".format(uiState.metrics.marksGapPercentage)}% to goal"
                            } else {
                                "Baseline Pending"
                            },
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))
                Text(
                    text = "Closed-loop progression: Identify lost marks, eliminate mistakes, and achieve target accuracy.",
                    style = typography.bodySmall,
                    color = colors.secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        // Today's Priorities Section
        Text(
            text = "Today's Priorities",
            style = typography.sectionTitle,
            color = colors.primaryText
        )
        Spacer(modifier = Modifier.height(spacing.s))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Recall Due Tile
            OS95Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToRecall
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.Psychology,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = "${uiState.dueRecallCards.size} Cards Due",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Active recall session",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
            }

            // Practice Papers Tile
            OS95Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPapers
            ) {
                Column {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(spacing.s))
                    Text(
                        text = "${uiState.recentPapers.size} Papers",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "PaperPilot practice",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        // Syllabus Progress Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Syllabus Mastery",
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            OS95Button(
                text = "Open Syllabus",
                onClick = onNavigateToSyllabus,
                isSecondary = true
            )
        }
        Spacer(modifier = Modifier.height(spacing.s))

        if (uiState.subjects.isEmpty()) {
            OS95EmptyState(
                title = "No subjects added yet",
                description = "Configure your academic subjects and chapters to begin tracking syllabus mastery.",
                primaryActionLabel = "Add Subjects",
                onPrimaryAction = onNavigateToSyllabus
            )
        } else {
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uiState.subjects.size} Subjects Enrolled",
                            style = typography.bodySmall,
                            color = colors.primaryText
                        )
                        Text(
                            text = "${uiState.metrics.syllabusCompletionPercentage.toInt()}% Mastered",
                            style = typography.caption,
                            color = colors.accent
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.s))
                    OS95ProgressBar(
                        progress = uiState.metrics.syllabusCompletionPercentage / 100f
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        // Recent Papers / Diagnostic Section
        Text(
            text = "Recent Papers & Diagnostics",
            style = typography.sectionTitle,
            color = colors.primaryText
        )
        Spacer(modifier = Modifier.height(spacing.s))

        if (uiState.recentPapers.isEmpty()) {
            OS95EmptyState(
                title = "No practice papers yet",
                description = "Create and run practice papers to start measuring exam performance and diagnostic lost marks.",
                primaryActionLabel = "Create First Paper",
                onPrimaryAction = onNavigateToPapers
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentPapers.forEach { paper ->
                    OS95Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToPapers
                    ) {
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
                                    text = "${paper.durationMinutes}m • Max Marks: ${paper.totalMarks.toInt()}",
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
}
