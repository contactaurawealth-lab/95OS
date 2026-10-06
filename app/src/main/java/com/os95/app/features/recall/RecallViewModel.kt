package com.os95.app.features.recall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.MistakeEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.RecallReviewEntity
import com.os95.app.domain.model.RecallRating
import com.os95.app.domain.repository.RecallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RecallPreset(val count: Int, val label: String) {
    BLITZ(5, "5 Blitz"),
    FOCUSED(10, "10 Focused"),
    DEEP(20, "20 Deep"),
    ALL_DUE(0, "All Due")
}

data class RecallUiState(
    val dueCards: List<RecallCardEntity> = emptyList(),
    val allCards: List<RecallCardEntity> = emptyList(),
    val sessionQueue: List<RecallCardEntity> = emptyList(),
    val recentReviews: List<RecallReviewEntity> = emptyList(),
    val activeSessionCard: RecallCardEntity? = null,
    val selectedPreset: RecallPreset = RecallPreset.ALL_DUE,
    val isAnswerRevealed: Boolean = false,
    val completedInSession: Int = 0,
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
                val preset = _uiState.value.selectedPreset
                val queue = applyPreset(due, preset)
                _uiState.value = _uiState.value.copy(
                    dueCards = due,
                    sessionQueue = queue,
                    activeSessionCard = queue.firstOrNull(),
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

    fun selectPreset(preset: RecallPreset) {
        val queue = applyPreset(_uiState.value.dueCards, preset)
        _uiState.value = _uiState.value.copy(
            selectedPreset = preset,
            sessionQueue = queue,
            activeSessionCard = queue.firstOrNull(),
            isAnswerRevealed = false,
            completedInSession = 0
        )
    }

    private fun applyPreset(cards: List<RecallCardEntity>, preset: RecallPreset): List<RecallCardEntity> {
        return if (preset.count == 0) cards else cards.take(preset.count)
    }

    fun revealAnswer() {
        _uiState.value = _uiState.value.copy(isAnswerRevealed = true)
    }

    fun submitRating(rating: RecallRating) {
        val currentCard = _uiState.value.activeSessionCard ?: return
        viewModelScope.launch {
            repository.submitReview(currentCard, rating)
            val updatedQueue = _uiState.value.sessionQueue.filter { it.id != currentCard.id }
            _uiState.value = _uiState.value.copy(
                sessionQueue = updatedQueue,
                activeSessionCard = updatedQueue.firstOrNull(),
                isAnswerRevealed = false,
                completedInSession = _uiState.value.completedInSession + 1
            )
        }
    }

    fun createCardFromMistake(mistake: MistakeEntity) {
        viewModelScope.launch {
            repository.createCardFromMistake(mistake)
        }
    }

    fun createCard(subjectId: String, chapterId: String, prompt: String, answer: String) {
        if (prompt.isBlank() || answer.isBlank()) return
        viewModelScope.launch {
            repository.createCard(subjectId, chapterId, null, prompt.trim(), answer.trim())
        }
    }
}
