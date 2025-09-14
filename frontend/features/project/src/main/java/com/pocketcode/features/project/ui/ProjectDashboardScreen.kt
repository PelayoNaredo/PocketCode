package com.pocketcode.features.project.ui

import androidx.compose.runtime.Composable
// import androidx.hilt.navigation.compose.hiltViewModel

/**
 * This is the main composable for the Project Dashboard feature.
 * It displays a list of the user's projects and provides options to create or open projects.
 *
 * Responsibilities:
 * - Observe the list of projects from the `ProjectDashboardViewModel`.
 * - Render a list of project cards.
 * - Handle user interactions like tapping on a project or clicking the "Create New" button,
 *   and delegate these events to the ViewModel.
 * - This screen is typically the start destination of the application's navigation graph.
 *
 * Interacts with:
 * - `ProjectDashboardViewModel`: To get state and send events.
 * - `:core:ui`: To use shared components like `Scaffold`, `FloatingActionButton`, and `AppTheme`.
 * - The navigation component in `:app` to navigate to other screens (e.g., the editor).
 */
@Composable
fun ProjectDashboardScreen(
    // viewModel: ProjectDashboardViewModel = hiltViewModel(),
    // onNavigateToEditor: (projectId: String) -> Unit
) {
    // val projects = viewModel.projects.collectAsState()
    // UI layout for the project dashboard goes here.
}
