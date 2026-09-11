package com.notesmd.core.markdown.view

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.notesmd.core.markdown.template.LocalViewerTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.ext.gfm.tables.*
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.*

@Composable
fun MarkdownAstView(
    node: Node,
    searchQuery: String = "",
    baseUri: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        var child = node.firstChild
        while (child != null) {
            MarkdownBlockNode(child, searchQuery, baseUri)
            child = child.next
        }
    }
}

@Composable
fun MarkdownBlockNode(node: Node, searchQuery: String, baseUri: String?) {
    val template = LocalViewerTemplate.current

    when (node) {
        is Heading -> {
            val style = when (node.level) {
                1 -> MaterialTheme.typography.headlineMedium
                2 -> MaterialTheme.typography.headlineSmall
                3 -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.titleMedium
            }.copy(
                color = if (template.headingColor != Color.Unspecified) template.headingColor else MaterialTheme.colorScheme.onSurface,
                fontFamily = template.fontFamily,
                fontSize = when (node.level) {
                    1 -> 24.sp * template.fontScale
                    2 -> 20.sp * template.fontScale
                    3 -> 14.sp * template.fontScale
                    else -> 16.sp * template.fontScale
                }
            )
            val inlineData = buildInlineText(node, searchQuery)
            Text(
                text = inlineData.text,
                inlineContent = inlineData.inlineContent,
                style = style,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
        is Paragraph -> {
            val inlineData = buildInlineText(node, searchQuery)
            Text(
                text = inlineData.text,
                inlineContent = inlineData.inlineContent,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = template.fontFamily,
                    fontSize = 16.sp * template.fontScale
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        is BlockQuote -> {
            Row(modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp).height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(template.blockquoteAccent)
                )
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                        .fillMaxWidth()
                ) {
                    var child = node.firstChild
                    while (child != null) {
                        MarkdownBlockNode(child, searchQuery, baseUri)
                        child = child.next
                    }
                }
            }
        }
        is FencedCodeBlock -> {
            Surface(
                color = template.codeBlockBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = node.literal.trimEnd(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp * template.fontScale,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                )
            }
        }
        is BulletList -> {
            Column(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)) {
                var child = node.firstChild
                while (child != null) {
                    Row {
                        Text("• ", modifier = Modifier.padding(end = 4.dp))
                        Column { MarkdownBlockNode(child, searchQuery, baseUri) }
                    }
                    child = child.next
                }
            }
        }
        is OrderedList -> {
            Column(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)) {
                var child = node.firstChild
                var index = node.startNumber
                while (child != null) {
                    Row {
                        Text("$index. ", modifier = Modifier.padding(end = 4.dp))
                        Column { MarkdownBlockNode(child, searchQuery, baseUri) }
                    }
                    index++
                    child = child.next
                }
            }
        }
        is ListItem -> {
            val firstChild = node.firstChild
            if (firstChild is Paragraph && firstChild.firstChild is Text) {
                val textNode = firstChild.firstChild as Text
                val text = textNode.literal
                if (text.startsWith("[ ] ") || text.startsWith("[x] ") || text.startsWith("[X] ")) {
                    val isChecked = text.startsWith("[x] ", ignoreCase = true)
                    textNode.literal = text.substring(4)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = null,
                            modifier = Modifier.padding(end = 8.dp).size(24.dp)
                        )
                        Column {
                            var child = node.firstChild
                            while (child != null) {
                                MarkdownBlockNode(child, searchQuery, baseUri)
                                child = child.next
                            }
                        }
                    }
                    return
                }
            }
            var child = node.firstChild
            while (child != null) {
                MarkdownBlockNode(child, searchQuery, baseUri)
                child = child.next
            }
        }
        is ThematicBreak -> {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
        is TableBlock -> {
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                var child = node.firstChild
                while (child != null) {
                    MarkdownBlockNode(child, searchQuery, baseUri)
                    child = child.next
                }
            }
        }
        is TableHead -> {
            Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
                var child = node.firstChild
                while (child != null) {
                    MarkdownBlockNode(child, searchQuery, baseUri)
                    child = child.next
                }
            }
        }
        is TableBody -> {
            Column {
                var child = node.firstChild
                while (child != null) {
                    MarkdownBlockNode(child, searchQuery, baseUri)
                    child = child.next
                }
            }
        }
        is TableRow -> {
            Row(modifier = Modifier.drawBehind {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            }) {
                var child = node.firstChild
                while (child != null) {
                    MarkdownBlockNode(child, searchQuery, baseUri)
                    child = child.next
                }
            }
        }
        is TableCell -> {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .widthIn(min = 100.dp)
            ) {
                val inlineData = buildInlineText(node, searchQuery)
                Text(
                    text = inlineData.text,
                    inlineContent = inlineData.inlineContent,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = template.fontFamily,
                        fontSize = 14.sp * template.fontScale
                    )
                )
            }
        }
        is Image -> {
            MarkdownImage(node.destination, node.title ?: "", baseUri)
        }
        else -> {
            var child = node.firstChild
            while (child != null) {
                MarkdownBlockNode(child, searchQuery, baseUri)
                child = child.next
            }
        }
    }
}

