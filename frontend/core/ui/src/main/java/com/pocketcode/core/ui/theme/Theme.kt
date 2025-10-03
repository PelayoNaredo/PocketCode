package com.pocketcode.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val themeViewModel: ThemeViewModel = viewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val systemDark = isSystemInDarkTheme()

    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    LaunchedEffect(themeMode, isDarkTheme) {
        themeViewModel.updateSystemDarkTheme(isDarkTheme)
    }

    PocketTheme(darkTheme = isDarkTheme) {
        content()
    }
}
