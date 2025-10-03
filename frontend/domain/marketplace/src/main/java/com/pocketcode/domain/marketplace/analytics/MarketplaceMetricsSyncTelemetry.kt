package com.pocketcode.domain.marketplace.analytics

/**
 * Telemetría asociada a la sincronización de métricas del Marketplace.
 * Permite reportar éxitos y fallos tanto en el canal inmediato (foreground)
 * como en procesamientos en segundo plano (WorkManager).
 */
interface MarketplaceMetricsSyncTelemetry {
    /**
     * Registra un éxito de sincronización. Los intentos son 1-indexados.
     */
    suspend fun trackSyncSuccess(
        snapshot: MarketplaceInteractionMetrics,
        attempt: Int,
        durationMillis: Long,
        channel: MarketplaceMetricsSyncChannel
    )

    /**
     * Registra un fallo de sincronización. Los intentos son 1-indexados.
     * @param snapshot Instantánea utilizada durante la sincronización (si está disponible).
     * @param isTerminal Indica si no quedan más reintentos programados.
     */
    suspend fun trackSyncFailure(
        snapshot: MarketplaceInteractionMetrics?,
        attempt: Int,
        maxAttempts: Int,
        error: Throwable?,
        isTerminal: Boolean,
        channel: MarketplaceMetricsSyncChannel
    )
}

/**
 * Canales posibles desde los que se dispara la sincronización de métricas.
 */
enum class MarketplaceMetricsSyncChannel {
    FOREGROUND,
    BACKGROUND
}
