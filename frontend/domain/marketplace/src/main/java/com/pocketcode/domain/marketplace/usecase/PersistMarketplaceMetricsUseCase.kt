package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.analytics.MarketplaceAnalyticsRepository
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import javax.inject.Inject

class PersistMarketplaceMetricsUseCase @Inject constructor(
    private val analyticsRepository: MarketplaceAnalyticsRepository
) {
    suspend operator fun invoke(snapshot: MarketplaceInteractionMetrics) {
        analyticsRepository.persistSnapshot(snapshot)
    }
}
