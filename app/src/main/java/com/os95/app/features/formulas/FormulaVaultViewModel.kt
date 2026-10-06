package com.os95.app.features.formulas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.database.entity.ChapterEntity
import com.os95.app.core.database.entity.FormulaEntity
import com.os95.app.core.database.entity.SubjectEntity
import com.os95.app.domain.repository.FormulaRepository
import com.os95.app.domain.repository.SyllabusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FormulaVaultUiState(
    val formulas: List<FormulaEntity> = emptyList(),
    val filteredFormulas: List<FormulaEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val onlyBookmarked: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class FormulaVaultViewModel(
    private val formulaRepository: FormulaRepository,
    private val syllabusRepository: SyllabusRepository
) : ViewModel() {

    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    private val _onlyBookmarked = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<FormulaVaultUiState> = combine(
        formulaRepository.getAllFormulas(),
        syllabusRepository.getAllSubjects(),
        _selectedSubjectId,
        _onlyBookmarked,
        _searchQuery
    ) { formulas: List<FormulaEntity>, subjects: List<SubjectEntity>, selectedSubject: String?, onlyBookmarked: Boolean, query: String ->
        val filtered = formulas.filter { formula ->
            val matchesSubject = selectedSubject == null || formula.subjectId == selectedSubject
            val matchesBookmark = !onlyBookmarked || formula.isBookmarked
            val matchesQuery = query.isBlank() ||
                formula.title.contains(query, ignoreCase = true) ||
                formula.expression.contains(query, ignoreCase = true) ||
                formula.explanation.contains(query, ignoreCase = true)
            matchesSubject && matchesBookmark && matchesQuery
        }

        FormulaVaultUiState(
            formulas = formulas,
            filteredFormulas = filtered,
            subjects = subjects,
            selectedSubjectId = selectedSubject,
            onlyBookmarked = onlyBookmarked,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FormulaVaultUiState()
    )

    fun getChaptersForSubject(subjectId: String): Flow<List<ChapterEntity>> {
        return syllabusRepository.getChaptersForSubject(subjectId)
    }

    fun selectSubject(subjectId: String?) {
        _selectedSubjectId.value = subjectId
    }

    fun toggleFilterBookmarked() {
        _onlyBookmarked.value = !_onlyBookmarked.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(formulaId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            formulaRepository.toggleBookmark(formulaId, !currentStatus)
        }
    }

    fun addFormula(
        subjectId: String,
        chapterId: String,
        title: String,
        expression: String,
        explanation: String,
        examRelevance: String
    ) {
        viewModelScope.launch {
            formulaRepository.addFormula(
                subjectId = subjectId,
                chapterId = chapterId,
                title = title.trim(),
                expression = expression.trim(),
                explanation = explanation.trim(),
                examRelevance = examRelevance
            )
        }
    }

    fun deleteFormula(formula: FormulaEntity) {
        viewModelScope.launch {
            formulaRepository.deleteFormula(formula)
        }
    }
}
