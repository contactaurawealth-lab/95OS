package com.os95.app.features.progress

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95Dialog
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95MenuAction
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95ProgressBar
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.component.StudyConsistencyHeatmap
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.domain.model.PaperTrendDirection
import com.os95.app.domain.model.RecoveryPriorityLevel
import com.os95.app.domain.model.RescueActionType
import com.os95.app.domain.model.RetentionRiskLevel

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRecall: () -> Unit = {},
    onNavigateToPapers: () -> Unit = {},
    onNavigateToMistakes: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes

    var showRescueDialog by remember { mutableStateOf(false) }
    var selectedRescueDuration by remember { mutableIntStateOf(15) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Marks Recovery Engine",
            subtitle = "Target: ${uiState.targetPercentage.toInt()}%",
            onBack = onNavigateBack,
            overflowActions = listOf(
                OS95MenuAction(
                    label = "15-Min Rescue Sprint",
                    onClick = {
                        selectedRescueDuration = 15
                        showRescueDialog = true
                    }
                ),
                OS95MenuAction(
                    label = "Recall Spaced Review",
                    onClick = onNavigateToRecall
                ),
                OS95MenuAction(
                    label = "Practice Papers Analysis",
                    onClick = onNavigateToPapers
                ),
                OS95MenuAction(
                    label = "Mistake Bank Remediation",
                    onClick = onNavigateToMistakes
                )
            )
        )

        if (uiState.isLoading || uiState.snapshot == null) {
            OS95LoadingState(message = "Computing deterministic marks recovery metrics...")
            return
        }

        val snapshot = uiState.snapshot!!

        // Navigation Tabs (Marks Gap, Forgetting Radar, Paper Analysis, Recovery Score)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressTab.values().forEach { tab ->
                val isSelected = uiState.selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp)
                        .clip(shapes.small)
                        .background(if (isSelected) colors.primaryText else colors.surface)
                        .clickable { viewModel.selectTab(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            ProgressTab.MARKS_GAP -> "Gap"
                            ProgressTab.FORGETTING_RADAR -> "Radar"
                            ProgressTab.PAPER_ANALYSIS -> "Papers"
                            ProgressTab.RECOVERY_SCORE -> "Score"
                        },
                        style = typography.caption,
                        color = if (isSelected) colors.background else colors.secondaryText,
                        maxLines = 1
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 70-Day Study Consistency Heatmap
            StudyConsistencyHeatmap(
                dailyStudyMinutes = uiState.dailyStudyMinutes,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tab Content
            when (uiState.selectedTab) {
                ProgressTab.MARKS_GAP -> MarksGapTabContent(
                    snapshot = snapshot,
                    sensitivityResult = uiState.sensitivityResult,
                    simulatedSubjectId = uiState.simulatedSubjectId,
                    simulatedPercentage = uiState.simulatedPercentage,
                    onRunSensitivity = { subId, pct -> viewModel.runSensitivitySimulation(subId, pct) },
                    onClearSensitivity = { viewModel.clearSensitivitySimulation() },
                    onStartRescue = { showRescueDialog = true },
                    onNavigateToMistakes = onNavigateToMistakes
                )
                ProgressTab.FORGETTING_RADAR -> ForgettingRadarTabContent(
                    snapshot = snapshot,
                    onNavigateToRecall = onNavigateToRecall
                )
                ProgressTab.PAPER_ANALYSIS -> PaperAnalysisTabContent(
                    snapshot = snapshot,
                    onNavigateToPapers = onNavigateToPapers,
                    onNavigateToRecall = onNavigateToRecall
                )
                ProgressTab.RECOVERY_SCORE -> RecoveryScoreTabContent(
                    snapshot = snapshot,
                    onNavigateToPapers = onNavigateToPapers
                )
            }
        }
    }

    // 15-Minute Rescue Mode Dialog
    if (showRescueDialog && uiState.snapshot != null) {
        val rescuePlan = uiState.snapshot!!.rescuePlan
        OS95Dialog(
            title = "15-Minute Rescue Mode",
            confirmButtonText = "Mark Session Done",
            dismissButtonText = "Close",
            onConfirm = {
                viewModel.completeRescueSession(
                    durationMinutes = selectedRescueDuration,
                    actionsCompleted = rescuePlan.blocks.size,
                    topicsCovered = rescuePlan.blocks.size,
                    cardsReviewed = rescuePlan.blocks.find { it.actionType == RescueActionType.RECALL_REVIEW }?.itemCount ?: 0,
                    mistakesResolved = rescuePlan.blocks.find { it.actionType == RescueActionType.MISTAKE_FIX }?.itemCount ?: 0
                )
                showRescueDialog = false
            },
            onDismissRequest = { showRescueDialog = false }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "High-impact rapid intervention prioritizing memory decay & repeated mistakes.",
                    style = OS95Theme.typography.bodySmall,
                    color = OS95Theme.colors.secondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Time duration selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5, 10, 15, 20, 30).forEach { mins ->
                        val isSel = selectedRescueDuration == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .clip(shapes.small)
                                .background(if (isSel) colors.accent else colors.surface)
                                .clickable { selectedRescueDuration = mins }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                style = typography.caption,
                                color = if (isSel) colors.surface else colors.primaryText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Schedule blocks
                rescuePlan.blocks.forEach { block ->
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

// --- 1. Marks Gap Tab Content ---

@Composable
fun MarksGapTabContent(
    snapshot: com.os95.app.domain.model.MarksRecoverySnapshot,
    sensitivityResult: com.os95.app.domain.engine.TargetSensitivityResult?,
    simulatedSubjectId: String?,
    simulatedPercentage: Float,
    onRunSensitivity: (String, Float) -> Unit,
    onClearSensitivity: () -> Unit,
    onStartRescue: () -> Unit,
    onNavigateToMistakes: () -> Unit
) {
    val plan = snapshot.marksGap
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    // Hero Gap Card
    OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CURRENT SCORE", style = typography.caption, color = colors.mutedText)
                    Text(
                        text = if (plan.currentPercentage > 0f) "${"%.1f".format(plan.currentPercentage)}%" else "--",
                        style = typography.statNumber,
                        color = colors.primaryText
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TARGET", style = typography.caption, color = colors.mutedText)
                    Text(
                        text = "${plan.targetPercentage.toInt()}%",
                        style = typography.statNumber,
                        color = colors.accentCyan
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "GAP", style = typography.caption, color = colors.mutedText)
                    Text(
                        text = "${"%.1f".format(plan.percentageGap)}%",
                        style = typography.statNumber,
                        color = colors.warning
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.m))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(OS95Theme.shapes.small)
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Potential Recovery:",
                    style = typography.bodySmall,
                    color = colors.secondaryText
                )
                Text(
                    text = "+${plan.potentialRecoverableMarks.toInt()} marks",
                    style = typography.sectionTitle,
                    color = colors.accent
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(spacing.l))

    // Rescue Mode Launch Banner
    OS95Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onStartRescue,
        backgroundColor = colors.surface
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
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "15-Minute Rescue Mode", style = typography.sectionTitle, color = colors.primaryText)
                    Text(text = "Highest-value rapid preparation right now", style = typography.caption, color = colors.mutedText)
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

    Spacer(modifier = Modifier.height(spacing.xl))

    // Top Recovery Opportunities
    Text(text = "Top Recovery Opportunities", style = typography.sectionTitle, color = colors.primaryText)
    Spacer(modifier = Modifier.height(spacing.s))

    if (plan.topOpportunities.isEmpty()) {
        OS95EmptyState(
            title = "No recorded lost marks yet",
            description = "Complete practice papers in PaperPilot or log mistakes to identify high-leverage mark recovery opportunities.",
            icon = Icons.Outlined.CheckCircle
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            plan.topOpportunities.forEachIndexed { index, opp ->
                OS95Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToMistakes
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${opp.subjectName}${if (opp.topicName != null) " — ${opp.topicName}" else ""}",
                                style = typography.sectionTitle,
                                color = colors.primaryText,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (opp.priorityLevel) {
                                            RecoveryPriorityLevel.VERY_HIGH -> colors.error.copy(alpha = 0.15f)
                                            RecoveryPriorityLevel.HIGH -> colors.warning.copy(alpha = 0.15f)
                                            else -> colors.surface
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = opp.priorityLevel.label,
                                    style = typography.caption,
                                    color = when (opp.priorityLevel) {
                                        RecoveryPriorityLevel.VERY_HIGH -> colors.error
                                        RecoveryPriorityLevel.HIGH -> colors.warning
                                        else -> colors.secondaryText
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lost: ${opp.lostMarks.toInt()} marks • ${opp.dominantMistakeCategory}",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = opp.actionDescription,
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(spacing.m))

    // --- Target Sensitivity Calculator («What-If» Scenario) ---
    Text(
        text = "TARGET SENSITIVITY CALCULATOR («WHAT-IF» SCENARIO)",
        style = typography.caption.copy(letterSpacing = 1.sp),
        color = colors.accent
    )
    Spacer(modifier = Modifier.height(spacing.s))

    OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Simulate how changes in one subject's score impact your overall benchmark, and see the exact compensatory marks needed across other subjects to stay on track.",
                style = typography.bodySmall,
                color = colors.secondaryText
            )

            val subjectItems = if (snapshot.paperAnalysis.subjectPerformance.isNotEmpty()) {
                snapshot.paperAnalysis.subjectPerformance.map { it.subjectId to it.subjectName }
            } else {
                snapshot.marksGap.topOpportunities.map { it.subjectId to it.subjectName }.distinctBy { it.first }
            }

            if (subjectItems.isEmpty()) {
                Text(
                    text = "No subjects registered for sensitivity modeling.",
                    style = typography.caption,
                    color = colors.mutedText
                )
            } else {
                Text(text = "Select subject to simulate:", style = typography.caption, color = colors.primaryText)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectItems.take(5).forEach { (subId, subName) ->
                        val isSelected = simulatedSubjectId == subId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.accent else colors.surface)
                                .clickable {
                                    onRunSensitivity(subId, simulatedPercentage)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subName.take(6),
                                style = typography.caption,
                                color = if (isSelected) colors.surface else colors.primaryText
                            )
                        }
                    }
                }

                if (simulatedSubjectId != null) {
                    Text(text = "Simulate score percentage:", style = typography.caption, color = colors.primaryText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(75f, 80f, 85f, 90f, 95f).forEach { pct ->
                            val isSelPct = simulatedPercentage == pct
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelPct) colors.accentCyan else colors.surface)
                                    .clickable {
                                        onRunSensitivity(simulatedSubjectId, pct)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${pct.toInt()}%",
                                    style = typography.caption,
                                    color = if (isSelPct) colors.surface else colors.primaryText
                                )
                            }
                        }
                    }
                }

                if (sensitivityResult != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surface)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Simulated Average:", style = typography.caption, color = colors.mutedText)
                                Text(
                                    text = "${"%.1f".format(sensitivityResult.simulatedOverallPercentage)}%",
                                    style = typography.caption,
                                    color = colors.primaryText
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Compensatory Marks Needed:", style = typography.caption, color = colors.mutedText)
                                Text(
                                    text = "+${"%.1f".format(sensitivityResult.totalCompensatoryMarksNeeded)} marks",
                                    style = typography.caption,
                                    color = if (sensitivityResult.isTargetAchievable) colors.accent else colors.error
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Subject Re-allocation Guidance:", style = typography.caption, color = colors.accentCyan)
                            sensitivityResult.subjectCompensations.forEach { comp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = comp.subjectName, style = typography.bodySmall, color = colors.primaryText)
                                    Text(
                                        text = "${"%.0f".format(comp.currentPercentage)}% → ${"%.0f".format(comp.targetPercentage)}% (+${"%.1f".format(comp.requiredAdditionalMarks)}m)",
                                        style = typography.caption,
                                        color = colors.mutedText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OS95OutlinedButton(
                                text = "Reset Simulation",
                                onClick = onClearSensitivity,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 2. Forgetting Radar Tab Content ---

@Composable
fun ForgettingRadarTabContent(
    snapshot: com.os95.app.domain.model.MarksRecoverySnapshot,
    onNavigateToRecall: () -> Unit
) {
    val radar = snapshot.forgettingRadar
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    // 4-Card Retention Grid
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RiskSummaryTile(
            title = "Critical",
            count = radar.criticalCount,
            color = colors.error,
            modifier = Modifier.weight(1f)
        )
        RiskSummaryTile(
            title = "At Risk",
            count = radar.atRiskCount,
            color = colors.warning,
            modifier = Modifier.weight(1f)
        )
        RiskSummaryTile(
            title = "Watch",
            count = radar.watchCount,
            color = colors.accent,
            modifier = Modifier.weight(1f)
        )
        RiskSummaryTile(
            title = "Stable",
            count = radar.stableCount,
            color = colors.success,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(spacing.xl))

    Text(
        text = "Retention Risk Radar",
        style = typography.sectionTitle,
        color = colors.primaryText
    )
    Spacer(modifier = Modifier.height(spacing.s))

    if (radar.topics.isEmpty()) {
        OS95EmptyState(
            title = "No recall history",
            description = "Start Recall to build your forgetting profile and memory decay timeline.",
            icon = Icons.Outlined.Psychology
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            radar.topics.forEach { topic ->
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (topic.riskLevel) {
                                                RetentionRiskLevel.CRITICAL -> colors.error
                                                RetentionRiskLevel.AT_RISK -> colors.warning
                                                RetentionRiskLevel.WATCH -> colors.accent
                                                RetentionRiskLevel.STABLE -> colors.success
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = topic.topicName,
                                    style = typography.sectionTitle,
                                    color = colors.primaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${topic.subjectName} • ${topic.chapterName}",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (topic.daysSinceLastReview > 0) {
                                    "Last recalled: ${topic.daysSinceLastReview} days ago • ${topic.cardsDueCount} due"
                                } else {
                                    "${topic.cardsDueCount} cards due for recall"
                                },
                                style = typography.caption,
                                color = colors.secondaryText
                            )
                        }

                        if (topic.cardsDueCount > 0 || topic.riskLevel != RetentionRiskLevel.STABLE) {
                            OS95OutlinedButton(
                                text = "Review Now",
                                onClick = onNavigateToRecall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskSummaryTile(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    OS95Card(modifier = modifier, backgroundColor = OS95Theme.colors.cardBackground) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                style = OS95Theme.typography.statNumber,
                color = color
            )
            Text(
                text = title,
                style = OS95Theme.typography.caption,
                color = OS95Theme.colors.mutedText
            )
        }
    }
}

// --- 3. Previous Paper Analyzer Tab Content ---

@Composable
fun PaperAnalysisTabContent(
    snapshot: com.os95.app.domain.model.MarksRecoverySnapshot,
    onNavigateToPapers: () -> Unit,
    onNavigateToRecall: () -> Unit
) {
    val analysis = snapshot.paperAnalysis
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    if (analysis.totalPapers == 0) {
        OS95EmptyState(
            title = "Complete a practice paper",
            description = "Complete a practice paper in PaperPilot to unlock paper analysis, recurring mistake patterns, and subject trends.",
            icon = Icons.AutoMirrored.Outlined.ShowChart,
            primaryActionLabel = "Create Practice Paper",
            onPrimaryAction = onNavigateToPapers
        )
        return
    }

    // Trend Card
    OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "AVERAGE SCORE", style = typography.caption, color = colors.mutedText)
                    Text(text = "${"%.1f".format(analysis.averagePercentage)}%", style = typography.statNumber, color = colors.primaryText)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "BEST", style = typography.caption, color = colors.mutedText)
                    Text(text = "${analysis.bestPercentage.toInt()}%", style = typography.statNumber, color = colors.accentCyan)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "TREND", style = typography.caption, color = colors.mutedText)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (analysis.trendDirection == PaperTrendDirection.IMPROVING) {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null, tint = colors.success, modifier = Modifier.size(16.dp))
                        } else if (analysis.trendDirection == PaperTrendDirection.DECLINING) {
                            Icon(imageVector = Icons.AutoMirrored.Outlined.TrendingDown, contentDescription = null, tint = colors.error, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = analysis.trendDirection.label,
                            style = typography.caption,
                            color = when (analysis.trendDirection) {
                                PaperTrendDirection.IMPROVING -> colors.success
                                PaperTrendDirection.DECLINING -> colors.error
                                else -> colors.secondaryText
                            }
                        )
                    }
                }
            }

            if (!analysis.hasEnoughDataForTrend) {
                Spacer(modifier = Modifier.height(spacing.m))
                Text(
                    text = "Not enough papers to determine a reliable trend. Complete another practice paper to view historical trajectory.",
                    style = typography.caption,
                    color = colors.mutedText
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(spacing.xl))

    // Repeated Weakness Alert (Section 12)
    if (analysis.repeatedWeaknesses.isNotEmpty()) {
        Text(text = "Repeated Weaknesses Detected", style = typography.sectionTitle, color = colors.primaryText)
        Spacer(modifier = Modifier.height(spacing.s))

        analysis.repeatedWeaknesses.forEach { weak ->
            OS95Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), backgroundColor = colors.surface) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${weak.subjectName} — ${weak.topicName}",
                            style = typography.sectionTitle,
                            color = colors.error
                        )
                        Text(
                            text = "-${weak.totalLostMarks.toInt()}m lost",
                            style = typography.caption,
                            color = colors.error
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Appeared in ${weak.papersAffectedCount} distinct papers (${weak.paperLostBreakdown.joinToString { "${it.first}: -${it.second.toInt()}m" }})",
                        style = typography.caption,
                        color = colors.secondaryText
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing.xl))
    }

    // Question Type Patterns
    if (analysis.questionTypePerformance.isNotEmpty()) {
        Text(text = "Question Type Accuracy", style = typography.sectionTitle, color = colors.primaryText)
        Spacer(modifier = Modifier.height(spacing.s))

        analysis.questionTypePerformance.forEach { qt ->
            OS95Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = qt.questionType, style = typography.bodySmall, color = colors.primaryText)
                        Text(text = "${qt.accuracyPercentage.toInt()}% accuracy", style = typography.caption, color = colors.accent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OS95ProgressBar(progress = qt.accuracyPercentage / 100f)
                }
            }
        }
    }
}

