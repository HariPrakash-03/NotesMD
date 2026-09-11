package com.notesmd.feature.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notesmd.core.markdown.template.LocalViewerTemplate
import com.notesmd.core.markdown.template.toUiTemplate
import com.notesmd.core.markdown.view.MarkdownAstView
import com.notesmd.core.model.FontFamilyOption
import com.notesmd.core.model.ViewerTemplate
import org.commonmark.parser.Parser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateStudioScreen(
    onNavigateBack: () -> Unit,
    viewModel: TemplateStudioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TemplateStudioEffect.NavigateBack -> onNavigateBack()
                is TemplateStudioEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.templateId == null) "New Template" else "Edit Template") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.resetTemplate() }) {
                        Text("Reset")
                    }
                    Button(onClick = { viewModel.saveTemplate() }, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Save")
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
            uiState.currentTemplate?.let { template ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Top 40% Live Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(16.dp)
                    ) {
                        val sampleMarkdown = """
                            # Sample Heading
                            Body text rendering with dynamic tokens applied.
                            > Blockquote container
                            ```
                            val isOffline = true
                            ```
                            - [x] Task list check item
                        """.trimIndent()
                        val parser = remember { Parser.builder().build() }
                        val node = remember(sampleMarkdown) { parser.parse(sampleMarkdown) }
                        
                        CompositionLocalProvider(LocalViewerTemplate provides template.toUiTemplate()) {
                            MarkdownAstView(node = node)
                        }
                    }

                    HorizontalDivider()

                    // Bottom 60% Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text("CUSTOMIZATION TOKENS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Template Name
                        OutlinedTextField(
                            value = template.name,
                            onValueChange = { viewModel.updateCurrentTemplate(template.copy(name = it)) },
                            label = { Text("Template Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Font Family Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = template.fontFamily.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Font Family") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                FontFamilyOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.name) },
                                        onClick = {
                                            viewModel.updateCurrentTemplate(template.copy(fontFamily = option))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Font Scale Slider
                        Text("Font Scale: ${String.format("%.1fx", template.fontScale)}")
                        Slider(
                            value = template.fontScale,
                            onValueChange = { viewModel.updateCurrentTemplate(template.copy(fontScale = it)) },
                            valueRange = 0.8f..1.5f,
                            steps = 6
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Color Pickers
                        ColorPickerRow(
                            label = "Background Color",
                            colorHex = template.backgroundColor,
                            onColorChange = { viewModel.updateCurrentTemplate(template.copy(backgroundColor = it)) }
                        )
                        ColorPickerRow(
                            label = "Heading Color",
                            colorHex = template.headingColor,
                            onColorChange = { viewModel.updateCurrentTemplate(template.copy(headingColor = it)) }
                        )
                        ColorPickerRow(
                            label = "Code Block Tint",
                            colorHex = template.codeBlockTint,
                            onColorChange = { viewModel.updateCurrentTemplate(template.copy(codeBlockTint = it)) }
                        )
                        ColorPickerRow(
                            label = "Quote Accent Color",
                            colorHex = template.quoteAccentColor,
                            onColorChange = { viewModel.updateCurrentTemplate(template.copy(quoteAccentColor = it)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ColorPickerRow(
    label: String,
    colorHex: String,
    onColorChange: (String) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { showColorPicker = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Text(text = colorHex.uppercase(), modifier = Modifier.padding(end = 8.dp), style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Transparent })
        )
    }

    if (showColorPicker) {
        ModalBottomSheet(onDismissRequest = { showColorPicker = false }) {
            val colors = listOf(
                "#FFFFFF", "#0D1117", "#F6F8FA", "#161B22",
                "#1F2328", "#C9D1D9", "#0969DA", "#58A6FF",
                "#EAEFF5", "#21262D", "#388BFD"
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colors.forEach { hex ->
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .clickable(onClickLabel = "Select color $hex") {
                                onColorChange(hex)
                                showColorPicker = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                        )
                    }
                }
            }
        }
    }
}
