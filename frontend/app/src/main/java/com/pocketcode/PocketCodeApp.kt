package com.pocketcode

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * This is the main Application class for the PocketCode IDE.
 * It serves as the entry point for the application process.
 *
 * Responsibilities:
 * - Initialize dependency injection with Hilt by using the `@HiltAndroidApp` annotation.
 *   This creates the application-level dependency container.
 * - Perform any necessary one-time initializations for the app, such as setting up
 *   logging libraries or analytics.
 *
 * This class is referenced in the `AndroidManifest.xml` file.
 */
@HiltAndroidApp
class PocketCodeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // App-level initializations go here.
    }
}
