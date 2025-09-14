package com.pocketcode.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the main and only Activity in the application.
 * It serves as the host for all Jetpack Compose content.
 *
 * Responsibilities:
 * - Set up the main content view using `setContent`.
 * - Host the main navigation component (`AppNavHost`), which controls screen transitions.
 * - Annotated with `@AndroidEntryPoint` to enable Hilt to inject dependencies into it.
 *
 * Interacts with:
 * - `com.pocketcode.ui.navigation.AppNavHost`: This is the primary component it will host.
 * - `core:ui` module: Uses themes and shared UI components defined there.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // The AppNavHost composable will be called here to set up the navigation graph.
            Text("Welcome to PocketCode IDE")
        }
    }
}
