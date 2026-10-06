package com.os95.app.features.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.audio.AcousticMode
import com.os95.app.core.audio.OfflineAcousticEngine
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FocusUiState(
    val remainingSeconds: Int = 25 * 60,
    val initialSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val acousticMode: AcousticMode = AcousticMode.OFF,
    val acousticVolume: Float = 0.5f
)

class FocusViewModel(
    private val studentRepository: StudentRepository,
    private val syllabusRepository: SyllabusRepository,
    private val acousticEngine: OfflineAcousticEngine = OfflineAcousticEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            syllabusRepository.getAllSubjects().collect { subjects ->
                _uiState.value = _uiState.value.copy(
                    subjects = subjects,
                    selectedSubjectId = subjects.firstOrNull()?.id
                )
            }
        }
    }

    fun setDuration(minutes: Int) {
        pause()
        _uiState.value = _uiState.value.copy(
            remainingSeconds = minutes * 60,
            initialSeconds = minutes * 60
        )
    }

    fun setAcousticMode(mode: AcousticMode) {
        _uiState.value = _uiState.value.copy(acousticMode = mode)
        acousticEngine.setMode(mode)
        if (_uiState.value.isRunning && mode != AcousticMode.OFF) {
            acousticEngine.start()
        }
    }

    fun setAcousticVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(acousticVolume = volume)
        acousticEngine.setVolume(volume)
    }

    fun start() {
        if (_uiState.value.isRunning) return
        _uiState.value = _uiState.value.copy(isRunning = true)
        if (_uiState.value.acousticMode != AcousticMode.OFF) {
            acousticEngine.start()
        }
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                val newRemaining = _uiState.value.remainingSeconds - 1
                _uiState.value = _uiState.value.copy(remainingSeconds = newRemaining)
            }
            finishSession()
        }
    }

    fun pause() {
        timerJob?.cancel()
        acousticEngine.stop()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun reset() {
        pause()
        _uiState.value = _uiState.value.copy(remainingSeconds = _uiState.value.initialSeconds)
    }

    private fun finishSession() {
        pause()
        val elapsedMinutes = _uiState.value.initialSeconds / 60
        val subjectId = _uiState.value.selectedSubjectId ?: "general_subject"
        viewModelScope.launch {
            studentRepository.logSession(subjectId, null, elapsedMinutes)
        }
    }

    override fun onCleared() {
        super.onCleared()
        acousticEngine.stop()
    }
}

