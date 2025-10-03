package com.pocketcode.features.settings.analytics

enum class SettingsTelemetrySurface {
    GENERAL,
    EDITOR,
    AI,
    PROJECT
}

enum class SettingsChangeType {
    TOGGLE,
    OPTION,
    ACTION
}

interface SettingsTelemetry {
    suspend fun trackSettingChanged(
        surface: SettingsTelemetrySurface,
        settingKey: String,
        changeType: SettingsChangeType,
        value: String
    )

    suspend fun applyPrivacyToggles(
        analyticsEnabled: Boolean,
        crashReportingEnabled: Boolean
    )
}
