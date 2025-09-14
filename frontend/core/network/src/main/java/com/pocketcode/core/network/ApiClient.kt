package com.pocketcode.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This file defines how the shared network client is provided for dependency injection.
 * Using Hilt, we can define a module that provides a singleton instance of our
 * HTTP client (e.g., Ktor or Retrofit) to the rest of the application.
 *
 * Responsibilities:
 * - Configure the HTTP client (e.g., setting base URL, timeouts, interceptors for adding auth tokens).
 * - Provide the configured client as a singleton instance so that the same client is reused
 *   throughout the app, which is efficient.
 *
 * Interacts with:
 * - `:data` modules: The repositories in the data layer will inject this client to make API calls.
 * - Hilt: This is a Hilt module that contributes bindings to the application's dependency graph.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiClient {

    @Provides
    @Singleton
    fun provideHttpClient(): Any { // Using 'Any' as a placeholder for a real client like HttpClient (Ktor) or Retrofit
        // The actual implementation of the HTTP client would be created and configured here.
        // For example:
        // return HttpClient(CIO) {
        //     install(JsonFeature) { ... }
        // }
        return Any()
    }
}
