package com.os95.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.domain.model.Last7DaysDashboard
import com.os95.app.domain.repository.AdvancedExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Last7DaysUiState(
    val dashboard: Last7DaysDashboard? = null,
    val selectedDay: Int = 7,
    val isLoading: Boolean = true
)

class Last7DaysViewModel(
    private val advancedExamRepository: AdvancedExamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(Last7DaysUiState())
    val uiState: StateFlow<Last7DaysUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dash = advancedExamRepository.getLast7DaysDashboard()
                _uiState.value = _uiState.value.copy(
                    dashboard = dash,
                    selectedDay = dash.currentDayNumber,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectDay(dayNumber: Int) {
        _uiState.value = _uiState.value.copy(selectedDay = dayNumber)
    }

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            advancedExamRepository.toggleLast7DaysTask(taskId)
            // Reload dashboard to update completion metrics
            val dash = advancedExamRepository.getLast7DaysDashboard()
            _uiState.value = _uiState.value.copy(dashboard = dash)
        }
    }

    fun setTargetExamDate(timestamp: Long?) {
        viewModelScope.launch {
            advancedExamRepository.setTargetExamDate(timestamp)
            loadDashboard()
        }
    }
}
