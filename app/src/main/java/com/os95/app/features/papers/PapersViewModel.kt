package com.os95.app.features.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.ExamResultEntity
import com.os95.app.core.database.entity.PaperEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.repository.PaperRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PapersUiState(
    val papers: List<PaperEntity> = emptyList(),
    val results: List<ExamResultEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val isLoading: Boolean = true
)

class PapersViewModel(
    private val paperRepository: PaperRepository,
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PapersUiState())
    val uiState: StateFlow<PapersUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            paperRepository.getAllPapers().collect { papers ->
                _uiState.value = _uiState.value.copy(papers = papers, isLoading = false)
            }
        }
        viewModelScope.launch {
            paperRepository.getAllResults().collect { results ->
                _uiState.value = _uiState.value.copy(results = results)
            }
        }
        viewModelScope.launch {
            syllabusRepository.getAllSubjects().collect { subjects ->
                _uiState.value = _uiState.value.copy(subjects = subjects)
            }
        }
    }

    fun createPaper(subjectId: String, title: String, totalMarks: Float, durationMinutes: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            paperRepository.createPaper(subjectId, title.trim(), totalMarks, durationMinutes)
        }
    }
}
