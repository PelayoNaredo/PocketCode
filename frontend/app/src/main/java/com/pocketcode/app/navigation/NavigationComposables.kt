package com.pocketcode.app.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.CoroutineScope

/**
 * Remember navigation manager with persistent state support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberNavigationManager(
    pagerState: PagerState,
    scope: CoroutineScope,
    stateManager: NavigationStateManager? = null
): NavigationManager {
    return remember(pagerState, scope, stateManager) {
        NavigationManager(pagerState, scope, stateManager)
    }
}

/**
 * Remember navigation state manager with Hilt injection
 */
@Composable
fun rememberNavigationStateManager(): NavigationStateManager {
    return hiltViewModel<NavigationStateManager>()
}

/**
 * Navigation composable provider for dependency injection
 */
@Composable
fun ProvideNavigationManagers(
    content: @Composable () -> Unit
) {
    // Navigation managers are provided through Hilt
    // This composable can be used for additional setup if needed
    content()
}