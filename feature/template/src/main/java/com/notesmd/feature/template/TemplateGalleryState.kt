package com.notesmd.feature.template

import com.notesmd.core.model.ViewerTemplate

data class TemplateGalleryUiState(
    val isLoading: Boolean = true,
    val templates: List<ViewerTemplate> = emptyList(),
    val error: String? = null
)

sealed interface TemplateGalleryEffect {
    data class NavigateToStudio(val templateId: Long?) : TemplateGalleryEffect
    data object NavigateBack : TemplateGalleryEffect
    data class ShowSnackbar(val message: String) : TemplateGalleryEffect
}
