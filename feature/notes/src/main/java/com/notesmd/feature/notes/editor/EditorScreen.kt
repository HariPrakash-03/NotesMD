package com.notesmd.feature.notes.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.notesmd.core.markdown.template.LocalViewerTemplate
import com.notesmd.core.markdown.template.ViewerTemplate
import com.notesmd.core.markdown.view.MarkdownAstView
import com.notesmd.designsystem.components.TagAutocompletePicker
import com.notesmd.designsystem.components.TagChip
import com.notesmd.designsystem.components.TagChipVariant
import com.notesmd.designsystem.components.TagSuggestionUiModel
import com.notesmd.core.model.Tag
import com.notesmd.core.markdown.template.toUiTemplate
import org.commonmark.parser.Parser

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun EditorScreen(
    uiState: EditorUiState,
    onNavigateAway: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onTagAdded: (Tag) -> Unit,
    onTagRemoved: (Long) -> Unit,
    onCreateNewTag: (String) -> Unit,
    onTogglePreview: () -> Unit,
    onInsertSyntax: (String, String, Int, Int) -> Unit,
    onForceFlush: () -> Unit,
    availableTags: List<Tag> = emptyList() // Should be passed from ViewModel observing TagRepository
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                onForceFlush()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler {
        onNavigateAway()
    }

    when (uiState) {
        is EditorUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is EditorUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is EditorUiState.Active -> {
            var contentTextFieldValue by remember {
                mutableStateOf(TextFieldValue(uiState.rawContent))
            }
            // Sync external updates (like accessory bar insertion)
            LaunchedEffect(uiState.rawContent) {
                if (uiState.rawContent != contentTextFieldValue.text) {
                    // Keep cursor position logic roughly in bounds
                    val newCursor = contentTextFieldValue.selection.start.coerceIn(0, uiState.rawContent.length)
                    contentTextFieldValue = contentTextFieldValue.copy(
                        text = uiState.rawContent,
                        selection = TextRange(newCursor)
                    )
                }
            }

            val (titleFocus, tagsFocus, contentFocus, accessoryFocus) = remember { FocusRequester.createRefs() }
            
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            BasicTextField(
                                value = uiState.title,
                                onValueChange = onTitleChanged,
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                modifier = Modifier
                                    .focusRequester(titleFocus)
                                    .focusProperties {
                                        next = tagsFocus
                                    },
                                decorationBox = { innerTextField ->
                                    if (uiState.title.isEmpty()) {
                                        Text(
                                            text = "Note Title Input...",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateAway) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                            }
                        },
                        actions = {
                            EditorPreviewToggle(
                                isPreviewMode = uiState.isPreviewMode,
                                onToggle = onTogglePreview
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!uiState.isPreviewMode) {
                        Box(modifier = Modifier
                            .focusRequester(accessoryFocus)
                            .focusProperties {
                                previous = contentFocus
                            }) {
                            EditorAccessoryBar(
                                onInsert = { prefix, suffix ->
                                    onInsertSyntax(
                                        prefix,
                                        suffix,
                                        contentTextFieldValue.selection.start,
                                        contentTextFieldValue.selection.end
                                    )
                                }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Tag Strip
                    Box(modifier = Modifier
                        .focusRequester(tagsFocus)
                        .focusProperties {
                            next = contentFocus
                            previous = titleFocus
                        }) {
                        EditorTagStrip(
                            activeTags = uiState.activeTags,
                            availableTags = availableTags,
                            onTagAdded = onTagAdded,
                            onTagRemoved = onTagRemoved,
                            onCreateNewTag = onCreateNewTag,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Content Area
                    if (uiState.isPreviewMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            val parser = remember { Parser.builder().build() }
                            val node = remember(uiState.rawContent) { parser.parse(uiState.rawContent) }
                            val template = uiState.viewerTemplate ?: com.notesmd.core.model.ViewerTemplate.Default
                            CompositionLocalProvider(LocalViewerTemplate provides template.toUiTemplate()) {
                                MarkdownAstView(node = node)
                            }
                        }
                    } else {
                        BasicTextField(
                            value = contentTextFieldValue,
                            onValueChange = {
                                contentTextFieldValue = it
                                onContentChanged(it.text)
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace // JetBrainsMono assumption
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .focusRequester(contentFocus)
                                .focusProperties {
                                    next = accessoryFocus
                                    previous = tagsFocus
                                },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            decorationBox = { innerTextField ->
                                if (contentTextFieldValue.text.isEmpty()) {
                                    Text(
                                        text = "Write markdown text here...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditorPreviewToggle(
    isPreviewMode: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .padding(end = 16.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClickLabel = "Toggle edit or preview mode") { onToggle() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(if (!isPreviewMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "✎",
                color = if (!isPreviewMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(if (isPreviewMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "👁",
                color = if (isPreviewMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorTagStrip(
    activeTags: List<Tag>,
    availableTags: List<Tag>,
    onTagAdded: (Tag) -> Unit,
    onTagRemoved: (Long) -> Unit,
    onCreateNewTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTagPicker by remember { mutableStateOf(false) }
    var tagSearchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activeTags.forEach { tag ->
                TagChip(
                    label = "#${tag.name}",
                    accentColorHex = tag.colorHex,
                    variant = TagChipVariant.INPUT,
                    onRemove = { onTagRemoved(tag.id) }
                )
            }

            Box {
                if (showTagPicker) {
                    BasicTextField(
                        value = tagSearchQuery,
                        onValueChange = { tagSearchQuery = it },
                        textStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        modifier = Modifier
                            .width(100.dp)
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    TagAutocompletePicker(
                        query = tagSearchQuery,
                        suggestions = availableTags
                            .filter { !activeTags.contains(it) && it.name.contains(tagSearchQuery, ignoreCase = true) }
                            .map { TagSuggestionUiModel(it.id, it.name, it.colorHex) },
                        onTagSelected = {
                            val tag = availableTags.find { t -> t.id == it.id }
                            if (tag != null) onTagAdded(tag)
                            showTagPicker = false
                            tagSearchQuery = ""
                        },
                        onCreateNewTag = {
                            onCreateNewTag(it)
                            showTagPicker = false
                            tagSearchQuery = ""
                        },
                        onDismissRequest = {
                            showTagPicker = false
                            tagSearchQuery = ""
                        },
                        modifier = Modifier.padding(top = 48.dp)
                    )
                } else {
                    Text(
                        text = "+ Add Tag",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClickLabel = "Add new tag") { showTagPicker = true }
                            .padding(horizontal = 8.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditorAccessoryBar(
    onInsert: (prefix: String, suffix: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AccessoryButton("H1", "Insert Heading 1") { onInsert("# ", "") }
        AccessoryButton("H2", "Insert Heading 2") { onInsert("## ", "") }
        AccessoryButton("B", "Insert bold formatting", fontWeight = FontWeight.Bold) { onInsert("**", "**") }
        AccessoryButton("I", "Insert italic formatting") { onInsert("*", "*") } // We skip true italics visually for simplicity
        AccessoryButton("~~", "Insert strikethrough formatting") { onInsert("~~", "~~") }
        AccessoryButton("- [ ]", "Insert task list item") { onInsert("- [ ] ", "") }
        AccessoryButton(">", "Insert blockquote formatting") { onInsert("> ", "") }
        AccessoryButton("```", "Insert fenced code block") { onInsert("```\n", "\n```") }
        AccessoryButton("🔗", "Insert hyperlink") { onInsert("[", "](url)") }
        AccessoryButton("🖼", "Insert image link") { onInsert("![alt text](", ")") }
    }
}

@Composable
fun AccessoryButton(
    label: String,
    contentDescription: String,
    fontWeight: FontWeight = FontWeight.Normal,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp).semantics { this.contentDescription = contentDescription }
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = fontWeight,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
