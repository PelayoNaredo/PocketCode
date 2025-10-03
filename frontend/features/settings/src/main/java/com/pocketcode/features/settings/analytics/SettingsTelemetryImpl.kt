package com.pocketcode.features.settings.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TAG = "SettingsTelemetry"

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

private object PreferenceKeys {
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    val CRASH_REPORTING = booleanPreferencesKey("crash_reporting")
}

private data class TelemetryToggles(
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true
)

@Singleton
class SettingsTelemetryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsTelemetry {

    private val dataStore: DataStore<Preferences> by lazy { context.userPreferencesDataStore }

    private val analytics by lazy {
        initialiseFirebaseComponent { FirebaseAnalytics.getInstance(context) }
    }

    private val crashlytics by lazy {
        initialiseFirebaseComponent { FirebaseCrashlytics.getInstance() }
    }

    override suspend fun trackSettingChanged(
        surface: SettingsTelemetrySurface,
        settingKey: String,
        changeType: SettingsChangeType,
        value: String
    ) {
        val toggles = readTelemetryToggles()

        if (toggles.analyticsEnabled) {
            analytics?.logEvent(
                "settings_change",
                Bundle().apply {
                    putString("surface", surface.name.lowercase())
                    putString("setting_key", settingKey)
                    putString("change_type", changeType.name.lowercase())
                    putString("value", value)
                }
            ) ?: Log.d(TAG, "Firebase Analytics no disponible; omitiendo evento settings_change")
        } else {
            Log.d(TAG, "Analytics desactivado por el usuario; omitiendo settings_change")
        }

        if (toggles.crashReportingEnabled) {
            crashlytics?.apply {
                log("settings_change:${surface.name}:${settingKey}=$value (${changeType.name})")
                setCustomKey("settings_last_surface", surface.name)
                setCustomKey("settings_last_key", settingKey)
                setCustomKey("settings_last_type", changeType.name)
                setCustomKey("settings_last_value", value)
            } ?: Log.d(TAG, "Crashlytics no disponible; omitiendo registro de settings_change")
        }
    }

    override suspend fun applyPrivacyToggles(
        analyticsEnabled: Boolean,
        crashReportingEnabled: Boolean
    ) {
        analytics?.setAnalyticsCollectionEnabled(analyticsEnabled)
        crashlytics?.setCrashlyticsCollectionEnabled(crashReportingEnabled)
        crashlytics?.setCustomKey(
            "settings_privacy_analytics_enabled",
            analyticsEnabled
        )
        crashlytics?.setCustomKey(
            "settings_privacy_crash_reporting_enabled",
            crashReportingEnabled
        )

        runCatching {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.ANALYTICS_ENABLED] = analyticsEnabled
                preferences[PreferenceKeys.CRASH_REPORTING] = crashReportingEnabled
            }
        }.onFailure { throwable ->
            Log.w(TAG, "No se pudo persistir las preferencias de privacidad", throwable)
        }
    }

    private fun <T> initialiseFirebaseComponent(factory: () -> T): T? {
        return runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            factory()
        }.getOrNull()
    }

    private suspend fun readTelemetryToggles(): TelemetryToggles {
        return runCatching {
            dataStore.data
                .map { preferences ->
                    TelemetryToggles(
                        analyticsEnabled = preferences[PreferenceKeys.ANALYTICS_ENABLED] ?: true,
                        crashReportingEnabled = preferences[PreferenceKeys.CRASH_REPORTING] ?: true
                    )
                }
                .first()
        }.getOrDefault(TelemetryToggles())
    }
}
