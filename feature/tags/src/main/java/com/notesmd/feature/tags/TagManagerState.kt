package com.notesmd.feature.tags

import com.notesmd.core.model.Tag

data class TagManagerUiState(
    val isLoading: Boolean = true,
    val tagsWithCounts: List<Pair<Tag, Int>> = emptyList(),
    val error: String? = null
)

sealed interface TagManagerEffect {
    data class ShowSnackbar(val message: String) : TagManagerEffect
}
