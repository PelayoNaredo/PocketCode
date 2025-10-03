package com.pocketcode.features.project.ui.selection

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

// Import PocketCode components
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.card.ProjectCard
import com.pocketcode.core.ui.components.feedback.NoProjectsEmptyState
import com.pocketcode.core.ui.components.feedback.PocketDialog
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import com.pocketcode.core.ui.snackbar.LocalGlobalToastDispatcher
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

data class Project(
    val id: String,
    val name: String,
    val localPath: String,
    val description: String,
    val lastModified: String,
    val icon: ImageVector = Icons.Default.Folder,
    val isRecent: Boolean = false
)

@Composable
fun ProjectSelectionScreen(
    viewModel: ProjectSelectionViewModel = hiltViewModel(),
    onProjectSelected: (Project) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val toastDispatcher = LocalGlobalToastDispatcher.current
    
    // Handle create project results
    LaunchedEffect(uiState.createProjectResult) {
        uiState.createProjectResult?.let { result ->
            when (result) {
                is CreateProjectResult.Success -> {
                    toastDispatcher.showMessage(
                        message = "Project \"${result.project.name}\" created",
                        style = PocketToastStyle.Success,
                        origin = GlobalSnackbarOrigin.PROJECTS
                    )
                    // Auto-select the newly created project
                    onProjectSelected(result.project)
                }
                is CreateProjectResult.Error -> {
                    toastDispatcher.showMessage(
                        message = result.message,
                        style = PocketToastStyle.Error,
                        origin = GlobalSnackbarOrigin.PROJECTS
                    )
                }
            }
            // Clear result after handling
            viewModel.clearCreateResult()
        }
    }

    LaunchedEffect(uiState.error) {
        val errorMessage = uiState.error ?: return@LaunchedEffect
        toastDispatcher.showMessage(
            message = errorMessage,
            style = PocketToastStyle.Error,
            origin = GlobalSnackbarOrigin.PROJECTS
        )
        viewModel.clearError()
    }
    
    PocketScaffold(
        config = PocketScaffoldConfig(
            hasTopBar = false,
            isScrollable = false,
            paddingValues = PaddingValues(24.dp),
            backgroundColor = MaterialTheme.colorScheme.background
        )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                WelcomeHeader()
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Create new project card
                CreateProjectCard(
                    onClick = { showCreateDialog = true }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Recent projects section
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Projects list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav
                ) {
                items(uiState.projects) { project ->
                    ProjectCard(onClick = { onProjectSelected(project) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(SpacingTokens.Semantic.contentSpacingNormal),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = project.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    style = TypographyTokens.Title.medium,
                                    color = ColorTokens.onSurface
                                )
                                project.description?.takeIf { it.isNotBlank() }?.let { description ->
                                    Text(
                                        text = description,
                                        style = TypographyTokens.Body.small,
                                        color = ColorTokens.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Last modified: ${project.lastModified}",
                                    style = TypographyTokens.Label.small,
                                    color = ColorTokens.onSurfaceVariant
                                )
                            }

                            if (project.isRecent) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiary
                                ) {
                                    Text(
                                        text = "RECENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.projects.isEmpty()) {
                    item {
                        NoProjectsEmptyState(
                            onCreateProject = { showCreateDialog = true }
                        )
                    }
                }
                }
            }
        }
        
        // Create project dialog
        if (showCreateDialog) {
            CreateProjectDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { projectName ->
                    viewModel.createProject(projectName)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
private fun WelcomeHeader() {
    Column {
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose a project to continue coding",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CreateProjectCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = "Create New Project",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ProjectCard and EmptyStateCard functions removed - replaced with PocketCode components

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    
    PocketDialog(
        title = "Create New Project",
        onDismissRequest = onDismiss,
        confirmText = "Create",
        confirmEnabled = projectName.isNotBlank(),
        onConfirm = {
            if (projectName.isNotBlank()) {
                onConfirm(projectName.trim())
            }
        },
        dismissText = "Cancel",
        onDismiss = onDismiss,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter a name for your new project:",
                    style = MaterialTheme.typography.bodyMedium
                )

                PocketTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = "Project Name",
                    placeholder = "My Awesome App",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}