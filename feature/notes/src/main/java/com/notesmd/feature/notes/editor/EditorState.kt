package com.notesmd.feature.notes.editor

import com.notesmd.core.model.Tag

sealed interface EditorUiState {
    data object Loading : EditorUiState

    data class Active(
        val noteId: Long,
        val title: String,
        val rawContent: String,
        val activeTags: List<Tag>,
        val isPreviewMode: Boolean = false,
        val isDirty: Boolean = false,
        val viewerTemplate: com.notesmd.core.model.ViewerTemplate? = null
    ) : EditorUiState

    data class Error(val message: String) : EditorUiState
}

sealed interface EditorEffect {
    data object NavigateBack : EditorEffect
    data class ShowSnackbar(val message: String) : EditorEffect
}
