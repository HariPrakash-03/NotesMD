package com.notesmd.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

data class TagSuggestionUiModel(
    val id: Long,
    val name: String,
    val colorHex: String
)

@Composable
fun TagAutocompletePicker(
    query: String,
    suggestions: List<TagSuggestionUiModel>,
    onTagSelected: (TagSuggestionUiModel) -> Unit,
    onCreateNewTag: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = false)
    ) {
        Surface(
            modifier = modifier
                .width(240.dp)
                .padding(top = 4.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(suggestions, key = { it.id }) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTagSelected(suggestion) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Color(android.graphics.Color.parseColor(suggestion.colorHex)),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = suggestion.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                if (query.isNotBlank() && suggestions.none { it.name.equals(query, ignoreCase = true) }) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCreateNewTag(query) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create '$query'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
