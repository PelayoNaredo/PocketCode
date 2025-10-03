package com.pocketcode.features.project.ui.ide

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.theme.PocketCodeTheme
import com.pocketcode.domain.project.model.Project
import com.pocketcode.features.project.ui.explorer.FileExplorer

// Import PocketCode components
import com.pocketcode.core.ui.components.navigation.ProjectTopBar
import com.pocketcode.core.ui.components.navigation.ScrollableTabIndicator
import com.pocketcode.core.ui.components.navigation.TabItem
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.feedback.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeWorkspaceScreen(
    project: Project,
    viewModel: IdeWorkspaceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onOpenAiAssistant: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            onNavigateBack()
        }
    }
    
    PocketCodeTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "File Explorer",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        FileExplorer(
                            onFileClick = { file ->
                                viewModel.openFile(file)
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        ) {
            PocketScaffold(
                config = PocketScaffoldConfig(
                    hasTopBar = true,
                    isScrollable = false,
                    paddingValues = PaddingValues(0.dp)
                ),
                topBar = {
                    ProjectTopBar(
                        projectName = project.name,
                        isModified = false, // TODO: Get from viewModel
                        onNavigationClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                        },
                        onSave = { /* TODO: Implement save */ },
                        onSettings = onOpenAiAssistant,
                        onMore = { /* TODO: Implement more options */ }
                    )
                }
            ) { _ ->
                IdeMainContent(
                    viewModel = viewModel,
                    project = project
                )
            }
        }
    }
}
@Composable
private fun IdeMainContent(
    viewModel: IdeWorkspaceViewModel,
    project: Project
) {
    val currentFile by viewModel.currentFile.collectAsState()
    val openFiles by viewModel.openFiles.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab bar for open files
        if (openFiles.isNotEmpty()) {
            val tabs = openFiles.map { openFile ->
                TabItem(
                    id = openFile.file.path,
                    title = openFile.file.name + if (openFile.isModified) " •" else "",
                    closeable = true
                )
            }
            
            ScrollableTabIndicator(
                tabs = tabs,
                selectedTabIndex = openFiles.indexOfFirst { it.file.path == currentFile?.file?.path }.coerceAtLeast(0),
                onTabSelected = { index ->
                    if (index < openFiles.size) {
                        viewModel.switchToFile(openFiles[index])
                    }
                },
                onTabClosed = { index ->
                    if (index < openFiles.size) {
                        viewModel.closeFile(openFiles[index].file)
                    }
                }
            )
        }
        
        // Code editor content
        Box(modifier = Modifier.weight(1f)) {
            currentFile?.let { openFile ->
                CodeEditorContent(openFile = openFile)
            } ?: run {
                IdeWelcomeScreen(project = project)
            }
        }
    }
}

@Composable
private fun CodeEditorContent(openFile: com.pocketcode.features.project.ui.ide.OpenFile) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Editing: ${openFile.file.name}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = openFile.content.takeIf { it.isNotEmpty() } 
                    ?: "// File content will appear here\n// Code editor with syntax highlighting coming soon!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun IdeWelcomeScreen(project: Project) {
    EmptyState(
        title = "Welcome to PocketCode IDE",
        description = "Project: ${project.name}\nUse the menu button to open the file explorer and select a file to edit",
        icon = Icons.Default.Menu,
        actionText = "Open File Explorer",
        onAction = { /* This will be handled by the drawer toggle */ }
    )
}
