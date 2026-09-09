package com.notesmd.feature.notes.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesmd.core.domain.repository.NoteRepository
import com.notesmd.core.domain.repository.TagRepository
import com.notesmd.core.domain.usecase.CreateNoteUseCase
import com.notesmd.core.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository,
    private val createNoteUseCase: CreateNoteUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<Tag?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_MODIFIED)
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedNoteIds = MutableStateFlow<Set<Long>>(emptySet())

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        noteRepository.observeActiveNotes(),
        combine(
            tagRepository.observeTagsWithCounts(),
            _searchQuery.debounce(300),
            _selectedTag,
            _sortOrder
        ) { tags, query, selectedTag, sortOrder ->
            FilterConfig(tags, query, selectedTag, sortOrder)
        },
        _isSelectionMode,
        _selectedNoteIds
    ) { notes, filterConfig, isSelectionMode, selectedNoteIds ->
        val tags = filterConfig.tags
        val query = filterConfig.query
        val selectedTag = filterConfig.selectedTag
        val sortOrder = filterConfig.sortOrder

        var filteredNotes = notes
        if (query.isNotBlank()) {
            filteredNotes = filteredNotes.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
        }
        if (selectedTag != null) {
            filteredNotes = filteredNotes.filter { note -> note.tags.any { it.id == selectedTag.id } }
        }
        
        filteredNotes = when (sortOrder) {
            SortOrder.DATE_MODIFIED -> filteredNotes.sortedByDescending { it.updatedAt }
            SortOrder.DATE_CREATED -> filteredNotes.sortedByDescending { it.createdAt }
            SortOrder.TITLE -> filteredNotes.sortedBy { it.title }
            SortOrder.MANUAL -> filteredNotes
        }
        
        if (notes.isEmpty() && tags.isEmpty() && query.isBlank()) {
            DashboardUiState.Empty
        } else {
            DashboardUiState.Content(
                notes = filteredNotes,
                availableTags = tags.map { it.first },
                selectedTagFilter = selectedTag,
                sortOrder = sortOrder,
                searchQuery = query,
                isSelectionMode = isSelectionMode,
                selectedNoteIds = selectedNoteIds
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

data class FilterConfig(
    val tags: List<Pair<Tag, Int>>,
    val query: String,
    val selectedTag: Tag?,
    val sortOrder: SortOrder
)

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun selectTag(tag: Tag?) { _selectedTag.value = tag }
    fun updateSortOrder(order: SortOrder) { _sortOrder.value = order }
    
    fun toggleSelectionMode(noteId: Long) {
        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
            _selectedNoteIds.value = setOf(noteId)
        } else {
            toggleNoteSelection(noteId)
        }
    }
    
    fun toggleNoteSelection(noteId: Long) {
        val current = _selectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) current.remove(noteId) else current.add(noteId)
        _selectedNoteIds.value = current
        if (current.isEmpty()) _isSelectionMode.value = false
    }
    
    fun exitSelectionMode() {
        _isSelectionMode.value = false
        _selectedNoteIds.value = emptySet()
    }
    
    fun createNewNote() {
        viewModelScope.launch {
            val noteId = createNoteUseCase()
            _effects.send(DashboardEffect.NavigateToEditor(noteId))
        }
    }
    
    fun navigateToSettings() {
        viewModelScope.launch { _effects.send(DashboardEffect.NavigateToSettings) }
    }
    
    fun openNote(noteId: Long) {
        if (_isSelectionMode.value) {
            toggleNoteSelection(noteId)
        } else {
            viewModelScope.launch { _effects.send(DashboardEffect.NavigateToViewer(noteId)) }
        }
    }
}
