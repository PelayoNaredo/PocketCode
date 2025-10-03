package com.pocketcode.app.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for navigation persistence
 */
private val Context.navigationDataStore by preferencesDataStore(
    name = "navigation_persistence"
)

/**
 * Serializable navigation state
 */
@Serializable
data class NavigationState(
    val currentDestination: String = AppDestination.PROJECT_SELECTION.name,
    val lastVisitedDestinations: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String = "",
    val backStack: List<String> = emptyList(),
    val restoredFromDeepLink: Boolean = false
)

/**
 * Navigation session data
 */
@Serializable
data class NavigationSession(
    val sessionId: String,
    val startTime: Long,
    val lastActiveTime: Long,
    val visitedDestinations: List<String>,
    val totalNavigations: Int,
    val deepLinkEntries: List<String>
)

/**
 * Navigation persistence repository
 */
@Singleton
class NavigationPersistenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val CURRENT_DESTINATION_KEY = stringPreferencesKey("current_destination")
        private val NAVIGATION_STATE_KEY = stringPreferencesKey("navigation_state")
        private val LAST_SESSION_KEY = stringPreferencesKey("last_session")
        private val NAVIGATION_HISTORY_KEY = stringPreferencesKey("navigation_history")
        private val RESTORE_ENABLED_KEY = booleanPreferencesKey("restore_enabled")
        private val SESSION_TIMEOUT_KEY = longPreferencesKey("session_timeout")
        
        private const val DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000L // 30 minutes
        private const val MAX_HISTORY_SIZE = 50
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Save current navigation state
     */
    suspend fun saveNavigationState(state: NavigationState) {
        try {
            context.navigationDataStore.edit { preferences ->
                preferences[CURRENT_DESTINATION_KEY] = state.currentDestination
                preferences[NAVIGATION_STATE_KEY] = json.encodeToString(state)
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    /**
     * Get current navigation state
     */
    fun getNavigationState(): Flow<NavigationState?> {
        return context.navigationDataStore.data
            .map { preferences ->
                try {
                    val stateJson = preferences[NAVIGATION_STATE_KEY]
                    if (stateJson != null) {
                        json.decodeFromString<NavigationState>(stateJson)
                    } else {
                        // Fallback to old format
                        val destination = preferences[CURRENT_DESTINATION_KEY]
                        if (destination != null) {
                            NavigationState(currentDestination = destination)
                        } else null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .catch { emit(null) }
    }
    
    /**
     * Save navigation session
     */
    suspend fun saveSession(session: NavigationSession) {
        try {
            context.navigationDataStore.edit { preferences ->
                preferences[LAST_SESSION_KEY] = json.encodeToString(session)
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    /**
     * Get last session
     */
    fun getLastSession(): Flow<NavigationSession?> {
        return context.navigationDataStore.data
            .map { preferences ->
                try {
                    val sessionJson = preferences[LAST_SESSION_KEY]
                    if (sessionJson != null) {
                        json.decodeFromString<NavigationSession>(sessionJson)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            .catch { emit(null) }
    }
    
    /**
     * Add destination to history
     */
    suspend fun addToHistory(destination: AppDestination) {
        try {
            context.navigationDataStore.edit { preferences ->
                val historyJson = preferences[NAVIGATION_HISTORY_KEY]
                val currentHistory = if (historyJson != null) {
                    try {
                        json.decodeFromString<List<String>>(historyJson)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                
                val updatedHistory = (listOf(destination.name) + currentHistory)
                    .distinct()
                    .take(MAX_HISTORY_SIZE)
                
                preferences[NAVIGATION_HISTORY_KEY] = json.encodeToString(updatedHistory)
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
    
    /**
     * Get navigation history
     */
    fun getNavigationHistory(): Flow<List<AppDestination>> {
        return context.navigationDataStore.data
            .map { preferences ->
                try {
                    val historyJson = preferences[NAVIGATION_HISTORY_KEY]
                    if (historyJson != null) {
                        val history = json.decodeFromString<List<String>>(historyJson)
                        history.mapNotNull { destinationName ->
                            try {
                                AppDestination.valueOf(destinationName)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            .catch { emit(emptyList()) }
    }
    
    /**
     * Check if restore is enabled
     */
    fun isRestoreEnabled(): Flow<Boolean> {
        return context.navigationDataStore.data
            .map { preferences ->
                preferences[RESTORE_ENABLED_KEY] ?: true
            }
            .catch { emit(true) }
    }
    
    /**
     * Set restore enabled
     */
    suspend fun setRestoreEnabled(enabled: Boolean) {
        context.navigationDataStore.edit { preferences ->
            preferences[RESTORE_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Get session timeout
     */
    fun getSessionTimeout(): Flow<Long> {
        return context.navigationDataStore.data
            .map { preferences ->
                preferences[SESSION_TIMEOUT_KEY] ?: DEFAULT_SESSION_TIMEOUT
            }
            .catch { emit(DEFAULT_SESSION_TIMEOUT) }
    }
    
    /**
     * Set session timeout
     */
    suspend fun setSessionTimeout(timeout: Long) {
        context.navigationDataStore.edit { preferences ->
            preferences[SESSION_TIMEOUT_KEY] = timeout
        }
    }
    
    /**
     * Clear all navigation data
     */
    suspend fun clearNavigationData() {
        context.navigationDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Check if session is valid (not expired)
     */
    suspend fun isSessionValid(): Boolean {
        return getLastSession().first()?.let { session ->
            val currentTime = System.currentTimeMillis()
            val sessionTimeout = getSessionTimeout().first()
            (currentTime - session.lastActiveTime) < sessionTimeout
        } ?: false
    }
}

/**
 * Navigation persistence manager - manages persistent navigation state
 */
@Singleton
class NavigationPersistenceManager @Inject constructor(
    private val repository: NavigationPersistenceRepository
) {
    
    private val currentSessionId = generateSessionId()
    private val sessionStartTime = System.currentTimeMillis()
    private var navigationCount = 0
    private val visitedDestinations = mutableSetOf<AppDestination>()
    private val deepLinkEntries = mutableListOf<String>()
    
    private val _currentNavigationState = MutableStateFlow<NavigationState?>(null)
    val currentNavigationState: StateFlow<NavigationState?> = _currentNavigationState.asStateFlow()
    
    private val _shouldRestore = MutableStateFlow(false)
    val shouldRestore: StateFlow<Boolean> = _shouldRestore.asStateFlow()
    
    private val _lastSession = MutableStateFlow<NavigationSession?>(null)
    val lastSession: StateFlow<NavigationSession?> = _lastSession.asStateFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            // Load saved state
            repository.getNavigationState().collect { state ->
                _currentNavigationState.value = state
            }
        }
        
        scope.launch {
            // Load last session
            repository.getLastSession().collect { session ->
                _lastSession.value = session
            }
        }
        
        scope.launch {
            // Check if we should restore state
            val restoreEnabled = repository.isRestoreEnabled().first()
            val sessionValid = repository.isSessionValid()
            _shouldRestore.value = restoreEnabled && sessionValid
        }
    }
    
    /**
     * Save current navigation state
     */
    fun saveNavigationState(
        currentDestination: AppDestination,
        backStack: List<AppDestination> = emptyList(),
        restoredFromDeepLink: Boolean = false
    ) {
        scope.launch {
            visitedDestinations.add(currentDestination)
            navigationCount++
            
            val state = NavigationState(
                currentDestination = currentDestination.name,
                lastVisitedDestinations = visitedDestinations.map { it.name },
                timestamp = System.currentTimeMillis(),
                sessionId = currentSessionId,
                backStack = backStack.map { it.name },
                restoredFromDeepLink = restoredFromDeepLink
            )
            
            repository.saveNavigationState(state)
            repository.addToHistory(currentDestination)
            _currentNavigationState.value = state
        }
    }
    
    /**
     * Save current session
     */
    fun saveCurrentSession() {
        scope.launch {
            val session = NavigationSession(
                sessionId = currentSessionId,
                startTime = sessionStartTime,
                lastActiveTime = System.currentTimeMillis(),
                visitedDestinations = visitedDestinations.map { it.name },
                totalNavigations = navigationCount,
                deepLinkEntries = deepLinkEntries.toList()
            )
            
            repository.saveSession(session)
            _lastSession.value = session
        }
    }
    
    /**
     * Record deep link entry
     */
    fun recordDeepLinkEntry(deepLink: String) {
        deepLinkEntries.add(deepLink)
    }
    
    /**
     * Get restore destination
     */
    suspend fun getRestoreDestination(): AppDestination? {
        return if (_shouldRestore.value) {
            _currentNavigationState.value?.let { state ->
                try {
                    AppDestination.valueOf(state.currentDestination)
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }
    
    /**
     * Get navigation history
     */
    fun getNavigationHistory(): Flow<List<AppDestination>> {
        return repository.getNavigationHistory()
    }
    
    /**
     * Mark restoration as completed
     */
    fun markRestorationCompleted() {
        _shouldRestore.value = false
    }
    
    /**
     * Enable or disable state restoration
     */
    fun setRestoreEnabled(enabled: Boolean) {
        scope.launch {
            repository.setRestoreEnabled(enabled)
        }
    }
    
    /**
     * Clear all saved navigation data
     */
    fun clearNavigationData() {
        scope.launch {
            repository.clearNavigationData()
            _currentNavigationState.value = null
            _lastSession.value = null
            _shouldRestore.value = false
        }
    }
    
    /**
     * Get session analytics
     */
    fun getSessionAnalytics(): Flow<NavigationSession?> {
        return repository.getLastSession()
    }
    
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
}