class InlineData(val text: AnnotatedString, val inlineContent: Map<String, InlineTextContent>)

@Composable
fun buildInlineText(parentNode: Node, searchQuery: String): InlineData {
    val inlineContentMap = mutableMapOf<String, InlineTextContent>()
    val text = buildAnnotatedString {
        appendInlineChildren(parentNode, inlineContentMap)
        
        if (searchQuery.isNotEmpty()) {
            val plainText = this.toAnnotatedString().text
            var index = plainText.indexOf(searchQuery, ignoreCase = true)
            while (index >= 0) {
                addStyle(
                    style = SpanStyle(background = Color.Yellow, color = Color.Black),
                    start = index,
                    end = index + searchQuery.length
                )
                index = plainText.indexOf(searchQuery, startIndex = index + searchQuery.length, ignoreCase = true)
            }
        }
    }
    return InlineData(text, inlineContentMap)
}

fun AnnotatedString.Builder.appendInlineChildren(parent: Node, inlineMap: MutableMap<String, InlineTextContent>) {
    var child = parent.firstChild
    while (child != null) {
        when (child) {
            is Text -> append(child.literal)
            is Emphasis -> {
                val start = length
                appendInlineChildren(child, inlineMap)
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
            }
            is StrongEmphasis -> {
                val start = length
                appendInlineChildren(child, inlineMap)
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
            }
            is Strikethrough -> {
                val start = length
                appendInlineChildren(child, inlineMap)
                addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, length)
            }
            is Code -> {
                val start = length
                append(child.literal)
                addStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFFEFF1F3),
                        color = Color(0xFF0969DA)
                    ),
                    start, length
                )
            }
            is Link -> {
                val start = length
                appendInlineChildren(child, inlineMap)
                addStyle(SpanStyle(color = Color(0xFF0969DA), textDecoration = TextDecoration.Underline), start, length)
            }
            is Image -> {
                // If it's inline, we could use appendInlineContent, but typically markdown images are block-level.
                // We'll insert a placeholder text since we handle it in MarkdownBlockNode if it's the only child.
                append("[Image: ${child.title ?: "Remote image not loaded (offline-first)"}]")
            }
            is SoftLineBreak -> append(" ")
            is HardLineBreak -> append("\n")
            else -> appendInlineChildren(child, inlineMap)
        }
        child = child.next
    }
}

@Composable
fun MarkdownImage(url: String, title: String, baseUri: String?) {
    if (url.startsWith("http://") || url.startsWith("https://") || baseUri == null) {
        // Render remote image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🚫", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (baseUri == null && !url.startsWith("http")) "Remote image not loaded (offline-first)" else "Remote image not loaded (offline-first)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        val context = LocalContext.current
        var bitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

        LaunchedEffect(url) {
            withContext(Dispatchers.IO) {
                try {
                    val rootFile = DocumentFile.fromTreeUri(context, Uri.parse(baseUri))
                    val imgFile = rootFile?.findFile(url)
                    if (imgFile != null) {
                        context.contentResolver.openInputStream(imgFile.uri)?.use { stream ->
                            val decoded = BitmapFactory.decodeStream(stream)
                            bitmap = decoded?.asImageBitmap()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // Loading or failed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
