package com.os95.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.RecallCardEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.engine.Target95Engine
import com.os95.app.domain.engine.Target95Metrics
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.RecallRepository
import com.os95.app.domain.repository.StudentRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val studentName: String = "Student",
    val targetPercentage: Float = 95.0f,
    val metrics: Target95Metrics = Target95Metrics(0f, 95f, 95f, 0f, 0f, 0f, 0),
    val subjects: List<SubjectEntity> = emptyList(),
    val dueRecallCards: List<RecallCardEntity> = emptyList(),
    val recentPapers: List<PaperEntity> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val studentRepository: StudentRepository,
    private val syllabusRepository: SyllabusRepository,
    private val recallRepository: RecallRepository,
    private val paperRepository: PaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val syllabusStatsFlow = combine(
                syllabusRepository.getAllSubjects(),
                syllabusRepository.getMasteredTopicsCount(),
                syllabusRepository.getTotalTopicsCount()
            ) { subjects, mastered, total ->
                Triple(subjects, mastered, total)
            }

            val activityFlow = combine(
                recallRepository.getDueCards(),
                paperRepository.getAllPapers(),
                paperRepository.getAllResults()
            ) { dueCards, papers, results ->
                Triple(dueCards, papers, results)
            }

            combine(
                studentRepository.getProfileFlow(),
                syllabusStatsFlow,
                activityFlow
            ) { profile, (subjects, masteredTopics, totalTopics), (dueCards, papers, results) ->
                val target = profile?.targetPercentage ?: 95.0f
                val name = profile?.name?.ifBlank { "Student" } ?: "Student"

                val scores = results.map { Pair(it.marksObtained, it.totalMarks) }
                val metrics = Target95Engine.calculateMetrics(
                    latestExamScores = scores,
                    totalLostMarks = 0f,
                    recoveredMarks = 0f,
                    masteredTopicsCount = masteredTopics,
                    totalTopicsCount = totalTopics,
                    totalStudyMinutes = 0,
                    targetScorePercentage = target
                )

                HomeUiState(
                    studentName = name,
                    targetPercentage = target,
                    metrics = metrics,
                    subjects = subjects,
                    dueRecallCards = dueCards,
                    recentPapers = papers.take(5),
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
