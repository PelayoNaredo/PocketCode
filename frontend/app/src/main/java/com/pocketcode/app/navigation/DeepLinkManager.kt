package com.pocketcode.app.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Deep link routes for navigation
 */
object DeepLinkRoutes {
    const val SCHEME = "pocketcode"
    const val HOST = "app"
    
    // Main destinations
    const val PROJECTS = "/projects"
    const val FILES = "/files"
    const val EDITOR = "/editor"
    const val AI_CHAT = "/chat"
    const val PREVIEW = "/preview"
    
    // Parameterized routes
    const val EDITOR_WITH_FILE = "/editor/{filePath}"
    const val PROJECT_WITH_ID = "/projects/{projectId}"
    const val CHAT_WITH_CONTEXT = "/chat/{context}"
}

/**
 * Deep link data class
 */
data class DeepLink(
    val destination: AppDestination,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * Deep link manager for handling navigation from external sources
 */
@Stable
class DeepLinkManager(
    private val navigationManager: NavigationManager,
    private val scope: CoroutineScope
) {
    
    private var _pendingDeepLink by mutableStateOf<DeepLink?>(null)
    val pendingDeepLink: DeepLink? get() = _pendingDeepLink
    
    /**
     * Handle incoming intent for deep linking
     */
    fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            parseDeepLink(uri)?.let { deepLink ->
                _pendingDeepLink = deepLink
                executeDeepLink(deepLink)
            }
        }
    }
    
    /**
     * Parse URI to deep link
     */
    fun parseDeepLink(uri: Uri): DeepLink? {
        if (uri.scheme != DeepLinkRoutes.SCHEME || uri.host != DeepLinkRoutes.HOST) {
            return null
        }
        
        return when (val path = uri.path) {
            DeepLinkRoutes.PROJECTS -> DeepLink(AppDestination.PROJECT_SELECTION)
            
            DeepLinkRoutes.FILES -> DeepLink(AppDestination.FILE_EXPLORER)
            
            DeepLinkRoutes.EDITOR -> DeepLink(AppDestination.CODE_EDITOR)
            
            DeepLinkRoutes.AI_CHAT -> DeepLink(AppDestination.AI_CHAT)
            
            DeepLinkRoutes.PREVIEW -> DeepLink(AppDestination.PREVIEW)
            
            else -> {
                // Handle parameterized routes
                when {
                    path?.startsWith("/editor/") == true -> {
                        val filePath = path.substringAfter("/editor/")
                        if (filePath.isNotEmpty()) {
                            DeepLink(
                                destination = AppDestination.CODE_EDITOR,
                                parameters = mapOf("filePath" to filePath)
                            )
                        } else null
                    }
                    
                    path?.startsWith("/projects/") == true -> {
                        val projectId = path.substringAfter("/projects/")
                        if (projectId.isNotEmpty()) {
                            DeepLink(
                                destination = AppDestination.PROJECT_SELECTION,
                                parameters = mapOf("projectId" to projectId)
                            )
                        } else null
                    }
                    
                    path?.startsWith("/chat/") == true -> {
                        val context = path.substringAfter("/chat/")
                        if (context.isNotEmpty()) {
                            DeepLink(
                                destination = AppDestination.AI_CHAT,
                                parameters = mapOf("context" to context)
                            )
                        } else null
                    }
                    
                    else -> null
                }
            }
        }
    }
    
    /**
     * Execute deep link navigation
     */
    private fun executeDeepLink(deepLink: DeepLink) {
        scope.launch {
            val handled = handleDeepLinkParameters(deepLink)
            if (!handled) {
                navigationManager.navigateToDestination(
                    destination = deepLink.destination,
                    fromDeepLink = true
                )
            }
        }
    }
    
    /**
     * Handle deep link parameters
     */
    private fun handleDeepLinkParameters(deepLink: DeepLink): Boolean {
        return when (deepLink.destination) {
            AppDestination.CODE_EDITOR -> {
                deepLink.parameters["filePath"]?.let { filePath ->
                    navigationManager.navigateToEditorWithFile(Uri.decode(filePath))
                    true
                } ?: false
            }

            AppDestination.PROJECT_SELECTION -> {
                deepLink.parameters["projectId"]?.let { projectId ->
                    navigationManager.navigateToProject(Uri.decode(projectId))
                    true
                } ?: false
            }

            AppDestination.AI_CHAT -> {
                deepLink.parameters["context"]?.let { context ->
                    navigationManager.navigateToChatWithContext(Uri.decode(context))
                    true
                } ?: false
            }

            else -> false
        }
    }
    
    /**
     * Create deep link URI for sharing
     */
    fun createDeepLink(destination: AppDestination, parameters: Map<String, String> = emptyMap()): Uri {
        val path = when (destination) {
            AppDestination.PROJECT_SELECTION -> {
                if (parameters.containsKey("projectId")) {
                    "/projects/${parameters["projectId"]}"
                } else {
                    DeepLinkRoutes.PROJECTS
                }
            }
            
            AppDestination.FILE_EXPLORER -> DeepLinkRoutes.FILES
            
            AppDestination.CODE_EDITOR -> {
                if (parameters.containsKey("filePath")) {
                    "/editor/${parameters["filePath"]}"
                } else {
                    DeepLinkRoutes.EDITOR
                }
            }
            
            AppDestination.AI_CHAT -> {
                if (parameters.containsKey("context")) {
                    "/chat/${parameters["context"]}"
                } else {
                    DeepLinkRoutes.AI_CHAT
                }
            }
            
            AppDestination.PREVIEW -> DeepLinkRoutes.PREVIEW
        }
        
        return Uri.Builder()
            .scheme(DeepLinkRoutes.SCHEME)
            .authority(DeepLinkRoutes.HOST)
            .path(path)
            .build()
    }
    
    /**
     * Clear pending deep link
     */
    fun clearPendingDeepLink() {
        _pendingDeepLink = null
    }
    
    /**
     * Navigate to deep link programmatically
     */
    fun navigateToDeepLink(
        destination: AppDestination,
        parameters: Map<String, String> = emptyMap()
    ) {
        val deepLink = DeepLink(destination, parameters)
        executeDeepLink(deepLink)
    }
}

/**
 * Remember deep link manager instance
 */
@Composable
fun rememberDeepLinkManager(
    navigationManager: NavigationManager,
    scope: CoroutineScope = rememberCoroutineScope()
): DeepLinkManager {
    return remember(navigationManager, scope) {
        DeepLinkManager(navigationManager, scope)
    }
}