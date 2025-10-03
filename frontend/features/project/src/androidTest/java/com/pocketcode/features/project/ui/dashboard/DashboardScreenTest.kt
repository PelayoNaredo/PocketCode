package com.pocketcode.features.project.ui.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pocketcode.core.ui.theme.PocketTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_isVisible_whenThereAreNoProjects() {
        composeTestRule.setContent {
            PocketTheme {
                DashboardScreen(
                    uiState = DashboardUiState(
                        isLoading = false,
                        projects = emptyList()
                    ),
                    onProjectClick = {},
                    onMarketplaceClick = {},
                    onAiAssistantClick = {},
                    onSettingsClick = {},
                    onRetry = {},
                    onCreateProject = {},
                    onImportProject = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No tienes proyectos todavía").assertExists()
        composeTestRule.onNodeWithText("Crear proyecto").assertExists()
    }

    @Test
    fun createProjectFlow_invokesCallbackWithTrimmedName() {
        var createdName: String? = null

        composeTestRule.setContent {
            PocketTheme {
                DashboardScreen(
                    uiState = DashboardUiState(
                        isLoading = false,
                        projects = emptyList()
                    ),
                    onProjectClick = {},
                    onMarketplaceClick = {},
                    onAiAssistantClick = {},
                    onSettingsClick = {},
                    onRetry = {},
                    onCreateProject = { createdName = it },
                    onImportProject = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Nuevo proyecto").performClick()
        composeTestRule.onNodeWithText("Crear un proyecto nuevo").performClick()
        composeTestRule.onNodeWithTag("dashboard_create_project_name").performTextInput("  Mi Proyecto  ")
        composeTestRule.onNodeWithText("Crear").performClick()

    composeTestRule.waitForIdle()
    assertEquals("Mi Proyecto", createdName)
    }

    @Test
    fun importOption_invokesCallback() {
        var importInvoked = false

        composeTestRule.setContent {
            PocketTheme {
                DashboardScreen(
                    uiState = DashboardUiState(
                        isLoading = false,
                        projects = emptyList()
                    ),
                    onProjectClick = {},
                    onMarketplaceClick = {},
                    onAiAssistantClick = {},
                    onSettingsClick = {},
                    onRetry = {},
                    onCreateProject = {},
                    onImportProject = { importInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Nuevo proyecto").performClick()
        composeTestRule.onNodeWithText("Importar un .zip existente").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { importInvoked }
        assert(importInvoked)
    }
}
