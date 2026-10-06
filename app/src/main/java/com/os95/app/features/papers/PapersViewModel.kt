package com.os95.app.features.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.PaperQuestionEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.model.GeneratedPaper
import com.os95.app.domain.model.PaperBlueprintRequest
import com.os95.app.domain.model.PaperDifficultyMode
import com.os95.app.domain.model.PaperGenerationResult
import com.os95.app.domain.model.QuestionResultInput
import com.os95.app.domain.model.RepetitionPolicy
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PapersUiState(
    val papers: List<PaperEntity> = emptyList(),
    val results: List<ExamResultEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),

    // Blueprint Generator Form State
    val selectedSubjectId: String? = null,
    val selectedChapterIds: Set<String> = emptySet(),
    val selectedDifficultyMode: PaperDifficultyMode = PaperDifficultyMode.MIXED,
    val selectedTotalMarks: Float = 50.0f,
    val selectedDurationMinutes: Int = 60,
    val allowedQuestionTypes: Set<String> = setOf("MCQ", "SHORT_ANSWER", "LONG_ANSWER", "NUMERICAL"),
    val adaptiveWeakWeighting: Boolean = true,
    val repetitionPolicy: RepetitionPolicy = RepetitionPolicy.AVOID_RECENT,

    // Generated Candidate & Failure State
    val generatedPaper: GeneratedPaper? = null,
    val generationFailureReason: String? = null,
    val generationRecoverySuggestions: List<String> = emptyList(),
    val isGenerating: Boolean = false,

    // Selected Active Paper for Preview / Exam
    val activePaper: PaperEntity? = null,
    val activePaperQuestions: List<PaperQuestionEntity> = emptyList(),

    // Physical Exam Mode State
    val isExamModeActive: Boolean = false,
    val examRemainingSeconds: Int = 0,
    val examTotalSeconds: Int = 0,
    val examStartTime: Long? = null,
    val showExitWarning: Boolean = false,

    // Result Recording State
    val showResultEntryDialog: Boolean = false,
    val feedbackMessage: String? = null,
    val isLoading: Boolean = true
)