// --- 4. Recovery Score Tab Content ---

@Composable
fun RecoveryScoreTabContent(
    snapshot: com.os95.app.domain.model.MarksRecoverySnapshot,
    onNavigateToPapers: () -> Unit
) {
    val report = snapshot.recoveryScore
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    if (!report.hasComparablePapers) {
        OS95EmptyState(
            title = "No comparable papers yet",
            description = "Complete a second practice paper in the same subject to measure marks recovered from prior weaknesses.",
            icon = Icons.Outlined.History,
            primaryActionLabel = "Take Another Paper",
            onPrimaryAction = onNavigateToPapers
        )
        return
    }

    // Recovery Hero
    OS95Card(modifier = Modifier.fillMaxWidth(), backgroundColor = colors.cardBackground) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "TOTAL RECOVERED", style = typography.caption, color = colors.mutedText)
                    Text(
                        text = if (report.totalRecoveredMarks >= 0f) "+${report.totalRecoveredMarks.toInt()}m" else "${report.totalRecoveredMarks.toInt()}m",
                        style = typography.statNumber,
                        color = if (report.totalRecoveredMarks >= 0f) colors.success else colors.error
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PROGRESSION", style = typography.caption, color = colors.mutedText)
                    Text(
                        text = "${report.previousLostMarks.toInt()}m lost → ${report.currentLostMarks.toInt()}m lost",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing.m))
            Text(
                text = "Measures marks eliminated from prior test weaknesses on comparable assessments.",
                style = typography.caption,
                color = colors.secondaryText
            )
        }
    }

    Spacer(modifier = Modifier.height(spacing.xl))

    // Recovery by Category
    if (report.recoveryByCategory.isNotEmpty()) {
        Text(text = "Recovered Marks by Category", style = typography.sectionTitle, color = colors.primaryText)
        Spacer(modifier = Modifier.height(spacing.s))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            report.recoveryByCategory.forEach { (cat, marks) ->
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = cat, style = typography.bodySmall, color = colors.primaryText)
                        Text(
                            text = if (marks >= 0f) "+${marks.toInt()} marks" else "${marks.toInt()} marks",
                            style = typography.sectionTitle,
                            color = if (marks >= 0f) colors.success else colors.error
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing.xl))
    }

    // Lost Marks Trend
    if (report.lostMarksTrend.size >= 2) {
        Text(text = "Lost Marks Elimination Trend", style = typography.sectionTitle, color = colors.primaryText)
        Spacer(modifier = Modifier.height(spacing.s))

        OS95Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                report.lostMarksTrend.forEachIndexed { i, p ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "-${p.second.toInt()}m", style = typography.sectionTitle, color = colors.warning)
                        Text(text = p.first.take(10), style = typography.caption, color = colors.mutedText)
                    }
                    if (i < report.lostMarksTrend.size - 1) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = colors.mutedText, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
