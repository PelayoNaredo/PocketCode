package com.pocketcode.app.ui.navigation

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box

enum class NavigationDirection {
    FORWARD,
    BACKWARD
}

data class NavigationTransition(
    val direction: NavigationDirection,
    val duration: Int,
    val easing: Easing
)

data class ScreenState(
    val id: String,
    val content: @Composable () -> Unit,
    val transition: NavigationTransition
)
@Suppress("UNUSED_PARAMETER")
@Composable
fun NavigationContainer(
    currentScreen: ScreenState,
    previousScreen: ScreenState?,
    isTransitioning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        currentScreen.content()
    }
}
