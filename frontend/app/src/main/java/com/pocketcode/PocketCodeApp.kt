package com.pocketcode

import android.app.Application
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * This is the main Application class for the PocketCode IDE.
 * It serves as the entry point for the application process.
 *
 * Responsibilities:
 * - Perform any necessary one-time initializations for the app, such as setting up
 *   logging libraries or analytics.
 *
 * This class is referenced in the `AndroidManifest.xml` file.
 */
@HiltAndroidApp
class PocketCodeApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // App-level initializations go here.
    }

    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
