package com.pocketcode.features.settings.di

import com.pocketcode.features.settings.repository.SettingsDataSource
import com.pocketcode.features.settings.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {

    @Binds
    abstract fun bindSettingsDataSource(
        repository: SettingsRepository
    ): SettingsDataSource
}
