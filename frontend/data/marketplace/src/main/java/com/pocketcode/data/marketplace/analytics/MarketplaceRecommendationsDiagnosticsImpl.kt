package com.pocketcode.data.marketplace.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pocketcode.domain.marketplace.analytics.MarketplaceRecommendationsDiagnostics
import com.pocketcode.domain.marketplace.analytics.RecommendationEvaluation
import com.pocketcode.domain.marketplace.analytics.RecommendationStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val RECOMMENDATION_DIAGNOSTICS_TAG = "MarketplaceRecDiag"

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

private object DiagnosticsPreferenceKeys {
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    val CRASH_REPORTING = booleanPreferencesKey("crash_reporting")
}

private data class DiagnosticsToggles(
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true
)

@Singleton
class MarketplaceRecommendationsDiagnosticsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MarketplaceRecommendationsDiagnostics {

    private val dataStore: DataStore<Preferences> by lazy { context.userPreferencesDataStore }

    private val analytics by lazy { initialiseFirebaseComponent { FirebaseAnalytics.getInstance(context) } }
    private val crashlytics by lazy { initialiseFirebaseComponent { FirebaseCrashlytics.getInstance() } }

    override suspend fun reportEvaluation(evaluation: RecommendationEvaluation) {
        val toggles = readToggles()

        if (toggles.analyticsEnabled) {
            analytics?.logEvent(
                "marketplace_recommendation_evaluation",
                Bundle().apply {
                    putInt("impressions", evaluation.impressions)
                    putInt("opens", evaluation.opens)
                    putDouble("conversion_rate", evaluation.conversionRate)
                    putString("status", evaluation.status.name.lowercase())
                }
            ) ?: Log.d(
                RECOMMENDATION_DIAGNOSTICS_TAG,
                "Firebase Analytics no disponible; omitiendo evaluación"
            )
        } else {
            Log.d(RECOMMENDATION_DIAGNOSTICS_TAG, "Analytics desactivado por el usuario")
        }

        if (toggles.crashReportingEnabled) {
            crashlytics?.apply {
                setCustomKey("marketplace_rec_impressions", evaluation.impressions)
                setCustomKey("marketplace_rec_opens", evaluation.opens)
                setCustomKey("marketplace_rec_conversion", evaluation.conversionRate)
                setCustomKey("marketplace_rec_status", evaluation.status.name)
                when (evaluation.status) {
                    RecommendationStatus.CRITICAL -> {
                        log("Conversión crítica en recomendaciones: ${evaluation.conversionRate}")
                        recordException(
                            RecommendationConversionException(evaluation.conversionRate)
                        )
                    }
                    RecommendationStatus.WARNING -> {
                        log("Conversión en observación: ${evaluation.conversionRate}")
                    }

                    RecommendationStatus.GOOD, RecommendationStatus.INSUFFICIENT_DATA -> {
                        // No-op
                    }
                }
            } ?: Log.d(
                RECOMMENDATION_DIAGNOSTICS_TAG,
                "Crashlytics no disponible; omitiendo registro"
            )
        } else {
            Log.d(RECOMMENDATION_DIAGNOSTICS_TAG, "Crash reporting desactivado por el usuario")
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

    private suspend fun readToggles(): DiagnosticsToggles {
        return runCatching {
            dataStore.data
                .map { preferences ->
                    DiagnosticsToggles(
                        analyticsEnabled = preferences[DiagnosticsPreferenceKeys.ANALYTICS_ENABLED] ?: true,
                        crashReportingEnabled = preferences[DiagnosticsPreferenceKeys.CRASH_REPORTING] ?: true
                    )
                }
                .first()
        }.getOrDefault(DiagnosticsToggles())
    }
}

private class RecommendationConversionException(conversionRate: Double) :
    RuntimeException("Conversión de recomendaciones por debajo del umbral crítico ($conversionRate)")
