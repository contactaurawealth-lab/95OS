package com.os95.app.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.domain.model.TimeToMarksReport
import com.os95.app.domain.repository.AdvancedExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimeToMarksUiState(
    val availableMinutes: Int = 60,
    val targetPercentage: Float = 95.0f,
    val report: TimeToMarksReport? = null,
    val isLoading: Boolean = true
)

class TimeToMarksViewModel(
    private val advancedExamRepository: AdvancedExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimeToMarksUiState())
    val uiState: StateFlow<TimeToMarksUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val report = advancedExamRepository.getTimeToMarksReport(
                    availableMinutes = _uiState.value.availableMinutes,
                    targetPercentage = _uiState.value.targetPercentage
                )
                _uiState.value = _uiState.value.copy(
                    report = report,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setAvailableMinutes(minutes: Int) {
        if (_uiState.value.availableMinutes == minutes) return
        _uiState.value = _uiState.value.copy(availableMinutes = minutes)
        loadReport()
    }

    fun setTargetPercentage(target: Float) {
        if (_uiState.value.targetPercentage == target) return
        _uiState.value = _uiState.value.copy(targetPercentage = target)
        loadReport()
    }

    fun setExamDate(timestamp: Long?) {
        viewModelScope.launch {
            advancedExamRepository.setTargetExamDate(timestamp)
            loadReport()
        }
    }
}
