package com.os95.app.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onNavigateToUniversalCsv: (() -> Unit)? = null,
    onAppReset: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    viewModel.exportDatabaseBackup(context, stream) { success, msg ->
                        backupStatusMessage = msg
                    }
                }
            } catch (e: Exception) {
                backupStatusMessage = "Export failed: ${e.message}"
            }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset 95OS Operating System?",
                    style = typography.sectionTitle,
                    color = colors.error
                )
            },
            text = {
                Text(
                    text = "This action will permanently delete all subjects, chapters, imported questions, exam results, mistake logs, and recall cards stored on this device.\n\nYour application will be restored to initial state and you will return to Onboarding.\n\nThis cannot be undone.",
                    style = typography.bodySmall,
                    color = colors.primaryText
                )
            },
            confirmButton = {
                OS95Button(
                    text = "Erase & Reset All Data",
                    onClick = {
                        showResetDialog = false
                        viewModel.resetEntireApplication {
                            onAppReset?.invoke()
                        }
                    }
                )
            },
            dismissButton = {
                OS95OutlinedButton(
                    text = "Cancel",
                    onClick = { showResetDialog = false }
                )
            },
            containerColor = colors.surface,
            textContentColor = colors.primaryText
        )
    }

    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            },
            title = {
                Text(
                    text = "Restore Database Backup?",
                    style = typography.sectionTitle,
                    color = colors.warning
                )
            },
            text = {
                Text(
                    text = "This will overwrite your current local database with the selected backup file. Existing test history will be replaced with the backup. Continue?",
                    style = typography.bodySmall,
                    color = colors.primaryText
                )
            },
            confirmButton = {
                OS95Button(
                    text = "Restore Database",
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                        if (uri != null) {
                            try {
                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                    viewModel.importDatabaseBackup(context, stream) { success, msg ->
                                        backupStatusMessage = msg
                                    }
                                }
                            } catch (e: Exception) {
                                backupStatusMessage = "Restore failed: ${e.message}"
                            }
                        }
                    }
                )
            },
            dismissButton = {
                OS95OutlinedButton(
                    text = "Cancel",
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                    }
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
            // Appearance Category (Custom Academic Themes)
            Column {
                Text(
                    text = "Academic Visual Themes",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = "Distraction-free palettes engineered for high-stakes exam concentration.",
                    style = typography.caption,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = ThemeMode.userSelectableThemes
                    themes.chunked(2).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowThemes.forEach { mode ->
                                val selected = uiState.themeMode == mode
                                OS95Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setThemeMode(mode) },
                                    backgroundColor = if (selected) colors.cardBackground else colors.surface
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = mode.displayName,
                                                style = typography.bodySmall,
                                                color = if (selected) colors.accent else colors.primaryText
                                            )
                                            if (selected) {
                                                Text(
                                                    text = "●",
                                                    style = typography.caption,
                                                    color = colors.accentCyan
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (mode.isDarkTheme) "Dark Canvas" else if (mode == ThemeMode.SYSTEM) "Auto Detect" else "Light Paper",
                                            style = typography.caption,
                                            color = colors.mutedText
                                        )
                                    }
                                }
                            }
                            if (rowThemes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
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

            // Data & Portability Category (CSV + Markdown)
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
                            text = "Universal CSV & Markdown (.md) Hub",
                            style = typography.sectionTitle,
                            color = colors.primaryText
                        )
                        Text(
                            text = "Import or export Questions, Syllabus Blueprints, Recall Cards, and Mistake Banks using either standard CSV or formatted Markdown (.md). 100% offline with zero cloud telemetry.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        if (onNavigateToUniversalCsv != null) {
                            OS95Button(
                                text = "Open CSV & Markdown Data Hub",
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

            // Database Sovereignty & Full Backup (.95os)
            Column {
                Text(
                    text = "Full Database Sovereignty (.95os)",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Export or restore the entire raw SQLite database (.95os). Includes all 14 database tables, exact SM-2 card intervals, historical sessions, and student profile.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OS95OutlinedButton(
                                text = "Export .95os Backup",
                                onClick = { exportLauncher.launch("95os_backup_${System.currentTimeMillis() / 1000}.95os") },
                                modifier = Modifier.weight(1f)
                            )
                            OS95OutlinedButton(
                                text = "Restore .95os Backup",
                                onClick = { importLauncher.launch(arrayOf("*/*")) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (backupStatusMessage != null) {
                            Text(
                                text = backupStatusMessage!!,
                                style = typography.caption,
                                color = colors.accent
                            )
                        }
                    }
                }
            }

            // Danger Zone: App Reset
            Column {
                Text(
                    text = "Operating System Maintenance",
                    style = typography.sectionTitle,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(spacing.s))
                OS95Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.cardBackground
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Reset 95OS Operating System",
                            style = typography.sectionTitle,
                            color = colors.error
                        )
                        Text(
                            text = "Permanently wipe all subjects, test papers, mistake banks, recall cards, and restore the initial onboarding state. Use this if you are starting a completely fresh academic year or exam cycle.",
                            style = typography.bodySmall,
                            color = colors.secondaryText
                        )
                        OS95OutlinedButton(
                            text = "Reset All Data...",
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
                            text = "Version 2.2.0 (The Offline Exam Operating System)",
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
