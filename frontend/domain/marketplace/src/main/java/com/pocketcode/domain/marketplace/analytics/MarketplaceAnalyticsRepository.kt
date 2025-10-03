package com.pocketcode.domain.marketplace.analytics

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio encargado de persistir y exponer las métricas de interacción del Marketplace
 * para su posterior envío al pipeline central de analytics.
 */
interface MarketplaceAnalyticsRepository {
    /**
     * Flujo con la última instantánea de métricas almacenadas.
     */
    val metrics: Flow<MarketplaceInteractionMetrics>

    /**
     * Persiste una nueva instantánea de métricas, sobrescribiendo los valores previos.
     */
    suspend fun persistSnapshot(snapshot: MarketplaceInteractionMetrics)

    /**
     * Envía la instantánea al pipeline remoto de analytics.
     */
    suspend fun uploadSnapshot(snapshot: MarketplaceInteractionMetrics): Result<Unit>
}
