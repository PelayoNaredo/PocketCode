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
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncChannel
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncTelemetry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.util.ArrayList

private const val TAG = "MarketplaceSyncTelemetry"

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

private object PreferenceKeys {
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    val CRASH_REPORTING = booleanPreferencesKey("crash_reporting")
}

private data class TelemetryToggles(
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true
)

@Singleton
class MarketplaceMetricsSyncTelemetryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MarketplaceMetricsSyncTelemetry {

    private val dataStore: DataStore<Preferences> by lazy { context.userPreferencesDataStore }

    private val crashlytics by lazy {
        initialiseFirebaseComponent { FirebaseCrashlytics.getInstance() }
    }

    private val analytics by lazy {
        initialiseFirebaseComponent { FirebaseAnalytics.getInstance(context) }
    }

    override suspend fun trackSyncSuccess(
        snapshot: MarketplaceInteractionMetrics,
        attempt: Int,
        durationMillis: Long,
        channel: MarketplaceMetricsSyncChannel
    ) {
        val toggles = readTelemetryToggles()

        if (toggles.analyticsEnabled) {
            analytics?.logEvent(
                "marketplace_metrics_sync_success",
                Bundle().apply {
                    putString("channel", channel.name.lowercase())
                    putInt("attempt", attempt)
                    putLong("duration_ms", durationMillis)
                    putInt("search_interactions", snapshot.searchInteractions)
                    putInt("filter_selections", snapshot.filterSelections)
                    putInt("asset_opens", snapshot.assetOpens)
                    putInt("offline_retries", snapshot.offlineRetries)
                    putInt("recommendation_impressions", snapshot.recommendationImpressions)
                    putInt("recommendation_opens", snapshot.recommendationOpens)
                    if (snapshot.lastRecommendedAssetIds.isNotEmpty()) {
                        putStringArrayList(
                            "recommended_asset_ids",
                            ArrayList(snapshot.lastRecommendedAssetIds)
                        )
                    }
                    putLong("last_recommendation_generated_at", snapshot.lastRecommendationGeneratedAtMillis)
                }
            ) ?: Log.d(TAG, "Firebase Analytics no disponible; omitiendo evento de éxito")
        } else {
            Log.d(TAG, "Analytics desactivado por el usuario; omitiendo evento de éxito")
        }

        if (toggles.crashReportingEnabled) {
            crashlytics?.apply {
                log("Marketplace metrics sync success via $channel in $attempt attempt(s)")
                setCustomKey("marketplace_sync_channel", channel.name)
                setCustomKey("marketplace_sync_attempt", attempt)
                setCustomKey("marketplace_sync_duration_ms", durationMillis)
                setCustomKey("marketplace_metrics_search_interactions", snapshot.searchInteractions)
                setCustomKey("marketplace_metrics_filter_selections", snapshot.filterSelections)
                setCustomKey("marketplace_metrics_asset_opens", snapshot.assetOpens)
                setCustomKey("marketplace_metrics_offline_retries", snapshot.offlineRetries)
                setCustomKey("marketplace_metrics_recommendation_impressions", snapshot.recommendationImpressions)
                setCustomKey("marketplace_metrics_recommendation_opens", snapshot.recommendationOpens)
                if (snapshot.lastRecommendedAssetIds.isNotEmpty()) {
                    setCustomKey(
                        "marketplace_metrics_last_recommended_ids",
                        snapshot.lastRecommendedAssetIds.joinToString(",")
                    )
                }
                setCustomKey(
                    "marketplace_metrics_last_recommendation_at",
                    snapshot.lastRecommendationGeneratedAtMillis
                )
            } ?: Log.d(TAG, "Crashlytics no disponible; omitiendo registro de éxito")
        } else {
            Log.d(TAG, "Crash reporting desactivado por el usuario; omitiendo registro de éxito")
        }
    }

    override suspend fun trackSyncFailure(
        snapshot: MarketplaceInteractionMetrics?,
        attempt: Int,
        maxAttempts: Int,
        error: Throwable?,
        isTerminal: Boolean,
        channel: MarketplaceMetricsSyncChannel
    ) {
        val toggles = readTelemetryToggles()

        if (toggles.analyticsEnabled) {
            analytics?.logEvent(
                "marketplace_metrics_sync_failure",
                Bundle().apply {
                    putString("channel", channel.name.lowercase())
                    putInt("attempt", attempt)
                    putInt("max_attempts", maxAttempts)
                    putBoolean("is_terminal", isTerminal)
                    putBoolean("has_snapshot", snapshot != null)
                    snapshot?.let {
                        putInt("search_interactions", it.searchInteractions)
                        putInt("filter_selections", it.filterSelections)
                        putInt("asset_opens", it.assetOpens)
                        putInt("offline_retries", it.offlineRetries)
                        putInt("recommendation_impressions", it.recommendationImpressions)
                        putInt("recommendation_opens", it.recommendationOpens)
                        if (it.lastRecommendedAssetIds.isNotEmpty()) {
                            putStringArrayList(
                                "recommended_asset_ids",
                                ArrayList(it.lastRecommendedAssetIds)
                            )
                        }
                        putLong("last_recommendation_generated_at", it.lastRecommendationGeneratedAtMillis)
                    }
                    putString("error_type", error?.javaClass?.canonicalName ?: "unknown")
                }
            ) ?: Log.w(TAG, "Firebase Analytics no disponible; omitiendo evento de fallo", error)
        } else {
            Log.d(TAG, "Analytics desactivado por el usuario; omitiendo evento de fallo")
        }

        if (toggles.crashReportingEnabled) {
            crashlytics?.apply {
                log(
                    "Marketplace metrics sync failure via $channel (attempt $attempt/$maxAttempts, terminal=$isTerminal)"
                )
                setCustomKey("marketplace_sync_channel", channel.name)
                setCustomKey("marketplace_sync_attempt", attempt)
                setCustomKey("marketplace_sync_max_attempts", maxAttempts)
                setCustomKey("marketplace_sync_is_terminal", isTerminal)
                setCustomKey("marketplace_sync_has_snapshot", snapshot != null)
                snapshot?.let {
                    setCustomKey("marketplace_metrics_search_interactions", it.searchInteractions)
                    setCustomKey("marketplace_metrics_filter_selections", it.filterSelections)
                    setCustomKey("marketplace_metrics_asset_opens", it.assetOpens)
                    setCustomKey("marketplace_metrics_offline_retries", it.offlineRetries)
                    setCustomKey("marketplace_metrics_recommendation_impressions", it.recommendationImpressions)
                    setCustomKey("marketplace_metrics_recommendation_opens", it.recommendationOpens)
                    if (it.lastRecommendedAssetIds.isNotEmpty()) {
                        setCustomKey(
                            "marketplace_metrics_last_recommended_ids",
                            it.lastRecommendedAssetIds.joinToString(",")
                        )
                    }
                    setCustomKey(
                        "marketplace_metrics_last_recommendation_at",
                        it.lastRecommendationGeneratedAtMillis
                    )
                }
                error?.let { recordException(it) }
            } ?: Log.w(TAG, "Crashlytics no disponible; omitiendo registro de fallo", error)
        } else {
            Log.d(TAG, "Crash reporting desactivado por el usuario; omitiendo registro de fallo")
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
