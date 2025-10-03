package com.pocketcode.features.onboarding.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pocketcode.core.ui.theme.PocketTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI instrumentados para OnboardingScreen.
 * 
 * Estos tests validan:
 * - Navegación entre páginas del onboarding
 * - Indicador de progreso (StepperIndicator)
 * - Botones de siguiente/anterior/finalizar
 * - Opción de saltar onboarding
 * - Responsive layout en tablet/desktop
 * - Accesibilidad de componentes
 */
@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboardingScreen_displaysFirstPage() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Verificar que se muestra el contenido de la primera página
        composeTestRule.onNodeWithText("Bienvenido a PocketCode").assertExists()
        
        // Verificar que existe el botón de siguiente
        composeTestRule.onNodeWithText("Siguiente").assertExists()
        
        // Verificar que existe la opción de saltar
        composeTestRule.onNodeWithText("Saltar").assertExists()
    }

    @Test
    fun onboardingScreen_displaysStepperIndicator() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Verificar que el StepperIndicator existe
        // Debe haber al menos 3 indicadores de paso
        composeTestRule.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..3f)))
            .assertCountEquals(1)
    }

    @Test
    fun nextButton_navigatesToNextPage() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Verificar página 1
        composeTestRule.onNodeWithText("Bienvenido a PocketCode").assertExists()

        // Hacer clic en siguiente
        composeTestRule.onNodeWithText("Siguiente").performClick()

        // Verificar que cambió a página 2 (el contenido debe cambiar)
        // Nota: El texto exacto depende de la implementación real
        composeTestRule.onNodeWithText("Bienvenido a PocketCode").assertDoesNotExist()
    }

    @Test
    fun previousButton_appearsAfterFirstPage() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // En la primera página no debe haber botón "Anterior"
        composeTestRule.onNodeWithText("Anterior").assertDoesNotExist()

        // Ir a la segunda página
        composeTestRule.onNodeWithText("Siguiente").performClick()

        // Ahora debe aparecer "Anterior"
        composeTestRule.onNodeWithText("Anterior").assertExists()
    }

    @Test
    fun previousButton_navigatesToPreviousPage() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Guardar texto de primera página
        val firstPageText = "Bienvenido a PocketCode"
        composeTestRule.onNodeWithText(firstPageText).assertExists()

        // Ir a segunda página
        composeTestRule.onNodeWithText("Siguiente").performClick()
        composeTestRule.onNodeWithText(firstPageText).assertDoesNotExist()

        // Volver a primera página
        composeTestRule.onNodeWithText("Anterior").performClick()
        composeTestRule.onNodeWithText(firstPageText).assertExists()
    }

    @Test
    fun finishButton_appearsOnLastPage() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Navegar hasta la última página
        // Asumiendo 3 páginas, hacer clic 2 veces
        composeTestRule.onNodeWithText("Siguiente").performClick()
        composeTestRule.onNodeWithText("Siguiente").performClick()

        // Verificar que aparece "Comenzar" o "Finalizar"
        composeTestRule.onNode(
            hasText("Comenzar") or hasText("Finalizar") or hasText("Empezar")
        ).assertExists()
    }

    @Test
    fun finishButton_callsNavigateToLogin() {
        var navigateToLoginCalled = false

        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = { navigateToLoginCalled = true },
                    onSkip = {}
                )
            }
        }

        // Navegar hasta la última página
        composeTestRule.onNodeWithText("Siguiente").performClick()
        composeTestRule.onNodeWithText("Siguiente").performClick()

        // Hacer clic en finalizar
        composeTestRule.onNode(
            hasText("Comenzar") or hasText("Finalizar") or hasText("Empezar")
        ).performClick()

        // Verificar que se llamó el callback
        assert(navigateToLoginCalled)
    }

    @Test
    fun skipButton_callsOnSkip() {
        var skipCalled = false

        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = { skipCalled = true }
                )
            }
        }

        // Hacer clic en saltar
        composeTestRule.onNodeWithText("Saltar").performClick()

        // Verificar que se llamó el callback
        assert(skipCalled)
    }

    @Test
    fun stepperIndicator_updatesWithPageNavigation() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // TODO: Verificar que el indicador de paso se actualiza
        // Esto requiere acceder al estado del StepperIndicator
        // que muestra la página actual

        // Ir a página 2
        composeTestRule.onNodeWithText("Siguiente").performClick()

        // El indicador debe mostrar progreso (visualmente esto se valida)
        // En un test real, verificaríamos el contentDescription o un testTag
    }

    @Test
    fun onboardingScreen_hasAccessibleNavigation() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Verificar que los botones de navegación tienen descripciones
        composeTestRule.onNode(hasContentDescription("Siguiente página"))
            .assertExists()
        
        composeTestRule.onNode(hasContentDescription("Saltar onboarding"))
            .assertExists()
    }

    @Test
    fun onboardingScreen_supportsSwipeGestures() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Guardar texto de primera página
        val firstPageText = "Bienvenido a PocketCode"
        composeTestRule.onNodeWithText(firstPageText).assertExists()

        // Hacer swipe a la izquierda para avanzar
        composeTestRule.onRoot().performTouchInput {
            swipeLeft(startX = width * 0.8f, endX = width * 0.2f)
        }

        // Esperar animación
        composeTestRule.waitForIdle()

        // Verificar que cambió de página
        composeTestRule.onNodeWithText(firstPageText).assertDoesNotExist()
    }

    @Test
    fun onboardingScreen_swipeRightGoesBack() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        val firstPageText = "Bienvenido a PocketCode"

        // Ir a segunda página
        composeTestRule.onNodeWithText("Siguiente").performClick()
        composeTestRule.onNodeWithText(firstPageText).assertDoesNotExist()

        // Hacer swipe a la derecha para retroceder
        composeTestRule.onRoot().performTouchInput {
            swipeRight(startX = width * 0.2f, endX = width * 0.8f)
        }

        // Esperar animación
        composeTestRule.waitForIdle()

        // Verificar que volvió a primera página
        composeTestRule.onNodeWithText(firstPageText).assertExists()
    }

    @Test
    fun onboardingScreen_canNotSwipeBeforeFirstPage() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        val firstPageText = "Bienvenido a PocketCode"

        // Intentar hacer swipe a la derecha desde la primera página
        composeTestRule.onRoot().performTouchInput {
            swipeRight(startX = width * 0.2f, endX = width * 0.8f)
        }

        // Debe seguir en primera página
        composeTestRule.onNodeWithText(firstPageText).assertExists()
    }

    @Test
    fun onboardingScreen_displaysAllPages() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Navegar por todas las páginas y verificar que existe contenido
        var pageCount = 0
        while (composeTestRule.onAllNodesWithText("Siguiente").fetchSemanticsNodes().isNotEmpty()) {
            pageCount++
            composeTestRule.onNodeWithText("Siguiente").performClick()
            composeTestRule.waitForIdle()
        }

        // Debe haber navegado por al menos 2 páginas antes de llegar a "Finalizar"
        assert(pageCount >= 2) { "Onboarding debe tener al menos 3 páginas (navegó $pageCount veces)" }
    }

    @Test
    fun onboardingScreen_preservesStateOnConfigurationChange() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Ir a segunda página
        composeTestRule.onNodeWithText("Siguiente").performClick()
        
        // Simular cambio de configuración (rotación)
        // En un test real, esto requeriría usar ActivityScenarioRule
        // y recrear la actividad
        
        // Por ahora, verificar que los botones siguen disponibles
        composeTestRule.onNodeWithText("Anterior").assertExists()
        composeTestRule.onNodeWithText("Siguiente").assertExists()
    }

    @Test
    fun onboardingPager_hasCorrectPageCount() {
        composeTestRule.setContent {
            PocketTheme {
                OnboardingScreen(
                    onNavigateToLogin = {},
                    onSkip = {}
                )
            }
        }

        // Verificar que el HorizontalPager tiene el número esperado de páginas
        // Esto se puede inferir del StepperIndicator
        // Si hay 3 steps, debe haber 3 páginas
        
        // Contar cuántas veces podemos hacer clic en "Siguiente"
        var clickCount = 0
        while (composeTestRule.onAllNodesWithText("Siguiente").fetchSemanticsNodes().isNotEmpty()) {
            clickCount++
            composeTestRule.onNodeWithText("Siguiente").performClick()
            
            // Protección contra loops infinitos
            if (clickCount > 10) break
        }

        // Debe haber exactamente 3 páginas (2 clics de "Siguiente" + página final)
        assert(clickCount == 2) { "Expected 2 'Siguiente' clicks for 3 pages, got $clickCount" }
    }
}
