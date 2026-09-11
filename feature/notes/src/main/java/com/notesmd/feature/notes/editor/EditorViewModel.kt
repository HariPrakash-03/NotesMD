package com.notesmd.feature.notes.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.domain.repository.TagRepository
import com.notesmd.core.domain.usecase.DiscardEmptyNoteUseCase
import com.notesmd.core.domain.usecase.SaveNoteContentUseCase
import com.notesmd.core.domain.usecase.SyncNoteTagsUseCase
import com.notesmd.core.domain.usecase.CreateTagUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val saveContentUseCase: SaveNoteContentUseCase,
    private val syncTagsUseCase: SyncNoteTagsUseCase,
    private val discardEmptyNoteUseCase: DiscardEmptyNoteUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val noteRepository: NoteRepository,
    val tagRepository: TagRepository,
    private val templateRepository: com.notesmd.core.domain.repository.TemplateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: -1L

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _effects = Channel<EditorEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val contentChanges = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            if (noteId != -1L) {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _uiState.value = EditorUiState.Active(
                        noteId = note.id,
                        title = note.title,
                        rawContent = note.content,
                        activeTags = note.tags,
                        isPreviewMode = false,
                        isDirty = false
                    )
                }
            }
        }

        // Debounced write for normal typing pauses
        contentChanges
            .debounce(800)
            .onEach { (title, content) -> persistContent(title, content) }
            .launchIn(viewModelScope)

        // Hard-cap flush: never more than 5s from being persisted during continuous typing
        contentChanges
            .sample(5_000)
            .onEach { (title, content) -> persistContent(title, content) }
            .launchIn(viewModelScope)

        templateRepository.observeDefaultTemplate()
            .onEach { defaultTemplate ->
                val state = _uiState.value as? EditorUiState.Active ?: return@onEach
                _uiState.value = state.copy(viewerTemplate = defaultTemplate)
            }
            .launchIn(viewModelScope)
    }

    fun updateTitle(newTitle: String) {
        val state = _uiState.value as? EditorUiState.Active ?: return
        _uiState.value = state.copy(title = newTitle, isDirty = true)
        contentChanges.tryEmit(newTitle to state.rawContent)
    }

    fun updateContent(newContent: String) {
        val state = _uiState.value as? EditorUiState.Active ?: return
        _uiState.value = state.copy(rawContent = newContent, isDirty = true)
        contentChanges.tryEmit(state.title to newContent)
    }

    fun addTag(tag: com.notesmd.core.model.Tag) {
        val state = _uiState.value as? EditorUiState.Active ?: return
        if (state.activeTags.any { it.id == tag.id }) return
        val newTags = state.activeTags + tag
        _uiState.value = state.copy(activeTags = newTags)
        viewModelScope.launch { syncTagsUseCase(state.noteId, newTags.map { it.id }) }
    }

    fun removeTag(tagId: Long) {
        val state = _uiState.value as? EditorUiState.Active ?: return
        val newTags = state.activeTags.filter { it.id != tagId }
        _uiState.value = state.copy(activeTags = newTags)
        viewModelScope.launch { syncTagsUseCase(state.noteId, newTags.map { it.id }) }
    }

    fun createNewTag(name: String) {
        viewModelScope.launch {
            // Use blue accent color by default for newly created tags from the editor
            val tagId = createTagUseCase(name, "#0969DA")
            // Fetch it back or construct it directly if we had a full Tag model returned,
            // but we can just let observeTags update the list if needed.
            // Wait, createTagUseCase only returns Long. We can just instantiate a Tag since we know the details.
            val newTag = com.notesmd.core.model.Tag(
                id = tagId,
                name = name.trim().lowercase(),
                colorHex = "#0969DA"
            )
            addTag(newTag)
        }
    }

    fun togglePreview() {
        val state = _uiState.value as? EditorUiState.Active ?: return
        viewModelScope.launch {
            if (!state.isPreviewMode) flushPendingChangesNow() // forced flush entering Preview
            _uiState.value = state.copy(isPreviewMode = !state.isPreviewMode)
        }
    }

    fun onNavigateAway() {
        viewModelScope.launch {
            flushPendingChangesNow()
            _effects.send(EditorEffect.NavigateBack)
        }
    }

    fun forceFlush() {
        viewModelScope.launch {
            flushPendingChangesNow()
        }
    }

    private suspend fun persistContent(title: String, content: String) {
        val state = _uiState.value as? EditorUiState.Active ?: return
        saveContentUseCase(state.noteId, title, content)
            .onSuccess { _uiState.value = state.copy(isDirty = false) }
            .onFailure { error -> _effects.send(EditorEffect.ShowSnackbar(error.message ?: "Save failed")) }
    }

    private suspend fun flushPendingChangesNow() {
        val state = _uiState.value as? EditorUiState.Active ?: return
        when {
            state.title.isBlank() && state.rawContent.isBlank() -> discardEmptyNoteUseCase(state.noteId)
            state.isDirty -> persistContent(state.title, state.rawContent)
        }
    }

    fun insertSyntax(prefix: String, suffix: String, selectionStart: Int, selectionEnd: Int) {
        val state = _uiState.value as? EditorUiState.Active ?: return
        
        val content = state.rawContent
        val actualStart = minOf(selectionStart, selectionEnd).coerceIn(0, content.length)
        val actualEnd = maxOf(selectionStart, selectionEnd).coerceIn(0, content.length)
        
        val isListToken = prefix.startsWith("- [ ]")
        if (isListToken) {
            val lines = content.substring(0, actualStart).split("\n")
            val lastLineStart = content.substring(0, actualStart).lastIndexOf('\n') + 1
            val newContent = content.substring(0, lastLineStart) + prefix + content.substring(lastLineStart)
            updateContent(newContent)
        } else {
            val selectedText = content.substring(actualStart, actualEnd)
            val newText = if (selectedText.isEmpty()) {
                prefix + suffix
            } else {
                prefix + selectedText + suffix
            }
            
            val newContent = content.substring(0, actualStart) + newText + content.substring(actualEnd)
            updateContent(newContent)
        }
    }
}
