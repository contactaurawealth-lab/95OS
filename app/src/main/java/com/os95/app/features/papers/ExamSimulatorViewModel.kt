package com.os95.app.features.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.model.ExamReadinessResult
import com.os95.app.domain.model.ExamSimulationSubmission
import com.os95.app.domain.model.ExamSimulatorConfig
import com.os95.app.domain.repository.AdvancedExamRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SimulatorStage {
    CONFIG,
    IN_EXAM,
    RESULT
}

data class ExamSimulatorUiState(
    val stage: SimulatorStage = SimulatorStage.CONFIG,
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val durationMinutes: Int = 60,
    val totalMarks: Float = 50.0f,
    val difficulty: String = "BALANCED", // BALANCED, EASY, MEDIUM, HARD
    val activePaper: PaperEntity? = null,
    val questions: List<PaperQuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val timeRemainingSeconds: Int = 3600,
    val timeSpentSeconds: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(),
    val marksAwardedMap: Map<Int, Float> = emptyMap(),
    val markedForReviewIndices: Set<Int> = emptySet(),
    val showConfirmSubmitDialog: Boolean = false,
    val readinessResult: ExamReadinessResult? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ExamSimulatorViewModel(
    private val advancedExamRepository: AdvancedExamRepository,
    private val syllabusRepository: SyllabusRepository,
    private val paperRepository: com.os95.app.domain.repository.PaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamSimulatorUiState())
    val uiState: StateFlow<ExamSimulatorUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            try {
                val subs = syllabusRepository.getAllSubjects().first()
                val initialSubjectId = subs.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    subjects = subs,
                    selectedSubjectId = initialSubjectId
                )
            } catch (_: Exception) {}
        }
    }

    fun selectSubject(subjectId: String?) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
    }

    fun setDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = minutes)
    }

    fun setTotalMarks(marks: Float) {
        _uiState.value = _uiState.value.copy(totalMarks = marks)
    }

    fun setDifficulty(difficulty: String) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    fun startExam() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val config = ExamSimulatorConfig(
                    subjectId = _uiState.value.selectedSubjectId,
                    durationMinutes = _uiState.value.durationMinutes,
                    totalMarks = _uiState.value.totalMarks,
                    difficulty = _uiState.value.difficulty
                )
                val paper = advancedExamRepository.generateExamSimulationPaper(config)
                val totalSeconds = paper.durationMinutes * 60

                // Fetch paper questions created
                val questions = (advancedExamRepository as? com.os95.app.data.repository.OfflineAdvancedExamRepository)
                    ?.let { /* handled via repository or dao */ }

                // Default questions loaded with paper
                _uiState.value = _uiState.value.copy(
                    activePaper = paper,
                    timeRemainingSeconds = totalSeconds,
                    timeSpentSeconds = 0,
                    currentQuestionIndex = 0,
                    userAnswers = emptyMap(),
                    marksAwardedMap = emptyMap(),
                    markedForReviewIndices = emptySet(),
                    stage = SimulatorStage.IN_EXAM,
                    isLoading = false
                )

                loadQuestionsForPaper(paper.id)
                startTimer(totalSeconds)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to generate simulation"
                )
            }
        }
    }

    private fun loadQuestionsForPaper(paperId: String) {
        viewModelScope.launch {
            try {
                paperRepository.getPaperQuestions(paperId).collect { qs ->
                    _uiState.value = _uiState.value.copy(questions = qs)
                }
            } catch (_: Exception) {}
        }
    }

    fun setLoadedQuestions(questions: List<PaperQuestionEntity>) {
        _uiState.value = _uiState.value.copy(questions = questions)
    }

    private fun startTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                val spent = totalSeconds - remaining
                _uiState.value = _uiState.value.copy(
                    timeRemainingSeconds = remaining,
                    timeSpentSeconds = spent
                )
            }
            // Auto submit when time expires
            confirmSubmit()
        }
    }

    fun recordAnswer(index: Int, text: String) {
        val updated = _uiState.value.userAnswers.toMutableMap()
        updated[index] = text
        _uiState.value = _uiState.value.copy(userAnswers = updated)
    }

    fun recordSelfMarks(index: Int, marks: Float) {
        val updated = _uiState.value.marksAwardedMap.toMutableMap()
        updated[index] = marks
        _uiState.value = _uiState.value.copy(marksAwardedMap = updated)
    }

    fun toggleMarkForReview(index: Int) {
        val currentSet = _uiState.value.markedForReviewIndices.toMutableSet()
        if (currentSet.contains(index)) {
            currentSet.remove(index)
        } else {
            currentSet.add(index)
        }
        _uiState.value = _uiState.value.copy(markedForReviewIndices = currentSet)
    }

    fun jumpToQuestion(index: Int) {
        if (index in _uiState.value.questions.indices) {
            _uiState.value = _uiState.value.copy(currentQuestionIndex = index)
        }
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

    fun showSubmitDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showConfirmSubmitDialog = show)
    }

    fun confirmSubmit() {
        timerJob?.cancel()
        val paper = _uiState.value.activePaper ?: return
        val questions = _uiState.value.questions

        // Default awarding: If user typed an answer and didn't manually self-grade, credit proportional marks
        val finalAwardedMap = _uiState.value.marksAwardedMap.toMutableMap()
        questions.forEachIndexed { idx, q ->
            if (!finalAwardedMap.containsKey(idx)) {
                val hasAnswer = !_uiState.value.userAnswers[idx].isNullOrBlank()
                finalAwardedMap[idx] = if (hasAnswer) q.snapshotMarks else 0f
            }
        }

        val submission = ExamSimulationSubmission(
            paperId = paper.id,
            answers = _uiState.value.userAnswers,
            marksAwardedMap = finalAwardedMap,
            timeSpentSeconds = _uiState.value.timeSpentSeconds,
            markedForReviewIndices = _uiState.value.markedForReviewIndices
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                showConfirmSubmitDialog = false
            )
            try {
                val result = advancedExamRepository.submitExamSimulation(
                    paperId = paper.id,
                    submission = submission
                )
                _uiState.value = _uiState.value.copy(
                    readinessResult = result,
                    stage = SimulatorStage.RESULT,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun restart() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            stage = SimulatorStage.CONFIG,
            activePaper = null,
            questions = emptyList(),
            readinessResult = null,
            userAnswers = emptyMap(),
            marksAwardedMap = emptyMap(),
            markedForReviewIndices = emptySet(),
            showConfirmSubmitDialog = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
