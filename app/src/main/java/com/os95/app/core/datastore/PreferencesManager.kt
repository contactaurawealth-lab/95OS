package com.os95.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.os95.app.core.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "95os_preferences")

data class OS95Preferences(
    val isOnboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val examTargetPercentage: Float = 95.0f,
    val targetExamDateTimestamp: Long? = null,
    val completedLast7DaysTaskIds: Set<String> = emptySet(),
    val availableStudyMinutes: Int = 60
)

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_EXAM_TARGET = floatPreferencesKey("exam_target_percentage")
        val KEY_TARGET_EXAM_DATE = longPreferencesKey("target_exam_date")
        val KEY_COMPLETED_LAST_7_DAYS_TASKS = stringSetPreferencesKey("completed_last_7_days_tasks")
        val KEY_AVAILABLE_STUDY_MINUTES = intPreferencesKey("available_study_minutes")
    }

    val preferencesFlow: Flow<OS95Preferences> = context.dataStore.data.map { prefs ->
        val onboarding = prefs[KEY_ONBOARDING_COMPLETED] ?: false
        val themeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val theme = try { ThemeMode.valueOf(themeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
        val target = prefs[KEY_EXAM_TARGET] ?: 95.0f
        val examDate = prefs[KEY_TARGET_EXAM_DATE]
        val completedTasks = prefs[KEY_COMPLETED_LAST_7_DAYS_TASKS] ?: emptySet()
        val studyMins = prefs[KEY_AVAILABLE_STUDY_MINUTES] ?: 60

        OS95Preferences(
            isOnboardingCompleted = onboarding,
            themeMode = theme,
            examTargetPercentage = target,
            targetExamDateTimestamp = examDate,
            completedLast7DaysTaskIds = completedTasks,
            availableStudyMinutes = studyMins
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setExamTarget(target: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EXAM_TARGET] = target
        }
    }

    suspend fun setTargetExamDate(timestamp: Long?) {
        context.dataStore.edit { prefs ->
            if (timestamp != null) {
                prefs[KEY_TARGET_EXAM_DATE] = timestamp
            } else {
                prefs.remove(KEY_TARGET_EXAM_DATE)
            }
        }
    }

    suspend fun toggleLast7DaysTask(taskId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_COMPLETED_LAST_7_DAYS_TASKS] ?: emptySet()
            val updated = if (current.contains(taskId)) {
                current - taskId
            } else {
                current + taskId
            }
            prefs[KEY_COMPLETED_LAST_7_DAYS_TASKS] = updated
        }
    }

    suspend fun setAvailableStudyMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AVAILABLE_STUDY_MINUTES] = minutes
        }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
