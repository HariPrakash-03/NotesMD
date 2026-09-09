package com.notesmd.feature.notes.dashboard

sealed interface DashboardEffect {
    data class ShowSnackbar(val message: String) : DashboardEffect
    data class NavigateToEditor(val noteId: Long) : DashboardEffect
    data class NavigateToViewer(val noteId: Long) : DashboardEffect
    data object NavigateToSettings : DashboardEffect
}
