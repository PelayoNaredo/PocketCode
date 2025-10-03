package com.pocketcode.data.marketplace.remote.dto

import com.google.gson.annotations.SerializedName

data class MarketplaceInteractionMetricsRequest(
    @SerializedName("search_interactions") val searchInteractions: Int,
    @SerializedName("filter_selections") val filterSelections: Int,
    @SerializedName("asset_opens") val assetOpens: Int,
    @SerializedName("offline_retries") val offlineRetries: Int,
    @SerializedName("last_result_count") val lastResultCount: Int,
    @SerializedName("last_opened_asset_id") val lastOpenedAssetId: String?,
    @SerializedName("last_opened_asset_name") val lastOpenedAssetName: String?,
    @SerializedName("recommendation_impressions") val recommendationImpressions: Int,
    @SerializedName("recommendation_opens") val recommendationOpens: Int,
    @SerializedName("last_recommended_asset_ids") val lastRecommendedAssetIds: List<String>,
    @SerializedName("last_recommendation_generated_at_iso") val lastRecommendationGeneratedAtIso: String?,
    @SerializedName("last_updated_at_iso") val lastUpdatedAtIso: String
)
