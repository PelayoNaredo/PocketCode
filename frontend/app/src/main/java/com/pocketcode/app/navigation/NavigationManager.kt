package com.pocketcode.app.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Navigation destinations for the main app
 */
enum class AppDestination(val index: Int, val title: String) {
    PROJECT_SELECTION(0, "Projects"),
    FILE_EXPLORER(1, "Files"),
    CODE_EDITOR(2, "Editor"),
    AI_CHAT(3, "AI Chat"),
    PREVIEW(4, "Preview")
}

/**
 * Navigation events that can be triggered
 */
sealed class NavigationEvent {
    data class NavigateToDestination(val destination: AppDestination) : NavigationEvent()
    data class NavigateToPage(val page: Int) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    object NavigateForward : NavigationEvent()
}

sealed class NavigationCommand {
    data class OpenEditor(val filePath: String) : NavigationCommand()
    data class OpenProject(val projectId: String) : NavigationCommand()
    data class OpenChat(val context: String) : NavigationCommand()
}

/**
 * Navigation state holder for managing app navigation with persistence
 */
@Stable
@OptIn(ExperimentalFoundationApi::class)
class NavigationManager(
    private val pagerState: PagerState,
    private val scope: CoroutineScope,
    private val stateManager: NavigationStateManager? = null
) {

    private val _navigationCommands = MutableSharedFlow<NavigationCommand>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navigationCommands = _navigationCommands.asSharedFlow()

    private suspend fun navigateInternal(
        destination: AppDestination,
        fromDeepLink: Boolean = false
    ) {
        stateManager?.navigateToDestination(
            destination = destination,
            fromDeepLink = fromDeepLink
        )
        pagerState.animateScrollToPage(destination.index)
    }
    
    val currentDestination: AppDestination
        get() = AppDestination.values().find { it.index == pagerState.currentPage } 
            ?: AppDestination.PROJECT_SELECTION
    
    val canNavigateBack: Boolean
        get() = stateManager?.canNavigateBack() ?: (pagerState.currentPage > 0)
    
    val canNavigateForward: Boolean
        get() = pagerState.currentPage < AppDestination.values().size - 1
    
    /**
     * Navigate to a specific destination with enhanced transitions and persistence
     */
    suspend fun navigateToDestinationWithTransition(
        destination: AppDestination,
        enhancedNavigationManager: EnhancedNavigationManager? = null,
        fromDeepLink: Boolean = false
    ) {
        if (enhancedNavigationManager != null) {
            stateManager?.navigateToDestination(
                destination = destination,
                fromDeepLink = fromDeepLink
            )
            enhancedNavigationManager.navigateToDestinationWithTransition(destination)
        } else {
            navigateInternal(destination, fromDeepLink)
        }
    }
    
    /**
     * Navigate to a specific destination with state persistence
     */
    fun navigateToDestination(
        destination: AppDestination,
        fromDeepLink: Boolean = false
    ) {
        scope.launch {
            navigateInternal(destination, fromDeepLink)
        }
    }
    
    /**
     * Navigate to a specific page index with state persistence
     */
    fun navigateToPage(page: Int) {
        if (page in 0 until AppDestination.values().size) {
            val destination = AppDestination.values().find { it.index == page }
            if (destination != null) {
                navigateToDestination(destination)
            }
        }
    }
    
    /**
     * Navigate back using persistent back stack or page navigation
     */
    fun navigateBack() {
        scope.launch {
            // Try to use persistent back stack first
            val handled = stateManager?.navigateBack() ?: false
            
            if (!handled && canNavigateBack) {
                // Fallback to page-based navigation
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        }
    }
    
    /**
     * Navigate forward to next page
     */
    fun navigateForward() {
        if (canNavigateForward) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }
    
    /**
     * Navigate to editor with file selection fallback
     */
    fun navigateToEditor() {
        navigateToDestination(AppDestination.CODE_EDITOR)
    }
    
    /**
     * Navigate to file explorer for file selection
     */
    fun navigateToFileExplorer() {
        navigateToDestination(AppDestination.FILE_EXPLORER)
    }
    
    /**
     * Navigate to editor with specific file
     */
    @Suppress("UNUSED_PARAMETER")
    fun navigateToEditorWithFile(filePath: String) {
        scope.launch {
            navigateInternal(
                destination = AppDestination.CODE_EDITOR,
                fromDeepLink = true
            )
            _navigationCommands.emit(NavigationCommand.OpenEditor(filePath))
        }
    }
    
    /**
     * Navigate to project with specific ID
     */
    @Suppress("UNUSED_PARAMETER")
    fun navigateToProject(projectId: String) {
        scope.launch {
            navigateInternal(
                destination = AppDestination.PROJECT_SELECTION,
                fromDeepLink = true
            )
            _navigationCommands.emit(NavigationCommand.OpenProject(projectId))
        }
    }
    
    /**
     * Navigate to AI chat with context
     */
    @Suppress("UNUSED_PARAMETER")
    fun navigateToChatWithContext(context: String) {
        scope.launch {
            navigateInternal(
                destination = AppDestination.AI_CHAT,
                fromDeepLink = true
            )
            _navigationCommands.emit(NavigationCommand.OpenChat(context))
        }
    }
    
    /**
     * Clear navigation back stack
     */
    fun clearBackStack() {
        stateManager?.clearBackStack()
    }
    
    /**
     * Get navigation history for analytics
     */
    fun getNavigationHistory() = stateManager?.navigationHistory
    
    /**
     * Get navigation summary for debugging
     */
    fun getNavigationStateSummary() = stateManager?.getNavigationStateSummary()
    
    /**
     * Handle navigation events
     */
    fun handleNavigationEvent(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToDestination -> navigateToDestination(event.destination)
            is NavigationEvent.NavigateToPage -> navigateToPage(event.page)
            NavigationEvent.NavigateBack -> navigateBack()
            NavigationEvent.NavigateForward -> navigateForward()
        }
    }
}
