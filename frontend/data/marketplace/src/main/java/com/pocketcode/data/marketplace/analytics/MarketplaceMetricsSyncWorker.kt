package com.pocketcode.data.marketplace.analytics

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pocketcode.domain.marketplace.analytics.MarketplaceAnalyticsRepository
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncChannel
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncTelemetry
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class MarketplaceMetricsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val analyticsRepository: MarketplaceAnalyticsRepository,
    private val metricsSyncTelemetry: MarketplaceMetricsSyncTelemetry
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val snapshot = analyticsRepository.metrics.firstOrNull()
        if (snapshot == null) {
            Log.i(TAG, "No existen métricas pendientes para sincronizar")
            return Result.success()
        }

        val startTime = System.currentTimeMillis()
        val uploadResult = analyticsRepository.uploadSnapshot(snapshot)
        return if (uploadResult.isSuccess) {
            metricsSyncTelemetry.trackSyncSuccess(
                snapshot = snapshot,
                attempt = runAttemptCount + 1,
                durationMillis = System.currentTimeMillis() - startTime,
                channel = MarketplaceMetricsSyncChannel.BACKGROUND
            )
            Log.i(TAG, "Sincronización de métricas completada correctamente")
            Result.success()
        } else {
            val error = uploadResult.exceptionOrNull()
            Log.w(TAG, "Error subiendo métricas, se reintentará", error)
            val attemptNumber = runAttemptCount + 1
            val isTerminal = attemptNumber >= MAX_ATTEMPTS
            metricsSyncTelemetry.trackSyncFailure(
                snapshot = snapshot,
                attempt = attemptNumber,
                maxAttempts = MAX_ATTEMPTS,
                error = error,
                isTerminal = isTerminal,
                channel = MarketplaceMetricsSyncChannel.BACKGROUND
            )
            if (isTerminal) {
                Log.e(TAG, "Se alcanzó el número máximo de reintentos, registrando fallo definitivo")
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "MarketplaceSyncWorker"
        private const val MAX_ATTEMPTS = 5
    }
}
