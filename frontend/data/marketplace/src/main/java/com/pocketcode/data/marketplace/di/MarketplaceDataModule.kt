package com.pocketcode.data.marketplace.di

import android.content.Context
import androidx.work.WorkManager
import com.pocketcode.data.marketplace.analytics.MarketplaceAnalyticsRepositoryImpl
import com.pocketcode.data.marketplace.analytics.MarketplaceMetricsSyncSchedulerImpl
import com.pocketcode.data.marketplace.analytics.MarketplaceMetricsSyncTelemetryImpl
import com.pocketcode.data.marketplace.analytics.MarketplaceRecommendationsDiagnosticsImpl
import com.pocketcode.data.marketplace.remote.api.MarketplaceAnalyticsApiService
import com.pocketcode.data.marketplace.remote.api.MarketplaceApiService
import com.pocketcode.data.marketplace.repository.MarketplaceRepositoryImpl
import com.pocketcode.domain.marketplace.analytics.MarketplaceAnalyticsRepository
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncScheduler
import com.pocketcode.domain.marketplace.analytics.MarketplaceMetricsSyncTelemetry
import com.pocketcode.domain.marketplace.analytics.MarketplaceRecommendationsDiagnostics
import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MarketplaceDataModule {

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(
        marketplaceRepositoryImpl: MarketplaceRepositoryImpl
    ): MarketplaceRepository

    @Binds
    @Singleton
    abstract fun bindMarketplaceAnalyticsRepository(
        marketplaceAnalyticsRepositoryImpl: MarketplaceAnalyticsRepositoryImpl
    ): MarketplaceAnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindMarketplaceMetricsSyncScheduler(
        marketplaceMetricsSyncSchedulerImpl: MarketplaceMetricsSyncSchedulerImpl
    ): MarketplaceMetricsSyncScheduler

    @Binds
    @Singleton
    abstract fun bindMarketplaceMetricsSyncTelemetry(
        marketplaceMetricsSyncTelemetryImpl: MarketplaceMetricsSyncTelemetryImpl
    ): MarketplaceMetricsSyncTelemetry

    @Binds
    @Singleton
    abstract fun bindMarketplaceRecommendationsDiagnostics(
        marketplaceRecommendationsDiagnosticsImpl: MarketplaceRecommendationsDiagnosticsImpl
    ): MarketplaceRecommendationsDiagnostics

    companion object {
        @Provides
        @Singleton
        fun provideMarketplaceApiService(retrofit: Retrofit): MarketplaceApiService {
            return retrofit.create(MarketplaceApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideMarketplaceAnalyticsApiService(retrofit: Retrofit): MarketplaceAnalyticsApiService {
            return retrofit.create(MarketplaceAnalyticsApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}
