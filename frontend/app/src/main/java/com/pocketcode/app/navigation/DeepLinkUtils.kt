package com.pocketcode.app.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Deep link utility functions for easy link generation and sharing
 */
object DeepLinkUtils {
    
    /**
     * Generate deep link URIs for different destinations
     */
    fun createProjectsDeepLink(): Uri {
        return Uri.Builder()
            .scheme(DeepLinkRoutes.SCHEME)
            .authority(DeepLinkRoutes.HOST)
            .path(DeepLinkRoutes.PROJECTS)
            .build()
    }
    
    fun createFilesDeepLink(): Uri {
        return Uri.Builder()
            .scheme(DeepLinkRoutes.SCHEME)
            .authority(DeepLinkRoutes.HOST)
            .path(DeepLinkRoutes.FILES)
            .build()
    }
    
    fun createEditorDeepLink(filePath: String? = null): Uri {
        val path = if (filePath != null) {
            "/editor/$filePath"
        } else {
            DeepLinkRoutes.EDITOR
        }
        
        return Uri.Builder()
            .scheme(DeepLinkRoutes.SCHEME)
            .authority(DeepLinkRoutes.HOST)
            .path(path)
            .build()
    }
    
    fun createChatDeepLink(context: String? = null): Uri {
        val path = if (context != null) {
            "/chat/$context"
        } else {
            DeepLinkRoutes.AI_CHAT
        }
        
        return Uri.Builder()
            .scheme(DeepLinkRoutes.SCHEME)
            .authority(DeepLinkRoutes.HOST)
            .path(path)
            .build()
    }
    
    fun createPreviewDeepLink(): Uri {
        return Uri.Builder()
            .scheme(DeepLinkRoutes.SCHEME)
            .authority(DeepLinkRoutes.HOST)
            .path(DeepLinkRoutes.PREVIEW)
            .build()
    }
    
    /**
     * Create sharing intent for deep link
     */
    fun createShareIntent(context: Context, deepLink: Uri, title: String = "Share PocketCode"): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, deepLink.toString())
            putExtra(Intent.EXTRA_SUBJECT, title)
        }
    }
    
    /**
     * Open deep link in external browser (for testing)
     */
    fun openInBrowser(context: Context, deepLink: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, deepLink)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    
    /**
     * Create an intent to open the app with deep link
     */
    fun createAppIntent(context: Context, deepLink: Uri): Intent {
        return Intent(Intent.ACTION_VIEW, deepLink).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
    
    /**
     * Validate if a URI is a valid PocketCode deep link
     */
    fun isValidDeepLink(uri: Uri): Boolean {
        return uri.scheme == DeepLinkRoutes.SCHEME && 
               uri.host == DeepLinkRoutes.HOST &&
               !uri.path.isNullOrEmpty()
    }
    
    /**
     * Extract destination from deep link URI
     */
    fun extractDestination(uri: Uri): AppDestination? {
        if (!isValidDeepLink(uri)) return null
        
        return when (val path = uri.path) {
            DeepLinkRoutes.PROJECTS -> AppDestination.PROJECT_SELECTION
            DeepLinkRoutes.FILES -> AppDestination.FILE_EXPLORER
            DeepLinkRoutes.EDITOR -> AppDestination.CODE_EDITOR
            DeepLinkRoutes.AI_CHAT -> AppDestination.AI_CHAT
            DeepLinkRoutes.PREVIEW -> AppDestination.PREVIEW
            else -> {
                when {
                    path?.startsWith("/editor/") == true -> AppDestination.CODE_EDITOR
                    path?.startsWith("/projects/") == true -> AppDestination.PROJECT_SELECTION
                    path?.startsWith("/chat/") == true -> AppDestination.AI_CHAT
                    else -> null
                }
            }
        }
    }
}

/**
 * Composable helper for accessing deep link utilities
 */
@Composable
fun rememberDeepLinkUtils(): DeepLinkUtils {
    return remember { DeepLinkUtils }
}