package com.os95.app.features.home

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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95Dialog
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95ProgressBar
import com.os95.app.core.ui.theme.OS95Theme
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToRecall: () -> Unit,
    onNavigateToPapers: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTimeToMarks: () -> Unit = {},
    onNavigateToAdaptiveRetest: () -> Unit = {},
    onNavigateToExamSimulator: () -> Unit = {},
    onNavigateToLast7Days: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()
    var showRescueDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        OS95LoadingState(message = "Initializing 95OS Command Center...")
        return
    }

    val cc = uiState.commandCenter

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Top Command Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "95OS COMMAND CENTER",
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

        Spacer(modifier = Modifier.height(16.dp))

        // =====================================================================
        // 1. TOP SECTION: 95% TARGET (5-SECOND SITUATIONAL AWARENESS)
        // =====================================================================
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
                            text = "PROJECTED / TARGET",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val currentPct = cc?.currentPredictedPercentage ?: uiState.metrics.currentScorePercentage
                        val targetPct = cc?.targetPercentage ?: uiState.targetPercentage
                        Text(
                            text = "${currentPct.roundToInt()}% → ${targetPct.toInt()}%",
                            style = typography.statNumber,
                            color = colors.accentCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXAM READINESS",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val readiness = cc?.examReadinessPercentage ?: 0f
                        Text(
                            text = "${readiness.roundToInt()}%",
                            style = typography.statNumber,
                            color = if (readiness >= 75f) colors.success else colors.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val gapPct = cc?.percentageGap ?: uiState.metrics.marksGapPercentage
                    val marksGapText = cc?.marksGap?.let { " (${it.toInt()} marks away)" } ?: ""
                    Text(
                        text = "Gap: ${String.format("%.1f", gapPct)}%$marksGapText",
                        style = typography.caption,
                        color = colors.warning
                    )

                    val daysText = cc?.daysRemaining?.let { "$it Days Left" } ?: "Final Prep"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = daysText,
                            style = typography.caption,
                            color = colors.primaryText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // =====================================================================
        // 2. MIDDLE SECTION: WHAT SHOULD I DO NOW? (PRIMARY RECOMMENDED ACTION)
        // =====================================================================
        val primaryAction = cc?.primaryAction
        OS95Card(
            modifier = Modifier.fillMaxWidth(),
            borderColor = colors.accent
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
                        text = "WHAT SHOULD I DO NOW?",
                        style = typography.caption,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = primaryAction?.title ?: "Complete Daily Deliberate Practice",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = primaryAction?.reason ?: "Targets your highest recovery yield based on historical performance.",
                    style = typography.bodySmall,
                    color = colors.secondaryText
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OS95Button(
                        text = "START ACTION",
                        onClick = {
                            when (primaryAction?.actionType) {
                                "REVISE_TOPIC" -> onNavigateToSyllabus()
                                "RETEST_WEAKNESS" -> onNavigateToAdaptiveRetest()
                                "RECALL_RADAR" -> onNavigateToRecall()
                                else -> onNavigateToTimeToMarks()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OS95OutlinedButton(
                        text = "ALTERNATIVE",
                        onClick = onNavigateToTimeToMarks,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================================
        // 3. ADVANCED EXAM ENGINES HUB
        // =====================================================================
        Text(
            text = "EXAM ENGINES",
            style = typography.caption,
            color = colors.mutedText
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: Time-to-Marks + Adaptive Re-Test
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Time-to-Marks Tile
            OS95Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTimeToMarks
            ) {
                Column {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Time-to-Marks",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Best next 60m yield",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
            }

            // Adaptive Re-Test Tile
            OS95Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAdaptiveRetest
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adaptive Re-Test",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "50/30/20 re-test",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Exam Simulator + Last-7-Days Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Exam Simulator Tile
            OS95Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToExamSimulator
            ) {
                Column {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = null,
                        tint = colors.accentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Exam Simulator",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Timed exam runner",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
            }

            // Last-7-Days Tile
            OS95Card(
                modifier = Modifier.weight(1f),
                onClick = onNavigateToLast7Days
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last-7-Days",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Final week sprint",
                        style = typography.caption,
                        color = colors.mutedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 15-Minute Rescue Mode Banner
        OS95Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showRescueDialog = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ElectricBolt,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "15-Minute Rescue Mode",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = if (uiState.potentialRecoverableMarks > 0f) {
                                "+${uiState.potentialRecoverableMarks.toInt()}m recoverable • ${uiState.criticalTopicsCount} at risk"
                            } else {
                                "Rapid high-yield intervention"
                            },
                            style = typography.caption,
                            color = colors.mutedText
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = colors.mutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =====================================================================
        // 4. TODAY'S STUDY & DIAGNOSTIC STATS
        // =====================================================================
        Text(
            text = "TODAY'S EXECUTION",
            style = typography.caption,
            color = colors.mutedText
        )
        Spacer(modifier = Modifier.height(10.dp))

        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val studied = cc?.todayStudyMinutes ?: 0
                    val target = cc?.todayTargetMinutes ?: 120
                    Column {
                        Text(
                            text = "STUDY TIME TODAY",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = "${studied}m / ${target}m",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ACCURACY TREND",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        val trend = cc?.accuracyTrend ?: "STABLE"
                        Text(
                            text = trend,
                            style = typography.caption,
                            color = when (trend) {
                                "IMPROVING" -> colors.success
                                "DECLINING" -> colors.warning
                                else -> colors.accentCyan
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                val progressRatio = ((cc?.todayStudyMinutes ?: 0).toFloat() / (cc?.todayTargetMinutes ?: 120)).coerceIn(0f, 1f)
                OS95ProgressBar(progress = progressRatio)
            }
        }

        // Weakest Chapters Spotlight
        if (cc?.topThreeWeakChapters?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(16.dp))
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TOP WEAKNESS SPOTLIGHT",
                            style = typography.caption,
                            color = colors.warning
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    cc.topThreeWeakChapters.forEach { (chapterName, _) ->
                        Text(
                            text = "• $chapterName",
                            style = typography.body,
                            color = colors.primaryText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // 15-Minute Rescue Mode Modal
    if (showRescueDialog) {
        val rescuePlan = uiState.rescuePlan
        OS95Dialog(
            title = "15-Minute Rescue Mode",
            confirmButtonText = "Complete Session",
            dismissButtonText = "Dismiss",
            onConfirm = {
                viewModel.completeRescueSession(15)
                showRescueDialog = false
            },
            onDismissRequest = { showRescueDialog = false }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "High-impact rapid session prioritizing memory decay and critical mistakes.",
                    style = OS95Theme.typography.bodySmall,
                    color = OS95Theme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                rescuePlan?.blocks?.forEach { block ->
                    OS95Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        backgroundColor = OS95Theme.colors.background
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${block.startMinute}:00–${block.endMinute}:00",
                                    style = OS95Theme.typography.caption,
                                    color = OS95Theme.colors.accentCyan
                                )
                                Text(
                                    text = block.actionType.label,
                                    style = OS95Theme.typography.caption,
                                    color = OS95Theme.colors.mutedText
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = block.title,
                                style = OS95Theme.typography.sectionTitle,
                                color = OS95Theme.colors.primaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = block.description,
                                style = OS95Theme.typography.caption,
                                color = OS95Theme.colors.secondaryText
                            )
                        }
                    }
                }
            }
        }
    }
}
