package com.notesmd.feature.notes.viewer

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.provider.DocumentStorageProvider
import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.domain.usecase.ForkExternalFileUseCase
import com.notesmd.core.markdown.parser.MarkdownParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val forkExternalFileUseCase: ForkExternalFileUseCase,
    private val documentStorageProvider: DocumentStorageProvider,
    private val templateRepository: com.notesmd.core.domain.repository.TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<ViewerEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L
    private val rawUri: String? = savedStateHandle.get<String>("deviceUri")
    private val fileUri: String? = rawUri?.let { Uri.decode(it) }

    init {
        loadContent()
        
        templateRepository.observeDefaultTemplate()
            .onEach { defaultTemplate ->
                val state = _uiState.value as? ViewerUiState.Content ?: return@onEach
                _uiState.value = state.copy(viewerTemplate = defaultTemplate)
            }
            .launchIn(viewModelScope)
    }

    private fun loadContent() {
        viewModelScope.launch {
            if (noteId != -1L) {
                noteRepository.getNoteById(noteId)?.let { note ->
                    val rootNode = MarkdownParser.parse(note.content)
                    val headings = extractHeadings(rootNode)
                    _uiState.value = ViewerUiState.Content(
                        title = note.title.ifBlank { "Untitled Note" },
                        rootNode = rootNode,
                        isDeviceFile = false,
                        headings = headings,
                        baseUri = null
                    )
                } ?: run {
                    _effects.send(ViewerEffect.ShowSnackbar("Note not found"))
                    _effects.send(ViewerEffect.NavigateBack)
                }
            } else if (!fileUri.isNullOrEmpty()) {
                documentStorageProvider.readExternalFile(fileUri).onSuccess { content ->
                    val rootNode = MarkdownParser.parse(content)
                    val headings = extractHeadings(rootNode)
                    val title = Uri.parse(fileUri).lastPathSegment ?: "External File"
                    _uiState.value = ViewerUiState.Content(
                        title = title,
                        rootNode = rootNode,
                        isDeviceFile = true,
                        headings = headings,
                        baseUri = fileUri
                    )
                }.onFailure {
                    _effects.send(ViewerEffect.ShowSnackbar(it.message ?: "Couldn't open file"))
                    _effects.send(ViewerEffect.NavigateBack)
                }
            }
        }
    }

    fun onEditClicked() {
        val state = _uiState.value as? ViewerUiState.Content ?: return
        viewModelScope.launch {
            if (state.isDeviceFile) {
                // Device file edit handled via Copy-on-Write confirmation sheet flow, we'll expose a callback to UI
            } else {
                _effects.send(ViewerEffect.NavigateToEditor(noteId))
            }
        }
    }
    
    fun onConfirmFork() {
        viewModelScope.launch {
            if (fileUri != null) {
                forkExternalFileUseCase(fileUri)
                    .onSuccess { newNoteId ->
                        _effects.send(ViewerEffect.ShowSnackbar("Saved to Local Notes"))
                        _effects.send(ViewerEffect.NavigateToEditor(newNoteId))
                    }
                    .onFailure {
                        _effects.send(ViewerEffect.ShowSnackbar(it.message ?: "Failed to fork file"))
                    }
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _effects.send(ViewerEffect.NavigateBack)
        }
    }
}
