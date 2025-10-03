package com.pocketcode.features.settings.model

import com.pocketcode.core.ui.theme.ThemeMode

enum class FontSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
}

enum class AIProvider {
    OPENAI_GPT4,
    GOOGLE_GEMINI,
    ANTHROPIC_CLAUDE
}

data class EditorSettings(
    val fontSize: FontSize = FontSize.MEDIUM,
    val lineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val syntaxHighlighting: Boolean = true,
    val autoIndent: Boolean = true,
    val tabSize: Int = 4
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "en",
    val autoSave: Boolean = true,
    val showWelcomeScreen: Boolean = true,
    val animationsEnabled: Boolean = true,
    val hapticFeedback: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true
)

data class AISettings(
    val autoSuggestions: Boolean = true,
    val codeCompletion: Boolean = true,
    val contextualExplanations: Boolean = true,
    val provider: AIProvider = AIProvider.OPENAI_GPT4
)

data class ProjectSettings(
    val autoBackup: Boolean = true,
    val cloudSync: Boolean = false,
    val compression: Boolean = true
)

data class UserSettings(
    val editorSettings: EditorSettings = EditorSettings(),
    val appSettings: AppSettings = AppSettings(),
    val aiSettings: AISettings = AISettings(),
    val projectSettings: ProjectSettings = ProjectSettings()
)
