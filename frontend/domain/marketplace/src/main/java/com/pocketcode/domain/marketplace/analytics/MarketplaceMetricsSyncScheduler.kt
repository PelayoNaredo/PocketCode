package com.pocketcode.domain.marketplace.analytics

/**
 * Abstracción para programar sincronizaciones resilientes de métricas del Marketplace
 * utilizando la infraestructura de background disponible en la capa de datos.
 */
interface MarketplaceMetricsSyncScheduler {
    /**
     * Encola un trabajo de sincronización único que garantizará el reintento del envío
     * de métricas cuando haya conectividad disponible.
     */
    fun scheduleSync()
}
