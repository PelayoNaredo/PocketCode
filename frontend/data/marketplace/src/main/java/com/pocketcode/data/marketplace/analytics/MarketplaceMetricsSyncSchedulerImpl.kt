package com.pocketcode.data.marketplace.analytics

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceMetricsSyncSchedulerImpl @Inject constructor(
    private val workManager: WorkManager
) : MarketplaceMetricsSyncScheduler {

    override fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MarketplaceMetricsSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                INITIAL_BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    private companion object {
        const val WORK_NAME = "marketplace_metrics_sync"
        private const val INITIAL_BACKOFF_DELAY_MINUTES = 15L
    }
}
