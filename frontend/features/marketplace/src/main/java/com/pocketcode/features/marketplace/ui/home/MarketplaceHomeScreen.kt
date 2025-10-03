@file:OptIn(ExperimentalMaterial3Api::class)

package com.pocketcode.features.marketplace.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.feedback.EmptyState
import com.pocketcode.core.ui.components.feedback.ErrorDisplay
import com.pocketcode.core.ui.components.feedback.LoadingIndicator
import com.pocketcode.core.ui.components.input.PocketSearchField
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.components.selection.PocketFilterChip
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.features.marketplace.ui.components.AssetCard
import java.util.Locale

@Composable
fun MarketplaceHomeScreen(
    viewModel: MarketplaceHomeViewModel = hiltViewModel(),
    onAssetClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onUploadClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val onInstrumentedAssetClick: (String, Boolean) -> Unit = { assetId, fromRecommendations ->
        viewModel.onAssetSelected(assetId, fromRecommendations)
        onAssetClick(assetId)
    }

    PocketScaffold(
        config = PocketScaffoldConfig(
            paddingValues = PaddingValues(
                horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
                vertical = SpacingTokens.Semantic.screenPaddingVertical
            ),
            isScrollable = false,
            backgroundColor = ColorTokens.background
        ),
        topBar = {
            PocketTopBar(
                title = "Marketplace",
                subtitle = "Descubre plantillas y recursos",
                navigationIcon = PocketIcons.ChevronLeft,
                onNavigationClick = onNavigateBack,
                actions = listOfNotNull(
                    onUploadClick?.let {
                        TopBarAction(
                            icon = PocketIcons.Add,
                            contentDescription = "Publicar recurso",
                            onClick = it
                        )
                    }
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Cargando recursos..."
                    )
                }

                uiState.error != null -> {
                    uiState.error?.let { message ->
                        ErrorDisplay(
                            error = message,
                            modifier = Modifier.align(Alignment.Center),
                            onRetry = viewModel::retryLoad
                        )
                    }
                }

                uiState.assets.isEmpty() -> {
                    EmptyState(
                        title = "Aún no hay recursos",
                        description = "Cuando haya plantillas disponibles aparecerán aquí. Puedes publicar la tuya para la comunidad.",
                        icon = PocketIcons.ShoppingCart,
                        actionText = onUploadClick?.let { "Publicar recurso" },
                        onAction = onUploadClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    MarketplaceContent(
                        state = uiState,
                        onAssetClick = { assetId -> onInstrumentedAssetClick(assetId, false) },
                        onRecommendedAssetClick = { assetId -> onInstrumentedAssetClick(assetId, true) },
                        onRecommendationsShown = viewModel::onRecommendationsImpression,
                        onSearchChange = viewModel::onSearchQueryChange,
                        onClearSearch = viewModel::onClearSearch,
                        onFilterSelected = viewModel::onRatingFilterSelected,
                        onClearFilters = viewModel::clearFilters,
                        onRetry = viewModel::retryLoad
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetList(
    assets: List<Asset>,
    onAssetClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = SpacingTokens.Semantic.contentSpacingNormal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        items(
            items = assets,
            key = { it.id }
        ) { asset ->
            AssetCard(
                asset = asset,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAssetClick(asset.id) }
            )
        }
    }
}

@Composable
private fun MarketplaceContent(
    state: MarketplaceHomeState,
    onAssetClick: (String) -> Unit,
    onRecommendedAssetClick: (String) -> Unit,
    onRecommendationsShown: (List<String>) -> Unit,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterSelected: (MarketplaceRatingFilter) -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            PocketSearchField(
                value = state.query,
                onValueChange = onSearchChange,
                onClear = onClearSearch,
                placeholder = "Buscar recursos...",
                modifier = Modifier.fillMaxWidth()
            )

            RatingFilterRow(
                selectedFilter = state.ratingFilter,
                onFilterSelected = onFilterSelected
            )

            if (state.isOffline) {
                OfflineBanner(onRetry = onRetry)
            }

            if (state.metrics.hasEngagement) {
                MarketplaceMetricsCard(metrics = state.metrics)
            }

            if (state.recommendedAssets.isNotEmpty()) {
                RecommendedAssetsSection(
                    assets = state.recommendedAssets,
                    onAssetClick = onRecommendedAssetClick,
                    onImpressions = onRecommendationsShown
                )
            }

            if (state.hasActiveFilters) {
                RowWithSpacing { 
                    Text(
                        text = "${state.filteredAssets.size} resultados",
                        style = TypographyTokens.Body.small,
                        color = ColorTokens.onSurfaceVariant
                    )
                    PocketButton(
                        text = "Limpiar filtros",
                        onClick = onClearFilters,
                        variant = ComponentTokens.ButtonVariant.Text
                    )
                }
            }
        }

        when {
            state.filteredAssets.isEmpty() -> {
                val hasCachedAssets = state.assets.isNotEmpty()
                val showOfflineEmpty = state.isOffline && !hasCachedAssets
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = if (showOfflineEmpty) "Sin conexión" else "Sin resultados",
                        description = when {
                            showOfflineEmpty -> "No pudimos conectar con el Marketplace. Revisa tu conexión e inténtalo de nuevo."
                            state.hasActiveFilters -> "No encontramos recursos que coincidan con tu búsqueda."
                            else -> "Aún no hay recursos disponibles."
                        },
                        icon = if (showOfflineEmpty) PocketIcons.CloudOff else PocketIcons.Search,
                        actionText = if (showOfflineEmpty) "Reintentar" else null,
                        onAction = if (showOfflineEmpty) onRetry else null
                    )
                }
            }

            else -> {
                AssetList(
                    assets = state.filteredAssets,
                    onAssetClick = onAssetClick,
                    modifier = Modifier.weight(1f, fill = true)
                )
            }
        }
    }
}

