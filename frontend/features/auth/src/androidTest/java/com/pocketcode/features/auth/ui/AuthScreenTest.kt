package com.pocketcode.features.auth.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_rendersBasicElements() {
        // NOTE: Test simplificado para verificar elementos básicos UI
        // Tests completos de interacción requieren ViewModels mock
        composeTestRule.setContent {
            LoginScreen()
        }

        // Verificar que el composable renderiza sin crashear
        composeTestRule.onRoot().assertExists()
        
        // Verificar título principal
        composeTestRule.onNodeWithText("Acceso a PocketCode", substring = true).assertExists()
        
        // Verificar sección de login
        composeTestRule.onNodeWithText("Iniciar sesión", substring = true).assertExists()
    }

    @Test
    fun signUpScreen_rendersBasicElements() {
        // NOTE: Test simplificado para verificar elementos básicos UI
        // Tests completos de interacción requieren ViewModels mock
        composeTestRule.setContent {
            SignUpScreen()
        }

        // Verificar que el composable renderiza sin crashear
        composeTestRule.onRoot().assertExists()
        
        // Verificar título principal
        composeTestRule.onNodeWithText("PocketCode", substring = true).assertExists()
        
        // Verificar sección de registro
        composeTestRule.onNodeWithText("Crear cuenta", substring = true, ignoreCase = true).assertExists()
    }

    @Test
    fun loginScreen_showsFormFields() {
        composeTestRule.setContent {
            LoginScreen()
        }

        // Verificar campos de formulario
        composeTestRule.onNodeWithText("Correo electrónico", substring = true).assertExists()
        composeTestRule.onNodeWithText("Contraseña", substring = true).assertExists()
    }

    @Test
    fun signUpScreen_showsFormFields() {
        composeTestRule.setContent {
            SignUpScreen()
        }

        // Verificar campos de formulario
        composeTestRule.onNodeWithText("Correo", substring = true).assertExists()
        composeTestRule.onNodeWithText("Contraseña", substring = true).assertExists()
    }

    @Test
    fun loginScreen_showsActionButtons() {
        composeTestRule.setContent {
            LoginScreen()
        }

        // Verificar que existen botones de acción
        composeTestRule.onNodeWithText("sesión", substring = true, ignoreCase = true).assertExists()
    }

    @Test
    fun signUpScreen_showsActionButtons() {
        composeTestRule.setContent {
            SignUpScreen()
        }

        // Verificar que existen botones de acción
        composeTestRule.onNodeWithText("Regist", substring = true, ignoreCase = true).assertExists()
    }

    // NOTE: Tests más avanzados (validación, navegación, etc.) requieren:
    // 1. Mock de ViewModels con Hilt
    // 2. Fake repositories
    // 3. Test runners configurados
    // Estos tests básicos verifican que el UI renderiza correctamente
}
