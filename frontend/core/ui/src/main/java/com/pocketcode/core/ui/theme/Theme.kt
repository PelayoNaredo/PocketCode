package com.pocketcode.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * This file defines the application's theme.
 * It's part of the `:core:ui` module so that all feature modules can use it
 * to create a consistent user interface.
 *
 * Responsibilities:
 * - Define the color schemes for light and dark themes (e.g., primary, secondary, background colors).
 * - Define typography styles (e.g., h1, body1, button text).
 * - Define shapes for components (e.g., corner radius for buttons and cards).
 * - Expose a main `AppTheme` composable that applies this styling.
 *
 * Interacts with:
 * - All `:features:*` modules: Every feature screen will wrap its content in the `AppTheme`
 *   to ensure visual consistency.
 */
@Composable
fun PocketCodeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // ColorScheme, Typography, and Shapes would be defined here
        content = content
    )
}