class PapersViewModel(
    private val paperRepository: PaperRepository,
    private val syllabusRepository: SyllabusRepository,
    private val studentRepository: StudentRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PapersUiState())
    val uiState: StateFlow<PapersUiState> = _uiState.asStateFlow()

    private var examTimerJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            paperRepository.getAllPapers().collect { papers ->
                _uiState.value = _uiState.value.copy(papers = papers, isLoading = false)
            }
        }
        viewModelScope.launch {
            paperRepository.getAllResults().collect { results ->
                _uiState.value = _uiState.value.copy(results = results)
            }
        }
        viewModelScope.launch {
            syllabusRepository.getAllSubjects().collect { subjects ->
                val currentSub = _uiState.value.selectedSubjectId ?: subjects.firstOrNull()?.id
                _uiState.value = _uiState.value.copy(
                    subjects = subjects,
                    selectedSubjectId = currentSub
                )
                if (currentSub != null) {
                    loadChaptersForSubject(currentSub)
                }
            }
        }
    }

    fun selectSubject(subjectId: String) {
        _uiState.value = _uiState.value.copy(
            selectedSubjectId = subjectId,
            selectedChapterIds = emptySet()
        )
        loadChaptersForSubject(subjectId)
    }

    private fun loadChaptersForSubject(subjectId: String) {
        viewModelScope.launch {
            val chps = syllabusRepository.getChaptersForSubject(subjectId).first()
            _uiState.value = _uiState.value.copy(
                chapters = chps,
                selectedChapterIds = chps.map { it.id }.toSet() // Default select all chapters in subject
            )
        }
    }

    fun toggleChapter(chapterId: String) {
        val current = _uiState.value.selectedChapterIds.toMutableSet()
        if (current.contains(chapterId)) {
            current.remove(chapterId)
        } else {
            current.add(chapterId)
        }
        _uiState.value = _uiState.value.copy(selectedChapterIds = current)
    }

    fun setDifficulty(mode: PaperDifficultyMode) {
        _uiState.value = _uiState.value.copy(selectedDifficultyMode = mode)
    }

    fun setTotalMarks(marks: Float) {
        _uiState.value = _uiState.value.copy(selectedTotalMarks = marks)
    }

    fun setDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(selectedDurationMinutes = minutes)
    }

    fun toggleQuestionType(type: String) {
        val current = _uiState.value.allowedQuestionTypes.toMutableSet()
        if (current.contains(type)) {
            if (current.size > 1) current.remove(type) // Ensure at least 1 remains
        } else {
            current.add(type)
        }
        _uiState.value = _uiState.value.copy(allowedQuestionTypes = current)
    }

    fun setAdaptiveWeighting(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(adaptiveWeakWeighting = enabled)
    }

    fun setRepetitionPolicy(policy: RepetitionPolicy) {
        _uiState.value = _uiState.value.copy(repetitionPolicy = policy)
    }

    fun generatePaper(title: String) {
        val state = _uiState.value
        val subId = state.selectedSubjectId ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(
                isGenerating = true,
                generationFailureReason = null,
                generationRecoverySuggestions = emptyList()
            )

            val request = PaperBlueprintRequest(
                title = if (title.isBlank()) "Practice Paper (${state.selectedTotalMarks.toInt()}m)" else title.trim(),
                subjectId = subId,
                chapterIds = state.selectedChapterIds.toList(),
                totalMarks = state.selectedTotalMarks,
                durationMinutes = state.selectedDurationMinutes,
                difficultyMode = state.selectedDifficultyMode,
                allowedQuestionTypes = state.allowedQuestionTypes,
                repetitionPolicy = state.repetitionPolicy,
                adaptiveWeakTopicWeighting = state.adaptiveWeakWeighting
            )

            when (val result = paperRepository.generatePaper(request)) {
                is PaperGenerationResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        generatedPaper = result.generatedPaper,
                        isGenerating = false,
                        feedbackMessage = "Paper generated successfully!"
                    )
                }
                is PaperGenerationResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        generatedPaper = null,
                        generationFailureReason = result.reason,
                        generationRecoverySuggestions = result.recoverySuggestions,
                        isGenerating = false
                    )
                }
            }
        }
    }

    fun finalizeGeneratedPaper() {
        val generated = _uiState.value.generatedPaper ?: return
        viewModelScope.launch {
            val savedPaper = paperRepository.finalizeAndSavePaper(generated)
            _uiState.value = _uiState.value.copy(
                generatedPaper = null,
                feedbackMessage = "Paper \"${savedPaper.title}\" saved to PaperPilot library."
            )
            openPaperDetail(savedPaper.id)
        }
    }

    fun openPaperDetail(paperId: String) {
        viewModelScope.launch {
            val paper = paperRepository.getPaperById(paperId)
            if (paper != null) {
                paperRepository.getPaperQuestions(paperId).collect { questions ->
                    _uiState.value = _uiState.value.copy(
                        activePaper = paper,
                        activePaperQuestions = questions
                    )
                }
            }
        }
    }

    fun startExamMode(paper: PaperEntity) {
        val totalSecs = paper.durationMinutes * 60
        _uiState.value = _uiState.value.copy(
            isExamModeActive = true,
            activePaper = paper,
            examTotalSeconds = totalSecs,
            examRemainingSeconds = totalSecs,
            examStartTime = System.currentTimeMillis(),
            showExitWarning = false
        )

        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_uiState.value.examRemainingSeconds > 0 && _uiState.value.isExamModeActive) {
                delay(1000L)
                val newSecs = _uiState.value.examRemainingSeconds - 1
                _uiState.value = _uiState.value.copy(examRemainingSeconds = newSecs)
            }
            if (_uiState.value.isExamModeActive) {
                finishExamMode()
            }
        }
    }

    fun requestExitExam() {
        _uiState.value = _uiState.value.copy(showExitWarning = true)
    }

    fun cancelExitExam() {
        _uiState.value = _uiState.value.copy(showExitWarning = false)
    }

    fun confirmExitExam() {
        _uiState.value = _uiState.value.copy(showExitWarning = false)
        finishExamMode()
    }

    fun finishExamMode() {
        examTimerJob?.cancel()
        val state = _uiState.value
        val actualMinutesTaken = if (state.examStartTime != null) {
            val elapsedMs = System.currentTimeMillis() - state.examStartTime
            maxOf(1, (elapsedMs / (1000 * 60)).toInt())
        } else {
            state.activePaper?.durationMinutes ?: 60
        }

        // Record focus study session
        state.activePaper?.let { paper ->
            viewModelScope.launch {
                studentRepository?.logSession(paper.subjectId, null, actualMinutesTaken)
            }
        }

        _uiState.value = _uiState.value.copy(
            isExamModeActive = false,
            showResultEntryDialog = true
        )
    }

    fun recordDetailedResult(
        marksObtained: Float,
        timeTakenMinutes: Int,
        questionResults: List<QuestionResultInput>
    ) {
        val paper = _uiState.value.activePaper ?: return
        viewModelScope.launch {
            paperRepository.recordDetailedResult(
                paperId = paper.id,
                marksObtained = marksObtained,
                totalMarks = paper.totalMarks,
                timeTakenMinutes = timeTakenMinutes,
                questionResults = questionResults
            )
            _uiState.value = _uiState.value.copy(
                showResultEntryDialog = false,
                feedbackMessage = "Result recorded! Mistake Bank and Weak Topics updated."
            )
        }
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(showResultEntryDialog = false)
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }

    fun createPaper(subjectId: String, title: String, totalMarks: Float, durationMinutes: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            paperRepository.createPaper(subjectId, title.trim(), totalMarks, durationMinutes)
        }
    }
}
