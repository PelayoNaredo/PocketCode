package com.pocketcode.app.state

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * UI overlay states for the main app
 */
@Stable
data class OverlayState(
    val showSettings: Boolean = false,
    val showUserProfile: Boolean = false,
    val showNavigationDrawer: Boolean = false
)

/**
 * App state manager for managing global UI state and overlays
 */
@Stable
class AppStateManager {
    
    private var _overlayState by mutableStateOf(OverlayState())
    val overlayState: OverlayState get() = _overlayState
    
    /**
     * Show settings overlay
     */
    fun showSettings() {
        _overlayState = _overlayState.copy(
            showSettings = true,
            showUserProfile = false,
            showNavigationDrawer = false
        )
    }
    
    /**
     * Hide settings overlay
     */
    fun hideSettings() {
        _overlayState = _overlayState.copy(showSettings = false)
    }
    
    /**
     * Show user profile overlay
     */
    fun showUserProfile() {
        _overlayState = _overlayState.copy(
            showUserProfile = true,
            showSettings = false,
            showNavigationDrawer = false
        )
    }
    
    /**
     * Hide user profile overlay
     */
    fun hideUserProfile() {
        _overlayState = _overlayState.copy(showUserProfile = false)
    }
    
    /**
     * Show navigation drawer
     */
    fun showNavigationDrawer() {
        _overlayState = _overlayState.copy(
            showNavigationDrawer = true,
            showSettings = false,
            showUserProfile = false
        )
    }
    
    /**
     * Hide navigation drawer
     */
    fun hideNavigationDrawer() {
        _overlayState = _overlayState.copy(showNavigationDrawer = false)
    }
    
    /**
     * Hide all overlays
     */
    fun hideAllOverlays() {
        _overlayState = OverlayState()
    }
    
    /**
     * Check if any overlay is currently showing
     */
    val hasActiveOverlay: Boolean
        get() = _overlayState.showSettings || 
                _overlayState.showUserProfile || 
                _overlayState.showNavigationDrawer
}

/**
 * Remember app state manager instance
 */
@Composable
fun rememberAppStateManager(): AppStateManager {
    return remember { AppStateManager() }
}