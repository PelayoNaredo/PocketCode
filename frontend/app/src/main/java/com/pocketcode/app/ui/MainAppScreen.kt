package com.pocketcode.app.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.pocketcode.core.ui.theme.ThemeViewModel
import com.pocketcode.app.navigation.*
import com.pocketcode.app.state.*
import com.pocketcode.app.ui.components.*
import com.pocketcode.core.ui.components.feedback.EmptyState
import com.pocketcode.core.ui.components.feedback.LoadingIndicator
import com.pocketcode.core.ui.components.feedback.PocketSnackbarHost
import com.pocketcode.core.ui.components.feedback.PocketSnackbarStyle
import com.pocketcode.core.ui.components.feedback.PocketToastHost
import com.pocketcode.core.ui.components.feedback.rememberPocketToastState
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.features.ai.ui.ChatScreen
import com.pocketcode.features.editor.ui.CodeEditor
import com.pocketcode.features.project.ui.explorer.FileExplorer
import com.pocketcode.features.project.ui.selection.ProjectSelectionScreen
import com.pocketcode.features.preview.ui.PreviewScreen
import com.pocketcode.core.ui.snackbar.GlobalSnackbarDispatcher
import com.pocketcode.core.ui.snackbar.GlobalSnackbarDuration
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.GlobalToastDispatcher
import com.pocketcode.core.ui.snackbar.LocalGlobalSnackbarDispatcher
import com.pocketcode.core.ui.snackbar.LocalGlobalToastDispatcher
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.MotionTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.providers.ConfigurationWatcher
import com.pocketcode.core.ui.providers.SharedStateProvider
import com.pocketcode.core.ui.providers.rememberThemeConfigurationManager
import com.pocketcode.core.ui.providers.ThemeConfiguration
import com.pocketcode.app.ui.navigation.NavigationDirection
import com.pocketcode.core.ui.theme.ThemeMode

