package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import com.pocketcode.domain.marketplace.analytics.MarketplaceRecommendationsDiagnostics
import com.pocketcode.domain.marketplace.analytics.RecommendationEvaluation
import com.pocketcode.domain.marketplace.analytics.RecommendationStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ValidateMarketplaceRecommendationsUseCaseTest {

    private val diagnostics = FakeDiagnostics()
    private val useCase = ValidateMarketplaceRecommendationsUseCase(diagnostics)

    @Test
    fun `report insufficient data when impressions below threshold`() = runTest {
        val snapshot = snapshot(impressions = 10, opens = 5)

        useCase(snapshot)

        val evaluation = diagnostics.singleEvaluation()
        assertEquals(10, evaluation.impressions)
        assertEquals(5, evaluation.opens)
        assertEquals(0.5, evaluation.conversionRate, 0.0001)
        assertSame(RecommendationStatus.INSUFFICIENT_DATA, evaluation.status)
    }

    @Test
    fun `report good status when conversion meets good threshold`() = runTest {
        val snapshot = snapshot(impressions = 40, opens = 12)

        useCase(snapshot)

        val evaluation = diagnostics.singleEvaluation()
        assertEquals(40, evaluation.impressions)
        assertEquals(12, evaluation.opens)
        assertEquals(0.3, evaluation.conversionRate, 0.0001)
        assertSame(RecommendationStatus.GOOD, evaluation.status)
    }

    @Test
    fun `report warning when conversion between warning and good`() = runTest {
        val snapshot = snapshot(impressions = 50, opens = 7)

        useCase(snapshot)

        val evaluation = diagnostics.singleEvaluation()
        assertEquals(0.14, evaluation.conversionRate, 0.0001)
        assertSame(RecommendationStatus.WARNING, evaluation.status)
    }

    @Test
    fun `report critical when conversion below warning`() = runTest {
        val snapshot = snapshot(impressions = 60, opens = 3)

        useCase(snapshot)

        val evaluation = diagnostics.singleEvaluation()
        assertEquals(0.05, evaluation.conversionRate, 0.0001)
        assertSame(RecommendationStatus.CRITICAL, evaluation.status)
    }

    private fun snapshot(impressions: Int, opens: Int): MarketplaceInteractionMetrics {
        return MarketplaceInteractionMetrics(
            recommendationImpressions = impressions,
            recommendationOpens = opens
        )
    }

    private class FakeDiagnostics : MarketplaceRecommendationsDiagnostics {
        private val evaluations = mutableListOf<RecommendationEvaluation>()

        override suspend fun reportEvaluation(evaluation: RecommendationEvaluation) {
            evaluations += evaluation
        }

        fun singleEvaluation(): RecommendationEvaluation {
            require(evaluations.isNotEmpty()) { "No se registraron evaluaciones" }
            val last = evaluations.last()
            evaluations.clear()
            return last
        }
    }
}
