package com.pocketcode.domain.marketplace.analytics

/**
 * Métricas básicas generadas en la pantalla Home del Marketplace.
 */
data class MarketplaceInteractionMetrics(
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
    val lastRecommendationGeneratedAtMillis: Long = 0L,
    val lastUpdatedAtMillis: Long = 0L
)
