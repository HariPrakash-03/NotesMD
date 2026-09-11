package com.notesmd.feature.notes.viewer

import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Heading
import org.commonmark.node.Text

data class HeadingUiModel(
    val level: Int,
    val title: String,
    val index: Int
)

class HeadingVisitor : AbstractVisitor() {
    val headings = mutableListOf<HeadingUiModel>()
    private var nodeIndex = 0

    override fun visit(heading: Heading) {
        var text = ""
        var child = heading.firstChild
        while (child != null) {
            if (child is Text) {
                text += child.literal
            }
            child = child.next
        }
        headings.add(HeadingUiModel(heading.level, text, nodeIndex))
        super.visit(heading)
    }

    override fun visit(customNode: org.commonmark.node.CustomNode?) {
        nodeIndex++
        super.visit(customNode)
    }

    override fun visit(node: org.commonmark.node.BlockQuote?) {
        nodeIndex++
        super.visit(node)
    }

    override fun visit(node: org.commonmark.node.Paragraph?) {
        nodeIndex++
        super.visit(node)
    }

    // A rough heuristic to get the block index.
    // Compose LazyColumn usually wants the top-level block index to scroll to.
    // We can just iterate the top-level nodes of the Document to find headings and their indices.
}

fun extractHeadings(document: org.commonmark.node.Node): List<HeadingUiModel> {
    val headings = mutableListOf<HeadingUiModel>()
    var child = document.firstChild
    var index = 0
    while (child != null) {
        if (child is Heading) {
            var text = ""
            var textChild = child.firstChild
            while (textChild != null) {
                if (textChild is Text) {
                    text += textChild.literal
                }
                textChild = textChild.next
            }
            headings.add(HeadingUiModel(child.level, text, index))
        }
        index++
        child = child.next
    }
    return headings
}
