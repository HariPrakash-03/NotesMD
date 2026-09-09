package com.notesmd.feature.notes.storage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageBrowserScreen(
    uiState: StorageBrowserUiState,
    onBreadcrumbClick: (String) -> Unit,
    onChangeDirectoryClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onFileClick: (com.notesmd.core.model.DeviceFile) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState is StorageBrowserUiState.Content) {
                            uiState.breadcrumbs.forEachIndexed { index, crumb ->
                                Text(
                                    text = crumb.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { onBreadcrumbClick(crumb.uri) }
                                )
                                if (index < uiState.breadcrumbs.size - 1) {
                                    Text(" ▸ ", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        } else {
                            Text("Storage")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onChangeDirectoryClick) {
                    Text("Tap to change target directory ▾")
                }
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
            
            Divider()
            
            when (uiState) {
                is StorageBrowserUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is StorageBrowserUiState.NoDirectorySelected -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No directory selected. Tap the banner above to choose a folder.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is StorageBrowserUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is StorageBrowserUiState.Content -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(uiState.files) { file ->
                            ListItem(
                                headlineContent = { Text(file.name) },
                                supportingContent = {
                                    if (!file.isDirectory) {
                                        Text(" KB")
                                    }
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                        contentDescription = null
                                    )
                                },
                                trailingContent = {
                                    if (file.isImported) {
                                        Text("✓ Imported", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                modifier = Modifier.clickable { onFileClick(file) }
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}
