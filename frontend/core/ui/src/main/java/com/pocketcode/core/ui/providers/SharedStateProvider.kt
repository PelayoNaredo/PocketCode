package com.pocketcode.core.ui.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.pocketcode.core.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Shared configuration for app-wide settings exposed through CompositionLocals.
 * This lightweight implementation focuses on theme/editor/app configurations so that
 * feature modules can read/write shared state without tight coupling.
 */
data class ThemeConfiguration(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val isDarkTheme: Boolean = false
)

data class EditorConfiguration(
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val syntaxHighlighting: Boolean = true
)

data class AppConfiguration(
    val language: String = "en",
    val autoSave: Boolean = true,
    val analyticsEnabled: Boolean = true
)

class ThemeConfigurationManager {
    private val _configuration = MutableStateFlow(ThemeConfiguration())
    val configuration: StateFlow<ThemeConfiguration> = _configuration.asStateFlow()

    suspend fun updateThemeMode(mode: ThemeMode) {
        val current = _configuration.value
        val isDark = when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> current.isDarkTheme
        }
        _configuration.emit(current.copy(mode = mode, isDarkTheme = isDark))
    }

    suspend fun updateConfiguration(configuration: ThemeConfiguration) {
        _configuration.emit(configuration)
    }
}

class EditorConfigurationManager {
    private val _configuration = MutableStateFlow(EditorConfiguration())
    val configuration: StateFlow<EditorConfiguration> = _configuration.asStateFlow()

    suspend fun updateConfiguration(configuration: EditorConfiguration) {
        _configuration.emit(configuration)
    }
}

class AppConfigurationManager {
    private val _configuration = MutableStateFlow(AppConfiguration())
    val configuration: StateFlow<AppConfiguration> = _configuration.asStateFlow()

    suspend fun updateConfiguration(configuration: AppConfiguration) {
        _configuration.emit(configuration)
    }
}

val LocalThemeConfigurationManager = compositionLocalOf<ThemeConfigurationManager> {
    error("ThemeConfigurationManager no disponible. Envuelve tu jerarquía en SharedStateProvider.")
}

val LocalEditorConfigurationManager = compositionLocalOf<EditorConfigurationManager> {
    error("EditorConfigurationManager no disponible. Envuelve tu jerarquía en SharedStateProvider.")
}

val LocalAppConfigurationManager = compositionLocalOf<AppConfigurationManager> {
    error("AppConfigurationManager no disponible. Envuelve tu jerarquía en SharedStateProvider.")
}

@Composable
fun SharedStateProvider(content: @Composable () -> Unit) {
    val themeManager = remember { ThemeConfigurationManager() }
    val editorManager = remember { EditorConfigurationManager() }
    val appManager = remember { AppConfigurationManager() }

    CompositionLocalProvider(
        LocalThemeConfigurationManager provides themeManager,
        LocalEditorConfigurationManager provides editorManager,
        LocalAppConfigurationManager provides appManager
    ) {
        content()
    }
}

@Composable
fun rememberThemeConfigurationManager(): ThemeConfigurationManager = LocalThemeConfigurationManager.current

@Composable
fun rememberEditorConfigurationManager(): EditorConfigurationManager = LocalEditorConfigurationManager.current

@Composable
fun rememberAppConfigurationManager(): AppConfigurationManager = LocalAppConfigurationManager.current

@Composable
fun ConfigurationWatcher(
    onThemeChange: (ThemeConfiguration) -> Unit = {},
    onEditorChange: (EditorConfiguration) -> Unit = {},
    onAppChange: (AppConfiguration) -> Unit = {}
) {
    val themeManager = rememberThemeConfigurationManager()
    val editorManager = rememberEditorConfigurationManager()
    val appManager = rememberAppConfigurationManager()

    val themeCallback = rememberUpdatedState(onThemeChange)
    val editorCallback = rememberUpdatedState(onEditorChange)
    val appCallback = rememberUpdatedState(onAppChange)

    LaunchedEffect(themeManager) {
        themeManager.configuration.collectLatest { configuration ->
            themeCallback.value(configuration)
        }
    }

    LaunchedEffect(editorManager) {
        editorManager.configuration.collectLatest { configuration ->
            editorCallback.value(configuration)
        }
    }

    LaunchedEffect(appManager) {
        appManager.configuration.collectLatest { configuration ->
            appCallback.value(configuration)
        }
    }
}
