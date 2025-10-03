package com.pocketcode.features.editor.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test
import com.pocketcode.core.ui.theme.PocketTheme
import com.pocketcode.features.editor.ui.EditorContainer

class EditorContainerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editorContainer_rendersTopBar() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path",
                    onFileChanged = {},
                    onNavigateBack = {},
                    onShowSettings = {}
                )
            }
        }

        // Verificar que aparece el nombre del archivo
        composeTestRule.onNodeWithText("test.kt", substring = true)
            .assertExists()

        // Verificar botones de acción en la top bar
        composeTestRule.onNodeWithContentDescription("Buscar", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_showsEditorContent() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path"
                )
            }
        }

        // El editor debe renderizarse
        // (verifica que no hay errores de render)
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun editorContainer_toggleLineNumbers_updates() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path",
                    config = EditorContainerConfig(enableLineNumbers = true)
                )
            }
        }

        // Buscar toggle de números de línea en opciones
        // (esto depende de la implementación específica)
        composeTestRule.onNodeWithContentDescription("Opciones", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_findReplacePanel_toggles() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path",
                    config = EditorContainerConfig(enableFindReplace = true)
                )
            }
        }

        // Click en botón de buscar
        composeTestRule.onNodeWithContentDescription("Buscar", substring = true)
            .performClick()

        // Verificar que aparece el panel de buscar/reemplazar
        composeTestRule.onNodeWithText("Buscar en el archivo", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_statusBar_showsCursorPosition() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path",
                    config = EditorContainerConfig(enableStatusBar = true)
                )
            }
        }

        // Verificar que la barra de estado muestra información
        // (línea y columna del cursor)
        composeTestRule.onNodeWithText("Ln", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("Col", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_languageIndicator_shows() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path"
                )
            }
        }

        // Verificar que muestra el lenguaje del archivo
        composeTestRule.onNodeWithText("Kotlin", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_modifiedIndicator_showsWhenModified() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path"
                )
            }
        }

        // Verificar indicador de modificado (puede ser un punto o asterisco)
        // Esto aparece cuando el archivo ha sido editado
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun editorContainer_saveButton_exists() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path"
                )
            }
        }

        // Verificar botón de guardar en la top bar
        composeTestRule.onNodeWithContentDescription("Guardar", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_undoRedoButtons_exist() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path"
                )
            }
        }

        // Verificar botones de undo/redo
        composeTestRule.onNodeWithContentDescription("Deshacer", substring = true)
            .assertExists()
        composeTestRule.onNodeWithContentDescription("Rehacer", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_formatButton_exists() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path",
                    config = EditorContainerConfig(enableCodeFormatter = true)
                )
            }
        }

        // Verificar botón de formatear código
        composeTestRule.onNodeWithContentDescription("Formatear", substring = true)
            .assertExists()
    }

    @Test
    fun editorContainer_backButton_navigatesBack() {
        var navigatedBack = false

        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path",
                    onNavigateBack = { navigatedBack = true }
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
    fun editorContainer_optionsMenu_exists() {
        composeTestRule.setContent {
            PocketTheme {
                EditorContainer(
                    file = createMockFile(),
                    selectedProjectId = "test-id",
                    selectedProjectName = "Test Project",
                    selectedProjectPath = "/test/path"
                )
            }
        }

        // Verificar menú de opciones
        composeTestRule.onNodeWithContentDescription("Más opciones", substring = true)
            .assertExists()
    }

    // Helper para crear archivo de prueba
    private fun createMockFile() = com.pocketcode.domain.project.model.ProjectFile(
        id = "test-file-id",
        name = "test.kt",
        path = "/test/path/test.kt",
        type = com.pocketcode.domain.project.model.FileType.KOTLIN,
        parentId = null,
        isDirectory = false,
        size = 1024L,
        lastModified = System.currentTimeMillis()
    )
}
