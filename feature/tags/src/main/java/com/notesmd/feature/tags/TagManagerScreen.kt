package com.notesmd.feature.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notesmd.core.model.Tag
import com.notesmd.designsystem.components.ConfirmationSheet
import com.notesmd.designsystem.components.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TagManagerEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manage Tags") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            TagInputBar(onAddTag = { viewModel.addTag(it) })
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(uiState.tagsWithCounts) { (tag, count) ->
                    TagManagerRow(
                        tag = tag,
                        noteCount = count,
                        allTags = uiState.tagsWithCounts.map { it.first },
                        onColorChange = { color -> viewModel.updateTagColor(tag, color) },
                        onNameChange = { name -> viewModel.updateTagName(tag, name) },
                        onMerge = { targetTag -> viewModel.mergeTags(tag.id, targetTag.id, targetTag.name) },
                        onDelete = { viewModel.deleteTag(tag.id, count) }
                    )
                }
            }
        }
    }
}

@Composable
fun TagInputBar(onAddTag: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.lowercase() },
                placeholder = { Text("Enter new tag name...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                onAddTag(text)
                text = ""
            }, enabled = text.isNotBlank()) {
                Text("+ Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagerRow(
    tag: Tag,
    noteCount: Int,
    allTags: List<Tag>,
    onColorChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onMerge: (Tag) -> Unit,
    onDelete: () -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showMergePicker by remember { mutableStateOf(false) }
    var mergeTarget by remember { mutableStateOf<Tag?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(tag.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clickable(onClickLabel = "Change tag color") { showColorPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(tag.colorHex)))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "#${tag.name}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$noteCount notes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = {
            renameText = tag.name
            showRenameDialog = true 
        }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit tag", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { showMergePicker = true }) {
            Icon(Icons.Default.MergeType, contentDescription = "Merge tag into another", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { 
            if (noteCount > 0) showDeleteConfirm = true else showDeleteDialog = true 
        }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete tag", tint = MaterialTheme.colorScheme.error)
        }
    }

    if (showColorPicker) {
        ModalBottomSheet(onDismissRequest = { showColorPicker = false }) {
            val colors = listOf(
                "#0969DA", "#1A7F37", "#8250DF", "#BC4C00", 
                "#CF222E", "#1B7C83", "#BF3989", "#9A6700"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                colors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(hex)))
                            .clickable {
                                onColorChange(hex)
                                showColorPicker = false
                            }
                    )
                }
            }
        }
    }

    if (showMergePicker) {
        ModalBottomSheet(onDismissRequest = { showMergePicker = false }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                item {
                    Text("Merge into...", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                }
                val availableTargets = allTags.filter { it.id != tag.id }
                items(availableTargets) { target ->
                    Text(
                        text = "#${target.name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mergeTarget = target
                                showMergePicker = false
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }

    if (mergeTarget != null) {
        ConfirmationSheet(
            title = "Merge Tags",
            description = "Merge #${tag.name} into #${mergeTarget?.name}? This cannot be undone.",
            confirmActionLabel = "Merge",
            onConfirm = { 
                onMerge(mergeTarget!!)
                mergeTarget = null
            },
            onDismissRequest = { mergeTarget = null }
        )
    }

    if (showDeleteConfirm) {
        ConfirmationSheet(
            title = "Remove Tag",
            description = "$noteCount notes use this tag. Remove it from them?",
            confirmActionLabel = "Continue",
            onConfirm = {
                showDeleteConfirm = false
                showDeleteDialog = true
            },
            onDismissRequest = { showDeleteConfirm = false }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Delete Tag",
            description = "Are you sure you want to delete #${tag.name}?",
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismissRequest = { showDeleteDialog = false }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Tag") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    onNameChange(renameText)
                    showRenameDialog = false
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
