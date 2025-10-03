package com.pocketcode.features.marketplace.ui.home

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pocketcode.core.ui.theme.PocketTheme
import com.pocketcode.domain.marketplace.model.MarketplaceAsset
import com.pocketcode.domain.marketplace.model.AssetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI instrumentados para MarketplaceHomeScreen.
 * 
 * Estos tests validan:
 * - Renderizado de lista de assets
 * - Búsqueda y filtrado
 * - Estados de carga, error y vacío
 * - Interacción con filtros por rating
 * - Carrusel de recursos recomendados
 * - Banner offline
 */
@RunWith(AndroidJUnit4::class)
class MarketplaceHomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun marketplaceScreen_displaysAssetList() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(
                createFakeAsset(id = "1", name = "Test Asset 1"),
                createFakeAsset(id = "2", name = "Test Asset 2"),
                createFakeAsset(id = "3", name = "Test Asset 3")
            )
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que los assets se muestran
        composeTestRule.onNodeWithText("Test Asset 1").assertExists()
        composeTestRule.onNodeWithText("Test Asset 2").assertExists()
        composeTestRule.onNodeWithText("Test Asset 3").assertExists()
    }

    @Test
    fun marketplaceScreen_showsLoadingState() {
        val fakeViewModel = FakeMarketplaceViewModel(isLoading = true)

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que se muestra el indicador de carga
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun marketplaceScreen_showsEmptyState() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = emptyList(),
            isLoading = false
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que se muestra el estado vacío
        composeTestRule.onNodeWithText("No hay recursos disponibles").assertExists()
    }

    @Test
    fun marketplaceScreen_showsErrorState() {
        val fakeViewModel = FakeMarketplaceViewModel(
            error = "Error loading assets",
            isLoading = false
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que se muestra el error
        composeTestRule.onNodeWithText("Error loading assets").assertExists()
        
        // Verificar que existe botón de reintentar
        composeTestRule.onNodeWithText("Reintentar").assertExists()
    }

    @Test
    fun searchField_canBeInteracted() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(
                createFakeAsset(id = "1", name = "JavaScript Library"),
                createFakeAsset(id = "2", name = "Python Script")
            )
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Buscar el campo de búsqueda
        val searchField = composeTestRule.onNodeWithContentDescription("Buscar recursos")
        searchField.assertExists()

        // Escribir en el campo de búsqueda
        searchField.performTextInput("JavaScript")

        // Verificar que el ViewModel recibió la consulta
        assert(fakeViewModel.currentSearchQuery == "JavaScript")
    }

    @Test
    fun ratingFilter_canBeApplied() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(
                createFakeAsset(id = "1", name = "Asset 1", rating = 4.5f),
                createFakeAsset(id = "2", name = "Asset 2", rating = 3.0f),
                createFakeAsset(id = "3", name = "Asset 3", rating = 5.0f)
            )
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Hacer clic en el filtro de rating alto
        composeTestRule.onNodeWithText("4+ estrellas").performClick()

        // Verificar que el filtro se aplicó
        assert(fakeViewModel.currentMinRating == 4.0f)
    }

    @Test
    fun assetCard_canBeClicked() {
        var clickedAssetId: String? = null
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(createFakeAsset(id = "test-123", name = "Clickable Asset"))
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = { clickedAssetId = it },
                    onNavigateBack = {}
                )
            }
        }

        // Hacer clic en el asset
        composeTestRule.onNodeWithText("Clickable Asset").performClick()

        // Verificar que se llamó el callback con el ID correcto
        assert(clickedAssetId == "test-123")
    }

    @Test
    fun recommendedSection_displaysWhenAvailable() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(createFakeAsset(id = "1", name = "Regular Asset")),
            recommendedAssets = listOf(
                createFakeAsset(id = "rec-1", name = "Recommended Asset 1"),
                createFakeAsset(id = "rec-2", name = "Recommended Asset 2")
            )
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que existe la sección de recomendados
        composeTestRule.onNodeWithText("Recomendados para ti").assertExists()
        composeTestRule.onNodeWithText("Recommended Asset 1").assertExists()
        composeTestRule.onNodeWithText("Recommended Asset 2").assertExists()
    }

    @Test
    fun offlineBanner_showsWhenOffline() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(createFakeAsset(id = "1", name = "Cached Asset")),
            isOffline = true
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que se muestra el banner offline
        composeTestRule.onNodeWithText("Sin conexión").assertExists()
        composeTestRule.onNodeWithText("Mostrando recursos en caché").assertExists()
    }

    @Test
    fun multipleFilters_canBeCombined() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(
                createFakeAsset(id = "1", name = "High Rated Library", rating = 4.8f),
                createFakeAsset(id = "2", name = "Low Rated Script", rating = 2.5f)
            )
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Aplicar filtro de rating
        composeTestRule.onNodeWithText("4+ estrellas").performClick()

        // Buscar por texto
        composeTestRule.onNodeWithContentDescription("Buscar recursos")
            .performTextInput("Library")

        // Verificar que ambos filtros se aplicaron
        assert(fakeViewModel.currentMinRating == 4.0f)
        assert(fakeViewModel.currentSearchQuery == "Library")
    }

    @Test
    fun errorRetry_triggersReload() {
        val fakeViewModel = FakeMarketplaceViewModel(
            error = "Network error",
            isLoading = false
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Hacer clic en reintentar
        composeTestRule.onNodeWithText("Reintentar").performClick()

        // Verificar que se llamó a retry
        assert(fakeViewModel.retryCallCount > 0)
    }

    @Test
    fun pullToRefresh_triggersReload() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(createFakeAsset(id = "1", name = "Asset 1"))
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Simular pull to refresh (swipe down desde la parte superior)
        composeTestRule.onRoot().performTouchInput {
            swipeDown(startY = 0f, endY = 500f)
        }

        // Verificar que se llamó a refresh
        assert(fakeViewModel.refreshCallCount > 0)
    }

    @Test
    fun assetRating_displaysCorrectly() {
        val fakeViewModel = FakeMarketplaceViewModel(
            assets = listOf(
                createFakeAsset(id = "1", name = "Rated Asset", rating = 4.5f)
            )
        )

        composeTestRule.setContent {
            PocketTheme {
                MarketplaceHomeScreen(
                    viewModel = fakeViewModel,
                    onAssetClick = {},
                    onNavigateBack = {}
                )
            }
        }

        // Verificar que el rating se muestra
        composeTestRule.onNodeWithText("4.5").assertExists()
    }

    // Helpers

    private fun createFakeAsset(
        id: String,
        name: String,
        rating: Float = 4.0f,
        description: String = "Test description"
    ) = MarketplaceAsset(
        id = id,
        name = name,
        description = description,
        authorId = "author-123",
        authorName = "Test Author",
        type = AssetType.LIBRARY,
        version = "1.0.0",
        rating = rating,
        downloadCount = 100,
        fileUrl = "https://example.com/$id.zip",
        thumbnailUrl = null,
        tags = listOf("test"),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    // Fake ViewModel

    private class FakeMarketplaceViewModel(
        private val assets: List<MarketplaceAsset> = emptyList(),
        private val recommendedAssets: List<MarketplaceAsset> = emptyList(),
        private val isLoading: Boolean = false,
        private val error: String? = null,
        private val isOffline: Boolean = false
    ) {
        var currentSearchQuery: String = ""
            private set
        var currentMinRating: Float = 0f
            private set
        var retryCallCount: Int = 0
            private set
        var refreshCallCount: Int = 0
            private set

        private val _uiState = MutableStateFlow(
            MarketplaceUiState(
                assets = assets,
                recommendedAssets = recommendedAssets,
                isLoading = isLoading,
                error = error,
                isOffline = isOffline
            )
        )
        val uiState: StateFlow<MarketplaceUiState> = _uiState

        fun search(query: String) {
            currentSearchQuery = query
        }

        fun filterByRating(minRating: Float) {
            currentMinRating = minRating
        }

        fun retry() {
            retryCallCount++
        }

        fun refresh() {
            refreshCallCount++
        }
    }

    // Data class auxiliar
    data class MarketplaceUiState(
        val assets: List<MarketplaceAsset> = emptyList(),
        val recommendedAssets: List<MarketplaceAsset> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isOffline: Boolean = false
    )
}
