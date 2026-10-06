package com.os95.app.features.focus

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
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
    val timeFormatted = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        OS95TopBar(
            title = "Focus & Exam Mode",
            subtitle = if (uiState.isRunning) "Session Active" else "Ready",
            onBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
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

            Spacer(modifier = Modifier.height(spacing.xxl))

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
