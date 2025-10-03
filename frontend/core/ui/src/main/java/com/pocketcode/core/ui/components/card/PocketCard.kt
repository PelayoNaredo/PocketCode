package com.pocketcode.core.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant

/**
 * Componente de tarjeta (Card) del sistema de diseño Pocket.
 *
 * PocketCard es un contenedor elevado que agrupa contenido relacionado.
 * Soporta múltiples variantes para diferentes contextos:
 * - Filled: Card estándar con elevación
 * - Elevated: Card con elevación aumentada
 * - Outlined: Card con borde sin elevación
 *
 * Características:
 * - Soporte para clics (opcional)
 * - Variantes configurables
 * - Elevación y bordes personalizables
 * - Integración con tokens del design system
 *
 * @param modifier Modificador para la card
 * @param onClick Callback cuando la card es clickeable (null = no clickeable)
 * @param variant Variante visual de la card (Filled, Elevated, Outlined)
 * @param enabled Si la card es interactiva (solo aplica si onClick != null)
 * @param shape Forma de la card
 * @param colors Colores personalizados (null = usa defaults de la variante)
 * @param elevation Elevación personalizada (null = usa default de la variante)
 * @param border Borde personalizado (null = usa default de la variante)
 * @param interactionSource Source de interacciones para animaciones
 * @param content Contenido de la card (ColumnScope)
 *
 * @example Card básica (no clickeable):
 * ```kotlin
 * PocketCard {
 *     Column(modifier = Modifier.padding(16.dp)) {
 *         Text("Título", style = MaterialTheme.typography.titleMedium)
 *         Text("Descripción", style = MaterialTheme.typography.bodyMedium)
 *     }
 * }
 * ```
 *
 * @example Card clickeable:
 * ```kotlin
 * PocketCard(
 *     onClick = { viewModel.openProject(project.id) }
 * ) {
 *     ProjectCardContent(project)
 * }
 * ```
 *
 * @example Card con variante elevada:
 * ```kotlin
 * PocketCard(
 *     variant = CardVariant.Elevated,
 *     onClick = { /* ... */ }
 * ) {
 *     Text("Card elevada")
 * }
 * ```
 *
 * @example Card outlined:
 * ```kotlin
 * PocketCard(
 *     variant = CardVariant.Outlined
 * ) {
 *     Text("Card con borde")
 * }
 * ```
 */
@Composable
fun PocketCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: CardVariant = CardVariant.Filled,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors? = null,
    elevation: CardElevation? = null,
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit
) {
    // Determinar colores según variante
    val cardColors = colors ?: when (variant) {
        CardVariant.Filled -> CardDefaults.cardColors()
        CardVariant.Elevated -> CardDefaults.elevatedCardColors()
        CardVariant.Outlined -> CardDefaults.outlinedCardColors()
    }

    // Determinar elevación según variante
    val cardElevation = elevation ?: when (variant) {
        CardVariant.Filled -> CardDefaults.cardElevation()
        CardVariant.Elevated -> CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        )
        CardVariant.Outlined -> CardDefaults.outlinedCardElevation()
    }

    // Determinar borde según variante
    val cardBorder = border ?: when (variant) {
        CardVariant.Outlined -> BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
        else -> null
    }

    // Renderizar card clickeable o no clickeable
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            border = cardBorder,
            interactionSource = interactionSource,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            elevation = cardElevation,
            border = cardBorder,
            content = content
        )
    }
}

/**
 * Variante especializada de PocketCard para tarjetas de proyecto.
 *
 * ProjectCard es una card prediseñada para mostrar información de proyectos
 * con un layout consistente.
 *
 * @param modifier Modificador para la card
 * @param onClick Callback cuando se hace clic en la card
 * @param enabled Si la card es interactiva
 * @param content Contenido de la card
 *
 * @example
 * ```kotlin
 * ProjectCard(
 *     onClick = { openProject(project) }
 * ) {
 *     Column(modifier = Modifier.padding(16.dp)) {
 *         Text(project.name, style = MaterialTheme.typography.titleLarge)
 *         Text(project.description, style = MaterialTheme.typography.bodyMedium)
 *         Text("Modificado: ${project.lastModified}")
 *     }
 * }
 * ```
 */
@Composable
fun ProjectCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    PocketCard(
        modifier = modifier,
        onClick = onClick,
        variant = CardVariant.Filled,
        enabled = enabled,
        content = content
    )
}
