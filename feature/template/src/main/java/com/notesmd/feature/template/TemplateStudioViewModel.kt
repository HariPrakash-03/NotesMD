package com.notesmd.feature.template

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.repository.TemplateRepository
import com.notesmd.core.model.ViewerTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateStudioViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val templateId: Long? = savedStateHandle.get<Long>("templateId")?.takeIf { it != -1L }

    private val _uiState = MutableStateFlow(TemplateStudioUiState(templateId = templateId))
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<TemplateStudioEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (templateId != null) {
                // We'd ideally have getTemplateById in the repository, 
                // but since observeTemplates gives all, let's filter from it.
                templateRepository.observeTemplates().firstOrNull()?.let { templates ->
                    val template = templates.find { it.id == templateId }
                    if (template != null) {
                        _uiState.update { 
                            it.copy(isLoading = false, savedTemplate = template, currentTemplate = template)
                        }
                    } else {
                        _effects.send(TemplateStudioEffect.ShowSnackbar("Template not found"))
                        _effects.send(TemplateStudioEffect.NavigateBack)
                    }
                }
            } else {
                // New template
                val newTemplate = ViewerTemplate.Default.copy(id = 0, name = "New Template")
                _uiState.update { 
                    it.copy(isLoading = false, savedTemplate = newTemplate, currentTemplate = newTemplate)
                }
            }
        }
    }

    fun updateCurrentTemplate(template: ViewerTemplate) {
        _uiState.update { it.copy(currentTemplate = template) }
    }

    fun resetTemplate() {
        val state = _uiState.value
        _uiState.update { it.copy(currentTemplate = state.savedTemplate) }
    }

    fun saveTemplate() {
        val current = _uiState.value.currentTemplate ?: return
        viewModelScope.launch {
            try {
                if (current.id == 0L) {
                    templateRepository.saveTemplate(current)
                } else {
                    templateRepository.updateTemplate(current)
                }
                _effects.send(TemplateStudioEffect.ShowSnackbar("Template saved"))
                _effects.send(TemplateStudioEffect.NavigateBack)
            } catch (e: Exception) {
                _effects.send(TemplateStudioEffect.ShowSnackbar("Failed to save template"))
            }
        }
    }
    
    fun navigateBack() {
        viewModelScope.launch {
            _effects.send(TemplateStudioEffect.NavigateBack)
        }
    }
}
