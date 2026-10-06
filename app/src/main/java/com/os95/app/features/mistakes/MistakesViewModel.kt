package com.os95.app.features.mistakes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.repository.MistakeRepository
import com.os95.app.domain.repository.RecallRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MistakesUiState(
    val activeMistakes: List<MistakeEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val totalMarksLost: Float = 0f,
    val feedbackMessage: String? = null,
    val isLoading: Boolean = true
)

class MistakesViewModel(
    private val mistakeRepository: MistakeRepository,
    private val syllabusRepository: SyllabusRepository,
    private val recallRepository: RecallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MistakesUiState())
    val uiState: StateFlow<MistakesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            mistakeRepository.getActiveMistakes().collect { mistakes ->
                val lost = mistakes.sumOf { it.marksLost.toDouble() }.toFloat()
                _uiState.value = _uiState.value.copy(
                    activeMistakes = mistakes,
                    totalMarksLost = lost,
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            syllabusRepository.getAllSubjects().collect { subjects ->
                _uiState.value = _uiState.value.copy(subjects = subjects)
            }
        }
    }

    fun recordMistake(
        subjectId: String,
        question: String,
        studentAnswer: String,
        correctAnswer: String,
        category: String,
        marksLost: Float
    ) {
        if (question.isBlank() || correctAnswer.isBlank()) return
        viewModelScope.launch {
            mistakeRepository.recordMistake(
                subjectId = subjectId,
                chapterId = "general_chapter",
                topicId = null,
                question = question.trim(),
                studentAnswer = studentAnswer.trim(),
                correctAnswer = correctAnswer.trim(),
                category = category,
                marksLost = marksLost
            )
        }
    }

    fun convertToRecallCard(mistake: MistakeEntity) {
        viewModelScope.launch {
            recallRepository.createCardFromMistake(mistake)
            _uiState.value = _uiState.value.copy(
                feedbackMessage = "Card added to Active Recall schedule."
            )
        }
    }

    fun resolveMistake(mistake: MistakeEntity) {
        viewModelScope.launch {
            mistakeRepository.resolveMistake(mistake)
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}
