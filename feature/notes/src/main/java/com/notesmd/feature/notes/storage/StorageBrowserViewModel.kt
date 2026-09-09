package com.notesmd.feature.notes.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.provider.DocumentStorageProvider
import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.domain.repository.SettingsRepository
import com.notesmd.core.domain.usecase.ForkExternalFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageBrowserViewModel @Inject constructor(
    private val storageProvider: DocumentStorageProvider,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val forkExternalFileUseCase: ForkExternalFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StorageBrowserUiState>(StorageBrowserUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<StorageBrowserEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentUri: String? = null
    private val breadcrumbs = mutableListOf<Breadcrumb>()

    init {
        viewModelScope.launch {
            settingsRepository.observeTargetDirectoryUri().collect { uri ->
                if (uri != null && uri.isNotEmpty()) {
                    loadDirectory(uri, isRoot = true)
                } else {
                    _uiState.value = StorageBrowserUiState.NoDirectorySelected
                }
            }
        }
    }

    fun setRootDirectory(uri: String) {
        viewModelScope.launch {
            settingsRepository.saveTargetDirectoryUri(uri)
            loadDirectory(uri, isRoot = true)
        }
    }

    fun navigateToDirectory(uri: String, name: String) {
        breadcrumbs.add(Breadcrumb(name, uri))
        loadDirectory(uri, isRoot = false)
    }

    fun navigateUpTo(uri: String) {
        val index = breadcrumbs.indexOfFirst { it.uri == uri }
        if (index != -1) {
            breadcrumbs.subList(index + 1, breadcrumbs.size).clear()
            loadDirectory(uri, isRoot = false)
        }
    }

    fun refresh() {
        currentUri?.let { loadDirectory(it, isRoot = false) }
    }

    private fun loadDirectory(uri: String, isRoot: Boolean) {
        currentUri = uri
        if (isRoot) {
            breadcrumbs.clear()
            breadcrumbs.add(Breadcrumb("Root", uri))
        }
        _uiState.value = StorageBrowserUiState.Loading
        viewModelScope.launch {
            val filesResult = storageProvider.listFiles(uri)
            filesResult.onSuccess { files ->
                val importedUris = noteRepository.observeActiveNotes().first().mapNotNull { it.remoteId }.toSet()
                val mappedFiles = files.map { it.copy(isImported = importedUris.contains(it.uri)) }.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
                _uiState.value = StorageBrowserUiState.Content(uri, breadcrumbs.toList(), mappedFiles)
            }.onFailure {
                _uiState.value = StorageBrowserUiState.Error(it.message ?: "Failed to load directory")
            }
        }
    }

    fun requestDirectoryPicker() {
        viewModelScope.launch { _effects.send(StorageBrowserEffect.LaunchDirectoryPicker) }
    }

    fun openFile(uri: String) {
        viewModelScope.launch { _effects.send(StorageBrowserEffect.NavigateToViewer(uri)) }
    }

    fun forkFile(uri: String) {
        viewModelScope.launch {
            _effects.send(StorageBrowserEffect.PromptCopyOnWrite(uri))
        }
    }
    
    fun confirmFork(uri: String) {
        viewModelScope.launch {
            forkExternalFileUseCase(uri).onSuccess { noteId ->
                _effects.send(StorageBrowserEffect.ShowSnackbar("Saved to Local Notes"))
                _effects.send(StorageBrowserEffect.NavigateToEditor(noteId))
            }.onFailure {
                _effects.send(StorageBrowserEffect.ShowSnackbar(it.message ?: "Failed to fork file"))
            }
        }
    }
}
