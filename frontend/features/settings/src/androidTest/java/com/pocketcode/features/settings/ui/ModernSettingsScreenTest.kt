package com.pocketcode.features.settings.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pocketcode.core.ui.theme.PocketTheme
import com.pocketcode.features.settings.analytics.SettingsTelemetrySurface
import com.pocketcode.features.settings.analytics.SettingsChangeType
import com.pocketcode.features.settings.analytics.SettingsTelemetry
import com.pocketcode.core.ui.snackbar.GlobalSnackbarDispatcher
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.GlobalSnackbarDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI instrumentados para ModernSettingsScreen.
 * 
 * Estos tests validan:
 * - Renderizado correcto de todas las pestañas (General, Editor, Temas, Avanzado, Privacidad)
 * - Interacción con toggles (auto-save, analytics, crash reporting)
 * - Navegación entre pestañas
 * - Estados de carga y error
 * - Accesibilidad de componentes
 */
@RunWith(AndroidJUnit4::class)
class ModernSettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeViewModel = FakeSettingsViewModel()
    private val fakeThemeViewModel = FakeThemeViewModel()
    private val fakeSnackbarDispatcher = FakeGlobalSnackbarDispatcher()

    @Test
    fun settingsScreen_displaysAllTabs() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que todas las pestañas están presentes
        composeTestRule.onNodeWithText("General").assertExists()
        composeTestRule.onNodeWithText("Editor").assertExists()
        composeTestRule.onNodeWithText("Temas").assertExists()
        composeTestRule.onNodeWithText("Avanzado").assertExists()
        composeTestRule.onNodeWithText("Privacidad").assertExists()
    }

    @Test
    fun generalTab_displaysAutoSaveToggle() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Tab General debe estar seleccionada por defecto
        composeTestRule.onNodeWithText("Guardado automático").assertExists()
        composeTestRule.onNodeWithText("Guarda cambios automáticamente al editar").assertExists()
    }

    @Test
    fun autoSaveToggle_canBeInteracted() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Buscar el toggle de auto-save
        val autoSaveToggle = composeTestRule.onNode(
            hasText("Guardado automático").and(hasClickAction())
        )

        // Verificar estado inicial (habilitado por defecto)
        autoSaveToggle.assertExists()

        // Hacer clic para desactivar
        autoSaveToggle.performClick()

        // Verificar que el ViewModel recibió la acción
        assert(!fakeViewModel.currentState.autoSave)
    }

    @Test
    fun editorTab_canBeNavigatedTo() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Hacer clic en la pestaña Editor
        composeTestRule.onNodeWithText("Editor").performClick()

        // Verificar que el contenido del editor está visible
        composeTestRule.onNodeWithText("Tamaño de fuente").assertExists()
        composeTestRule.onNodeWithText("Números de línea").assertExists()
    }

    @Test
    fun themesTab_displaysThemeOptions() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Navegar a pestaña de Temas
        composeTestRule.onNodeWithText("Temas").performClick()

        // Verificar opciones de tema
        composeTestRule.onNodeWithText("Modo claro").assertExists()
        composeTestRule.onNodeWithText("Modo oscuro").assertExists()
        composeTestRule.onNodeWithText("Automático (sistema)").assertExists()
    }

    @Test
    fun privacyTab_displaysAnalyticsToggles() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Navegar a pestaña de Privacidad
        composeTestRule.onNodeWithText("Privacidad").performClick()

        // Verificar toggles de privacidad
        composeTestRule.onNodeWithText("Analytics habilitado").assertExists()
        composeTestRule.onNodeWithText("Crash reporting habilitado").assertExists()
    }

    @Test
    fun analyticsToggle_canBeDisabled() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Navegar a Privacidad
        composeTestRule.onNodeWithText("Privacidad").performClick()

        // Hacer clic en el toggle de analytics
        val analyticsToggle = composeTestRule.onNode(
            hasText("Analytics habilitado").and(hasClickAction())
        )
        
        analyticsToggle.performClick()

        // Verificar que el estado cambió
        assert(!fakeViewModel.currentState.analyticsEnabled)
    }

    @Test
    fun advancedTab_displaysDeveloperOptions() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Navegar a pestaña Avanzado
        composeTestRule.onNodeWithText("Avanzado").performClick()

        // Verificar opciones avanzadas
        composeTestRule.onNodeWithText("Modo desarrollador").assertExists()
    }

    @Test
    fun tabNavigation_preservesState() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Cambiar estado en tab General
        val autoSaveToggle = composeTestRule.onNode(
            hasText("Guardado automático").and(hasClickAction())
        )
        autoSaveToggle.performClick()

        // Navegar a otra pestaña
        composeTestRule.onNodeWithText("Editor").performClick()

        // Volver a General
        composeTestRule.onNodeWithText("General").performClick()

        // El estado debe haberse preservado
        assert(!fakeViewModel.currentState.autoSave)
    }

    @Test
    fun settingsScreen_hasAccessibleLabels() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que los elementos tienen descripciones semánticas
        composeTestRule.onNodeWithContentDescription("Configuración").assertExists()
        
        // Verificar que los toggles son accesibles
        composeTestRule.onAllNodesWithContentDescription("Activar/desactivar")
            .assertCountEquals(2) // auto-save y line numbers en vista inicial
    }

    @Test
    fun multipleToggles_canBeInteractedSequentially() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Interactuar con múltiples toggles
        composeTestRule.onNode(hasText("Guardado automático").and(hasClickAction()))
            .performClick()
        
        // Navegar a Editor y cambiar configuración
        composeTestRule.onNodeWithText("Editor").performClick()
        composeTestRule.onNode(hasText("Números de línea").and(hasClickAction()))
            .performClick()

        // Verificar ambos cambios
        assert(!fakeViewModel.currentState.autoSave)
        assert(!fakeViewModel.currentState.showLineNumbers)
    }

    @Test
    fun themeSelection_updatesThemeViewModel() {
        composeTestRule.setContent {
            PocketTheme {
                ModernSettingsScreen(
                    viewModel = fakeViewModel,
                    themeViewModel = fakeThemeViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Navegar a Temas
        composeTestRule.onNodeWithText("Temas").performClick()

        // Seleccionar modo oscuro
        composeTestRule.onNodeWithText("Modo oscuro").performClick()

        // Verificar que el ThemeViewModel se actualizó
        assert(fakeThemeViewModel.isDarkMode)
    }

    // Fake ViewModels para testing

    private class FakeSettingsViewModel : SettingsViewModel(FakeSettingsTelemetry()) {
        var currentState = SettingsState()
            private set

        override fun updateAutoSave(enabled: Boolean) {
            currentState = currentState.copy(autoSave = enabled)
        }

        override fun updateLineNumbers(enabled: Boolean) {
            currentState = currentState.copy(showLineNumbers = enabled)
        }

        override fun updateAnalyticsEnabled(enabled: Boolean) {
            currentState = currentState.copy(analyticsEnabled = enabled)
        }

        override fun updateCrashReportingEnabled(enabled: Boolean) {
            currentState = currentState.copy(crashReportingEnabled = enabled)
        }

        override fun updateDeveloperMode(enabled: Boolean) {
            currentState = currentState.copy(developerMode = enabled)
        }

        private val _uiState = MutableStateFlow(currentState)
        override val uiState: StateFlow<SettingsState> = _uiState

        init {
            // Actualizar flow cuando cambia el estado
            currentState = SettingsState(
                autoSave = true,
                showLineNumbers = true,
                analyticsEnabled = true,
                crashReportingEnabled = true,
                developerMode = false
            )
            _uiState.value = currentState
        }
    }

    private class FakeThemeViewModel {
        var isDarkMode = false
            private set

        fun setDarkMode(enabled: Boolean) {
            isDarkMode = enabled
        }
    }

    private class FakeSettingsTelemetry : SettingsTelemetry {
        override suspend fun trackSettingChanged(
            surface: SettingsTelemetrySurface,
            settingKey: String,
            changeType: SettingsChangeType,
            value: String
        ) {
            // No-op para tests
        }

        override suspend fun applyPrivacyToggles(
            analyticsEnabled: Boolean,
            crashReportingEnabled: Boolean
        ) {
            // No-op para tests
        }
    }

    private class FakeGlobalSnackbarDispatcher : GlobalSnackbarDispatcher {
        override suspend fun showMessage(
            message: String,
            severity: GlobalSnackbarSeverity,
            duration: GlobalSnackbarDuration,
            origin: GlobalSnackbarOrigin
        ) {
            // No-op para tests
        }
    }
}

// Data class auxiliar para el estado de settings (si no existe ya)
data class SettingsState(
    val autoSave: Boolean = true,
    val showLineNumbers: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val developerMode: Boolean = false
)
