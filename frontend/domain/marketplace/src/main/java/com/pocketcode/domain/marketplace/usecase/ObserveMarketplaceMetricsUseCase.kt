package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.analytics.MarketplaceAnalyticsRepository
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMarketplaceMetricsUseCase @Inject constructor(
    private val analyticsRepository: MarketplaceAnalyticsRepository
) {
    operator fun invoke(): Flow<MarketplaceInteractionMetrics> {
        return analyticsRepository.metrics
    }
}
