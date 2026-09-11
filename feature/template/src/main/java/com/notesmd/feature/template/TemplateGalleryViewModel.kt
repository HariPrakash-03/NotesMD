package com.notesmd.feature.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateGalleryViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateGalleryUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<TemplateGalleryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        templateRepository.observeTemplates()
            .onEach { templates ->
                _uiState.update { it.copy(isLoading = false, templates = templates) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun setAsDefault(templateId: Long) {
        viewModelScope.launch {
            try {
                templateRepository.setAsDefault(templateId)
                _effects.send(TemplateGalleryEffect.ShowSnackbar("Template applied"))
            } catch (e: Exception) {
                _effects.send(TemplateGalleryEffect.ShowSnackbar("Failed to set template"))
            }
        }
    }

    fun createNewTemplate() {
        viewModelScope.launch {
            _effects.send(TemplateGalleryEffect.NavigateToStudio(null))
        }
    }

    fun editTemplate(templateId: Long) {
        viewModelScope.launch {
            _effects.send(TemplateGalleryEffect.NavigateToStudio(templateId))
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _effects.send(TemplateGalleryEffect.NavigateBack)
        }
    }
}
