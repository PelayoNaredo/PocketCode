package com.pocketcode.features.marketplace.ui.home

import com.pocketcode.domain.marketplace.model.Asset

data class MarketplaceHomeState(
    val isLoading: Boolean = false,
    val assets: List<Asset> = emptyList(),
    val filteredAssets: List<Asset> = emptyList(),
    val query: String = "",
    val ratingFilter: MarketplaceRatingFilter = MarketplaceRatingFilter.All,
    val isOffline: Boolean = false,
    val error: String? = null,
    val metrics: MarketplaceMetrics = MarketplaceMetrics(),
    val recommendedAssets: List<Asset> = emptyList()
)

val MarketplaceHomeState.hasActiveFilters: Boolean
    get() = query.isNotBlank() || ratingFilter != MarketplaceRatingFilter.All

data class MarketplaceMetrics(
    val searchInteractions: Int = 0,
    val filterSelections: Int = 0,
    val assetOpens: Int = 0,
    val offlineRetries: Int = 0,
    val lastResultCount: Int = 0,
    val lastOpenedAssetId: String? = null,
    val lastOpenedAssetName: String? = null,
    val recommendationImpressions: Int = 0,
    val recommendationOpens: Int = 0,
    val lastRecommendedAssetIds: List<String> = emptyList(),
    val lastRecommendationGeneratedAtMillis: Long = 0L
) {
    val hasEngagement: Boolean
        get() = searchInteractions > 0 || filterSelections > 0 || assetOpens > 0 || offlineRetries > 0 || recommendationImpressions > 0 || recommendationOpens > 0

    val recommendationConversionRate: Double
        get() = if (recommendationImpressions == 0) 0.0 else recommendationOpens.toDouble() / recommendationImpressions.toDouble()
}

enum class MarketplaceRatingFilter(val label: String, val minimumRating: Double?) {
    All("Todos", null),
    FourPlus("⭐ 4+", 4.0),
    ThreePlus("⭐ 3+", 3.0)
}
