package com.notesmd.feature.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notesmd.core.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onExportAllClick: () -> Unit,
    onExportDirSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        onExportDirSelected(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            
            ThemeSelectionItem(
                title = "System Default",
                isSelected = uiState.themeMode == ThemeMode.SYSTEM,
                onClick = { onThemeSelected(ThemeMode.SYSTEM) }
            )
            ThemeSelectionItem(
                title = "Light",
                isSelected = uiState.themeMode == ThemeMode.LIGHT,
                onClick = { onThemeSelected(ThemeMode.LIGHT) }
            )
            ThemeSelectionItem(
                title = "Dark",
                isSelected = uiState.themeMode == ThemeMode.DARK,
                onClick = { onThemeSelected(ThemeMode.DARK) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            ListItem(
                headlineContent = { Text("Export All Notes") },
                supportingContent = {
                    if (uiState.isExporting) {
                        val progressMsg = if (uiState.exportProgressTotal > 0) {
                            "Exporting ${uiState.exportProgressCurrent} of ${uiState.exportProgressTotal}…"
                        } else {
                            "Starting export…"
                        }
                        Column {
                            Text(progressMsg)
                            if (uiState.exportProgressTotal > 0) {
                                LinearProgressIndicator(
                                    progress = { uiState.exportProgressCurrent.toFloat() / uiState.exportProgressTotal.toFloat() },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            } else {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                            }
                        }
                    } else {
                        Text("Export all notes to a folder")
                    }
                },
                modifier = Modifier.clickable(enabled = !uiState.isExporting) {
                    onExportAllClick()
                    launcher.launch(null)
                }
            )
        }
    }
}

@Composable
fun ThemeSelectionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}
