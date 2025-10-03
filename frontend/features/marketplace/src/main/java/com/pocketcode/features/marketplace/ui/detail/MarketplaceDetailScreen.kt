@file:OptIn(ExperimentalMaterial3Api::class)

package com.pocketcode.features.marketplace.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.feedback.EmptyState
import com.pocketcode.core.ui.components.feedback.ErrorDisplay
import com.pocketcode.core.ui.components.feedback.LoadingIndicator
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.model.Review
import java.text.DateFormat

@Composable
fun MarketplaceDetailScreen(
    assetId: String,
    viewModel: MarketplaceDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(assetId) {
        viewModel.refreshAsset(assetId)
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
                title = uiState.asset?.name ?: "Detalle del recurso",
                subtitle = uiState.asset?.authorId?.let { "Creado por $it" },
                navigationIcon = PocketIcons.ChevronLeft,
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.asset == null -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Cargando recurso..."
                    )
                }

                uiState.error != null -> {
                    ErrorDisplay(
                        error = uiState.error ?: "No se pudo cargar el recurso",
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.refreshAsset(assetId) }
                    )
                }

                uiState.asset != null -> {
                    AssetDetailsContent(
                        asset = uiState.asset!!,
                        reviews = uiState.reviews
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetDetailsContent(
    asset: Asset,
    reviews: List<Review>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = SpacingTokens.Semantic.contentSpacingNormal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        item {
            AssetOverviewCard(asset)
        }

        item {
            Text(
                text = "Reseñas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (reviews.isEmpty()) {
            item {
                EmptyState(
                    title = "No hay reseñas",
                    description = "Sé la primera persona en valorar este recurso.",
                    icon = PocketIcons.Message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SpacingTokens.Semantic.contentSpacingNormal)
                )
            }
        } else {
            items(
                items = reviews,
                key = { it.id }
            ) { review ->
                ReviewCard(review = review)
            }
        }
    }
}

@Composable
private fun AssetOverviewCard(asset: Asset) {
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }
    val createdAt = remember(asset.createdAt) { dateFormatter.format(asset.createdAt) }
    val updatedAt = remember(asset.updatedAt) { dateFormatter.format(asset.updatedAt) }

    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated
    ) {
        Text(
            text = asset.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Autor: ${asset.authorId}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = asset.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SpacingTokens.Semantic.contentSpacingNormal)
        )

        ReviewSummaryRow(
            rating = asset.averageRating,
            count = asset.ratingCount
        )

        Text(
            text = "Publicado: $createdAt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Actualizado: $updatedAt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewSummaryRow(rating: Double, count: Int) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.Semantic.contentSpacingNormal),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = PocketIcons.Star,
            contentDescription = null,
            tint = ColorTokens.Semantic.warning500
        )
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "($count reseñas)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewCard(review: Review) {
    val dateFormatter = remember { DateFormat.getDateInstance(DateFormat.MEDIUM) }
    val reviewDate = remember(review.createdAt) { dateFormatter.format(review.createdAt) }

    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Outlined
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = PocketIcons.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
            ) {
                Text(
                    text = review.userId,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = reviewDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
