package com.notesmd.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.model.ExportProgress
import com.notesmd.core.domain.repository.SettingsRepository
import com.notesmd.core.domain.usecase.BulkExportNotesUseCase
import com.notesmd.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isExporting: Boolean = false,
    val exportProgressCurrent: Int = 0,
    val exportProgressTotal: Int = 0
)

sealed class SettingsEffect {
    data class ShowSnackbar(
        val message: String, 
        val actionLabel: String? = null,
        val onActionClick: (() -> Unit)? = null
    ) : SettingsEffect()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val bulkExportNotesUseCase: BulkExportNotesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects: SharedFlow<SettingsEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.observeThemeMode().collect { themeMode ->
                _uiState.update { it.copy(themeMode = themeMode) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun startExportAllNotes() {
        // We will signal the UI to open SAF folder picker. 
        // This is handled by a separate event or directly in the UI if requested.
        // The instruction says "wire the 'Export All Notes' row... to SAF folder picker -> BulkExportNotesUseCase(null, ...)"
        // The UI will handle opening the picker and calling onExportDirSelected.
    }

    fun onExportDirSelected(uri: Uri?) {
        if (uri == null) return
        
        // Take persistable permission to ensure we can write to it even if activity stops
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportProgressCurrent = 0, exportProgressTotal = 0) }
            
            bulkExportNotesUseCase(null, uri.toString()).collect { progress ->
                when (progress) {
                    is ExportProgress.InProgress -> {
                        _uiState.update { 
                            it.copy(
                                exportProgressCurrent = progress.current, 
                                exportProgressTotal = progress.total
                            ) 
                        }
                    }
                    is ExportProgress.Completed -> {
                        _uiState.update { it.copy(isExporting = false) }
                        _effects.emit(
                            SettingsEffect.ShowSnackbar(
                                message = "Export complete",
                                actionLabel = "Open Folder",
                                onActionClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(progress.folderUri)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            )
                        )
                    }
                    is ExportProgress.Failed -> {
                        _uiState.update { it.copy(isExporting = false) }
                        _effects.emit(SettingsEffect.ShowSnackbar("Export failed: ${progress.error}"))
                    }
                }
            }
        }
    }
}
