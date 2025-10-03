package com.pocketcode.domain.marketplace.analytics

/**
 * Canal para reportar evaluaciones del algoritmo de recomendaciones utilizando datos reales.
 */
interface MarketplaceRecommendationsDiagnostics {
    suspend fun reportEvaluation(evaluation: RecommendationEvaluation)
}

/**
 * Resultado agregado de la conversión de recomendaciones.
 */
data class RecommendationEvaluation(
    val impressions: Int,
    val opens: Int,
    val conversionRate: Double,
    val status: RecommendationStatus
)

enum class RecommendationStatus {
    GOOD,
    WARNING,
    CRITICAL,
    INSUFFICIENT_DATA
}
