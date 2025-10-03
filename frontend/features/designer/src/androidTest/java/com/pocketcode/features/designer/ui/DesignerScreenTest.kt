package com.pocketcode.features.designer.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test
import com.pocketcode.core.ui.theme.PocketTheme
import com.pocketcode.features.designer.ui.DesignerScreen

class DesignerScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun designerScreen_rendersAllSections() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Verificar secciones principales
        composeTestRule.onNodeWithText("Componentes").assertExists()
        composeTestRule.onNodeWithText("Lienzo").assertExists()
        composeTestRule.onNodeWithText("Propiedades").assertExists()
    }

    @Test
    fun designerScreen_componentPalette_showsAllComponents() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Verificar componentes disponibles en la paleta
        composeTestRule.onNodeWithText("Texto").assertExists()
        composeTestRule.onNodeWithText("Botón").assertExists()
    }

    @Test
    fun designerScreen_addTextComponent_addsToCanvas() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Click en "Añadir" para componente Texto
        composeTestRule.onAllNodesWithText("Añadir")[0]
            .performClick()

        // Verificar que aparece en el lienzo
        composeTestRule.onNodeWithText("Texto 0").assertExists()

        // Verificar mensaje de confirmación
        composeTestRule.onNodeWithText("añadido al lienzo", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun designerScreen_addButtonComponent_addsToCanvas() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Click en "Añadir" para componente Botón
        composeTestRule.onAllNodesWithText("Añadir")[1]
            .performClick()

        // Verificar que aparece en el lienzo
        composeTestRule.onNodeWithText("Botón 0").assertExists()
    }

    @Test
    fun designerScreen_selectComponent_showsProperties() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Añadir un componente
        composeTestRule.onAllNodesWithText("Añadir")[0]
            .performClick()

        // Click en el componente del lienzo para seleccionarlo
        composeTestRule.onNodeWithText("Texto 0")
            .performClick()

        // Verificar que aparece el panel de propiedades
        composeTestRule.onNodeWithText("Propiedades").assertExists()
        composeTestRule.onNodeWithText("Texto del componente").assertExists()
    }

    @Test
    fun designerScreen_editComponentText_updatesCanvas() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Añadir un componente
        composeTestRule.onAllNodesWithText("Añadir")[0]
            .performClick()

        // Seleccionar el componente
        composeTestRule.onNodeWithText("Texto 0")
            .performClick()

        // Editar el texto en propiedades
        composeTestRule.onNodeWithText("Texto del componente")
            .performTextClearance()
        composeTestRule.onNodeWithText("Texto del componente")
            .performTextInput("Nuevo Texto")

        // Aplicar cambios
        composeTestRule.onNodeWithText("Aplicar")
            .performClick()

        // Verificar que el texto se actualizó en el lienzo
        composeTestRule.onNodeWithText("Nuevo Texto").assertExists()
    }

    @Test
    fun designerScreen_deleteComponent_removesFromCanvas() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Añadir un componente
        composeTestRule.onAllNodesWithText("Añadir")[0]
            .performClick()

        // Verificar que existe
        composeTestRule.onNodeWithText("Texto 0").assertExists()

        // Seleccionar el componente
        composeTestRule.onNodeWithText("Texto 0")
            .performClick()

        // Click en eliminar
        composeTestRule.onNodeWithText("Eliminar")
            .performClick()

        // Verificar que ya no existe
        composeTestRule.onNodeWithText("Texto 0").assertDoesNotExist()
    }

    @Test
    fun designerScreen_emptyCanvas_showsEmptyState() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Verificar mensaje de lienzo vacío
        composeTestRule.onNodeWithText("añade componentes", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun designerScreen_previewButton_exists() {
        var previewOpened = false

        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = { previewOpened = true }
                )
            }
        }

        // Buscar botón de preview en la top bar
        composeTestRule.onNodeWithContentDescription("Vista previa", substring = true)
            .assertExists()
    }

    @Test
    fun designerScreen_backButton_navigatesBack() {
        var navigatedBack = false

        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = { navigatedBack = true },
                    onOpenPreview = {}
                )
            }
        }

        // Click en botón de back
        composeTestRule.onNodeWithContentDescription("Navegar atrás", substring = true)
            .performClick()

        // Verificar navegación
        assert(navigatedBack) { "Should navigate back" }
    }

    @Test
    fun designerScreen_multipleComponents_allRenderOnCanvas() {
        composeTestRule.setContent {
            PocketTheme {
                DesignerScreen(
                    onNavigateBack = {},
                    onOpenPreview = {}
                )
            }
        }

        // Añadir múltiples componentes
        composeTestRule.onAllNodesWithText("Añadir")[0].performClick() // Texto
        composeTestRule.onAllNodesWithText("Añadir")[1].performClick() // Botón
        composeTestRule.onAllNodesWithText("Añadir")[0].performClick() // Otro Texto

        // Verificar que todos están en el lienzo
        composeTestRule.onNodeWithText("Texto 0").assertExists()
        composeTestRule.onNodeWithText("Botón 1").assertExists()
        composeTestRule.onNodeWithText("Texto 2").assertExists()
    }
}
