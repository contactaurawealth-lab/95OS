package com.os95.app.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.StudentProfileEntity
import com.os95.app.core.datastore.PreferencesManager
import com.os95.app.core.ui.theme.ThemeMode
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    PROFILE,
    SUBJECTS,
    EXAM_TARGET,
    APPEARANCE,
    SUMMARY
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val studentName: String = "",
    val gradeLevel: String = "",
    val targetScore: Float = 95.0f,
    val selectedSubjects: List<String> = listOf("Mathematics", "Physics", "Chemistry"),
    val customSubjectInput: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class OnboardingViewModel(
    private val preferencesManager: PreferencesManager,
    private val studentRepository: StudentRepository,
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setStudentName(name: String) {
        _uiState.value = _uiState.value.copy(studentName = name)
    }

    fun setGradeLevel(grade: String) {
        _uiState.value = _uiState.value.copy(gradeLevel = grade)
    }

    fun setTargetScore(target: Float) {
        _uiState.value = _uiState.value.copy(targetScore = target)
    }

    fun toggleSubject(subject: String) {
        val current = _uiState.value.selectedSubjects.toMutableList()
        if (current.contains(subject)) {
            current.remove(subject)
        } else {
            current.add(subject)
        }
        _uiState.value = _uiState.value.copy(selectedSubjects = current)
    }

    fun addCustomSubject(subject: String) {
        if (subject.isBlank()) return
        val current = _uiState.value.selectedSubjects.toMutableList()
        if (!current.contains(subject.trim())) {
            current.add(subject.trim())
        }
        _uiState.value = _uiState.value.copy(selectedSubjects = current)
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun nextStep() {
        val current = _uiState.value.step
        val steps = OnboardingStep.values()
        val nextIndex = current.ordinal + 1
        if (nextIndex < steps.size) {
            _uiState.value = _uiState.value.copy(step = steps[nextIndex])
        }
    }

    fun previousStep() {
        val current = _uiState.value.step
        val steps = OnboardingStep.values()
        val prevIndex = current.ordinal - 1
        if (prevIndex >= 0) {
            _uiState.value = _uiState.value.copy(step = steps[prevIndex])
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            // 1. Save Profile
            val profile = StudentProfileEntity(
                name = state.studentName.ifBlank { "Student" },
                gradeLevel = state.gradeLevel,
                targetPercentage = state.targetScore
            )
            studentRepository.saveProfile(profile)

            // 2. Insert initial subjects
            state.selectedSubjects.forEach { subName ->
                try {
                    syllabusRepository.createSubject(subName, "#B8781E")
                } catch (_: Exception) {}
            }

            // 3. Save Theme & Preferences
            preferencesManager.setThemeMode(state.themeMode)
            preferencesManager.setExamTarget(state.targetScore)
            preferencesManager.setOnboardingCompleted(true)

            onFinished()
        }
    }
}
