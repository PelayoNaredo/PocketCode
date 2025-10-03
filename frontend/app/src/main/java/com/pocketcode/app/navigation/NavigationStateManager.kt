package com.pocketcode.app.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Navigation state holder and manager
 */
@HiltViewModel
class NavigationStateManager @Inject constructor(
    private val persistenceManager: NavigationPersistenceManager
) : ViewModel() {
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _currentDestination = MutableStateFlow(AppDestination.PROJECT_SELECTION)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()
    
    private val _navigationHistory = MutableStateFlow<List<AppDestination>>(emptyList())
    val navigationHistory: StateFlow<List<AppDestination>> = _navigationHistory.asStateFlow()
    
    private val _backStack = MutableStateFlow<List<AppDestination>>(emptyList())
    val backStack: StateFlow<List<AppDestination>> = _backStack.asStateFlow()
    
    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()
    
    private val _restorationCompleted = MutableStateFlow(false)
    val restorationCompleted: StateFlow<Boolean> = _restorationCompleted.asStateFlow()
    
    private val _lastNavigationTime = MutableStateFlow(0L)
    val lastNavigationTime: StateFlow<Long> = _lastNavigationTime.asStateFlow()
    
    // Navigation session tracking
    private val _sessionAnalytics = MutableStateFlow<NavigationSession?>(null)
    val sessionAnalytics: StateFlow<NavigationSession?> = _sessionAnalytics.asStateFlow()
    
    init {
        viewModelScope.launch {
            initializeNavigationState()
        }
        
        // Track session analytics
        viewModelScope.launch {
            persistenceManager.getSessionAnalytics().collect { session ->
                _sessionAnalytics.value = session
            }
        }
        
        // Track navigation history
        viewModelScope.launch {
            persistenceManager.getNavigationHistory().collect { history ->
                _navigationHistory.value = history
            }
        }
    }
    
    /**
     * Initialize navigation state from persistence or defaults
     */
    private suspend fun initializeNavigationState() {
        try {
            _isRestoring.value = true
            
            // Check if we should restore from saved state
            val shouldRestore = persistenceManager.shouldRestore.first()
            
            if (shouldRestore) {
                val restoreDestination = persistenceManager.getRestoreDestination()
                if (restoreDestination != null) {
                    _currentDestination.value = restoreDestination
                    
                    // Load saved navigation state
                    persistenceManager.currentNavigationState.first()?.let { state ->
                        val backStackDestinations = state.backStack.mapNotNull { destinationName ->
                            try {
                                AppDestination.valueOf(destinationName)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        _backStack.value = backStackDestinations
                    }
                    
                    // Mark restoration as completed
                    persistenceManager.markRestorationCompleted()
                    _restorationCompleted.value = true
                } else {
                    // No valid restore destination, use default
                    _currentDestination.value = AppDestination.PROJECT_SELECTION
                }
            } else {
                // Fresh start, use default destination
                _currentDestination.value = AppDestination.PROJECT_SELECTION
            }
            
            _lastNavigationTime.value = System.currentTimeMillis()
            
        } catch (e: Exception) {
            // Fallback to default destination if restoration fails
            _currentDestination.value = AppDestination.PROJECT_SELECTION
        } finally {
            _isRestoring.value = false
            _isInitialized.value = true
            
            // Save initial state
            saveCurrentNavigationState()
        }
    }
    
    /**
     * Navigate to a destination with state persistence
     */
    suspend fun navigateToDestination(
        destination: AppDestination,
        updateBackStack: Boolean = true,
        fromDeepLink: Boolean = false
    ) {
        if (!_isInitialized.value) {
            // Wait for initialization
            _isInitialized.first { it }
        }
        
        val previousDestination = _currentDestination.value
        
        // Update back stack if needed
        if (updateBackStack && previousDestination != destination) {
            val currentBackStack = _backStack.value.toMutableList()
            
            // Remove destination if already in stack to avoid duplicates
            currentBackStack.removeAll { it == destination }
            
            // Add previous destination to back stack
            if (previousDestination != destination) {
                currentBackStack.add(0, previousDestination)
            }
            
            // Limit back stack size
            if (currentBackStack.size > 10) {
                currentBackStack.removeAt(currentBackStack.size - 1)
            }
            
            _backStack.value = currentBackStack
        }
        
        // Update current destination
        _currentDestination.value = destination
        _lastNavigationTime.value = System.currentTimeMillis()
        
        // Record deep link if applicable
        if (fromDeepLink) {
            persistenceManager.recordDeepLinkEntry(destination.name)
        }
        
        // Save navigation state
        saveCurrentNavigationState(fromDeepLink)
        
    }

    /**
     * Update the current destination to keep pager state and persistence in sync
     */
    fun updateCurrentDestination(destination: AppDestination) {
        if (_currentDestination.value == destination) return

        _currentDestination.value = destination
        _lastNavigationTime.value = System.currentTimeMillis()
        saveCurrentNavigationState()
    }
    
    /**
     * Navigate back using back stack
     */
    suspend fun navigateBack(): Boolean {
        if (!_isInitialized.value) return false
        
        val currentBackStack = _backStack.value
        
        return if (currentBackStack.isNotEmpty()) {
            val previousDestination = currentBackStack[0]
            val newBackStack = currentBackStack.drop(1)
            
            _backStack.value = newBackStack
            _currentDestination.value = previousDestination
            _lastNavigationTime.value = System.currentTimeMillis()
            
            saveCurrentNavigationState()
            true
        } else {
            false
        }
    }
    
    /**
     * Clear back stack
     */
    fun clearBackStack() {
        _backStack.value = emptyList()
        saveCurrentNavigationState()
    }
    
    /**
     * Check if can navigate back
     */
    fun canNavigateBack(): Boolean {
        return _backStack.value.isNotEmpty()
    }
    
    /**
     * Get back stack depth
     */
    fun getBackStackDepth(): Int {
        return _backStack.value.size
    }
    
    /**
     * Save current navigation state to persistence
     */
    private fun saveCurrentNavigationState(fromDeepLink: Boolean = false) {
        viewModelScope.launch {
            persistenceManager.saveNavigationState(
                currentDestination = _currentDestination.value,
                backStack = _backStack.value,
                restoredFromDeepLink = fromDeepLink
            )
        }
    }
    
    /**
     * Enable or disable state restoration
     */
    fun setRestoreEnabled(enabled: Boolean) {
        persistenceManager.setRestoreEnabled(enabled)
    }
    
    /**
     * Clear all navigation data
     */
    fun clearNavigationData() {
        persistenceManager.clearNavigationData()
        _navigationHistory.value = emptyList()
        _backStack.value = emptyList()
        _sessionAnalytics.value = null
        _restorationCompleted.value = false
    }
    
    /**
     * Get frequently visited destinations
     */
    fun getFrequentDestinations(): Flow<List<AppDestination>> {
        return _navigationHistory.map { history ->
            history.groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(5)
                .map { it.first }
        }
    }
    
    /**
     * Get recent destinations (excluding current)
     */
    fun getRecentDestinations(): StateFlow<List<AppDestination>> {
        return _navigationHistory.map { history ->
            history.filter { it != _currentDestination.value }
                .take(3)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    /**
     * Force save current session
     */
    fun saveCurrentSession() {
        persistenceManager.saveCurrentSession()
    }
    
    /**
     * Get navigation state summary for debugging
     */
    fun getNavigationStateSummary(): NavigationStateSummary {
        return NavigationStateSummary(
            currentDestination = _currentDestination.value,
            backStackSize = _backStack.value.size,
            historySize = _navigationHistory.value.size,
            isInitialized = _isInitialized.value,
            isRestoring = _isRestoring.value,
            restorationCompleted = _restorationCompleted.value,
            lastNavigationTime = _lastNavigationTime.value,
            sessionId = _sessionAnalytics.value?.sessionId ?: "unknown"
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        // Save final navigation state when ViewModel is cleared
        viewModelScope.launch {
            saveCurrentNavigationState()
            persistenceManager.saveCurrentSession()
        }
    }
}

/**
 * Navigation state summary for debugging and analytics
 */
data class NavigationStateSummary(
    val currentDestination: AppDestination,
    val backStackSize: Int,
    val historySize: Int,
    val isInitialized: Boolean,
    val isRestoring: Boolean,
    val restorationCompleted: Boolean,
    val lastNavigationTime: Long,
    val sessionId: String
)

/**
 * Navigation state utilities
 */
object NavigationStateUtils {
    
    /**
     * Check if navigation state is healthy
     */
    fun isNavigationStateHealthy(summary: NavigationStateSummary): Boolean {
        return summary.isInitialized && 
               !summary.isRestoring &&
               summary.backStackSize < 20 &&
               summary.historySize < 100
    }
    
    /**
     * Get recommended action based on navigation state
     */
    fun getRecommendedAction(summary: NavigationStateSummary): String? {
        return when {
            !summary.isInitialized -> "Navigation not initialized"
            summary.isRestoring -> "Restoration in progress"
            summary.backStackSize > 15 -> "Consider clearing back stack"
            summary.historySize > 80 -> "Consider clearing history"
            else -> null
        }
    }
    
    /**
     * Format navigation time for display
     */
    fun formatNavigationTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 1000 -> "Just now"
            diff < 60 * 1000 -> "${diff / 1000}s ago"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        }
    }
}