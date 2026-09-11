package com.rsenterprise.notesmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.notesmd.feature.notes.dashboard.DashboardScreen
import com.notesmd.feature.notes.dashboard.DashboardViewModel
import com.notesmd.feature.notes.storage.StorageBrowserScreen
import com.notesmd.feature.notes.storage.StorageBrowserViewModel
import com.notesmd.designsystem.MarkdownManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var settingsRepository: com.notesmd.core.domain.repository.SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsRepository.observeThemeMode().collectAsState(initial = com.notesmd.core.model.ThemeMode.SYSTEM)
            val isDark = when (themeMode) {
                com.notesmd.core.model.ThemeMode.LIGHT -> false
                com.notesmd.core.model.ThemeMode.DARK -> true
                com.notesmd.core.model.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MarkdownManagerTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        var selectedTabIndex by remember { mutableIntStateOf(0) }
                        val snackBarHostState = remember { SnackbarHostState() }
                        
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(snackBarHostState) },
                            bottomBar = {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = selectedTabIndex == 0,
                                        onClick = { selectedTabIndex = 0 },
                                        icon = { Icon(Icons.AutoMirrored.Filled.List, "Dashboard") },
                                        label = { Text("Dashboard") }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTabIndex == 1,
                                        onClick = { selectedTabIndex = 1 },
                                        icon = { Icon(Icons.Default.Folder, "Storage") },
                                        label = { Text("Storage") }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                if (selectedTabIndex == 0) {
                                    val viewModel: DashboardViewModel = hiltViewModel()
                                    val uiState by viewModel.uiState.collectAsState()
                                    
                                    LaunchedEffect(viewModel) {
                                        viewModel.effects.collect { effect ->
                                            when (effect) {
                                                is com.notesmd.feature.notes.dashboard.DashboardEffect.ShowSnackbar -> {
                                                    if (effect.actionLabel != null) {
                                                        val result = snackBarHostState.showSnackbar(
                                                            message = effect.message,
                                                            actionLabel = effect.actionLabel,
                                                            duration = SnackbarDuration.Long
                                                        )
                                                        if (result == SnackbarResult.ActionPerformed) {
                                                            effect.onActionClick?.invoke()
                                                        }
                                                    } else {
                                                        snackBarHostState.showSnackbar(effect.message)
                                                    }
                                                }
                                                is com.notesmd.feature.notes.dashboard.DashboardEffect.NavigateToEditor -> 
                                                    navController.navigate("editor/${effect.noteId}")
                                                is com.notesmd.feature.notes.dashboard.DashboardEffect.NavigateToViewer -> 
                                                    navController.navigate("viewer/${effect.noteId}")
                                                is com.notesmd.feature.notes.dashboard.DashboardEffect.NavigateToSettings -> 
                                                    snackBarHostState.showSnackbar("Nav to Settings")
                                            }
                                        }
                                    }
                                    
                                    DashboardScreen(
                                        uiState = uiState,
                                        onSearchQueryChanged = viewModel::updateSearchQuery,
                                        onTagSelected = viewModel::selectTag,
                                        onNoteClick = viewModel::openNote,
                                        onNoteLongClick = viewModel::toggleSelectionMode,
                                        onCreateNote = viewModel::createNewNote,
                                        onSettingsClick = { navController.navigate("settings") },
                                        onSortOrderSelected = viewModel::updateSortOrder,
                                        onExportDirSelected = viewModel::onExportDirSelected
                                    )
                                } else {
                                    val viewModel: StorageBrowserViewModel = hiltViewModel()
                                    val uiState by viewModel.uiState.collectAsState()
                                    
                                    LaunchedEffect(viewModel) {
                                        viewModel.effects.collect { effect ->
                                            when (effect) {
                                                is com.notesmd.feature.notes.storage.StorageBrowserEffect.ShowSnackbar -> 
                                                    snackBarHostState.showSnackbar(effect.message)
                                                is com.notesmd.feature.notes.storage.StorageBrowserEffect.NavigateToEditor -> 
                                                    navController.navigate("editor/${effect.noteId}")
                                                is com.notesmd.feature.notes.storage.StorageBrowserEffect.NavigateToViewer -> 
                                                    navController.navigate("viewer?deviceUri=${android.net.Uri.encode(effect.uri)}")
                                                is com.notesmd.feature.notes.storage.StorageBrowserEffect.LaunchDirectoryPicker -> 
                                                    snackBarHostState.showSnackbar("Launch Picker")
                                                is com.notesmd.feature.notes.storage.StorageBrowserEffect.PromptCopyOnWrite -> {
                                                    snackBarHostState.showSnackbar("Prompt Copy-on-Write")
                                                    viewModel.confirmFork(effect.uri)
                                                }
                                            }
                                        }
                                    }
                                    
                                    StorageBrowserScreen(
                                        uiState = uiState,
                                        onBreadcrumbClick = viewModel::navigateUpTo,
                                        onChangeDirectoryClick = viewModel::requestDirectoryPicker,
                                        onRefreshClick = viewModel::refresh,
                                        onFileClick = { file -> 
                                            if (file.isDirectory) viewModel.navigateToDirectory(file.uri, file.name) else viewModel.openFile(file.uri)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    composable(
                        route = "viewer/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = androidx.navigation.NavType.LongType })
                    ) {
                        var showPicker by remember { mutableStateOf(false) }
                        com.notesmd.feature.notes.viewer.ViewerScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEditor = { newNoteId ->
                                navController.navigate("editor/$newNoteId")
                            },
                            onShowTemplatePicker = { showPicker = true }
                        )
                        if (showPicker) {
                            com.notesmd.feature.template.TemplatePickerSheet(
                                onDismissRequest = { showPicker = false },
                                onNavigateToStudio = { templateId ->
                                    showPicker = false
                                    navController.navigate(if (templateId == null) "template_studio" else "template_studio/$templateId")
                                }
                            )
                        }
                    }
                    
                    composable("tag_manager") {
                        com.notesmd.feature.tags.TagManagerScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable("template_gallery") {
                        com.notesmd.feature.template.TemplateGalleryScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToStudio = { templateId ->
                                navController.navigate(if (templateId == null) "template_studio" else "template_studio/$templateId")
                            }
                        )
                    }
                    
                    composable(
                        route = "template_studio/{templateId}",
                        arguments = listOf(navArgument("templateId") { type = androidx.navigation.NavType.LongType })
                    ) {
                        com.notesmd.feature.template.TemplateStudioScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable("template_studio") {
                        com.notesmd.feature.template.TemplateStudioScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    
                    composable(
                        route = "viewer?deviceUri={deviceUri}",
                        arguments = listOf(navArgument("deviceUri") { type = androidx.navigation.NavType.StringType; nullable = true })
                    ) {
                        var showPicker by remember { mutableStateOf(false) }
                        com.notesmd.feature.notes.viewer.ViewerScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEditor = { newNoteId ->
                                navController.navigate("editor/$newNoteId")
                            },
                            onShowTemplatePicker = { showPicker = true }
                        )
                        if (showPicker) {
                            com.notesmd.feature.template.TemplatePickerSheet(
                                onDismissRequest = { showPicker = false },
                                onNavigateToStudio = { templateId ->
                                    showPicker = false
                                    navController.navigate(if (templateId == null) "template_studio" else "template_studio/$templateId")
                                }
                            )
                        }
                    }
                    composable(
                        route = "editor/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = androidx.navigation.NavType.LongType })
                    ) {
                        val viewModel: com.notesmd.feature.notes.editor.EditorViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()
                        val availableTagsPairs by viewModel.tagRepository.observeTagsWithCounts().collectAsState(initial = emptyList())
                        val availableTags = availableTagsPairs.map { it.first }
                        val snackbarHostState = remember { SnackbarHostState() }

                        LaunchedEffect(viewModel) {
                            viewModel.effects.collect { effect ->
                                when (effect) {
                                    is com.notesmd.feature.notes.editor.EditorEffect.NavigateBack -> navController.popBackStack()
                                    is com.notesmd.feature.notes.editor.EditorEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                                }
                            }
                        }

                        com.notesmd.feature.notes.editor.EditorScreen(
                            uiState = uiState,
                            onNavigateAway = viewModel::onNavigateAway,
                            onTitleChanged = viewModel::updateTitle,
                            onContentChanged = viewModel::updateContent,
                            onTagAdded = viewModel::addTag,
                            onTagRemoved = viewModel::removeTag,
                            onCreateNewTag = viewModel::createNewTag,
                            onTogglePreview = viewModel::togglePreview,
                            onInsertSyntax = viewModel::insertSyntax,
                            onForceFlush = viewModel::forceFlush,
                            availableTags = availableTags
                        )
                    }
                    
                    composable("settings") {
                        val viewModel: com.notesmd.feature.settings.SettingsViewModel = hiltViewModel()
                        val uiState by viewModel.uiState.collectAsState()
                        val snackbarHostState = remember { SnackbarHostState() }
                        
                        LaunchedEffect(viewModel) {
                            viewModel.effects.collect { effect ->
                                when (effect) {
                                    is com.notesmd.feature.settings.SettingsEffect.ShowSnackbar -> {
                                        if (effect.actionLabel != null) {
                                            val result = snackbarHostState.showSnackbar(
                                                message = effect.message,
                                                actionLabel = effect.actionLabel,
                                                duration = SnackbarDuration.Long
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                effect.onActionClick?.invoke()
                                            }
                                        } else {
                                            snackbarHostState.showSnackbar(effect.message)
                                        }
                                    }
                                }
                            }
                        }

                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) }
                        ) { padding ->
                            Box(modifier = Modifier.padding(padding)) {
                                com.notesmd.feature.settings.SettingsScreen(
                                    uiState = uiState,
                                    onNavigateBack = { navController.popBackStack() },
                                    onThemeSelected = viewModel::setThemeMode,
                                    onExportAllClick = viewModel::startExportAllNotes,
                                    onExportDirSelected = viewModel::onExportDirSelected
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
