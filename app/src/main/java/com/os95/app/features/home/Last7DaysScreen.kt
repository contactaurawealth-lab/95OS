package com.os95.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95IconButton
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95ProgressBar
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.domain.model.Last7DaysTask
import kotlin.math.roundToInt

@Composable
fun Last7DaysScreen(
    viewModel: Last7DaysViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRetest: () -> Unit = {},
    onNavigateToRecall: () -> Unit = {},
    onNavigateToMistakes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes

    val scrollState = rememberScrollState()
    val tabScrollState = rememberScrollState()

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
                    text = "FINAL COUNTDOWN",
                    style = typography.caption.copy(letterSpacing = 1.sp),
                    color = colors.accent
                )
                Text(
                    text = "Last-7-Days Exam Mode",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
            }
        }

        if (uiState.isLoading && uiState.dashboard == null) {
            OS95LoadingState(message = "Calibrating final-week curriculum...")
            return
        }

        val dashboard = uiState.dashboard ?: return

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Countdown Header Card
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "EXAM HORIZON",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (dashboard.daysRemaining > 0) "${dashboard.daysRemaining} DAYS REMAINING" else "EXAM DAY TODAY",
                                style = typography.sectionTitle,
                                color = if (dashboard.daysRemaining <= 2) colors.warning else colors.accentCyan
                            )
                        }

                        // Readiness Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.cardBackground)
                                .border(1.dp, colors.accent, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${dashboard.overallWeekCompletionPercentage.roundToInt()}% COMPLETE",
                                style = typography.caption,
                                color = colors.accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OS95ProgressBar(
                        progress = dashboard.overallWeekCompletionPercentage / 100f
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day Selector Tabs (DAY 7 -> DAY 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(tabScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dashboard.dailyPlans.forEach { plan ->
                    val isSelected = uiState.selectedDay == plan.dayNumber
                    val isCurrentDay = plan.isToday

                    Box(
                        modifier = Modifier
                            .height(spacing.minTouchTarget)
                            .clip(shapes.small)
                            .background(
                                when {
                                    isSelected -> colors.accent
                                    isCurrentDay -> colors.cardBackground
                                    else -> colors.surface
                                }
                            )
                            .border(
                                width = if (isCurrentDay) 2.dp else 1.dp,
                                color = if (isSelected) colors.accent else if (isCurrentDay) colors.accentCyan else colors.border,
                                shape = shapes.small
                            )
                            .clickable { viewModel.selectDay(plan.dayNumber) }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = plan.dayLabel,
                            style = typography.caption,
                            color = if (isSelected) colors.surface else colors.primaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Today's 3 Most Important Tasks Card
            OS95Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S 3 MOST IMPORTANT TASKS",
                            style = typography.caption,
                            color = colors.accentCyan
                        )
                        Text(
                            text = "High Yield",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    dashboard.todayTopThreeTasks.forEach { task ->
                        TaskCheckboxItem(
                            task = task,
                            onToggle = { viewModel.toggleTask(task.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Full Curriculum for Selected Day
            val selectedPlan = dashboard.dailyPlans.find { it.dayNumber == uiState.selectedDay } ?: dashboard.dailyPlans.first()

            Text(
                text = "${selectedPlan.dayLabel.uppercase()} CURRICULUM",
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            Spacer(modifier = Modifier.height(10.dp))

            selectedPlan.tasks.forEach { task ->
                DailyCurriculumTaskCard(
                    task = task,
                    onToggle = { viewModel.toggleTask(task.id) },
                    onActionLaunch = {
                        when (task.category) {
                            "ADAPTIVE_RETEST" -> onNavigateToRetest()
                            "SHORT_RECALL" -> onNavigateToRecall()
                            "PREVIOUS_MISTAKES" -> onNavigateToMistakes()
                            else -> {}
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskCheckboxItem(
    task: Last7DaysTask,
    onToggle: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.minTouchTarget)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (task.isCompleted) colors.success else colors.mutedText,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = typography.body,
                color = if (task.isCompleted) colors.mutedText else colors.primaryText
            )
            Text(
                text = "~${task.estimatedMinutes} mins • ${task.description}",
                style = typography.caption,
                color = colors.secondaryText
            )
        }
    }
}

@Composable
private fun DailyCurriculumTaskCard(
    task: Last7DaysTask,
    onToggle: () -> Unit,
    onActionLaunch: () -> Unit
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val shapes = OS95Theme.shapes

    OS95Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.minTouchTarget)
                        .clickable(onClick = onToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.isCompleted) colors.success else colors.mutedText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = typography.sectionTitle,
                        color = if (task.isCompleted) colors.mutedText else colors.primaryText
                    )
                    Text(
                        text = "${task.estimatedMinutes}m • ${task.category.replace('_', ' ')}",
                        style = typography.caption,
                        color = colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = typography.body,
                color = colors.secondaryText
            )

            if (!task.isCompleted && task.category in listOf("ADAPTIVE_RETEST", "SHORT_RECALL", "PREVIOUS_MISTAKES")) {
                Spacer(modifier = Modifier.height(10.dp))
                OS95Button(
                    text = when (task.category) {
                        "ADAPTIVE_RETEST" -> "Launch Re-Test"
                        "SHORT_RECALL" -> "Start Recall Sprint"
                        else -> "Resolve Mistakes"
                    },
                    onClick = onActionLaunch,
                    isSecondary = true
                )
            }
        }
    }
}
