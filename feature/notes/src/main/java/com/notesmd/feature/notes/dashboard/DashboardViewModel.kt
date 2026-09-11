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

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.notesmd.core.domain.model.ExportProgress
import com.notesmd.core.domain.usecase.BulkExportNotesUseCase
import dagger.hilt.android.qualifiers.ApplicationContext

@OptIn(FlowPreview::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val tagRepository: TagRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val bulkExportNotesUseCase: BulkExportNotesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<Tag?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_MODIFIED)
    private val _isSelectionMode = MutableStateFlow(false)
    private val _selectedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isExporting = MutableStateFlow(false)
    private val _exportProgressCurrent = MutableStateFlow(0)
    private val _exportProgressTotal = MutableStateFlow(0)

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
        _selectedNoteIds,
        combine(_isExporting, _exportProgressCurrent, _exportProgressTotal) { isExporting, current, total ->
            Triple(isExporting, current, total)
        }
    ) { notes, filterConfig, isSelectionMode, selectedNoteIds, exportState ->
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
                selectedNoteIds = selectedNoteIds,
                isExporting = exportState.first,
                exportProgressCurrent = exportState.second,
                exportProgressTotal = exportState.third
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

    fun onExportDirSelected(uri: Uri?) {
        if (uri == null) return
        val noteIds = _selectedNoteIds.value.toList()
        if (noteIds.isEmpty()) return

        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        viewModelScope.launch {
            _isExporting.value = true
            _exportProgressCurrent.value = 0
            _exportProgressTotal.value = 0
            
            bulkExportNotesUseCase(noteIds, uri.toString()).collect { progress ->
                when (progress) {
                    is ExportProgress.InProgress -> {
                        _exportProgressCurrent.value = progress.current
                        _exportProgressTotal.value = progress.total
                    }
                    is ExportProgress.Completed -> {
                        _isExporting.value = false
                        exitSelectionMode()
                        _effects.send(
                            DashboardEffect.ShowSnackbar(
                                message = "Export complete",
                                actionLabel = "Open Folder",
                                onActionClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(progress.folderUri)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            )
                        )
                    }
                    is ExportProgress.Failed -> {
                        _isExporting.value = false
                        _effects.send(DashboardEffect.ShowSnackbar("Export failed: ${progress.error}"))
                    }
                }
            }
        }
    }
}
