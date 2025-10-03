package com.pocketcode.app.ui

import android.content.Context
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.features.project.ui.selection.Project
import com.pocketcode.core.ui.components.feedback.PocketToastDuration
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import com.pocketcode.core.ui.snackbar.GlobalSnackbarDuration
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.GlobalToastEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AppStateViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _currentFile = MutableStateFlow<ProjectFile?>(null)
    val currentFile: StateFlow<ProjectFile?> = _currentFile.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _navigationEvents = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvents: StateFlow<NavigationEvent?> = _navigationEvents.asStateFlow()

    private val _chatContext = MutableStateFlow<String?>(null)
    val chatContext: StateFlow<String?> = _chatContext.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<GlobalSnackbarEvent>(extraBufferCapacity = 1)
    val snackbarEvents: SharedFlow<GlobalSnackbarEvent> = _snackbarEvents.asSharedFlow()

    private val _toastEvents = MutableSharedFlow<GlobalToastEvent>(extraBufferCapacity = 1)
    val toastEvents: SharedFlow<GlobalToastEvent> = _toastEvents.asSharedFlow()

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_preferences"
    )

    private val dataStore: DataStore<Preferences> by lazy { context.userPreferencesDataStore }

    private val analytics: FirebaseAnalytics? by lazy {
        initialiseFirebaseComponent { FirebaseAnalytics.getInstance(context) }
    }
    private val crashlytics: FirebaseCrashlytics? by lazy {
        initialiseFirebaseComponent { FirebaseCrashlytics.getInstance() }
    }

    fun selectProject(project: Project) {
        viewModelScope.launch {
            _currentProject.value = project
            _currentFile.value = null
            // Auto-navigate to file explorer
            navigateToPage(1)
        }
    }

    fun selectFile(file: ProjectFile) {
        viewModelScope.launch {
            _currentFile.value = file
            // Auto-navigate to code editor
            navigateToPage(2)
        }
    }

    fun selectProjectById(projectId: String) {
        viewModelScope.launch {
            val current = _currentProject.value
            if (current?.id != projectId) {
                val existing = current?.takeIf { it.id == projectId }
                val projectPath = File(context.filesDir, "projects/$projectId").absolutePath
                _currentProject.value = Project(
                    id = projectId,
                    name = existing?.name ?: projectId,
                    localPath = existing?.localPath ?: projectPath,
                    description = existing?.description ?: "Deep link project",
                    lastModified = existing?.lastModified ?: "Recently",
                    icon = existing?.icon ?: Icons.Default.Folder,
                    isRecent = true
                )
                _currentFile.value = null
            }
            navigateToPage(0)
        }
    }

    fun selectFileByPath(filePath: String) {
        viewModelScope.launch {
            val trimmedPath = filePath.trim()
            if (trimmedPath.isEmpty()) return@launch

            val normalizedForName = trimmedPath.replace('\\', '/').trimEnd('/')
            val fileName = normalizedForName.substringAfterLast('/', normalizedForName)
            val resolvedName = fileName.ifBlank { trimmedPath }

            val projectFile = ProjectFile(
                name = resolvedName,
                path = trimmedPath,
                isDirectory = false
            )

            _currentFile.value = projectFile
            navigateToPage(2)
        }
    }

    fun navigateToPage(page: Int) {
        viewModelScope.launch {
            _currentPage.value = page
            _navigationEvents.value = NavigationEvent.NavigateToPage(page)
        }
    }

    fun clearNavigationEvent() {
        _navigationEvents.value = null
    }

    fun navigateToChat() {
        navigateToPage(3)
    }

    fun navigateToPreview() {
        navigateToPage(4)
    }

    fun applyChatContext(context: String) {
        viewModelScope.launch {
            _chatContext.value = context
            navigateToPage(3)
        }
    }

    fun clearChatContext() {
        _chatContext.value = null
    }

    fun showSnackbar(event: GlobalSnackbarEvent) {
        viewModelScope.launch {
            _snackbarEvents.emit(event)
            logSnackbarTelemetry(event)
        }
    }

    fun showSnackbarMessage(
        message: String,
        origin: GlobalSnackbarOrigin = GlobalSnackbarOrigin.SHELL,
        severity: GlobalSnackbarSeverity = GlobalSnackbarSeverity.INFO,
        duration: GlobalSnackbarDuration = GlobalSnackbarDuration.SHORT
    ) {
        showSnackbar(
            GlobalSnackbarEvent(
                message = message,
                origin = origin,
                severity = severity,
                duration = duration
            )
        )
    }

    fun showToast(event: GlobalToastEvent) {
        viewModelScope.launch {
            _toastEvents.emit(event)
            logToastTelemetry(event)
        }
    }

    fun showToastMessage(
        message: String,
        style: PocketToastStyle = PocketToastStyle.Info,
        origin: GlobalSnackbarOrigin = GlobalSnackbarOrigin.SHELL,
        duration: PocketToastDuration = PocketToastDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showToast(
            GlobalToastEvent(
                message = message,
                style = style,
                origin = origin,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }

    private fun <T> initialiseFirebaseComponent(factory: () -> T): T? {
        return runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            factory()
        }.getOrNull()
    }

    private suspend fun logSnackbarTelemetry(event: GlobalSnackbarEvent) {
        val toggles = readTelemetryToggles()

        if (toggles.analyticsEnabled) {
            analytics?.logEvent("shell_snackbar_shown", Bundle().apply {
                putString("origin", event.origin.name.lowercase())
                putString("severity", event.severity.name.lowercase())
                putString("duration", event.duration.name.lowercase())
                putString("analytics_id", event.analyticsId)
                putInt("has_action", if (event.actionLabel != null) 1 else 0)
            })
        }

        if (toggles.crashReportingEnabled && event.severity == GlobalSnackbarSeverity.ERROR) {
            crashlytics?.apply {
                log("Snackbar error: ${event.message}")
                setCustomKey("snackbar_origin", event.origin.name)
                setCustomKey("snackbar_analytics_id", event.analyticsId)
                setCustomKey("snackbar_has_action", event.actionLabel != null)
            }
        }
    }

    private suspend fun logToastTelemetry(event: GlobalToastEvent) {
        val toggles = readTelemetryToggles()

        if (toggles.analyticsEnabled) {
            analytics?.logEvent("shell_toast_shown", Bundle().apply {
                putString("origin", event.origin.name.lowercase())
                putString("style", event.style.name.lowercase())
                putString("duration", event.duration.name.lowercase())
                putString("analytics_id", event.analyticsId)
                putInt("has_action", if (event.actionLabel != null) 1 else 0)
            })
        }

        if (toggles.crashReportingEnabled && event.style == PocketToastStyle.Error) {
            crashlytics?.apply {
                log("Toast error: ${event.message}")
                setCustomKey("toast_origin", event.origin.name)
                setCustomKey("toast_analytics_id", event.analyticsId)
                setCustomKey("toast_has_action", event.actionLabel != null)
            }
        }
    }

    private suspend fun readTelemetryToggles(): TelemetryToggles {
        return runCatching {
            dataStore.data
                .map { preferences ->
                    TelemetryToggles(
                        analyticsEnabled = preferences[TelemetryPreferenceKeys.ANALYTICS_ENABLED] ?: true,
                        crashReportingEnabled = preferences[TelemetryPreferenceKeys.CRASH_REPORTING] ?: true
                    )
                }
                .first()
        }.getOrDefault(TelemetryToggles())
    }

    private object TelemetryPreferenceKeys {
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val CRASH_REPORTING = booleanPreferencesKey("crash_reporting")
    }

    private data class TelemetryToggles(
        val analyticsEnabled: Boolean = true,
        val crashReportingEnabled: Boolean = true
    )
}

sealed class NavigationEvent {
    data class NavigateToPage(val page: Int) : NavigationEvent()
}