package com.os95.app.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.domain.engine.Target95Engine
import com.os95.app.domain.engine.Target95Metrics
import com.os95.app.domain.repository.MistakeRepository
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProgressUiState(
    val metrics: Target95Metrics = Target95Metrics(0f, 95f, 95f, 0f, 0f, 0f, 0),
    val examResults: List<ExamResultEntity> = emptyList(),
    val isLoading: Boolean = true
)

class ProgressViewModel(
    private val studentRepository: StudentRepository,
    private val syllabusRepository: SyllabusRepository,
    private val paperRepository: PaperRepository,
    private val mistakeRepository: MistakeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                studentRepository.getProfileFlow(),
                syllabusRepository.getMasteredTopicsCount(),
                syllabusRepository.getTotalTopicsCount(),
                paperRepository.getAllResults(),
                mistakeRepository.getAllMistakes()
            ) { profile, masteredTopics, totalTopics, results, mistakes ->
                val target = profile?.targetPercentage ?: 95.0f
                val scores = results.map { Pair(it.marksObtained, it.totalMarks) }
                val lostMarks = mistakes.filter { !it.isResolved }.sumOf { it.marksLost.toDouble() }.toFloat()
                val recoveredMarks = mistakes.filter { it.isResolved }.sumOf { it.marksLost.toDouble() }.toFloat()

                val metrics = Target95Engine.calculateMetrics(
                    latestExamScores = scores,
                    totalLostMarks = lostMarks,
                    recoveredMarks = recoveredMarks,
                    masteredTopicsCount = masteredTopics,
                    totalTopicsCount = totalTopics,
                    totalStudyMinutes = 0,
                    targetScorePercentage = target
                )

                ProgressUiState(
                    metrics = metrics,
                    examResults = results,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
