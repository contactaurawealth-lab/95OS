package com.os95.app.features.syllabus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.core.database.entity.TopicEntity
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SyllabusUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val currentSubject: SubjectEntity? = null,
    val currentChapters: List<ChapterEntity> = emptyList(),
    val currentChapter: ChapterEntity? = null,
    val currentTopics: List<TopicEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SyllabusViewModel(
    private val repository: SyllabusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyllabusUiState(isLoading = true))
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            repository.getAllSubjects().collect { list ->
                _uiState.value = _uiState.value.copy(
                    subjects = list,
                    isLoading = false
                )
            }
        }
    }

    fun addSubject(name: String, colorHex: String = "#B8781E") {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                repository.createSubject(name.trim(), colorHex)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Could not save subject. Name may already exist.")
            }
        }
    }

    fun selectSubject(subjectId: String) {
        viewModelScope.launch {
            val subject = repository.getSubjectById(subjectId)
            _uiState.value = _uiState.value.copy(currentSubject = subject)
            repository.getChaptersForSubject(subjectId).collect { chapters ->
                _uiState.value = _uiState.value.copy(currentChapters = chapters)
            }
        }
    }

    fun addChapter(subjectId: String, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createChapter(subjectId, name.trim())
        }
    }

    fun selectChapter(chapterId: String) {
        viewModelScope.launch {
            repository.getTopicsForChapter(chapterId).collect { topics ->
                _uiState.value = _uiState.value.copy(currentTopics = topics)
            }
        }
    }

    fun addTopic(chapterId: String, name: String, relevance: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createTopic(chapterId, name.trim(), relevance)
        }
    }

    fun updateTopicMastery(topic: TopicEntity, newState: String) {
        viewModelScope.launch {
            repository.updateTopic(topic.copy(masteryState = newState, lastRevisedAt = System.currentTimeMillis()))
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
