package com.os95.app.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.domain.engine.Target95Metrics
import com.os95.app.domain.model.MarksRecoverySnapshot
import com.os95.app.domain.repository.MarksRecoveryRepository
import com.os95.app.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ProgressTab(val label: String) {
    MARKS_GAP("Marks Gap"),
    FORGETTING_RADAR("Forgetting Radar"),
    PAPER_ANALYSIS("Paper Analysis"),
    RECOVERY_SCORE("Recovery Score")
}

data class ProgressUiState(
    val selectedTab: ProgressTab = ProgressTab.MARKS_GAP,
    val snapshot: MarksRecoverySnapshot? = null,
    val metrics: Target95Metrics = Target95Metrics(0f, 95f, 95f, 0f, 0f, 0f, 0),
    val targetPercentage: Float = 95.0f,
    val isLoading: Boolean = true,
    val feedbackMessage: String? = null
)

class ProgressViewModel(
    private val marksRecoveryRepository: MarksRecoveryRepository,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            marksRecoveryRepository.getRecoverySnapshotFlow().collectLatest { snapshot ->
                val target = snapshot.marksGap.targetPercentage
                val metrics = Target95Metrics(
                    currentScorePercentage = snapshot.marksGap.currentPercentage,
                    targetScorePercentage = target,
                    marksGapPercentage = snapshot.marksGap.percentageGap,
                    totalLostMarks = snapshot.marksGap.potentialRecoverableMarks,
                    recoveredMarks = snapshot.recoveryScore.totalRecoveredMarks,
                    syllabusCompletionPercentage = 0f,
                    totalStudyMinutes = 0
                )
                _uiState.value = _uiState.value.copy(
                    snapshot = snapshot,
                    metrics = metrics,
                    targetPercentage = target,
                    isLoading = false
                )
            }
        }
    }

    fun selectTab(tab: ProgressTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun completeRescueSession(
        durationMinutes: Int,
        actionsCompleted: Int,
        topicsCovered: Int,
        cardsReviewed: Int,
        mistakesResolved: Int
    ) {
        viewModelScope.launch {
            marksRecoveryRepository.completeRescueSession(
                durationMinutes = durationMinutes,
                actionsCompleted = actionsCompleted,
                topicsCovered = topicsCovered,
                cardsReviewed = cardsReviewed,
                mistakesResolved = mistakesResolved
            )
            _uiState.value = _uiState.value.copy(
                feedbackMessage = "Completed $durationMinutes-min Rescue Session! History updated."
            )
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}
