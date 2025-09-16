package com.pocketcode.app

import android.app.Application
import com.pocketcode.settings.subscriptions.SubscriptionManager

class PocketCodeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the SubscriptionManager
        SubscriptionManager.initialize(this)
    }
}
