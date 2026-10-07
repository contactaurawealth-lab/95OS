package com.os95.app.features.recall

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95DropdownSelector
import com.os95.app.core.ui.component.OS95EmptyState
import com.os95.app.core.ui.component.OS95LoadingState
import com.os95.app.core.ui.component.OS95MenuAction
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.domain.model.RecallRating

@Composable
fun RecallScreen(
    viewModel: RecallViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToForgettingRadar: (() -> Unit)? = null
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
        OS95TopBar(
            title = "Recall Engine",
            subtitle = "${uiState.dueCards.size} Due Today",
            onBack = onNavigateBack,
            overflowActions = listOfNotNull(
                if (onNavigateToForgettingRadar != null) {
                    OS95MenuAction(
                        label = "Forgetting Radar",
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        onClick = onNavigateToForgettingRadar
                    )
                } else null,
                OS95MenuAction(
                    label = "Review 5 Cards Blitz",
                    isSelected = uiState.selectedPreset == RecallPreset.BLITZ,
                    onClick = { viewModel.selectPreset(RecallPreset.BLITZ) }
                ),
                OS95MenuAction(
                    label = "Review 10 Cards Focused",
                    isSelected = uiState.selectedPreset == RecallPreset.FOCUSED,
                    onClick = { viewModel.selectPreset(RecallPreset.FOCUSED) }
                ),
                OS95MenuAction(
                    label = "Review 20 Cards Deep",
                    isSelected = uiState.selectedPreset == RecallPreset.DEEP,
                    onClick = { viewModel.selectPreset(RecallPreset.DEEP) }
                ),
                OS95MenuAction(
                    label = "Review All Due Cards",
                    isSelected = uiState.selectedPreset == RecallPreset.ALL_DUE,
                    onClick = { viewModel.selectPreset(RecallPreset.ALL_DUE) }
                )
            )
        )

        if (uiState.isLoading) {
            OS95LoadingState(message = "Querying spaced repetition schedule...")
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Metrics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OS95Card(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "DUE TODAY",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = "${uiState.dueCards.size}",
                            style = typography.statNumber,
                            color = colors.accent
                        )
                    }
                }
                OS95Card(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "TOTAL CARDS",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Text(
                            text = "${uiState.allCards.size}",
                            style = typography.statNumber,
                            color = colors.primaryText
                        )
                    }
                }
            }

            if (onNavigateToForgettingRadar != null) {
                Spacer(modifier = Modifier.height(spacing.m))
                OS95Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToForgettingRadar() },
                    backgroundColor = colors.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Psychology,
                                contentDescription = null,
                                tint = colors.accentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Forgetting Radar",
                                    style = typography.bodySmall,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = "Monitor SM-2 memory decay & retention risk",
                                    style = typography.caption,
                                    color = colors.mutedText
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = colors.mutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.m))
            }

            // Session Presets Dropdown Selector
            OS95DropdownSelector(
                label = "Session Preset",
                selectedValue = uiState.selectedPreset,
                items = RecallPreset.values().toList(),
                itemLabel = { preset ->
                    when (preset) {
                        RecallPreset.BLITZ -> "5 Blitz (Quick Recall)"
                        RecallPreset.FOCUSED -> "10 Focused (Standard SM-2)"
                        RecallPreset.DEEP -> "20 Deep (Retention Push)"
                        RecallPreset.ALL_DUE -> "All Due (${uiState.dueCards.size} cards)"
                    }
                },
                onItemSelected = { viewModel.selectPreset(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // Active Review Session or Clean Empty State
            val activeCard = uiState.activeSessionCard
            if (activeCard != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Spaced Review (SM-2)",
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Completed: ${uiState.completedInSession}",
                        style = typography.caption,
                        color = colors.accent
                    )
                }
                Spacer(modifier = Modifier.height(spacing.s))

                OS95Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.cardBackground
                ) {
                    Column {
                        Text(
                            text = "PROMPT",
                            style = typography.caption,
                            color = colors.accent
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = activeCard.prompt,
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )

                        Spacer(modifier = Modifier.height(spacing.l))

                        if (uiState.isAnswerRevealed) {
                            Text(
                                text = "EXPECTED ANSWER",
                                style = typography.caption,
                                color = colors.accentCyan
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = activeCard.expectedAnswer,
                                style = typography.body,
                                color = colors.primaryText
                            )

                            Spacer(modifier = Modifier.height(spacing.xl))

                            Text(
                                text = "How well did you recall this?",
                                style = typography.caption,
                                color = colors.mutedText
                            )
                            Spacer(modifier = Modifier.height(spacing.s))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OS95OutlinedButton(
                                    text = "Again",
                                    onClick = { viewModel.submitRating(RecallRating.AGAIN) },
                                    modifier = Modifier.weight(1f)
                                )
                                OS95OutlinedButton(
                                    text = "Hard",
                                    onClick = { viewModel.submitRating(RecallRating.HARD) },
                                    modifier = Modifier.weight(1f)
                                )
                                OS95Button(
                                    text = "Good",
                                    onClick = { viewModel.submitRating(RecallRating.GOOD) },
                                    modifier = Modifier.weight(1f)
                                )
                                OS95Button(
                                    text = "Easy",
                                    onClick = { viewModel.submitRating(RecallRating.EASY) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            OS95Button(
                                text = "Reveal Answer",
                                onClick = { viewModel.revealAnswer() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                if (uiState.allCards.isEmpty()) {
                    OS95EmptyState(
                        title = "No Recall Cards Created Yet",
                        description = "Add recall flashcards from Syllabus topics or convert errors from Mistake Bank to build your spaced repetition schedule.",
                        icon = Icons.Outlined.Psychology
                    )
                } else {
                    OS95EmptyState(
                        title = "All caught up on Recall!",
                        description = "No cards due for review today (${uiState.allCards.size} total cards scheduled). Next reviews will trigger automatically according to your forgetting curve.",
                        icon = Icons.Outlined.CheckCircle
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Recent Logs Section
            Text(
                text = "Recent Review History",
                style = typography.sectionTitle,
                color = colors.primaryText
            )
            Spacer(modifier = Modifier.height(spacing.s))

            if (uiState.recentReviews.isEmpty()) {
                Text(
                    text = "No review logs yet. Complete a recall card to record your retention history.",
                    style = typography.bodySmall,
                    color = colors.mutedText
                )
            } else {
                Text(
                    text = "${uiState.recentReviews.size} recent review events recorded locally.",
                    style = typography.bodySmall,
                    color = colors.secondaryText
                )
            }
        }
    }
}
