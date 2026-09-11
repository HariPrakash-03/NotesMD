package com.notesmd.core.markdown.parser

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.node.Node
import org.commonmark.parser.Parser

object MarkdownParser {
    private val parser = Parser.builder()
        .extensions(listOf(TablesExtension.create(), StrikethroughExtension.create()))
        .build()

    fun parse(content: String): Node {
        return parser.parse(content)
    }
}
