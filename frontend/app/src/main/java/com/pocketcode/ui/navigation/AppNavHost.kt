package com.pocketcode.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * This composable function is responsible for setting up the application's navigation graph.
 * It uses Jetpack Navigation Compose to define all possible navigation paths and their
 * corresponding screens (composables).
 *
 * Responsibilities:
 * - Create a `NavController` to manage the navigation state.
 * - Configure a `NavHost` with all the navigation destinations.
 * - Define routes for each feature screen (e.g., "project_dashboard", "editor/{projectId}", "settings").
 * - Handle the navigation logic between screens.
 *
 * Interacts with:
 * - `MainActivity`: This `AppNavHost` is hosted within the MainActivity.
 * - All `:features:*` modules: It will reference the main composable screen from each feature module
 *   to set them as destinations in the navigation graph.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        // Example destination. Each feature will have its own navigation graph or composable screen.
        composable("dashboard") {
            // Placeholder for the Project Dashboard screen from the :features:project module
        }
        composable("editor/{projectId}") {
            // Placeholder for the Editor screen from the :features:editor module
        }
    }
}
