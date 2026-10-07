package com.os95.app.features.focus

import java.util.Locale
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.os95.app.core.audio.AcousticMode
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95DropdownSelector
import com.os95.app.core.ui.component.OS95MenuAction
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme

@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    val minutes = uiState.remainingSeconds / 60
    val seconds = uiState.remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    var showAbortDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = uiState.isRunning) {
        showAbortDialog = true
    }

    if (showAbortDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = {
                Text(
                    text = "End Focus Session Early?",
                    style = typography.sectionTitle,
                    color = colors.warning
                )
            },
            text = {
                Text(
                    text = "Your active focus timer is still running. Exiting now will cancel the remaining session duration. Are you sure?",
                    style = typography.bodySmall,
                    color = colors.primaryText
                )
            },
            confirmButton = {
                OS95Button(
                    text = "End Session",
                    onClick = {
                        showAbortDialog = false
                        viewModel.reset()
                        onNavigateBack()
                    }
                )
            },
            dismissButton = {
                OS95OutlinedButton(
                    text = "Keep Focusing",
                    onClick = { showAbortDialog = false }
                )
            },
            containerColor = colors.surface,
            textContentColor = colors.primaryText
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Focus & Exam Mode",
            subtitle = if (uiState.isRunning) "Session Active" else "Ready",
            onBack = {
                if (uiState.isRunning) {
                    showAbortDialog = true
                } else {
                    onNavigateBack()
                }
            },
            overflowActions = listOf(
                OS95MenuAction(
                    label = "25m Sprint Preset",
                    enabled = !uiState.isRunning,
                    onClick = { viewModel.setDuration(25) }
                ),
                OS95MenuAction(
                    label = "50m Study Preset",
                    enabled = !uiState.isRunning,
                    onClick = { viewModel.setDuration(50) }
                ),
                OS95MenuAction(
                    label = "90m Exam Preset",
                    enabled = !uiState.isRunning,
                    onClick = { viewModel.setDuration(90) }
                ),
                OS95MenuAction(
                    label = "Reset Timer",
                    onClick = { viewModel.reset() }
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Preset Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OS95OutlinedButton(
                    text = "25m Sprint",
                    onClick = { viewModel.setDuration(25) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isRunning
                )
                OS95OutlinedButton(
                    text = "50m Study",
                    onClick = { viewModel.setDuration(50) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isRunning
                )
                OS95OutlinedButton(
                    text = "90m Exam",
                    onClick = { viewModel.setDuration(90) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isRunning
                )
            }

            Spacer(modifier = Modifier.height(spacing.xxl))

            // Large Minimalist Timer
            OS95Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.cardBackground
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timeFormatted,
                        style = typography.statNumber.copy(fontSize = 64.sp, lineHeight = 70.sp),
                        color = if (uiState.isRunning) colors.accent else colors.primaryText
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    Text(
                        text = if (uiState.isRunning) "Exam conditions enforced: No distractions" else "Tap start to begin focus session",
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Procedural Offline Focus Acoustics Dropdown Selector
            OS95DropdownSelector(
                label = "Procedural Audio Ambience (100% Offline)",
                selectedValue = uiState.acousticMode,
                items = AcousticMode.values().toList(),
                itemLabel = { mode ->
                    when (mode) {
                        AcousticMode.OFF -> "Mute (Silent Focus)"
                        AcousticMode.BROWNIAN -> "Brown Noise (${mode.description})"
                        AcousticMode.PINK -> "Pink Noise (${mode.description})"
                        AcousticMode.CLOCK_TICK -> "Exam Clock (${mode.description})"
                    }
                },
                onItemSelected = { viewModel.setAcousticMode(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.l))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isRunning) {
                    OS95OutlinedButton(
                        text = "Pause",
                        icon = Icons.Outlined.Pause,
                        onClick = { viewModel.pause() },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    OS95Button(
                        text = "Start Session",
                        icon = Icons.Outlined.PlayArrow,
                        onClick = { viewModel.start() },
                        modifier = Modifier.weight(1f)
                    )
                }

                OS95OutlinedButton(
                    text = "Reset",
                    icon = Icons.Outlined.Refresh,
                    onClick = { viewModel.reset() }
                )
            }
        }
    }
}
