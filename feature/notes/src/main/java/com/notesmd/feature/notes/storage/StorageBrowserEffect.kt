package com.notesmd.feature.notes.storage

sealed interface StorageBrowserEffect {
    data class ShowSnackbar(val message: String) : StorageBrowserEffect
    data class NavigateToViewer(val uri: String) : StorageBrowserEffect
    data class PromptCopyOnWrite(val uri: String) : StorageBrowserEffect
    data object LaunchDirectoryPicker : StorageBrowserEffect
    data class NavigateToEditor(val noteId: Long) : StorageBrowserEffect
}
