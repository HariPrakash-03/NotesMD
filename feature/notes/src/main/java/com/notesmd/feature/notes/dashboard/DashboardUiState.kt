package com.notesmd.feature.notes.dashboard

import com.notesmd.core.model.Note
import com.notesmd.core.model.Tag

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Content(
        val notes: List<Note>,
        val availableTags: List<Tag>,
        val selectedTagFilter: Tag? = null,
        val sortOrder: SortOrder = SortOrder.DATE_MODIFIED,
        val searchQuery: String = "",
        val isSelectionMode: Boolean = false,
        val selectedNoteIds: Set<Long> = emptySet()
    ) : DashboardUiState
}

enum class SortOrder {
    DATE_MODIFIED, DATE_CREATED, TITLE, MANUAL
}
