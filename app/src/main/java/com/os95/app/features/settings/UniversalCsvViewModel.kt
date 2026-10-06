package com.os95.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.csv.CsvDatasetType
import com.os95.app.core.csv.CsvImportSummary
import com.os95.app.core.csv.CsvPreviewResult
import com.os95.app.core.csv.DuplicateResolutionStrategy
import com.os95.app.core.csv.MissingRelationshipMode
import com.os95.app.core.csv.UniversalCsvEngine
import com.os95.app.core.database.OS95Database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UniversalCsvUiState(
    val selectedDatasetType: CsvDatasetType = CsvDatasetType.QUESTIONS,
    val csvContent: String = "",
    val duplicateStrategy: DuplicateResolutionStrategy = DuplicateResolutionStrategy.SKIP,
    val missingRelMode: MissingRelationshipMode = MissingRelationshipMode.CREATE_MISSING,
    val previewResult: CsvPreviewResult? = null,
    val importSummary: CsvImportSummary? = null,
    val exportedCsv: String? = null,
    val isAnalyzing: Boolean = false,
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

class UniversalCsvViewModel(
    private val csvEngine: UniversalCsvEngine,
    private val database: OS95Database
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversalCsvUiState())
    val uiState: StateFlow<UniversalCsvUiState> = _uiState.asStateFlow()

    init {
        loadTemplate(CsvDatasetType.QUESTIONS)
    }

    fun selectDataset(datasetType: CsvDatasetType) {
        _uiState.value = _uiState.value.copy(
            selectedDatasetType = datasetType,
            previewResult = null,
            importSummary = null,
            exportedCsv = null
        )
        loadTemplate(datasetType)
    }

    fun setCsvContent(content: String) {
        _uiState.value = _uiState.value.copy(csvContent = content)
    }

    fun setDuplicateStrategy(strategy: DuplicateResolutionStrategy) {
        _uiState.value = _uiState.value.copy(duplicateStrategy = strategy)
    }

    fun setMissingRelMode(mode: MissingRelationshipMode) {
        _uiState.value = _uiState.value.copy(missingRelMode = mode)
    }

    fun loadTemplate(datasetType: CsvDatasetType = _uiState.value.selectedDatasetType) {
        val templateText = com.os95.app.core.csv.CsvExporter().getTemplate(datasetType)
        _uiState.value = _uiState.value.copy(
            csvContent = templateText,
            previewResult = null,
            statusMessage = "Loaded template for ${datasetType.displayName}"
        )
    }

    fun runPreview() {
        val state = _uiState.value
        if (state.csvContent.isBlank()) {
            _uiState.value = state.copy(statusMessage = "Please paste or input CSV content first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, statusMessage = null)
            val preview = csvEngine.preview(
                datasetType = state.selectedDatasetType,
                content = state.csvContent,
                database = database,
                duplicateStrategy = state.duplicateStrategy,
                missingRelMode = state.missingRelMode
            )
            _uiState.value = _uiState.value.copy(
                previewResult = preview,
                isAnalyzing = false,
                statusMessage = if (preview.errors.isNotEmpty()) {
                    "Found ${preview.errors.size} validation error(s)"
                } else {
                    "Validated: ${preview.validRows.size} row(s) ready for import"
                }
            )
        }
    }

    fun confirmImport() {
        val preview = _uiState.value.previewResult ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            val summary = csvEngine.executeImport(
                preview = preview,
                database = database,
                duplicateStrategy = _uiState.value.duplicateStrategy
            )
            _uiState.value = _uiState.value.copy(
                importSummary = summary,
                isImporting = false,
                previewResult = null,
                statusMessage = "Import Complete: ${summary.totalImported} records saved"
            )
        }
    }

    fun exportDataset(datasetType: CsvDatasetType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val csv = csvEngine.export(datasetType, database)
            _uiState.value = _uiState.value.copy(
                exportedCsv = csv,
                isExporting = false,
                statusMessage = "Exported ${datasetType.displayName} CSV"
            )
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun resetImport() {
        _uiState.value = _uiState.value.copy(
            previewResult = null,
            importSummary = null,
            exportedCsv = null,
            statusMessage = null
        )
        loadTemplate(_uiState.value.selectedDatasetType)
    }
}
