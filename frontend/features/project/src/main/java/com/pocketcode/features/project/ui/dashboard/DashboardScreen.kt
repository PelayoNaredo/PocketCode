package com.pocketcode.features.project.ui.dashboard

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.pocketcode.core.ui.components.layout.PocketDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.ProjectCard
import com.pocketcode.core.ui.components.feedback.EmptyState
import com.pocketcode.core.ui.components.feedback.ErrorDisplay
import com.pocketcode.core.ui.components.feedback.LoadingIndicator
import com.pocketcode.core.ui.components.feedback.PocketDialog
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.snackbar.LocalGlobalSnackbarDispatcher
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonSize
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.domain.project.model.Project
import kotlinx.coroutines.launch

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
    onProjectClick: (Project) -> Unit,
    onMarketplaceClick: () -> Unit = {},
    onAiAssistantClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarDispatcher = LocalGlobalSnackbarDispatcher.current
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val displayName = context.contentResolver.queryDisplayName(it)
            viewModel.importProject(it, displayName)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.ShowSnackbar -> snackbarDispatcher.dispatch(event.event)
            }
        }
    }

    DashboardScreen(
        uiState = uiState,
        onProjectClick = onProjectClick,
        onMarketplaceClick = onMarketplaceClick,
        onAiAssistantClick = onAiAssistantClick,
        onSettingsClick = onSettingsClick,
        onRetry = viewModel::refresh,
        onCreateProject = viewModel::createProject,
        onImportProject = {
            importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(
    uiState: DashboardUiState,
    onProjectClick: (Project) -> Unit,
    onMarketplaceClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRetry: () -> Unit,
    onCreateProject: (String) -> Unit,
    onImportProject: () -> Unit
) {
    var showActionsSheet by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val openCreateDialog: () -> Unit = {
        projectName = ""
        showCreateDialog = true
    }

    PocketScaffold(
        config = PocketScaffoldConfig(
            paddingValues = PaddingValues(
                horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
                vertical = SpacingTokens.Semantic.screenPaddingVertical
            ),
            isScrollable = false,
            backgroundColor = ColorTokens.background
        ),
        topBar = {
            PocketTopBar(
                title = "Proyectos",
                subtitle = "Administra y retoma tu trabajo",
                actions = listOf(
                    TopBarAction(
                        icon = PocketIcons.SmartToy,
                        contentDescription = "Abrir asistente de IA",
                        onClick = onAiAssistantClick
                    ),
                    TopBarAction(
                        icon = PocketIcons.ShoppingCart,
                        contentDescription = "Ir al marketplace",
                        onClick = onMarketplaceClick
                    ),
                    TopBarAction(
                        icon = PocketIcons.Settings,
                        contentDescription = "Abrir configuración",
                        onClick = onSettingsClick
                    )
                )
            )
        },
        floatingActionButton = {
            PocketButton(
                text = "Nuevo proyecto",
                onClick = { showActionsSheet = true },
                leadingIcon = {
                    Icon(
                        imageVector = PocketIcons.Add,
                        contentDescription = null
                    )
                },
                size = ButtonSize.Medium
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Cargando proyectos..."
                    )
                }

                uiState.errorMessage != null -> {
                    ErrorDisplay(
                        error = uiState.errorMessage,
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = onRetry
                    )
                }

                uiState.projects.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
                    ) {
                        EmptyState(
                            title = "No tienes proyectos todavía",
                            description = "Crea un nuevo proyecto o importa uno existente para comenzar.",
                            icon = PocketIcons.FolderOpen,
                            actionText = "Crear proyecto",
                            onAction = openCreateDialog,
                            secondaryActionText = "Importar proyecto",
                            onSecondaryAction = onImportProject,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PocketButton(
                            text = "Explorar recursos",
                            onClick = onMarketplaceClick,
                            variant = ButtonVariant.Text
                        )
                    }
                }

                else -> {
                    ProjectList(
                        projects = uiState.projects,
                        onProjectClick = onProjectClick
                    )
                }
            }

            if (uiState.isImportingProject) {
                LoadingIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = SpacingTokens.Semantic.contentSpacingNormal),
                    text = "Importando proyecto..."
                )
            }
        }
    }

    if (showActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionsSheet = false },
            sheetState = sheetState
        ) {
            DashboardActionSheetItem(
                icon = PocketIcons.Add,
                title = "Crear un proyecto nuevo",
                description = "Empieza desde una plantilla base",
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showActionsSheet = false
                        openCreateDialog()
                    }
                }
            )
            PocketDivider(modifier = Modifier.padding(vertical = 8.dp))
            DashboardActionSheetItem(
                icon = PocketIcons.FileUpload,
                title = "Importar un .zip existente",
                description = "Selecciona un proyecto comprimido para trabajarlo en PocketCode",
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showActionsSheet = false
                        onImportProject()
                    }
                }
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Semantic.contentSpacingLoose))
        }
    }

    if (showCreateDialog) {
        PocketDialog(
            title = "Crear un nuevo proyecto",
            message = "Define el nombre con el que aparecerá en tu dashboard.",
            onDismissRequest = {
                if (!uiState.isCreatingProject) {
                    showCreateDialog = false
                }
            },
            confirmText = "Crear",
            onConfirm = {
                onCreateProject(projectName.trim())
                showCreateDialog = false
            },
            confirmEnabled = projectName.isNotBlank(),
            confirmLoading = uiState.isCreatingProject,
            dismissText = "Cancelar",
            onDismiss = {
                showCreateDialog = false
            },
            content = {
                PocketTextField(
                    modifier = Modifier.testTag("dashboard_create_project_name"),
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = "Nombre del proyecto",
                    placeholder = "Mi App Pocket",
                    singleLine = true,
                    helperText = if (projectName.isNotBlank()) "Crearemos una carpeta con este nombre" else null
                )
            }
        )
    }
}

@Composable
private fun DashboardActionSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    PocketButton(
        text = title,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.Semantic.contentSpacingNormal),
        variant = ButtonVariant.Secondary,
        size = ButtonSize.Large,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.Semantic.contentSpacingNormal)
    )
}

@Composable
private fun ProjectList(
    projects: List<Project>,
    onProjectClick: (Project) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = SpacingTokens.Semantic.contentSpacingNormal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        items(
            items = projects,
            key = { it.id }
        ) { project ->
            ProjectListItem(
                project = project,
                onClick = { onProjectClick(project) }
            )
        }
    }
}

@Composable
private fun ProjectListItem(
    project: Project,
    onClick: () -> Unit
) {
    ProjectCard(
        title = project.name,
        subtitle = project.localPath,
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = PocketIcons.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            PocketButton(
                text = "Abrir",
                onClick = onClick,
                variant = ButtonVariant.Secondary,
                size = ButtonSize.Small
            )
        }
    )
}

private fun android.content.ContentResolver.queryDisplayName(uri: Uri): String? {
    return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor: Cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index == -1 || !cursor.moveToFirst()) {
                null
            } else {
                cursor.getString(index)
            }
        }
}
