package com.notesmd.core.domain.model

sealed class ExportProgress {
    data class InProgress(val current: Int, val total: Int) : ExportProgress()
    data class Completed(val folderUri: String) : ExportProgress()
    data class Failed(val error: String) : ExportProgress()
}
