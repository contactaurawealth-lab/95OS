package com.os95.app.features.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95ProgressBar
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "95% Target Engine",
            subtitle = "Target: ${uiState.metrics.targetScorePercentage.toInt()}%",
            onBack = onNavigateBack
        )

        if (uiState.isLoading) {
            OS95LoadingState(message = "Computing deterministic progress metrics...")
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Target Gap Card
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
                                text = "CURRENT AVERAGE",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Text(
                                text = if (uiState.metrics.currentScorePercentage > 0f) {
                                    "${"%.1f".format(uiState.metrics.currentScorePercentage)}%"
                                } else {
                                    "--"
                                },
                                style = typography.statNumber,
                                color = colors.primaryText
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TARGET SCORE",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Text(
                                text = "${uiState.metrics.targetScorePercentage.toInt()}%",
                                style = typography.statNumber,
                                color = colors.accentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.m))
                    Text(
                        text = if (uiState.metrics.currentScorePercentage > 0f) {
                            "Remaining Gap: ${"%.1f".format(uiState.metrics.marksGapPercentage)}% to target."
                        } else {
                            "Take practice papers in PaperPilot to establish your diagnostic baseline score."
                        },
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Syllabus Mastery Breakdown
            Text(
                text = "Syllabus Coverage",
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            Spacer(modifier = Modifier.height(spacing.s))

            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Topics Mastered",
                            style = typography.bodySmall,
                            color = colors.primaryText
                        )
                        Text(
                            text = "${uiState.metrics.syllabusCompletionPercentage.toInt()}%",
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

            Spacer(modifier = Modifier.height(spacing.xl))

            // Lost Marks Recovery Diagnostic
            Text(
                text = "Mark Loss & Recovery",
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            Spacer(modifier = Modifier.height(spacing.s))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OS95Card(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "UNRESOLVED LOSS",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = "-${uiState.metrics.totalLostMarks.toInt()}m",
                            style = typography.statNumber,
                            color = colors.error
                        )
                    }
                }
                OS95Card(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "RECOVERED",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = "+${uiState.metrics.recoveredMarks.toInt()}m",
                            style = typography.statNumber,
                            color = colors.success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Exam Performance History
            Text(
                text = "Recent Test Scores",
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            Spacer(modifier = Modifier.height(spacing.s))

            if (uiState.examResults.isEmpty()) {
                OS95EmptyState(
                    title = "No test results recorded",
                    description = "Results from PaperPilot exams will automatically generate your marks trajectory and weakness radar.",
                    icon = Icons.AutoMirrored.Outlined.ShowChart
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.examResults.forEach { result ->
                        OS95Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Score: ${result.marksObtained.toInt()}/${result.totalMarks.toInt()}",
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                                val pct = (result.marksObtained / result.totalMarks) * 100f
                                Text(
                                    text = "${pct.toInt()}%",
                                    style = typography.sectionTitle,
                                    color = if (pct >= 95f) colors.accentCyan else colors.primaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
