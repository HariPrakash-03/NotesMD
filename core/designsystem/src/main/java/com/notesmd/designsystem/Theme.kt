package com.notesmd.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = PrimaryLight,
    secondary = PrimaryLight,
    onSecondary = OnPrimaryLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = PrimaryLight,
    surface = SurfaceLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceVariant = SurfaceVariantLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = PrimaryDark,
    secondary = PrimaryDark,
    onSecondary = OnPrimaryDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = PrimaryDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceVariant = SurfaceVariantDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

@Composable
fun MarkdownManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotesMDTypography, // Includes Monospace tokens for markdown
        content = content
    )
}
