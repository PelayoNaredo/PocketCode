package com.pocketcode.data.marketplace.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pocketcode.data.marketplace.remote.api.MarketplaceAnalyticsApiService
import com.pocketcode.data.marketplace.remote.dto.MarketplaceInteractionMetricsRequest
import com.pocketcode.domain.marketplace.analytics.MarketplaceAnalyticsRepository
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val MARKETPLACE_METRICS_STORE = "marketplace_metrics"

class MarketplaceAnalyticsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsApiService: MarketplaceAnalyticsApiService
) : MarketplaceAnalyticsRepository {

    private val Context.metricsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = MARKETPLACE_METRICS_STORE
    )

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private object PreferenceKeys {
        val searchInteractions = intPreferencesKey("marketplace_search_interactions")
        val filterSelections = intPreferencesKey("marketplace_filter_selections")
        val assetOpens = intPreferencesKey("marketplace_asset_opens")
        val offlineRetries = intPreferencesKey("marketplace_offline_retries")
        val lastResultCount = intPreferencesKey("marketplace_last_result_count")
        val lastOpenedAssetId = stringPreferencesKey("marketplace_last_opened_asset_id")
        val lastOpenedAssetName = stringPreferencesKey("marketplace_last_opened_asset_name")
        val recommendationImpressions = intPreferencesKey("marketplace_recommendation_impressions")
        val recommendationOpens = intPreferencesKey("marketplace_recommendation_opens")
        val lastRecommendedAssetIds = stringPreferencesKey("marketplace_last_recommended_asset_ids")
        val lastRecommendationGeneratedAtMillis = longPreferencesKey("marketplace_last_recommendation_at_millis")
        val lastUpdatedAtMillis = longPreferencesKey("marketplace_last_updated_at_millis")
    }

    override val metrics: Flow<MarketplaceInteractionMetrics> =
        context.metricsDataStore.data.map { preferences ->
            val recommendedIdsRaw = preferences[PreferenceKeys.lastRecommendedAssetIds]
            val recommendedIds = recommendedIdsRaw
                ?.split('|')
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()
            MarketplaceInteractionMetrics(
                searchInteractions = preferences[PreferenceKeys.searchInteractions] ?: 0,
                filterSelections = preferences[PreferenceKeys.filterSelections] ?: 0,
                assetOpens = preferences[PreferenceKeys.assetOpens] ?: 0,
                offlineRetries = preferences[PreferenceKeys.offlineRetries] ?: 0,
                lastResultCount = preferences[PreferenceKeys.lastResultCount] ?: 0,
                lastOpenedAssetId = preferences[PreferenceKeys.lastOpenedAssetId],
                lastOpenedAssetName = preferences[PreferenceKeys.lastOpenedAssetName],
                recommendationImpressions = preferences[PreferenceKeys.recommendationImpressions] ?: 0,
                recommendationOpens = preferences[PreferenceKeys.recommendationOpens] ?: 0,
                lastRecommendedAssetIds = recommendedIds,
                lastRecommendationGeneratedAtMillis = preferences[PreferenceKeys.lastRecommendationGeneratedAtMillis] ?: 0L,
                lastUpdatedAtMillis = preferences[PreferenceKeys.lastUpdatedAtMillis] ?: 0L
            )
        }

    override suspend fun persistSnapshot(snapshot: MarketplaceInteractionMetrics) {
        context.metricsDataStore.edit { preferences ->
            preferences[PreferenceKeys.searchInteractions] = snapshot.searchInteractions
            preferences[PreferenceKeys.filterSelections] = snapshot.filterSelections
            preferences[PreferenceKeys.assetOpens] = snapshot.assetOpens
            preferences[PreferenceKeys.offlineRetries] = snapshot.offlineRetries
            preferences[PreferenceKeys.lastResultCount] = snapshot.lastResultCount
            preferences[PreferenceKeys.recommendationImpressions] = snapshot.recommendationImpressions
            preferences[PreferenceKeys.recommendationOpens] = snapshot.recommendationOpens

            val lastOpenedAssetId = snapshot.lastOpenedAssetId
            if (lastOpenedAssetId.isNullOrBlank()) {
                preferences.remove(PreferenceKeys.lastOpenedAssetId)
            } else {
                preferences[PreferenceKeys.lastOpenedAssetId] = lastOpenedAssetId
            }

            val lastOpenedAssetName = snapshot.lastOpenedAssetName
            if (lastOpenedAssetName.isNullOrBlank()) {
                preferences.remove(PreferenceKeys.lastOpenedAssetName)
            } else {
                preferences[PreferenceKeys.lastOpenedAssetName] = lastOpenedAssetName
            }

            val recommendedAssetIds = snapshot.lastRecommendedAssetIds
            if (recommendedAssetIds.isEmpty()) {
                preferences.remove(PreferenceKeys.lastRecommendedAssetIds)
            } else {
                preferences[PreferenceKeys.lastRecommendedAssetIds] = recommendedAssetIds.joinToString("|")
            }

            preferences[PreferenceKeys.lastRecommendationGeneratedAtMillis] = snapshot.lastRecommendationGeneratedAtMillis

            preferences[PreferenceKeys.lastUpdatedAtMillis] = snapshot.lastUpdatedAtMillis
        }
    }

    override suspend fun uploadSnapshot(snapshot: MarketplaceInteractionMetrics): Result<Unit> {
        return try {
            analyticsApiService.uploadInteractionSnapshot(snapshot.toRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun MarketplaceInteractionMetrics.toRequest(): MarketplaceInteractionMetricsRequest {
        val timestamp = if (lastUpdatedAtMillis > 0L) lastUpdatedAtMillis else System.currentTimeMillis()
        val recommendationTimestamp = lastRecommendationGeneratedAtMillis.takeIf { it > 0L }
            ?.let { isoFormatter.format(Date(it)) }
        return MarketplaceInteractionMetricsRequest(
            searchInteractions = searchInteractions,
            filterSelections = filterSelections,
            assetOpens = assetOpens,
            offlineRetries = offlineRetries,
            lastResultCount = lastResultCount,
            lastOpenedAssetId = lastOpenedAssetId,
            lastOpenedAssetName = lastOpenedAssetName,
            recommendationImpressions = recommendationImpressions,
            recommendationOpens = recommendationOpens,
            lastRecommendedAssetIds = lastRecommendedAssetIds,
            lastRecommendationGeneratedAtIso = recommendationTimestamp,
            lastUpdatedAtIso = isoFormatter.format(Date(timestamp))
        )
    }
}
