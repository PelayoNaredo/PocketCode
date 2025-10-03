package com.pocketcode.data.ai.di

import com.pocketcode.data.ai.repository.AiRepositoryImpl
import com.pocketcode.domain.ai.repository.AiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for AI dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiDataModule {
    
    @Binds
    @Singleton
    abstract fun bindAiRepository(
        aiRepositoryImpl: AiRepositoryImpl
    ): AiRepository
}