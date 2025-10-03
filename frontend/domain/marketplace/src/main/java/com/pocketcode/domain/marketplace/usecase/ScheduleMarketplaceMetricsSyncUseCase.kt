package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncScheduler
import javax.inject.Inject

class ScheduleMarketplaceMetricsSyncUseCase @Inject constructor(
    private val syncScheduler: MarketplaceMetricsSyncScheduler
) {
    operator fun invoke() {
        syncScheduler.scheduleSync()
    }
}
