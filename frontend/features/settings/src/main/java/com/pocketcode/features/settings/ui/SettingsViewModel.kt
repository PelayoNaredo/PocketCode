package com.pocketcode.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.features.settings.analytics.SettingsChangeType
import com.pocketcode.features.settings.analytics.SettingsTelemetry
import com.pocketcode.features.settings.analytics.SettingsTelemetrySurface
import com.pocketcode.features.settings.model.AIProvider
import com.pocketcode.features.settings.model.AISettings
import com.pocketcode.features.settings.model.AppSettings
import com.pocketcode.features.settings.model.EditorSettings
import com.pocketcode.features.settings.model.FontSize
import com.pocketcode.features.settings.model.ProjectSettings
import com.pocketcode.core.ui.theme.ThemeMode
import com.pocketcode.features.settings.model.UserSettings
import com.pocketcode.features.settings.repository.SettingsDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userSettings: UserSettings = UserSettings(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val telemetry: SettingsTelemetry,
    private val settingsRepository: SettingsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            settingsRepository.userSettings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    userSettings = settings,
                    isLoading = false
                )
                syncPrivacyToggles(settings.appSettings)
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        val currentSettings = _uiState.value.userSettings
        val newAppSettings = currentSettings.appSettings.copy(themeMode = themeMode)
        val newUserSettings = currentSettings.copy(appSettings = newAppSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.GENERAL,
            settingKey = "theme_mode",
            changeType = SettingsChangeType.OPTION,
            value = themeMode.name.lowercase()
        )
    }

    fun updateFontSize(fontSize: FontSize) {
        val currentSettings = _uiState.value.userSettings
        val newEditorSettings = currentSettings.editorSettings.copy(fontSize = fontSize)
        val newUserSettings = currentSettings.copy(editorSettings = newEditorSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.EDITOR,
            settingKey = "font_size",
            changeType = SettingsChangeType.OPTION,
            value = fontSize.name.lowercase()
        )
    }

    fun updateLineNumbers(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newEditorSettings = currentSettings.editorSettings.copy(lineNumbers = enabled)
        val newUserSettings = currentSettings.copy(editorSettings = newEditorSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.EDITOR,
            settingKey = "line_numbers",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateWordWrap(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newEditorSettings = currentSettings.editorSettings.copy(wordWrap = enabled)
        val newUserSettings = currentSettings.copy(editorSettings = newEditorSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.EDITOR,
            settingKey = "word_wrap",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateSyntaxHighlighting(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newEditorSettings = currentSettings.editorSettings.copy(syntaxHighlighting = enabled)
        val newUserSettings = currentSettings.copy(editorSettings = newEditorSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.EDITOR,
            settingKey = "syntax_highlighting",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateAutoSave(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newAppSettings = currentSettings.appSettings.copy(autoSave = enabled)
        val newUserSettings = currentSettings.copy(appSettings = newAppSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.GENERAL,
            settingKey = "auto_save",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newAppSettings = currentSettings.appSettings.copy(animationsEnabled = enabled)
        val newUserSettings = currentSettings.copy(appSettings = newAppSettings)

        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.GENERAL,
            settingKey = "animations_enabled",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateHapticFeedback(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newAppSettings = currentSettings.appSettings.copy(hapticFeedback = enabled)
        val newUserSettings = currentSettings.copy(appSettings = newAppSettings)

        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.GENERAL,
            settingKey = "haptic_feedback",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateAnalyticsEnabled(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newAppSettings = currentSettings.appSettings.copy(analyticsEnabled = enabled)
        val newUserSettings = currentSettings.copy(appSettings = newAppSettings)

        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.GENERAL,
            settingKey = "analytics_enabled",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
        syncPrivacyToggles(newAppSettings)
    }

    fun updateCrashReportingEnabled(enabled: Boolean) {
        val currentSettings = _uiState.value.userSettings
        val newAppSettings = currentSettings.appSettings.copy(crashReportingEnabled = enabled)
        val newUserSettings = currentSettings.copy(appSettings = newAppSettings)

        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.GENERAL,
            settingKey = "crash_reporting_enabled",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
        syncPrivacyToggles(newAppSettings)
    }

    fun updateTabSize(size: Int) {
        val currentSettings = _uiState.value.userSettings
        val newEditorSettings = currentSettings.editorSettings.copy(tabSize = size)
        val newUserSettings = currentSettings.copy(editorSettings = newEditorSettings)
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        logSettingChange(
            surface = SettingsTelemetrySurface.EDITOR,
            settingKey = "tab_size",
            changeType = SettingsChangeType.OPTION,
            value = size.toString()
        )
    }

    fun updateAutoSuggestions(enabled: Boolean) {
        updateAISettings { it.copy(autoSuggestions = enabled) }
        logSettingChange(
            surface = SettingsTelemetrySurface.AI,
            settingKey = "auto_suggestions",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateCodeCompletion(enabled: Boolean) {
        updateAISettings { it.copy(codeCompletion = enabled) }
        logSettingChange(
            surface = SettingsTelemetrySurface.AI,
            settingKey = "code_completion",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateContextualExplanations(enabled: Boolean) {
        updateAISettings { it.copy(contextualExplanations = enabled) }
        logSettingChange(
            surface = SettingsTelemetrySurface.AI,
            settingKey = "contextual_explanations",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateAIProvider(provider: AIProvider) {
        updateAISettings { it.copy(provider = provider) }
        logSettingChange(
            surface = SettingsTelemetrySurface.AI,
            settingKey = "ai_provider",
            changeType = SettingsChangeType.OPTION,
            value = provider.name.lowercase()
        )
    }

    fun updateAutoBackup(enabled: Boolean) {
        updateProjectSettings { it.copy(autoBackup = enabled) }
        logSettingChange(
            surface = SettingsTelemetrySurface.PROJECT,
            settingKey = "auto_backup",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateCloudSync(enabled: Boolean) {
        updateProjectSettings { it.copy(cloudSync = enabled) }
        logSettingChange(
            surface = SettingsTelemetrySurface.PROJECT,
            settingKey = "cloud_sync",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    fun updateCompression(enabled: Boolean) {
        updateProjectSettings { it.copy(compression = enabled) }
        logSettingChange(
            surface = SettingsTelemetrySurface.PROJECT,
            settingKey = "compression",
            changeType = SettingsChangeType.TOGGLE,
            value = enabled.toEnabledDisabled()
        )
    }

    private fun updateAISettings(transform: (AISettings) -> AISettings) {
        val currentSettings = _uiState.value.userSettings
        val newAiSettings = transform(currentSettings.aiSettings)
        val newUserSettings = currentSettings.copy(aiSettings = newAiSettings)
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
    }

    private fun updateProjectSettings(transform: (ProjectSettings) -> ProjectSettings) {
        val currentSettings = _uiState.value.userSettings
        val newProjectSettings = transform(currentSettings.projectSettings)
        val newUserSettings = currentSettings.copy(projectSettings = newProjectSettings)
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
    }

    private fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            try {
                settingsRepository.saveSettings(settings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al guardar configuración: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun applyAllSettings(formData: Map<String, Any>) {
        // Apply all settings from form data at once
        // This is used when FormContainer submits all data together
        var newUserSettings = _uiState.value.userSettings
        
        formData.forEach { (key, value) ->
            when (key) {
                "fontSize" -> if (value is FontSize) {
                    newUserSettings = newUserSettings.copy(
                        editorSettings = newUserSettings.editorSettings.copy(fontSize = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.EDITOR,
                        settingKey = "font_size",
                        changeType = SettingsChangeType.OPTION,
                        value = value.name.lowercase()
                    )
                }
                "lineNumbers" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        editorSettings = newUserSettings.editorSettings.copy(lineNumbers = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.EDITOR,
                        settingKey = "line_numbers",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "wordWrap" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        editorSettings = newUserSettings.editorSettings.copy(wordWrap = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.EDITOR,
                        settingKey = "word_wrap",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "syntaxHighlighting" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        editorSettings = newUserSettings.editorSettings.copy(syntaxHighlighting = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.EDITOR,
                        settingKey = "syntax_highlighting",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "tabSize" -> if (value is Int) {
                    newUserSettings = newUserSettings.copy(
                        editorSettings = newUserSettings.editorSettings.copy(tabSize = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.EDITOR,
                        settingKey = "tab_size",
                        changeType = SettingsChangeType.OPTION,
                        value = value.toString()
                    )
                }
                "themeMode" -> if (value is ThemeMode) {
                    newUserSettings = newUserSettings.copy(
                        appSettings = newUserSettings.appSettings.copy(themeMode = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.GENERAL,
                        settingKey = "theme_mode",
                        changeType = SettingsChangeType.OPTION,
                        value = value.name.lowercase()
                    )
                }
                "autoSave" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        appSettings = newUserSettings.appSettings.copy(autoSave = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.GENERAL,
                        settingKey = "auto_save",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "analyticsEnabled" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        appSettings = newUserSettings.appSettings.copy(analyticsEnabled = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.GENERAL,
                        settingKey = "analytics_enabled",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "animationsEnabled" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        appSettings = newUserSettings.appSettings.copy(animationsEnabled = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.GENERAL,
                        settingKey = "animations_enabled",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "hapticFeedback" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        appSettings = newUserSettings.appSettings.copy(hapticFeedback = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.GENERAL,
                        settingKey = "haptic_feedback",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "crashReportingEnabled" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        appSettings = newUserSettings.appSettings.copy(crashReportingEnabled = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.GENERAL,
                        settingKey = "crash_reporting_enabled",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "autoSuggestions" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        aiSettings = newUserSettings.aiSettings.copy(autoSuggestions = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.AI,
                        settingKey = "auto_suggestions",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "codeCompletion" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        aiSettings = newUserSettings.aiSettings.copy(codeCompletion = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.AI,
                        settingKey = "code_completion",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "contextualExplanations" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        aiSettings = newUserSettings.aiSettings.copy(contextualExplanations = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.AI,
                        settingKey = "contextual_explanations",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "aiProvider" -> if (value is AIProvider) {
                    newUserSettings = newUserSettings.copy(
                        aiSettings = newUserSettings.aiSettings.copy(provider = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.AI,
                        settingKey = "ai_provider",
                        changeType = SettingsChangeType.OPTION,
                        value = value.name.lowercase()
                    )
                }
                "autoBackup" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        projectSettings = newUserSettings.projectSettings.copy(autoBackup = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.PROJECT,
                        settingKey = "auto_backup",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "cloudSync" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        projectSettings = newUserSettings.projectSettings.copy(cloudSync = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.PROJECT,
                        settingKey = "cloud_sync",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
                "compression" -> if (value is Boolean) {
                    newUserSettings = newUserSettings.copy(
                        projectSettings = newUserSettings.projectSettings.copy(compression = value)
                    )
                    logSettingChange(
                        surface = SettingsTelemetrySurface.PROJECT,
                        settingKey = "compression",
                        changeType = SettingsChangeType.TOGGLE,
                        value = value.toEnabledDisabled()
                    )
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(userSettings = newUserSettings)
        saveSettings(newUserSettings)
        syncPrivacyToggles(newUserSettings.appSettings)
    }

    private fun syncPrivacyToggles(appSettings: AppSettings) {
        viewModelScope.launch {
            telemetry.applyPrivacyToggles(
                analyticsEnabled = appSettings.analyticsEnabled,
                crashReportingEnabled = appSettings.crashReportingEnabled
            )
        }
    }

    private fun Boolean.toEnabledDisabled(): String = if (this) "enabled" else "disabled"

    private fun logSettingChange(
        surface: SettingsTelemetrySurface,
        settingKey: String,
        changeType: SettingsChangeType,
        value: String
    ) {
        viewModelScope.launch {
            telemetry.trackSettingChanged(surface, settingKey, changeType, value)
        }
    }
}
