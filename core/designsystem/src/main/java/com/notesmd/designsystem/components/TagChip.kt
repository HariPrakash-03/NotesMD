
package com.notesmd.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class TagChipVariant {
    DISPLAY, FILTER, INPUT
}

@Composable
fun TagChip(
    label: String,
    accentColorHex: String,
    variant: TagChipVariant,
    isSelected: Boolean = false,
    showDot: Boolean = true,
    onClick: () -> Unit = {},
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = Color(android.graphics.Color.parseColor(accentColorHex))
    
    val containerShape = RoundedCornerShape(8.dp)
    
    val backgroundColor = when (variant) {
        TagChipVariant.DISPLAY -> MaterialTheme.colorScheme.surface
        TagChipVariant.FILTER -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        TagChipVariant.INPUT -> MaterialTheme.colorScheme.surfaceContainer
    }
    
    val borderColor = when (variant) {
        TagChipVariant.DISPLAY -> Color.Transparent
        TagChipVariant.FILTER -> if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant
        TagChipVariant.INPUT -> Color.Transparent
    }
    
    val textColor = when (variant) {
        TagChipVariant.DISPLAY -> MaterialTheme.colorScheme.onSurfaceVariant
        TagChipVariant.FILTER -> if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        TagChipVariant.INPUT -> MaterialTheme.colorScheme.onSurface
    }
    
    val textStyle = when (variant) {
        TagChipVariant.DISPLAY -> MaterialTheme.typography.labelSmall
        TagChipVariant.FILTER -> if (isSelected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall
        TagChipVariant.INPUT -> MaterialTheme.typography.labelMedium
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(containerShape)
            .background(backgroundColor)
            .border(if (borderColor != Color.Transparent) 1.dp else 0.dp, borderColor, containerShape)
            .clickable(enabled = variant != TagChipVariant.DISPLAY) { onClick() }
            .padding(horizontal = if (variant == TagChipVariant.DISPLAY) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = textColor,
            style = textStyle,
            maxLines = 1
        )
        if (variant == TagChipVariant.INPUT) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clickable(onClickLabel = "Remove tag") { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null, // Handled by Box
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
