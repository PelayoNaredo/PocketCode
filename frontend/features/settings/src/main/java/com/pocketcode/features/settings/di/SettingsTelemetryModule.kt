package com.pocketcode.features.settings.di

import com.pocketcode.features.settings.analytics.SettingsTelemetry
import com.pocketcode.features.settings.analytics.SettingsTelemetryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsTelemetryModule {

    @Binds
    abstract fun bindSettingsTelemetry(
        impl: SettingsTelemetryImpl
    ): SettingsTelemetry
}
