package com.notesmd.feature.template

import com.notesmd.core.model.ViewerTemplate

data class TemplateStudioUiState(
    val isLoading: Boolean = true,
    val templateId: Long? = null,
    val savedTemplate: ViewerTemplate? = null,
    val currentTemplate: ViewerTemplate? = null
)

sealed interface TemplateStudioEffect {
    data object NavigateBack : TemplateStudioEffect
    data class ShowSnackbar(val message: String) : TemplateStudioEffect
}
