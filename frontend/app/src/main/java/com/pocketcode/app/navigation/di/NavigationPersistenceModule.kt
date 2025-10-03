package com.pocketcode.app.navigation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.pocketcode.app.navigation.NavigationPersistenceRepository
import javax.inject.Singleton

/**
 * Dagger Hilt module for navigation persistence dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object NavigationPersistenceModule {
    
    @Provides
    @Singleton
    fun provideNavigationPersistenceRepository(
        @ApplicationContext context: Context
    ): NavigationPersistenceRepository {
        return NavigationPersistenceRepository(context)
    }
}