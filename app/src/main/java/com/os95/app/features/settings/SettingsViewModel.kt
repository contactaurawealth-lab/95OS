package com.os95.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.StudyPreferencesEntity
import com.os95.app.core.datastore.PreferencesManager
import com.os95.app.core.ui.theme.ThemeMode
import com.os95.app.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val targetPercentage: Float = 95.0f,
    val preferences: StudyPreferencesEntity = StudyPreferencesEntity(),
    val isSaved: Boolean = false
)

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val studentRepository: StudentRepository,
    private val database: com.os95.app.core.database.OS95Database
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.preferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    themeMode = prefs.themeMode,
                    targetPercentage = prefs.examTargetPercentage
                )
            }
        }
        viewModelScope.launch {
            studentRepository.getPreferencesFlow().collect { dbPrefs ->
                if (dbPrefs != null) {
                    _uiState.value = _uiState.value.copy(preferences = dbPrefs)
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setTargetPercentage(target: Float) {
        viewModelScope.launch {
            preferencesManager.setExamTarget(target)
            val currentProfile = studentRepository.getProfile()
            if (currentProfile != null) {
                studentRepository.saveProfile(currentProfile.copy(targetPercentage = target))
            }
        }
    }

    fun updateDailyTargetMinutes(minutes: Int) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(dailyTargetMinutes = minutes)
            studentRepository.savePreferences(updated)
        }
    }

    fun updateDefaultExamDuration(minutes: Int) {
        viewModelScope.launch {
            val updated = _uiState.value.preferences.copy(defaultExamDurationMinutes = minutes)
            studentRepository.savePreferences(updated)
        }
    }

    fun resetEntireApplication(onResetComplete: () -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                database.clearAllTables()
                preferencesManager.resetAllPreferences()
            }
            onResetComplete()
        }
    }

    fun exportDatabaseBackup(context: android.content.Context, outputStream: java.io.OutputStream, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = com.os95.app.core.database.backup.DatabaseBackupManager.exportDatabase(context, outputStream)
            if (result.isSuccess) {
                val bytes = result.getOrNull() ?: 0L
                val kb = bytes / 1024
                onResult(true, "Successfully exported database backup ($kb KB).")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Export failed.")
            }
        }
    }

    fun importDatabaseBackup(context: android.content.Context, inputStream: java.io.InputStream, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = com.os95.app.core.database.backup.DatabaseBackupManager.importDatabase(context, inputStream)
            if (result.isSuccess) {
                onResult(true, "Database backup restored successfully.")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Restore failed.")
            }
        }
    }
}
