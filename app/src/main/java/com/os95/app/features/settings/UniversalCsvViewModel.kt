package com.os95.app.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.os95.app.core.csv.CsvDatasetType
import com.os95.app.core.csv.CsvExporter
import com.os95.app.core.csv.CsvImportSummary
import com.os95.app.core.csv.CsvPreviewResult
import com.os95.app.core.csv.CsvValidationError
import com.os95.app.core.csv.DuplicateResolutionStrategy
import com.os95.app.core.csv.MissingRelationshipMode
import com.os95.app.core.csv.UniversalCsvEngine
import com.os95.app.core.csv.UniversalMarkdownEngine
import com.os95.app.core.database.OS95Database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DataPortabilityFormat(val displayName: String, val extension: String) {
    CSV("CSV (.csv)", ".csv"),
    MARKDOWN("Markdown (.md)", ".md")
}

data class UniversalCsvUiState(
    val selectedFormat: DataPortabilityFormat = DataPortabilityFormat.CSV,
    val selectedDatasetType: CsvDatasetType = CsvDatasetType.QUESTIONS,
    val csvContent: String = "",
    val duplicateStrategy: DuplicateResolutionStrategy = DuplicateResolutionStrategy.SKIP,
    val missingRelMode: MissingRelationshipMode = MissingRelationshipMode.CREATE_MISSING,
    val previewResult: CsvPreviewResult? = null,
    val importSummary: CsvImportSummary? = null,
    val exportedContent: String? = null,
    val isAnalyzing: Boolean = false,
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
    val statusMessage: String? = null
)

class UniversalCsvViewModel(
    private val csvEngine: UniversalCsvEngine,
    private val database: OS95Database,
    private val markdownEngine: UniversalMarkdownEngine = UniversalMarkdownEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversalCsvUiState())
    val uiState: StateFlow<UniversalCsvUiState> = _uiState.asStateFlow()

    init {
        loadTemplate(CsvDatasetType.QUESTIONS, DataPortabilityFormat.CSV)
    }

    fun selectFormat(format: DataPortabilityFormat) {
        _uiState.value = _uiState.value.copy(
            selectedFormat = format,
            previewResult = null,
            importSummary = null,
            exportedContent = null
        )
        loadTemplate(_uiState.value.selectedDatasetType, format)
    }

    fun selectDataset(datasetType: CsvDatasetType) {
        _uiState.value = _uiState.value.copy(
            selectedDatasetType = datasetType,
            previewResult = null,
            importSummary = null,
            exportedContent = null
        )
        loadTemplate(datasetType, _uiState.value.selectedFormat)
    }

    fun setContent(content: String) {
        _uiState.value = _uiState.value.copy(csvContent = content)
    }

    fun setDuplicateStrategy(strategy: DuplicateResolutionStrategy) {
        _uiState.value = _uiState.value.copy(duplicateStrategy = strategy)
    }

    fun setMissingRelMode(mode: MissingRelationshipMode) {
        _uiState.value = _uiState.value.copy(missingRelMode = mode)
    }

    fun loadTemplate(
        datasetType: CsvDatasetType = _uiState.value.selectedDatasetType,
        format: DataPortabilityFormat = _uiState.value.selectedFormat
    ) {
        val templateText = if (format == DataPortabilityFormat.MARKDOWN) {
            markdownEngine.getMarkdownTemplate(datasetType)
        } else {
            CsvExporter().getTemplate(datasetType)
        }
        _uiState.value = _uiState.value.copy(
            csvContent = templateText,
            previewResult = null,
            statusMessage = "Loaded ${format.displayName} template for ${datasetType.displayName}"
        )
    }

    fun runPreview() {
        val state = _uiState.value
        if (state.csvContent.isBlank()) {
            _uiState.value = state.copy(statusMessage = "Please paste or input content first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, statusMessage = null)

            val effectiveCsvContent = if (state.selectedFormat == DataPortabilityFormat.MARKDOWN) {
                val parsedRows = markdownEngine.parseMarkdown(state.selectedDatasetType, state.csvContent)
                if (parsedRows.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        previewResult = CsvPreviewResult(
                            datasetType = state.selectedDatasetType,
                            totalRowsDetected = 0,
                            validRows = emptyList(),
                            duplicateCount = 0,
                            errorCount = 1,
                            errors = listOf(CsvValidationError(1, null, "", "No valid Markdown items detected. Use headers (#, ##, -) or tables (|)"))
                        ),
                        statusMessage = "Could not parse Markdown structure"
                    )
                    return@launch
                }
                convertRowsToCsv(state.selectedDatasetType, parsedRows)
            } else {
                state.csvContent
            }

            val preview = csvEngine.preview(
                datasetType = state.selectedDatasetType,
                content = effectiveCsvContent,
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
                    "Validated: ${preview.validRows.size} record(s) ready for import"
                }
            )
        }
    }

    private fun convertRowsToCsv(datasetType: CsvDatasetType, rows: List<Map<String, String>>): String {
        if (rows.isEmpty()) return ""
        val templateHeader = CsvExporter().getTemplate(datasetType).lines().first()
        val headers = templateHeader.split(",").map { it.trim() }
        val sb = StringBuilder()
        sb.appendLine(templateHeader)
        for (row in rows) {
            val line = headers.joinToString(",") { h ->
                val raw = row[h.lowercase()] ?: row[h] ?: ""
                if (raw.contains(",") || raw.contains("\"") || raw.contains("\n")) {
                    "\"" + raw.replace("\"", "\"\"") + "\""
                } else {
                    raw
                }
            }
            sb.appendLine(line)
        }
        return sb.toString()
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
                statusMessage = "Import Complete: ${summary.totalImported} records saved into 95OS"
            )
        }
    }

    fun exportDataset(datasetType: CsvDatasetType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val format = _uiState.value.selectedFormat
            val exported = if (format == DataPortabilityFormat.MARKDOWN) {
                markdownEngine.exportToMarkdown(datasetType, database)
            } else {
                csvEngine.export(datasetType, database)
            }
            _uiState.value = _uiState.value.copy(
                exportedContent = exported,
                isExporting = false,
                statusMessage = "Exported ${datasetType.displayName} (${format.displayName})"
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
            exportedContent = null,
            statusMessage = null
        )
        loadTemplate(_uiState.value.selectedDatasetType, _uiState.value.selectedFormat)
    }
}
