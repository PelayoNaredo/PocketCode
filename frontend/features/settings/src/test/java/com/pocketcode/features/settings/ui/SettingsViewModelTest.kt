package com.pocketcode.features.settings.ui

import com.pocketcode.core.ui.theme.ThemeMode
import com.pocketcode.features.settings.analytics.SettingsChangeType
import com.pocketcode.features.settings.analytics.SettingsTelemetry
import com.pocketcode.features.settings.analytics.SettingsTelemetrySurface
import com.pocketcode.features.settings.model.UserSettings
import com.pocketcode.features.settings.repository.SettingsDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updateAutoSave_emitsTelemetry() = runTest(mainDispatcherRule.dispatcher) {
        val telemetry = FakeSettingsTelemetry()
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(telemetry, repository)

        viewModel.updateAutoSave(false)
        advanceUntilIdle()

        assertEquals(
            listOf(
                RecordedChange(
                    surface = SettingsTelemetrySurface.GENERAL,
                    settingKey = "auto_save",
                    changeType = SettingsChangeType.TOGGLE,
                    value = "disabled"
                )
            ),
            telemetry.recorded
        )
    }

    @Test
    fun updateAnalyticsEnabled_syncsPrivacyToggles() = runTest(mainDispatcherRule.dispatcher) {
        val telemetry = FakeSettingsTelemetry()
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(telemetry, repository)

        advanceUntilIdle()
        telemetry.clear()

        viewModel.updateAnalyticsEnabled(false)
        advanceUntilIdle()

        assertEquals(
            listOf(
                RecordedChange(
                    surface = SettingsTelemetrySurface.GENERAL,
                    settingKey = "analytics_enabled",
                    changeType = SettingsChangeType.TOGGLE,
                    value = "disabled"
                )
            ),
            telemetry.recorded
        )
        assertEquals(false to true, telemetry.privacyToggles.lastOrNull())
    }

    private class FakeSettingsTelemetry : SettingsTelemetry {
        val recorded = mutableListOf<RecordedChange>()
        val privacyToggles = mutableListOf<Pair<Boolean, Boolean>>()

        fun clear() {
            recorded.clear()
            privacyToggles.clear()
        }

        override suspend fun trackSettingChanged(
            surface: SettingsTelemetrySurface,
            settingKey: String,
            changeType: SettingsChangeType,
            value: String
        ) {
            recorded += RecordedChange(surface, settingKey, changeType, value)
        }

        override suspend fun applyPrivacyToggles(
            analyticsEnabled: Boolean,
            crashReportingEnabled: Boolean
        ) {
            privacyToggles += analyticsEnabled to crashReportingEnabled
        }
    }

    private data class RecordedChange(
        val surface: SettingsTelemetrySurface,
        val settingKey: String,
        val changeType: SettingsChangeType,
        val value: String
    )

    private class FakeSettingsRepository : SettingsDataSource {
        private val state = MutableStateFlow(UserSettings())

        override val userSettings = state

        override suspend fun saveSettings(settings: UserSettings) {
            state.value = settings
        }

        override suspend fun updateThemeMode(themeMode: ThemeMode) {
            state.value = state.value.copy(
                appSettings = state.value.appSettings.copy(themeMode = themeMode)
            )
        }
    }
}
