package com.pocketcode.features.marketplace.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.domain.marketplace.model.Asset

/**
 * Tarjeta reutilizable para representar un recurso del Marketplace.
 *
 * Ofrece cabecera, descripción, rating y una acción primaria opcional reutilizando
 * los wrappers Pocket, de modo que la misma estructura se pueda emplear en listas,
 * grids u otros contenedores.
 */
@Composable
fun AssetCard(
    asset: Asset,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    primaryActionText: String = "Ver detalle",
    onPrimaryActionClick: (() -> Unit)? = onClick,
    showRating: Boolean = true,
    showDescription: Boolean = true
) {
    PocketCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            Text(
                text = asset.name,
                style = TypographyTokens.Headline.small,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (showDescription) {
                Text(
                    text = asset.description,
                    style = TypographyTokens.Body.medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showRating) {
                RatingRow(
                    rating = asset.averageRating,
                    ratingCount = asset.ratingCount
                )
            }

            if (onPrimaryActionClick != null) {
                PocketButton(
                    text = primaryActionText,
                    onClick = onPrimaryActionClick,
                    variant = ComponentTokens.ButtonVariant.Outline,
                    size = ComponentTokens.ButtonSize.Small,
                    trailingIcon = {
                        Icon(
                            imageVector = PocketIcons.ArrowForward,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun RatingRow(
    rating: Double,
    ratingCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            style = TypographyTokens.Title.small,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "($ratingCount reseñas)",
            style = TypographyTokens.Body.small,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
