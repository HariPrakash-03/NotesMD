package com.notesmd.core.model

/**
 * User-customizable rendering template for the Markdown Viewer.
 * All color fields are stored as hex strings (e.g., "#FFFFFF").
 */
data class ViewerTemplate(
    val id: Long = 0,
    val name: String = "",
    val fontFamily: FontFamilyOption = FontFamilyOption.SANS_SERIF,
    val fontScale: Float = 1.0f,
    val backgroundColor: String = "#FFFFFF",
    val headingColor: String = "#1F2328",
    val codeBlockTint: String = "#F6F8FA",
    val quoteAccentColor: String = "#0969DA"
) {
    companion object {
        val Default = ViewerTemplate(
            name = "Default",
            fontFamily = FontFamilyOption.SANS_SERIF,
            fontScale = 1.0f,
            backgroundColor = "#FFFFFF",
            headingColor = "#1F2328",
            codeBlockTint = "#F6F8FA",
            quoteAccentColor = "#0969DA"
        )
    }
}
