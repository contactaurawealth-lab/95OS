package com.os95.app.features.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.model.AdaptiveRetestCompletionSummary
import com.os95.app.domain.model.AdaptiveRetestConfig
import com.os95.app.domain.repository.AdvancedExamRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AdaptiveRetestStage {
    CONFIG,
    IN_TEST,
    SUMMARY
}

data class AdaptiveRetestUiState(
    val stage: AdaptiveRetestStage = AdaptiveRetestStage.CONFIG,
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val targetMarks: Float = 25.0f,
    val durationMinutes: Int = 30,
    val activePaper: PaperEntity? = null,
    val questions: List<PaperQuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val marksAwardedMap: Map<Int, Float> = emptyMap(),
    val userAnswers: Map<Int, String> = emptyMap(),
    val completionSummary: AdaptiveRetestCompletionSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AdaptiveRetestViewModel(
    private val advancedExamRepository: AdvancedExamRepository,
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdaptiveRetestUiState())
    val uiState: StateFlow<AdaptiveRetestUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                val subs = syllabusRepository.getAllSubjects().first()
                _uiState.value = _uiState.value.copy(subjects = subs)
            } catch (_: Exception) {}
        }
    }

    fun selectSubject(subjectId: String?) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
    }

    fun setTargetMarks(marks: Float) {
        _uiState.value = _uiState.value.copy(targetMarks = marks)
    }

    fun setDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = minutes)
    }

    fun generateRetest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val config = AdaptiveRetestConfig(
                    subjectId = _uiState.value.selectedSubjectId,
                    title = "Adaptive Diagnostic Re-Test",
                    durationMinutes = _uiState.value.durationMinutes,
                    targetMarks = _uiState.value.targetMarks
                )
                val result = advancedExamRepository.generateAdaptiveRetest(config)
                _uiState.value = _uiState.value.copy(
                    activePaper = result.paper,
                    questions = result.questions,
                    currentQuestionIndex = 0,
                    marksAwardedMap = emptyMap(),
                    userAnswers = emptyMap(),
                    stage = AdaptiveRetestStage.IN_TEST,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate re-test"
                )
            }
        }
    }

    fun recordAnswer(index: Int, text: String) {
        val updated = _uiState.value.userAnswers.toMutableMap()
        updated[index] = text
        _uiState.value = _uiState.value.copy(userAnswers = updated)
    }

    fun recordMarksAwarded(index: Int, marks: Float) {
        val updated = _uiState.value.marksAwardedMap.toMutableMap()
        updated[index] = marks
        _uiState.value = _uiState.value.copy(marksAwardedMap = updated)
    }

    fun nextQuestion() {
        val next = _uiState.value.currentQuestionIndex + 1
        if (next < _uiState.value.questions.size) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = next)
        }
    }

    fun previousQuestion() {
        val prev = _uiState.value.currentQuestionIndex - 1
        if (prev >= 0) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = prev)
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = index)
        }
    }

    fun finishRetest() {
        val paper = _uiState.value.activePaper ?: return
        val totalMarks = paper.totalMarks
        val awardedMap = _uiState.value.marksAwardedMap
        val obtainedMarks = awardedMap.values.sum()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val summary = advancedExamRepository.completeAdaptiveRetest(
                    paperId = paper.id,
                    marksObtained = obtainedMarks,
                    totalMarks = totalMarks,
                    marksAwardedMap = awardedMap
                )
                _uiState.value = _uiState.value.copy(
                    completionSummary = summary,
                    stage = AdaptiveRetestStage.SUMMARY,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun restart() {
        _uiState.value = _uiState.value.copy(
            stage = AdaptiveRetestStage.CONFIG,
            activePaper = null,
            questions = emptyList(),
            completionSummary = null,
            marksAwardedMap = emptyMap(),
            userAnswers = emptyMap()
        )
    }
}
