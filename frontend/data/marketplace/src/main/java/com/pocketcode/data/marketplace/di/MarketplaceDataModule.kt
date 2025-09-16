package com.pocketcode.data.marketplace.di

import com.pocketcode.data.marketplace.remote.api.MarketplaceApiService
import com.pocketcode.data.marketplace.repository.MarketplaceRepositoryImpl
import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

    companion object {
        @Provides
        @Singleton
        fun provideMarketplaceApiService(retrofit: Retrofit): MarketplaceApiService {
            return retrofit.create(MarketplaceApiService::class.java)
        }
    }
}
