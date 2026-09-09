package com.notesmd.feature.notes.viewer

import org.commonmark.node.Node

sealed interface ViewerUiState {
    data object Loading : ViewerUiState

    data class Content(
        val title: String,
        val rootNode: Node,
        val isDeviceFile: Boolean,
        val headings: List<HeadingUiModel>,
        val baseUri: String? = null
    ) : ViewerUiState
}

sealed interface ViewerEffect {
    data class NavigateToEditor(val noteId: Long) : ViewerEffect
    data object NavigateBack : ViewerEffect
    data class ShowSnackbar(val message: String) : ViewerEffect
}
