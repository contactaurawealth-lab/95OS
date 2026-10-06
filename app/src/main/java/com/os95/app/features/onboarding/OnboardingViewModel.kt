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
    EXAM_TARGET,
    SUBJECTS,
    DAILY_HABIT,
    APPEARANCE,
    SUMMARY
}

data class SubjectCatalogItem(
    val name: String,
    val stream: String,
    val colorHex: String
)

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val studentName: String = "",
    val gradeLevel: String = "",
    val targetScore: Float = 95.0f,
    val targetDaysAhead: Int = 90,
    val selectedSubjects: List<String> = listOf("Mathematics", "Physics", "Chemistry", "Computer Science", "English Literature"),
    val customSubjectInput: String = "",
    val dailyStudyMinutes: Int = 90,
    val defaultExamDurationMinutes: Int = 90,
    val selectedStreamFilter: String = "ALL",
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

class OnboardingViewModel(
    private val preferencesManager: PreferencesManager,
    private val studentRepository: StudentRepository,
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    companion object {
        val ALL_SUBJECTS = listOf(
            SubjectCatalogItem("Mathematics", "SCIENCE", "#B8781E"),
            SubjectCatalogItem("Physics", "SCIENCE", "#D97706"),
            SubjectCatalogItem("Chemistry", "SCIENCE", "#0284C7"),
            SubjectCatalogItem("Biology", "SCIENCE", "#2E7D32"),
            SubjectCatalogItem("Computer Science", "SCIENCE", "#0E7490"),
            SubjectCatalogItem("Statistics", "SCIENCE", "#78350F"),
            SubjectCatalogItem("History", "HUMANITIES", "#854D0E"),
            SubjectCatalogItem("Geography", "HUMANITIES", "#15803D"),
            SubjectCatalogItem("Political Science", "HUMANITIES", "#B45309"),
            SubjectCatalogItem("Economics", "COMMERCE", "#0369A1"),
            SubjectCatalogItem("Psychology", "HUMANITIES", "#7C2D12"),
            SubjectCatalogItem("Sociology", "HUMANITIES", "#65A30D"),
            SubjectCatalogItem("Accountancy", "COMMERCE", "#0F766E"),
            SubjectCatalogItem("Business Studies", "COMMERCE", "#B91C1C"),
            SubjectCatalogItem("Financial Markets", "COMMERCE", "#D97706"),
            SubjectCatalogItem("English Literature", "LANGUAGES", "#475569"),
            SubjectCatalogItem("General Aptitude", "LANGUAGES", "#0284C7"),
            SubjectCatalogItem("Environmental Science", "SCIENCE", "#166534")
        )
    }

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

    fun setTargetDaysAhead(days: Int) {
        _uiState.value = _uiState.value.copy(targetDaysAhead = days)
    }

    fun setDailyStudyMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(dailyStudyMinutes = minutes)
    }

    fun setDefaultExamDurationMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(defaultExamDurationMinutes = minutes)
    }

    fun setStreamFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedStreamFilter = filter)
    }

    fun applyStreamPreset(presetName: String) {
        val preset = when (presetName) {
            "PCM" -> listOf("Mathematics", "Physics", "Chemistry", "Computer Science", "English Literature")
            "PCB" -> listOf("Biology", "Physics", "Chemistry", "English Literature", "Psychology")
            "COMMERCE" -> listOf("Accountancy", "Business Studies", "Economics", "Mathematics", "English Literature")
            "HUMANITIES" -> listOf("History", "Political Science", "Geography", "Economics", "English Literature")
            else -> _uiState.value.selectedSubjects
        }
        _uiState.value = _uiState.value.copy(selectedSubjects = preset)
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
        val trimmed = subject.trim()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
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
                gradeLevel = state.gradeLevel.ifBlank { "Board Exam / 95OS Candidate" },
                targetPercentage = state.targetScore
            )
            studentRepository.saveProfile(profile)

            // 2. Insert initial subjects with color palette
            val subjectColorMap = ALL_SUBJECTS.associate { it.name to it.colorHex }
            state.selectedSubjects.forEach { subName ->
                try {
                    val color = subjectColorMap[subName] ?: "#B8781E"
                    syllabusRepository.createSubject(subName, color)
                } catch (_: Exception) {}
            }

            // 3. Save Theme, Target, Exam Date & Preferences
            val targetExamTimestamp = System.currentTimeMillis() + (state.targetDaysAhead.toLong() * 24L * 60L * 60L * 1000L)
            preferencesManager.setThemeMode(state.themeMode)
            preferencesManager.setExamTarget(state.targetScore)
            preferencesManager.setTargetExamDate(targetExamTimestamp)
            preferencesManager.setAvailableStudyMinutes(state.dailyStudyMinutes)
            preferencesManager.setOnboardingCompleted(true)

            // 4. Save Study Preferences in database
            studentRepository.savePreferences(
                com.os95.app.core.database.entity.StudyPreferencesEntity(
                    dailyTargetMinutes = state.dailyStudyMinutes,
                    defaultExamDurationMinutes = state.defaultExamDurationMinutes
                )
            )

            onFinished()
        }
    }
}
