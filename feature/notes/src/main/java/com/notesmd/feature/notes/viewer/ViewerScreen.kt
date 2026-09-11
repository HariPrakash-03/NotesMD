package com.notesmd.feature.notes.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notesmd.core.markdown.template.LocalViewerTemplate
import com.notesmd.core.markdown.template.toUiTemplate
import com.notesmd.core.markdown.view.MarkdownBlockNode
import kotlinx.coroutines.launch
import org.commonmark.node.Node

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (Long) -> Unit,
    onShowTemplatePicker: () -> Unit = {},
    viewModel: ViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showOutline by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showCopyConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ViewerEffect.NavigateBack -> onNavigateBack()
                is ViewerEffect.NavigateToEditor -> onNavigateToEditor(effect.noteId)
                is ViewerEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState is ViewerUiState.Content) {
                            val content = uiState as ViewerUiState.Content
                            Text(
                                text = content.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (content.isDeviceFile) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Device File",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOutline = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Outline")
                    }
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onShowTemplatePicker) {
                        Icon(Icons.Default.Palette, contentDescription = "Style")
                    }
                    // TODO: Export scoped to bulk-export phase
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Upload, contentDescription = "Export")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is ViewerUiState.Content) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val state = uiState as ViewerUiState.Content
                        if (state.isDeviceFile) {
                            showCopyConfirm = true
                        } else {
                            viewModel.onEditClicked()
                        }
                    },
                    text = { Text("Edit") },
                    icon = { }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ViewerUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ViewerUiState.Content -> {
                    val topLevelNodes = remember(state.rootNode) {
                        val nodes = mutableListOf<Node>()
                        var child = state.rootNode.firstChild
                        while (child != null) {
                            nodes.add(child)
                            child = child.next
                        }
                        nodes
                    }
                    
                    AnimatedVisibility(visible = showSearch) {
                        SearchBarOverlay(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onNext = {
                                val matchIndex = topLevelNodes.indexOfFirst { node ->
                                    val text = nodeToString(node)
                                    text.contains(searchQuery, ignoreCase = true)
                                }
                                if (matchIndex >= 0) {
                                    coroutineScope.launch { listState.animateScrollToItem(matchIndex) }
                                }
                            },
                            onPrev = {
                                // naive prev
                                val matchIndex = topLevelNodes.indexOfFirst { node ->
                                    val text = nodeToString(node)
                                    text.contains(searchQuery, ignoreCase = true)
                                }
                                if (matchIndex >= 0) {
                                    coroutineScope.launch { listState.animateScrollToItem(matchIndex) }
                                }
                            },
                            onClose = { 
                                showSearch = false
                                searchQuery = ""
                            }
                        )
                    }
                    
                    val template = state.viewerTemplate ?: com.notesmd.core.model.ViewerTemplate.Default
                    CompositionLocalProvider(LocalViewerTemplate provides template.toUiTemplate()) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(topLevelNodes) { index, node ->
                                MarkdownBlockNode(node, searchQuery, state.baseUri)
                            }
                        }
                    }
                }
            }
        }

        if (showOutline && uiState is ViewerUiState.Content) {
            val state = uiState as ViewerUiState.Content
            ModalBottomSheet(
                onDismissRequest = { showOutline = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "Document Outline",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn {
                        items(state.headings.size) { index ->
                            val heading = state.headings[index]
                            Text(
                                text = heading.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(heading.index)
                                        }
                                        showOutline = false
                                    }
                                    .padding(
                                        start = ((heading.level - 1) * 16).dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    )
                            )
                        }
                    }
                }
            }
        }
        
        if (showCopyConfirm) {
            ModalBottomSheet(
                onDismissRequest = { showCopyConfirm = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "Make a local copy?",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Create local note to start editing? Original file will remain unchanged.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCopyConfirm = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            showCopyConfirm = false
                            viewModel.onConfirmFork()
                        }) {
                            Text("Make Copy & Edit")
                        }
                    }
                }
            }
        }
    }
}

fun nodeToString(node: Node): String {
    val builder = StringBuilder()
    var child = node.firstChild
    while (child != null) {
        if (child is org.commonmark.node.Text) {
            builder.append(child.literal)
        } else if (child is org.commonmark.node.Code) {
            builder.append(child.literal)
        } else {
            builder.append(nodeToString(child))
        }
        child = child.next
    }
    return builder.toString()
}

@Composable
fun SearchBarOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Find in document...") },
                singleLine = true,
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = onPrev) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
                            }
                            IconButton(onClick = onNext) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
                            }
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    }
                }
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close search")
            }
        }
    }
}