@Composable
private fun RatingFilterRow(
    selectedFilter: MarketplaceRatingFilter,
    onFilterSelected: (MarketplaceRatingFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal),
        contentPadding = PaddingValues(horizontal = SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        items(MarketplaceRatingFilter.values()) { filter ->
            PocketFilterChip(
                label = filter.label,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun RowWithSpacing(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun OfflineBanner(onRetry: () -> Unit) {
    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = ComponentTokens.CardVariant.Filled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            Icon(
                imageVector = PocketIcons.CloudOff,
                contentDescription = null,
                tint = ColorTokens.onSurfaceVariant
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
            ) {
                Text(
                    text = "Modo sin conexión",
                    style = TypographyTokens.Title.small,
                    color = ColorTokens.onSurface
                )
                Text(
                    text = "Mostramos los últimos datos disponibles. Reintenta cuando recuperes la conexión.",
                    style = TypographyTokens.Body.small,
                    color = ColorTokens.onSurfaceVariant
                )
            }
            PocketButton(
                text = "Reintentar",
                onClick = onRetry,
                variant = ComponentTokens.ButtonVariant.Outline,
                size = ComponentTokens.ButtonSize.Small
            )
        }
    }
}

@Composable
private fun MarketplaceMetricsCard(metrics: MarketplaceMetrics) {
    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = ComponentTokens.CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
        ) {
            Text(
                text = "Actividad reciente",
                style = TypographyTokens.Title.small,
                color = ColorTokens.onSurface
            )
            MetricLine(label = "Búsquedas realizadas", value = metrics.searchInteractions)
            MetricLine(label = "Cambios de filtro", value = metrics.filterSelections)
            MetricLine(label = "Recursos abiertos", value = metrics.assetOpens)
            if (metrics.offlineRetries > 0) {
                MetricLine(label = "Reintentos sin conexión", value = metrics.offlineRetries)
            }
            MetricLine(label = "Resultados visibles", value = metrics.lastResultCount)
            if (metrics.recommendationImpressions > 0) {
                MetricLine(
                    label = "Impresiones recomendados",
                    value = metrics.recommendationImpressions
                )
            }
            if (metrics.recommendationOpens > 0) {
                MetricLine(
                    label = "Aperturas desde recomendados",
                    value = metrics.recommendationOpens
                )
                val conversionPercentage = metrics.recommendationConversionRate * 100
                Text(
                    text = "Conversión recomendados: ${String.format(Locale.getDefault(), "%.1f%%", conversionPercentage)}",
                    style = TypographyTokens.Body.small,
                    color = ColorTokens.onSurfaceVariant
                )
            }
            metrics.lastOpenedAssetName?.let { assetName ->
                Text(
                    text = "Último recurso abierto: $assetName",
                    style = TypographyTokens.Body.small,
                    color = ColorTokens.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecommendedAssetsSection(
    assets: List<Asset>,
    onAssetClick: (String) -> Unit,
    onImpressions: (List<String>) -> Unit
) {
    val assetIds = remember(assets) { assets.map { it.id } }
    LaunchedEffect(assetIds) {
        if (assetIds.isNotEmpty()) {
            onImpressions(assetIds)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
    ) {
        Text(
            text = "Recomendados para ti",
            style = TypographyTokens.Title.small,
            color = ColorTokens.onSurface
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal),
            contentPadding = PaddingValues(horizontal = SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            items(assets, key = { it.id }) { asset ->
                AssetCard(
                    asset = asset,
                    modifier = Modifier.width(260.dp),
                    onClick = { onAssetClick(asset.id) },
                    showDescription = false,
                    primaryActionText = "Abrir",
                    onPrimaryActionClick = { onAssetClick(asset.id) }
                )
            }
        }
    }
}

@Composable
private fun MetricLine(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TypographyTokens.Body.small,
            color = ColorTokens.onSurfaceVariant
        )
        Text(
            text = value.toString(),
            style = TypographyTokens.Body.small,
            color = ColorTokens.onSurface
        )
    }
}
