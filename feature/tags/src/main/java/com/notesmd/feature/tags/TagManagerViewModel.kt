package com.notesmd.feature.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.repository.TagRepository
import com.notesmd.core.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagManagerViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagerUiState())
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<TagManagerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        tagRepository.observeTagsWithCounts()
            .onEach { tags ->
                _uiState.update { it.copy(isLoading = false, tagsWithCounts = tags) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun addTag(name: String) {
        val trimmed = name.trim().lowercase()
        if (trimmed.isEmpty()) return
        
        viewModelScope.launch {
            try {
                tagRepository.createTag(trimmed, "#0969DA")
            } catch (e: Exception) {
                _effects.send(TagManagerEffect.ShowSnackbar("Failed to create tag"))
            }
        }
    }

    fun deleteTag(tagId: Long, noteCount: Int) {
        viewModelScope.launch {
            try {
                tagRepository.deleteTag(tagId)
                if (noteCount > 0) {
                    _effects.send(TagManagerEffect.ShowSnackbar("Tag removed from $noteCount notes"))
                } else {
                    _effects.send(TagManagerEffect.ShowSnackbar("Tag deleted"))
                }
            } catch (e: Exception) {
                _effects.send(TagManagerEffect.ShowSnackbar("Failed to delete tag"))
            }
        }
    }

    fun updateTagColor(tag: Tag, newColorHex: String) {
        viewModelScope.launch {
            try {
                tagRepository.updateTag(tag.id, tag.name, newColorHex)
            } catch (e: Exception) {
                _effects.send(TagManagerEffect.ShowSnackbar("Failed to update color"))
            }
        }
    }

    fun updateTagName(tag: Tag, newName: String) {
        val trimmed = newName.trim().lowercase()
        if (trimmed.isEmpty() || trimmed == tag.name) return
        
        viewModelScope.launch {
            try {
                tagRepository.updateTag(tag.id, trimmed, tag.colorHex)
            } catch (e: Exception) {
                _effects.send(TagManagerEffect.ShowSnackbar("Failed to rename tag"))
            }
        }
    }

    fun mergeTags(sourceTagId: Long, targetTagId: Long, targetName: String) {
        viewModelScope.launch {
            try {
                tagRepository.mergeTag(sourceTagId, targetTagId)
                _effects.send(TagManagerEffect.ShowSnackbar("Merged into #$targetName"))
            } catch (e: Exception) {
                _effects.send(TagManagerEffect.ShowSnackbar("Failed to merge tags"))
            }
        }
    }
}
