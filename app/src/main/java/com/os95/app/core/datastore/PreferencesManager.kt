package com.os95.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.os95.app.core.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "95os_preferences")

data class OS95Preferences(
    val isOnboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val examTargetPercentage: Float = 95.0f
)

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_EXAM_TARGET = floatPreferencesKey("exam_target_percentage")
    }

    val preferencesFlow: Flow<OS95Preferences> = context.dataStore.data.map { prefs ->
        val onboarding = prefs[KEY_ONBOARDING_COMPLETED] ?: false
        val themeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val theme = try { ThemeMode.valueOf(themeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
        val target = prefs[KEY_EXAM_TARGET] ?: 95.0f

        OS95Preferences(
            isOnboardingCompleted = onboarding,
            themeMode = theme,
            examTargetPercentage = target
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
}
