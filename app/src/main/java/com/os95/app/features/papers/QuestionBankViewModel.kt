package com.os95.app.features.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.QuestionBankEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class QuestionBankUiState(
    val questions: List<QuestionBankEntity> = emptyList(),
    val filteredQuestions: List<QuestionBankEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val selectedDifficulty: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class QuestionBankViewModel(
    private val paperRepository: PaperRepository,
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionBankUiState())
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                paperRepository.getAllQuestions(),
                syllabusRepository.getAllSubjects()
            ) { questions, subjects ->
                Pair(questions, subjects)
            }.collect { (questions, subjects) ->
                _uiState.value = _uiState.value.copy(
                    questions = questions,
                    subjects = subjects,
                    isLoading = false
                )
                applyFilters()
            }
        }
    }

    fun setSubjectFilter(subjectId: String?) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
        applyFilters()
    }

    fun setDifficultyFilter(difficulty: String?) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.questions.filter { q ->
            val matchSubject = state.selectedSubjectId == null || q.subjectId == state.selectedSubjectId
            val matchDifficulty = state.selectedDifficulty == null || q.difficulty == state.selectedDifficulty
            val matchSearch = state.searchQuery.isBlank() || q.questionText.contains(state.searchQuery, ignoreCase = true)
            matchSubject && matchDifficulty && matchSearch
        }
        _uiState.value = state.copy(filteredQuestions = filtered)
    }

    fun addQuestion(
        subjectId: String,
        chapterId: String,
        text: String,
        marks: Float,
        difficulty: String,
        questionType: String
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            paperRepository.addQuestion(
                subjectId = subjectId,
                chapterId = chapterId,
                topicId = null,
                text = text.trim(),
                marks = marks,
                difficulty = difficulty,
                questionType = questionType
            )
        }
    }

    fun deleteQuestion(question: QuestionBankEntity) {
        viewModelScope.launch {
            paperRepository.deleteQuestion(question)
        }
    }
}
