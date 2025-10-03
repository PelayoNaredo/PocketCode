package com.pocketcode.features.settings.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pocketcode.core.ui.theme.ThemeMode
import com.pocketcode.features.settings.model.AIProvider
import com.pocketcode.features.settings.model.AISettings
import com.pocketcode.features.settings.model.AppSettings
import com.pocketcode.features.settings.model.EditorSettings
import com.pocketcode.features.settings.model.FontSize
import com.pocketcode.features.settings.model.ProjectSettings
import com.pocketcode.features.settings.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

interface SettingsDataSource {
    val userSettings: Flow<UserSettings>
    suspend fun saveSettings(settings: UserSettings)
    suspend fun updateThemeMode(themeMode: ThemeMode)
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsDataSource {
    private val dataStore = context.dataStore

    // Keys for AppSettings
    private object PreferenceKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val SHOW_WELCOME = booleanPreferencesKey("show_welcome_screen")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val CRASH_REPORTING = booleanPreferencesKey("crash_reporting_enabled")

        // EditorSettings
        val FONT_SIZE = stringPreferencesKey("font_size")
        val LINE_NUMBERS = booleanPreferencesKey("line_numbers")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val SYNTAX_HIGHLIGHTING = booleanPreferencesKey("syntax_highlighting")
        val AUTO_INDENT = booleanPreferencesKey("auto_indent")
        val TAB_SIZE = intPreferencesKey("tab_size")

        // AISettings
        val AI_AUTO_SUGGESTIONS = booleanPreferencesKey("ai_auto_suggestions")
        val AI_CODE_COMPLETION = booleanPreferencesKey("ai_code_completion")
        val AI_CONTEXTUAL_EXPLANATIONS = booleanPreferencesKey("ai_contextual_explanations")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")

        // ProjectSettings
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val CLOUD_SYNC = booleanPreferencesKey("cloud_sync")
        val COMPRESSION = booleanPreferencesKey("compression")
    }

    override val userSettings: Flow<UserSettings> = dataStore.data.map { preferences ->
        val themeModeValue = preferences[PreferenceKeys.THEME_MODE]
        val fontSizeValue = preferences[PreferenceKeys.FONT_SIZE]
        val aiProviderValue = preferences[PreferenceKeys.AI_PROVIDER]

        UserSettings(
            appSettings = AppSettings(
                themeMode = themeModeValue.parseEnumOrDefault(ThemeMode.SYSTEM),
                language = preferences[PreferenceKeys.LANGUAGE] ?: "en",
                autoSave = preferences[PreferenceKeys.AUTO_SAVE] ?: true,
                showWelcomeScreen = preferences[PreferenceKeys.SHOW_WELCOME] ?: true,
                animationsEnabled = preferences[PreferenceKeys.ANIMATIONS_ENABLED] ?: true,
                hapticFeedback = preferences[PreferenceKeys.HAPTIC_FEEDBACK] ?: true,
                analyticsEnabled = preferences[PreferenceKeys.ANALYTICS_ENABLED] ?: true,
                crashReportingEnabled = preferences[PreferenceKeys.CRASH_REPORTING] ?: true
            ),
            editorSettings = EditorSettings(
                fontSize = fontSizeValue.parseEnumOrDefault(FontSize.MEDIUM),
                lineNumbers = preferences[PreferenceKeys.LINE_NUMBERS] ?: true,
                wordWrap = preferences[PreferenceKeys.WORD_WRAP] ?: false,
                syntaxHighlighting = preferences[PreferenceKeys.SYNTAX_HIGHLIGHTING] ?: true,
                autoIndent = preferences[PreferenceKeys.AUTO_INDENT] ?: true,
                tabSize = preferences[PreferenceKeys.TAB_SIZE] ?: 4
            ),
            aiSettings = AISettings(
                autoSuggestions = preferences[PreferenceKeys.AI_AUTO_SUGGESTIONS] ?: true,
                codeCompletion = preferences[PreferenceKeys.AI_CODE_COMPLETION] ?: true,
                contextualExplanations = preferences[PreferenceKeys.AI_CONTEXTUAL_EXPLANATIONS] ?: true,
                provider = aiProviderValue.parseEnumOrDefault(AIProvider.OPENAI_GPT4)
            ),
            projectSettings = ProjectSettings(
                autoBackup = preferences[PreferenceKeys.AUTO_BACKUP] ?: true,
                cloudSync = preferences[PreferenceKeys.CLOUD_SYNC] ?: false,
                compression = preferences[PreferenceKeys.COMPRESSION] ?: true
            )
        )
    }

    override suspend fun saveSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            // AppSettings
            preferences[PreferenceKeys.THEME_MODE] = settings.appSettings.themeMode.name
            preferences[PreferenceKeys.LANGUAGE] = settings.appSettings.language
            preferences[PreferenceKeys.AUTO_SAVE] = settings.appSettings.autoSave
            preferences[PreferenceKeys.SHOW_WELCOME] = settings.appSettings.showWelcomeScreen
            preferences[PreferenceKeys.ANIMATIONS_ENABLED] = settings.appSettings.animationsEnabled
            preferences[PreferenceKeys.HAPTIC_FEEDBACK] = settings.appSettings.hapticFeedback
            preferences[PreferenceKeys.ANALYTICS_ENABLED] = settings.appSettings.analyticsEnabled
            preferences[PreferenceKeys.CRASH_REPORTING] = settings.appSettings.crashReportingEnabled

            // EditorSettings
            preferences[PreferenceKeys.FONT_SIZE] = settings.editorSettings.fontSize.name
            preferences[PreferenceKeys.LINE_NUMBERS] = settings.editorSettings.lineNumbers
            preferences[PreferenceKeys.WORD_WRAP] = settings.editorSettings.wordWrap
            preferences[PreferenceKeys.SYNTAX_HIGHLIGHTING] = settings.editorSettings.syntaxHighlighting
            preferences[PreferenceKeys.AUTO_INDENT] = settings.editorSettings.autoIndent
            preferences[PreferenceKeys.TAB_SIZE] = settings.editorSettings.tabSize

            // AISettings
            preferences[PreferenceKeys.AI_AUTO_SUGGESTIONS] = settings.aiSettings.autoSuggestions
            preferences[PreferenceKeys.AI_CODE_COMPLETION] = settings.aiSettings.codeCompletion
            preferences[PreferenceKeys.AI_CONTEXTUAL_EXPLANATIONS] = settings.aiSettings.contextualExplanations
            preferences[PreferenceKeys.AI_PROVIDER] = settings.aiSettings.provider.name

            // ProjectSettings
            preferences[PreferenceKeys.AUTO_BACKUP] = settings.projectSettings.autoBackup
            preferences[PreferenceKeys.CLOUD_SYNC] = settings.projectSettings.cloudSync
            preferences[PreferenceKeys.COMPRESSION] = settings.projectSettings.compression
        }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = themeMode.name
        }
    }
}

private inline fun <reified T : Enum<T>> String?.parseEnumOrDefault(default: T): T {
    val raw = this ?: return default
    return runCatching { enumValueOf<T>(raw) }
        .recoverCatching { enumValueOf<T>(raw.uppercase()) }
        .getOrDefault(default)
}
