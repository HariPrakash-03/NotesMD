package com.notesmd.feature.notes.storage

import com.notesmd.core.model.DeviceFile

sealed interface StorageBrowserUiState {
    data object Loading : StorageBrowserUiState
    data object NoDirectorySelected : StorageBrowserUiState
    data class Content(
        val currentDirectoryUri: String,
        val breadcrumbs: List<Breadcrumb>,
        val files: List<DeviceFile>
    ) : StorageBrowserUiState
    data class Error(val message: String) : StorageBrowserUiState
}

data class Breadcrumb(val name: String, val uri: String)
