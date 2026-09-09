package com.notesmd.core.markdown.template

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class ViewerTemplate(
    val fontFamily: FontFamily,
    val fontScale: Float,
    val backgroundColor: Color,
    val headingColor: Color,
    val codeBlockBackground: Color,
    val blockquoteAccent: Color
) {
    companion object {
        val Default = ViewerTemplate(
            fontFamily = FontFamily.SansSerif,
            fontScale = 1.0f,
            backgroundColor = Color.Transparent,
            headingColor = Color.Unspecified,
            codeBlockBackground = Color(0xFFEAEFF5), // SurfaceVariantLight
            blockquoteAccent = Color(0xFF0969DA) // QuoteBarLight
        )
    }
}
