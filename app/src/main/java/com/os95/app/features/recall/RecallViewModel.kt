package com.os95.app.features.recall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.RecallReviewEntity
import com.os95.app.domain.model.RecallRating
import com.os95.app.domain.repository.RecallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecallUiState(
    val dueCards: List<RecallCardEntity> = emptyList(),
    val allCards: List<RecallCardEntity> = emptyList(),
    val recentReviews: List<RecallReviewEntity> = emptyList(),
    val activeSessionCard: RecallCardEntity? = null,
    val isAnswerRevealed: Boolean = false,
    val isLoading: Boolean = true
)

class RecallViewModel(
    private val repository: RecallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecallUiState())
    val uiState: StateFlow<RecallUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getDueCards().collect { due ->
                _uiState.value = _uiState.value.copy(
                    dueCards = due,
                    activeSessionCard = due.firstOrNull(),
                    isLoading = false
                )
            }
        }
        viewModelScope.launch {
            repository.getAllCards().collect { all ->
                _uiState.value = _uiState.value.copy(allCards = all)
            }
        }
        viewModelScope.launch {
            repository.getRecentReviews().collect { reviews ->
                _uiState.value = _uiState.value.copy(recentReviews = reviews)
            }
        }
    }

    fun revealAnswer() {
        _uiState.value = _uiState.value.copy(isAnswerRevealed = true)
    }

    fun submitRating(rating: RecallRating) {
        val currentCard = _uiState.value.activeSessionCard ?: return
        viewModelScope.launch {
            repository.submitReview(currentCard, rating)
            val remaining = _uiState.value.dueCards.filter { it.id != currentCard.id }
            _uiState.value = _uiState.value.copy(
                activeSessionCard = remaining.firstOrNull(),
                isAnswerRevealed = false
            )
        }
    }

    fun createCard(subjectId: String, chapterId: String, prompt: String, answer: String) {
        if (prompt.isBlank() || answer.isBlank()) return
        viewModelScope.launch {
            repository.createCard(subjectId, chapterId, null, prompt.trim(), answer.trim())
        }
    }
}
