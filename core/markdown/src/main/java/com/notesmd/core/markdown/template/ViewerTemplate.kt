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

fun com.notesmd.core.model.ViewerTemplate.toUiTemplate(): ViewerTemplate {
    return ViewerTemplate(
        fontFamily = when (this.fontFamily) {
            com.notesmd.core.model.FontFamilyOption.MONOSPACE -> FontFamily.Monospace
            com.notesmd.core.model.FontFamilyOption.SERIF -> FontFamily.Serif
            com.notesmd.core.model.FontFamilyOption.SANS_SERIF -> FontFamily.SansSerif
        },
        fontScale = this.fontScale,
        backgroundColor = try { Color(android.graphics.Color.parseColor(this.backgroundColor)) } catch (e: Exception) { Color.Transparent },
        headingColor = try { Color(android.graphics.Color.parseColor(this.headingColor)) } catch (e: Exception) { Color.Unspecified },
        codeBlockBackground = try { Color(android.graphics.Color.parseColor(this.codeBlockTint)) } catch (e: Exception) { Color(0xFFEAEFF5) },
        blockquoteAccent = try { Color(android.graphics.Color.parseColor(this.quoteAccentColor)) } catch (e: Exception) { Color(0xFF0969DA) }
    )
}
