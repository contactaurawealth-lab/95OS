package com.os95.app.features.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Speed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.domain.model.TimeToMarksChapterItem
import kotlin.math.roundToInt

@Composable
fun TimeToMarksScreen(
    viewModel: TimeToMarksViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (String) -> Unit = {},
    onStartFocusSession: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes

    val scrollState = rememberScrollState()

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
                onClick = onNavigateBack,
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(spacing.minTouchTarget)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "TIME-TO-MARKS INTELLIGENCE",
                    style = typography.caption.copy(letterSpacing = 1.sp),
                    color = colors.accent
                )
                Text(
                    text = "High-Impact Study Yield",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
            }
        }

        if (uiState.isLoading && uiState.report == null) {
            OS95LoadingState(message = "Calculating Time-to-Marks optimization...")
            return
        }

        val report = uiState.report

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Configuration Controls
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "PLANNING PARAMETERS",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Available Study Time Selector
                    Text(
                        text = "Available Study Time:",
                        style = typography.caption,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 90, 120).forEach { mins ->
                            val isSelected = uiState.availableMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(spacing.minTouchTarget)
                                    .clip(shapes.small)
                                    .background(if (isSelected) colors.accent else colors.cardBackground)
                                    .border(1.dp, if (isSelected) colors.accent else colors.border, shapes.small)
                                    .clickable { viewModel.setAvailableMinutes(mins) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    style = typography.caption,
                                    color = if (isSelected) colors.surface else colors.primaryText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Score Selector
                    Text(
                        text = "Target Exam Score:",
                        style = typography.caption,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(85f, 90f, 95f, 98f).forEach { target ->
                            val isSelected = (uiState.targetPercentage - target).let { kotlin.math.abs(it) < 0.5f }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(spacing.minTouchTarget)
                                    .clip(shapes.small)
                                    .background(if (isSelected) colors.accentCyan else colors.cardBackground)
                                    .border(1.dp, if (isSelected) colors.accentCyan else colors.border, shapes.small)
                                    .clickable { viewModel.setTargetPercentage(target) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${target.toInt()}%",
                                    style = typography.caption,
                                    color = if (isSelected) colors.surface else colors.primaryText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Gap & Yield Projections
            if (report != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OS95Card(modifier = Modifier.weight(1f)) {
                        Column {
                            Text(
                                text = "CURRENT / TARGET",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${report.currentEstimatedScore.roundToInt()}% → ${report.targetScore.toInt()}%",
                                style = typography.sectionTitle,
                                color = colors.accentCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gap: ${String.format("%.1f", report.marksGap)}%",
                                style = typography.caption,
                                color = colors.warning
                            )
                        }
                    }

                    OS95Card(modifier = Modifier.weight(1f)) {
                        Column {
                            Text(
                                text = "EXPECTED YIELD",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${String.format("%.1f", report.expectedMarksImprovementPerHour)} m/hr",
                                style = typography.sectionTitle,
                                color = colors.accent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "~${String.format("%.1f", report.totalEstimatedStudyHoursNeeded)} hrs needed",
                                style = typography.caption,
                                color = colors.secondaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary High-Yield Recommendation Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shapes.medium)
                        .background(colors.cardBackground)
                        .border(1.dp, colors.accent.copy(alpha = 0.5f), shapes.medium)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Speed,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = report.bestUseTitle.uppercase(),
                                style = typography.caption,
                                color = colors.accent
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (report.topImpactChapters.isNotEmpty()) {
                                "Focusing on ${report.topImpactChapters.first().chapterName} provides the highest deterministic return on your target score."
                            } else {
                                "All chapters balanced. Begin revision across core syllabus."
                            },
                            style = typography.body,
                            color = colors.primaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chapter Ranking List
                Text(
                    text = "HIGHEST-IMPACT CHAPTERS",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (report.topImpactChapters.isEmpty()) {
                    OS95EmptyState(
                        title = "No Chapters Found",
                        description = "Add chapters and topics to your syllabus to compute impact yield."
                    )
                } else {
                    report.topImpactChapters.forEachIndexed { index, chapterItem ->
                        ChapterImpactCard(
                            rank = index + 1,
                            item = chapterItem,
                            onStudyClick = {
                                onNavigateToChapter(chapterItem.chapterId)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Estimations Disclaimer Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.mutedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yield values and study times are deterministic estimates based on your mistake patterns, question test frequency, and syllabus weightage. Exam scores are never guaranteed.",
                    style = typography.caption,
                    color = colors.mutedText
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChapterImpactCard(
    rank: Int,
    item: TimeToMarksChapterItem,
    onStudyClick: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes

    OS95Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(shapes.small)
                            .background(if (rank == 1) colors.accent else colors.cardBackground)
                            .border(1.dp, if (rank == 1) colors.accent else colors.border, shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = typography.caption,
                            color = if (rank == 1) colors.surface else colors.secondaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${item.subjectName} — ${item.chapterName}",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Weightage: ${String.format("%.1f", item.examWeightage)}x • Lost Marks: ${item.activeLostMarks.toInt()}",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                    }
                }

                // Potential Gain Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.15f))
                        .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${item.potentialGainPercentage.roundToInt()}% gain",
                        style = typography.caption,
                        color = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expected: +${String.format("%.1f", item.expectedMarksImprovementPerHour)} m/hr (~${String.format("%.1f", item.estimatedHoursRequired)}h)",
                    style = typography.caption,
                    color = colors.secondaryText
                )

                OS95Button(
                    text = "Study Chapter",
                    onClick = onStudyClick,
                    isSecondary = true
                )
            }
        }
    }
}