/**
 * Main app screen with horizontal pager navigation, deep linking, and persistent state support
 * Simplified version focusing on composition over implementation
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainAppScreen(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    appStateViewModel: AppStateViewModel = hiltViewModel(),
    navigationStateManager: NavigationStateManager = hiltViewModel()
) {
    // Envolver toda la aplicación con SharedStateProvider
    SharedStateProvider {
        CompositionLocalProvider(
            LocalGlobalSnackbarDispatcher provides GlobalSnackbarDispatcher.from(appStateViewModel::showSnackbar),
            LocalGlobalToastDispatcher provides GlobalToastDispatcher.from(appStateViewModel::showToast)
        ) {
            MainAppContent(
                themeViewModel = themeViewModel,
                appStateViewModel = appStateViewModel,
                navigationStateManager = navigationStateManager
            )
        }
    }
}

@Composable
private fun NoFileSelectedScreen(
    onSelectFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        title = "No file selected",
        description = "Select a file from the explorer to start coding",
        icon = Icons.Default.Description,
        actionText = "Browse Files",
        onAction = onSelectFile,
        modifier = modifier.fillMaxSize()
    )
}

private fun GlobalSnackbarSeverity.toPocketStyle(): PocketSnackbarStyle = when (this) {
    GlobalSnackbarSeverity.INFO -> PocketSnackbarStyle.Info
    GlobalSnackbarSeverity.SUCCESS -> PocketSnackbarStyle.Success
    GlobalSnackbarSeverity.WARNING -> PocketSnackbarStyle.Warning
    GlobalSnackbarSeverity.ERROR -> PocketSnackbarStyle.Error
}

private fun GlobalSnackbarDuration.toSnackbarDuration(): SnackbarDuration = when (this) {
    GlobalSnackbarDuration.SHORT -> SnackbarDuration.Short
    GlobalSnackbarDuration.LONG -> SnackbarDuration.Long
}

private fun GlobalSnackbarDispatcher.showMessage(
    message: String,
    origin: GlobalSnackbarOrigin = GlobalSnackbarOrigin.SHELL,
    severity: GlobalSnackbarSeverity = GlobalSnackbarSeverity.INFO,
    duration: GlobalSnackbarDuration = GlobalSnackbarDuration.SHORT
) {
    dispatch(
        GlobalSnackbarEvent(
            message = message,
            origin = origin,
            severity = severity,
            duration = duration
        )
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MainAppContent(
    themeViewModel: ThemeViewModel,
    appStateViewModel: AppStateViewModel,
    navigationStateManager: NavigationStateManager
) {
    val context = LocalContext.current

    val navigationState by navigationStateManager.currentDestination.collectAsState()
    val isNavigationInitialized by navigationStateManager.isInitialized.collectAsState()
    val isRestoring by navigationStateManager.isRestoring.collectAsState()
    val currentProject by appStateViewModel.currentProject.collectAsState()
    val currentFile by appStateViewModel.currentFile.collectAsState()
    val chatContext by appStateViewModel.chatContext.collectAsState()
    val snackbarDispatcher = LocalGlobalSnackbarDispatcher.current
    val toastDispatcher = LocalGlobalToastDispatcher.current
    val snackbarHostState = remember { SnackbarHostState() }
    val toastState = rememberPocketToastState()

    val pagerState = rememberPagerState(
        initialPage = if (isNavigationInitialized) navigationState.index else 0,
        pageCount = { AppDestination.values().size }
    )
    val scope = rememberCoroutineScope()

    val navigationManager = rememberNavigationManager(pagerState, scope, navigationStateManager)
    val appStateManager = rememberAppStateManager()
    val deepLinkManager = rememberDeepLinkManager(navigationManager, scope)

    val themeConfigManager = rememberThemeConfigurationManager()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val resolvedDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    LaunchedEffect(themeMode, resolvedDarkTheme) {
        themeConfigManager.updateConfiguration(
            ThemeConfiguration(
                mode = themeMode,
                isDarkTheme = resolvedDarkTheme
            )
        )
        themeViewModel.updateSystemDarkTheme(resolvedDarkTheme)
    }

    var activeSnackbarEvent by remember { mutableStateOf<GlobalSnackbarEvent?>(null) }
    LaunchedEffect(appStateViewModel) {
        appStateViewModel.snackbarEvents.collect { event ->
            activeSnackbarEvent = event
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = event.duration.toSnackbarDuration(),
                withDismissAction = event.onDismiss != null
            )
            when (result) {
                SnackbarResult.ActionPerformed -> event.onAction?.invoke()
                SnackbarResult.Dismissed -> event.onDismiss?.invoke()
            }
            activeSnackbarEvent = null
        }
    }

    LaunchedEffect(appStateViewModel) {
        appStateViewModel.toastEvents.collect { event ->
            toastState.show(
                message = event.message,
                style = event.style,
                duration = event.duration,
                actionLabel = event.actionLabel,
                onAction = event.onAction
            )
        }
    }

    LaunchedEffect(themeViewModel, themeConfigManager) {
        themeViewModel.syncWithSharedState(
            onThemeModeChange = { mode ->
                scope.launch {
                    themeConfigManager.updateThemeMode(mode)
                }
            }
        )
    }

    ConfigurationWatcher(
        onThemeChange = { config ->
            themeViewModel.updateFromSharedConfig(config)
        },
        onEditorChange = {
            scope.launch {
                // Propagar cambios del editor a todos los editores abiertos
                // Aquí podrías disparar eventos para actualizar editores
            }
        },
        onAppChange = {
            scope.launch {
                // Aplicar configuraciones de la app globalmente
                // Por ejemplo, cambiar idioma, intervalos de auto-guardado, etc.
            }
        }
    )

    val transitionConfig = NavigationTransitionConfig(
        type = NavigationTransitionType.MATERIAL_SHARED_AXIS,
        duration = MotionTokens.Duration.pageTransition,
        easing = MotionTokens.Easing.emphasizedDecelerate,
        enableParallax = true
    )

    var activeDestination by remember { mutableStateOf(navigationState) }
    var previousDestination by remember { mutableStateOf<AppDestination?>(null) }
    var transitionDirection by remember { mutableStateOf(NavigationDirection.FORWARD) }
    var isTransitioning by remember { mutableStateOf(false) }
    var transitionProgress by remember { mutableStateOf(0f) }
    var pendingDestination by remember { mutableStateOf<AppDestination?>(null) }
    val transitionAnim = remember { Animatable(0f) }

    LaunchedEffect(navigationState) {
        if (navigationState != activeDestination) {
            val fromDestination = activeDestination
            val toDestination = navigationState
            val direction = if (toDestination.index >= fromDestination.index) {
                NavigationDirection.FORWARD
            } else {
                NavigationDirection.BACKWARD
            }
            previousDestination = fromDestination
            activeDestination = toDestination
            transitionDirection = direction
            pendingDestination = toDestination
            isTransitioning = true
            transitionAnim.stop()
            transitionAnim.snapTo(0f)
            transitionAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = transitionConfig.duration,
                    easing = transitionConfig.easing
                )
            ) {
                transitionProgress = value
            }
            transitionProgress = 1f
            isTransitioning = false
            previousDestination = null
            pendingDestination = null
            transitionProgress = 0f
            transitionAnim.snapTo(0f)
        }
    }

    LaunchedEffect(navigationManager) {
        navigationManager.navigationCommands.collect { command ->
            when (command) {
                is NavigationCommand.OpenEditor -> appStateViewModel.selectFileByPath(command.filePath)
                is NavigationCommand.OpenProject -> appStateViewModel.selectProjectById(command.projectId)
                is NavigationCommand.OpenChat -> appStateViewModel.applyChatContext(command.context)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (context is Activity) {
            deepLinkManager.handleIntent(context.intent)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (isNavigationInitialized && !isRestoring) {
            val currentDestination = AppDestination.values().find { it.index == pagerState.currentPage }
            currentDestination?.let { destination ->
                navigationStateManager.updateCurrentDestination(destination)
            }
        }
    }

    LaunchedEffect(isNavigationInitialized, navigationState) {
        if (isNavigationInitialized && !isRestoring) {
            if (pagerState.currentPage != navigationState.index) {
                pagerState.animateScrollToPage(navigationState.index)
            }
        }
    }

    val navigationEvent by appStateViewModel.navigationEvents.collectAsState()
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is NavigationEvent.NavigateToPage -> {
                    navigationManager.navigateToPage(event.page)
                    appStateViewModel.clearNavigationEvent()
                }
            }
        }
    }

    val topBarSubtitle = remember(navigationState, currentProject, currentFile) {
        when (navigationState) {
            AppDestination.CODE_EDITOR -> currentFile?.name ?: currentProject?.name
            AppDestination.FILE_EXPLORER, AppDestination.AI_CHAT, AppDestination.PREVIEW -> currentProject?.name
            else -> null
        }
    }

    val openSettings: () -> Unit = remember(appStateManager, toastDispatcher) {
        {
            appStateManager.showSettings()
            toastDispatcher.showMessage("Configuración abierta")
        }
    }

    val openUserProfile: () -> Unit = remember(appStateManager, toastDispatcher) {
        {
            appStateManager.showUserProfile()
            toastDispatcher.showMessage("Perfil de usuario abierto")
        }
    }

    val topBarActions = remember(appStateManager, appStateViewModel, snackbarDispatcher) {
        listOf(
            TopBarAction(
                icon = PocketIcons.Chat,
                contentDescription = "Abrir el chat de IA",
                onClick = {
                    appStateViewModel.navigateToChat()
                    snackbarDispatcher.showMessage("Asistente de IA abierto")
                }
            ),
            TopBarAction(
                icon = PocketIcons.Settings,
                contentDescription = "Abrir configuración",
                onClick = openSettings
            ),
            TopBarAction(
                icon = PocketIcons.Person,
                contentDescription = "Ver perfil",
                onClick = openUserProfile
            )
        )
    }

    val snackbarStyleResolver: (SnackbarData) -> PocketSnackbarStyle = { _ ->
        (activeSnackbarEvent?.severity ?: GlobalSnackbarSeverity.INFO).toPocketStyle()
    }
    val snackbarSupportingTextResolver: (SnackbarData) -> String? = { _ ->
        activeSnackbarEvent?.supportingText
    }

    @Composable
    fun DestinationContent(destination: AppDestination) {
        when (destination) {
            AppDestination.PROJECT_SELECTION -> {
                ProjectSelectionScreen(
                    onProjectSelected = { project ->
                        appStateViewModel.selectProject(project)
                        navigationManager.navigateToDestination(AppDestination.FILE_EXPLORER)
                    }
                )
            }

            AppDestination.FILE_EXPLORER -> {
                FileExplorer(
                    selectedProject = currentProject,
                    onFileClick = { file ->
                        appStateViewModel.selectFile(file)
                        navigationManager.navigateToDestination(AppDestination.CODE_EDITOR)
                    }
                )
            }

            AppDestination.CODE_EDITOR -> {
                currentFile?.let { file ->
                    CodeEditor(
                        file = file,
                        selectedProjectId = currentProject?.id,
                        selectedProjectName = currentProject?.name,
                        selectedProjectPath = currentProject?.localPath
                    )
                } ?: run {
                    NoFileSelectedScreen(
                        onSelectFile = {
                            navigationManager.navigateToDestination(AppDestination.FILE_EXPLORER)
                        }
                    )
                }
            }

            AppDestination.AI_CHAT -> {
                ChatScreen(
                    currentProjectId = currentProject?.id,
                    currentProjectName = currentProject?.name,
                    currentFilePath = currentFile?.path,
                    currentFileName = currentFile?.name,
                    deepLinkContext = chatContext
                )
            }

            AppDestination.PREVIEW -> {
                PreviewScreen()
            }
        }
    }

    if (isRestoring) {
        PocketScaffold(
            config = PocketScaffoldConfig(
                isScrollable = false,
                backgroundColor = ColorTokens.background
            ),
            topBar = {
                PocketTopBar(
                    title = navigationState.title,
                    subtitle = topBarSubtitle,
                    actions = topBarActions
                )
            },
            snackbarHost = {
                PocketSnackbarHost(
                    hostState = snackbarHostState,
                    style = snackbarStyleResolver,
                    supportingText = snackbarSupportingTextResolver
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(
                    text = "Restaurando tu espacio...",
                    color = ColorTokens.primary
                )

                PocketToastHost(
                    state = toastState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = SpacingTokens.large)
                )
            }
        }
        return
    }

    PocketScaffold(
        config = PocketScaffoldConfig(
            hasBottomBar = true,
            hasTopBar = false,
            isScrollable = false,
            backgroundColor = ColorTokens.background
        ),
        bottomBar = {
            UnifiedNavigationBar(
                currentPage = pagerState.currentPage,
                pageCount = AppDestination.values().size,
                transitionProgress = transitionProgress,
                pendingPage = pendingDestination?.index,
                onAiChatClick = {
                    appStateViewModel.navigateToChat()
                    toastDispatcher.showMessage("Asistente de IA abierto")
                },
                onSettingsClick = openSettings,
                onUserClick = openUserProfile
            )
        },
        snackbarHost = {
            PocketSnackbarHost(
                hostState = snackbarHostState,
                style = snackbarStyleResolver,
                supportingText = snackbarSupportingTextResolver
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // HorizontalPager with gesture navigation enabled
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true, // Enable swipe gestures
                beyondBoundsPageCount = 0
            ) { page ->
                val destination = AppDestination.values()[page]
                DestinationContent(destination)
            }

            NavigationTransitionIndicator(
                isTransitioning = isTransitioning,
                pendingDestination = pendingDestination,
                transitionProgress = transitionProgress,
                transitionType = transitionConfig.type,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = SpacingTokens.large)
            )

            AppOverlays(
                showSettings = appStateManager.overlayState.showSettings,
                showUserProfile = appStateManager.overlayState.showUserProfile,
                onDismissSettings = appStateManager::hideSettings,
                onDismissUserProfile = appStateManager::hideUserProfile
            )

            PocketToastHost(
                state = toastState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = SpacingTokens.large)
            )
        }
    }
}
