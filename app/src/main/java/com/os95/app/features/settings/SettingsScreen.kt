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
import com.os95.app.core.ui.component.OS95Dialog
import com.os95.app.core.ui.component.OS95OutlinedButton
import com.os95.app.core.ui.component.OS95TextField
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

    var showTargetScoreDialog by remember { mutableStateOf(false) }
    var targetScoreInput by remember { mutableStateOf("") }
    var showDailyTargetDialog by remember { mutableStateOf(false) }
    var dailyTargetInput by remember { mutableStateOf("") }
    var showDurationDialog by remember { mutableStateOf(false) }
    var durationInput by remember { mutableStateOf("") }

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
        OS95Dialog(
            title = "Reset 95OS Operating System?",
            message = "This action will permanently delete all subjects, chapters, imported questions, exam results, mistake logs, and recall cards stored on this device.\n\nYour application will be restored to initial state and you will return to Onboarding.\n\nThis cannot be undone.",
            confirmButtonText = "Erase & Reset All Data",
            dismissButtonText = "Cancel",
            onConfirm = {
                showResetDialog = false
                viewModel.resetEntireApplication {
                    onAppReset?.invoke()
                }
            },
            onDismissRequest = { showResetDialog = false }
        )
    }

    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        OS95Dialog(
            title = "Restore Database Backup?",
            message = "This will overwrite your current local database with the selected backup file. Existing test history will be replaced with the backup. Continue?",
            confirmButtonText = "Restore Database",
            dismissButtonText = "Cancel",
            onConfirm = {
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
            },
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            }
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
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    targetScoreInput = uiState.targetPercentage.toInt().toString()
                                    showTargetScoreDialog = true
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Target Exam Score", style = typography.bodySmall, color = colors.primaryText)
                                Text(text = "Tap to edit target threshold", style = typography.caption, color = colors.mutedText)
                            }
                            Text(text = "${uiState.targetPercentage.toInt()}%", style = typography.sectionTitle, color = colors.accentCyan)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dailyTargetInput = uiState.preferences.dailyTargetMinutes.toString()
                                    showDailyTargetDialog = true
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Daily Study Target", style = typography.bodySmall, color = colors.primaryText)
                                Text(text = "Tap to adjust daily minutes", style = typography.caption, color = colors.mutedText)
                            }
                            Text(text = "${uiState.preferences.dailyTargetMinutes} min", style = typography.sectionTitle, color = colors.accent)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    durationInput = uiState.preferences.defaultExamDurationMinutes.toString()
                                    showDurationDialog = true
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Default Exam Duration", style = typography.bodySmall, color = colors.primaryText)
                                Text(text = "Tap to set default duration", style = typography.caption, color = colors.mutedText)
                            }
                            Text(text = "${uiState.preferences.defaultExamDurationMinutes} min", style = typography.sectionTitle, color = colors.mutedText)
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
                            text = "Version 2.3.0 (The Offline Exam Operating System)",
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

    if (showTargetScoreDialog) {
        OS95Dialog(
            title = "Set Target Exam Score",
            confirmButtonText = "Save",
            dismissButtonText = "Cancel",
            onConfirm = {
                val parsed = targetScoreInput.toFloatOrNull()
                if (parsed != null && parsed in 50f..100f) {
                    viewModel.setTargetPercentage(parsed)
                    showTargetScoreDialog = false
                }
            },
            onDismissRequest = { showTargetScoreDialog = false },
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your goal score percentage (e.g. 95%):",
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    OS95TextField(
                        value = targetScoreInput,
                        onValueChange = { targetScoreInput = it },
                        label = "Target Percentage (%)",
                        placeholder = "e.g. 95",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (showDailyTargetDialog) {
        OS95Dialog(
            title = "Set Daily Study Target",
            confirmButtonText = "Save",
            dismissButtonText = "Cancel",
            onConfirm = {
                val parsed = dailyTargetInput.toIntOrNull()
                if (parsed != null && parsed > 0) {
                    viewModel.updateDailyTargetMinutes(parsed)
                    showDailyTargetDialog = false
                }
            },
            onDismissRequest = { showDailyTargetDialog = false },
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter daily deliberate practice target in minutes:",
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    OS95TextField(
                        value = dailyTargetInput,
                        onValueChange = { dailyTargetInput = it },
                        label = "Daily Minutes",
                        placeholder = "e.g. 120",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (showDurationDialog) {
        OS95Dialog(
            title = "Set Default Exam Duration",
            confirmButtonText = "Save",
            dismissButtonText = "Cancel",
            onConfirm = {
                val parsed = durationInput.toIntOrNull()
                if (parsed != null && parsed > 0) {
                    viewModel.updateDefaultExamDuration(parsed)
                    showDurationDialog = false
                }
            },
            onDismissRequest = { showDurationDialog = false },
            content = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter default duration for practice exam papers in minutes:",
                        style = typography.bodySmall,
                        color = colors.secondaryText
                    )
                    Spacer(modifier = Modifier.height(spacing.m))
                    OS95TextField(
                        value = durationInput,
                        onValueChange = { durationInput = it },
                        label = "Duration (Minutes)",
                        placeholder = "e.g. 90",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}
