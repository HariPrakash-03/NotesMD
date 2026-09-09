package com.rsenterprise.notesmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownManagerTheme {
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
                                                is com.notesmd.feature.notes.dashboard.DashboardEffect.ShowSnackbar -> 
                                                    snackBarHostState.showSnackbar(effect.message)
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
                                        onSettingsClick = viewModel::navigateToSettings,
                                        onSortOrderSelected = viewModel::updateSortOrder
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
                        com.notesmd.feature.notes.viewer.ViewerScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEditor = { newNoteId ->
                                navController.navigate("editor/$newNoteId")
                            }
                        )
                    }
                    
                    composable(
                        route = "viewer?deviceUri={deviceUri}",
                        arguments = listOf(navArgument("deviceUri") { type = androidx.navigation.NavType.StringType; nullable = true })
                    ) {
                        com.notesmd.feature.notes.viewer.ViewerScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEditor = { newNoteId ->
                                navController.navigate("editor/$newNoteId")
                            }
                        )
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
                }
            }
        }
    }
}
