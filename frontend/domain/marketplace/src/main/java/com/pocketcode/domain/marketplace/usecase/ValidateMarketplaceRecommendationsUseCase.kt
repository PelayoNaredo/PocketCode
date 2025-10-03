package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.analytics.MarketplaceRecommendationsDiagnostics
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import com.pocketcode.domain.marketplace.analytics.RecommendationEvaluation
import com.pocketcode.domain.marketplace.analytics.RecommendationStatus
import javax.inject.Inject

class ValidateMarketplaceRecommendationsUseCase @Inject constructor(
    private val diagnostics: MarketplaceRecommendationsDiagnostics
) {

    suspend operator fun invoke(snapshot: MarketplaceInteractionMetrics) {
        val impressions = snapshot.recommendationImpressions
        val opens = snapshot.recommendationOpens
        val conversionRate = if (impressions == 0) 0.0 else opens.toDouble() / impressions.toDouble()

        val evaluation = when {
            impressions < MIN_SAMPLE -> RecommendationEvaluation(
                impressions = impressions,
                opens = opens,
                conversionRate = conversionRate,
                status = RecommendationStatus.INSUFFICIENT_DATA
            )

            else -> {
                val status = when {
                    conversionRate >= GOOD_THRESHOLD -> RecommendationStatus.GOOD
                    conversionRate >= WARNING_THRESHOLD -> RecommendationStatus.WARNING
                    else -> RecommendationStatus.CRITICAL
                }

                RecommendationEvaluation(
                    impressions = impressions,
                    opens = opens,
                    conversionRate = conversionRate,
                    status = status
                )
            }
        }

        diagnostics.reportEvaluation(evaluation)
    }

    private companion object {
        const val MIN_SAMPLE = 20
        const val GOOD_THRESHOLD = 0.25
        const val WARNING_THRESHOLD = 0.1
    }
}
