package com.notesmd.feature.notes.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notesmd.feature.notes.components.MarkdownCard
import com.notesmd.designsystem.components.TagChip
import com.notesmd.designsystem.components.TagChipVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onSearchQueryChanged: (String) -> Unit,
    onTagSelected: (com.notesmd.core.model.Tag?) -> Unit,
    onNoteClick: (Long) -> Unit,
    onNoteLongClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onSettingsClick: () -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit
) {
    Scaffold(
        topBar = {
            if (uiState is DashboardUiState.Content && uiState.isSelectionMode) {
                TopAppBar(
                    title = { Text("${uiState.selectedNoteIds.size} selected") }
                )
            } else {
                var showSortMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                SearchBar(
                    query = (uiState as? DashboardUiState.Content)?.searchQuery ?: "",
                    onQueryChange = onSearchQueryChanged,
                    onSearch = onSearchQueryChanged,
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search notes or content...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Date Modified") },
                                    onClick = { onSortOrderSelected(SortOrder.DATE_MODIFIED); showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date Created") },
                                    onClick = { onSortOrderSelected(SortOrder.DATE_CREATED); showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Title A-Z") },
                                    onClick = { onSortOrderSelected(SortOrder.TITLE); showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Manual") },
                                    onClick = { onSortOrderSelected(SortOrder.MANUAL); showSortMenu = false }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {}
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNote,
                icon = { Icon(Icons.Default.Add, "New Note") },
                text = { Text("New Note") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Create your first note", style = MaterialTheme.typography.titleMedium)
                    }
                }
                is DashboardUiState.Content -> {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        item {
                            TagChip(
                                label = "All",
                                accentColorHex = "#0969DA", // Primary
                                variant = TagChipVariant.FILTER,
                                isSelected = uiState.selectedTagFilter == null,
                                showDot = false,
                                onClick = { onTagSelected(null) }
                            )
                        }
                        items(uiState.availableTags) { tag ->
                            TagChip(
                                label = tag.name,
                                accentColorHex = tag.colorHex,
                                variant = TagChipVariant.FILTER,
                                isSelected = uiState.selectedTagFilter?.id == tag.id,
                                onClick = { onTagSelected(tag) }
                            )
                        }
                        item {
                            IconButton(onClick = onSettingsClick) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                        }
                    }
                    
                    Text(
                        text = "${uiState.notes.size} notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            MarkdownCard(
                                note = note,
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = uiState.selectedNoteIds.contains(note.id),
                                onClick = { onNoteClick(note.id) },
                                onLongClick = { onNoteLongClick(note.id) },
                                onOverflowClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
