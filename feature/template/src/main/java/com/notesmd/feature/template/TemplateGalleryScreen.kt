package com.notesmd.feature.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notesmd.core.markdown.template.LocalViewerTemplate
import com.notesmd.core.markdown.template.toUiTemplate
import com.notesmd.core.markdown.view.MarkdownAstView
import com.notesmd.core.model.ViewerTemplate
import org.commonmark.parser.Parser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateGalleryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStudio: (Long?) -> Unit,
    viewModel: TemplateGalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TemplateGalleryEffect.NavigateBack -> onNavigateBack()
                is TemplateGalleryEffect.NavigateToStudio -> onNavigateToStudio(effect.templateId)
                is TemplateGalleryEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TemplateGrid(
                templates = uiState.templates,
                onTemplateSelected = { viewModel.setAsDefault(it.id) },
                onEditTemplate = { viewModel.editTemplate(it.id) },
                onCreateNew = { viewModel.createNewTemplate() },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerSheet(
    onDismissRequest: () -> Unit,
    onNavigateToStudio: (Long?) -> Unit,
    viewModel: TemplateGalleryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TemplateGalleryEffect.NavigateToStudio -> onNavigateToStudio(effect.templateId)
                is TemplateGalleryEffect.ShowSnackbar -> { /* Handle or let caller handle */ }
                is TemplateGalleryEffect.NavigateBack -> onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Template",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                TemplateGrid(
                    templates = uiState.templates,
                    onTemplateSelected = { 
                        viewModel.setAsDefault(it.id)
                        onDismissRequest() 
                    },
                    onEditTemplate = { viewModel.editTemplate(it.id) },
                    onCreateNew = { viewModel.createNewTemplate() }
                )
            }
        }
    }
}

@Composable
fun TemplateGrid(
    templates: List<ViewerTemplate>,
    onTemplateSelected: (ViewerTemplate) -> Unit,
    onEditTemplate: (ViewerTemplate) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleMarkdown = "# Sample\nBody text"
    val parser = remember { Parser.builder().build() }
    val node = remember(sampleMarkdown) { parser.parse(sampleMarkdown) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(templates) { template ->
            TemplateCard(
                template = template,
                isSelected = false, // We could determine this if we knew the default
                onClick = { onTemplateSelected(template) },
                onEdit = { onEditTemplate(template) },
                previewContent = {
                    CompositionLocalProvider(LocalViewerTemplate provides template.toUiTemplate()) {
                        MarkdownAstView(node = node)
                    }
                }
            )
        }
        item {
            NewTemplateCard(onClick = onCreateNew)
        }
    }
}

@Composable
fun TemplateCard(
    template: ViewerTemplate,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(8.dp)
                    .clipToBounds()
            ) {
                previewContent()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Template", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun NewTemplateCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = "New Template")
                Spacer(modifier = Modifier.height(8.dp))
                Text("New Template", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
