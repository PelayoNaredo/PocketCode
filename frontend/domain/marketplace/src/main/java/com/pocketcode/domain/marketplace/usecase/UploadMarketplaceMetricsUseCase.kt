package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.analytics.MarketplaceAnalyticsRepository
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncChannel
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UploadMarketplaceMetricsUseCase @Inject constructor(
    private val analyticsRepository: MarketplaceAnalyticsRepository,
    private val metricsSyncTelemetry: MarketplaceMetricsSyncTelemetry
) {
    suspend operator fun invoke(snapshot: MarketplaceInteractionMetrics): Result<Unit> =
        withContext(Dispatchers.IO) {
            var lastError: Throwable? = null
            val startedAt = System.currentTimeMillis()
            repeat(MAX_ATTEMPTS) { index ->
                val attempt = index + 1
                val result = analyticsRepository.uploadSnapshot(snapshot)
                if (result.isSuccess) {
                    val elapsed = System.currentTimeMillis() - startedAt
                    metricsSyncTelemetry.trackSyncSuccess(
                        snapshot = snapshot,
                        attempt = attempt,
                        durationMillis = elapsed,
                        channel = MarketplaceMetricsSyncChannel.FOREGROUND
                    )
                    return@withContext result
                }

                lastError = result.exceptionOrNull()
                val isTerminal = attempt >= MAX_ATTEMPTS
                metricsSyncTelemetry.trackSyncFailure(
                    snapshot = snapshot,
                    attempt = attempt,
                    maxAttempts = MAX_ATTEMPTS,
                    error = lastError,
                    isTerminal = isTerminal,
                    channel = MarketplaceMetricsSyncChannel.FOREGROUND
                )

                if (!isTerminal) {
                    delay(RETRY_DELAYS_MS[index])
                }
            }

            Result.failure(lastError ?: IllegalStateException("Unknown upload failure"))
        }

    private companion object {
        const val MAX_ATTEMPTS = 3
        val RETRY_DELAYS_MS = longArrayOf(500L, 1500L)
    }
}
