package com.os95.app.features.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.component.OS95Button
import com.os95.app.core.ui.component.OS95Card
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TopBar
import com.os95.app.core.ui.theme.OS95Theme
import com.os95.app.core.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToUniversalCsv: (() -> Unit)? = null
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
            title = "Settings",
            subtitle = "Preferences & Data",
            onBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Appearance Category
            Column {
                Text(
                    text = "Appearance",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Theme Mode",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.s))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.values().forEach { mode ->
                                val selected = uiState.themeMode == mode
                                if (selected) {
                                    OS95Button(
                                        text = mode.name,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    OS95OutlinedButton(
                                        text = mode.name,
                                        onClick = { viewModel.setThemeMode(mode) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Study & Targets Category
            Column {
                Text(
                    text = "Study & Exam Targets",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Target Exam Score", style = typography.bodySmall, color = colors.primaryText)
                            Text(text = "${uiState.targetPercentage.toInt()}%", style = typography.sectionTitle, color = colors.accentCyan)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Daily Study Target", style = typography.bodySmall, color = colors.primaryText)
                            Text(text = "${uiState.preferences.dailyTargetMinutes} min", style = typography.caption, color = colors.accent)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Default Exam Duration", style = typography.bodySmall, color = colors.primaryText)
                            Text(text = "${uiState.preferences.defaultExamDurationMinutes} min", style = typography.caption, color = colors.mutedText)
                        }
                    }
                }
            }

            // Data & Portability Category
            Column {
                Text(
                    text = "Data Sovereignty & Portability",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Universal CSV Engine",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Import or export Questions, Syllabus, Recall Cards, Mistakes, and Exam Results. 100% offline with zero cloud dependencies.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        if (onNavigateToUniversalCsv != null) {
                            OS95Button(
                                text = "Open Universal CSV Engine",
                                onClick = onNavigateToUniversalCsv,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Privacy Category
            Column {
                Text(
                    text = "Privacy & Local Offline Sovereignty",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "95OS is engineered offline-first. Your academic questions, test scores, mistakes, and targets stay exclusively on this physical device. No telemetry, no background analytics, and no remote authentication.",
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                }
            }

            // About Category
            Column {
                Text(
                    text = "About",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "95OS — The Offline Exam Operating System",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "Version 1.0.0 (Phase 1 Architecture Foundation)",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = "«Syllabus → Learn → Recall → Practice → Result → Lost Marks → Mistakes → Revision → Re-test → Score Progress»",
                            style = typography.caption,
                            color = colors.accent
                        )
                    }
                }
            }
        }
    }
}
