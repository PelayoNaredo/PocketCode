package com.pocketcode.core.ui.components.selection

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Filter chip (chip de filtro) del sistema de diseño Pocket.
 *
 * PocketFilterChip es un componente para selección/filtrado que incluye:
 * - Estado seleccionado/no seleccionado
 * - Icono opcional (leading)
 * - Icono de check cuando está seleccionado
 * - Animaciones de transición
 * - Integración con listas de filtros
 *
 * Los filter chips se usan típicamente en:
 * - Filtros de búsqueda
 * - Selección de categorías
 * - Tags/etiquetas interactivas
 * - Opciones de configuración rápida
 *
 * @param selected Si el chip está seleccionado
 * @param onClick Callback cuando se hace clic
 * @param label Texto del chip
 * @param modifier Modificador para el chip
 * @param enabled Si el chip está habilitado
 * @param leadingIcon Icono al inicio (opcional)
 * @param trailingIcon Icono al final (opcional, por defecto muestra check si selected)
 * @param colors Colores personalizados (null = usa defaults)
 * @param interactionSource Source de interacciones para animaciones
 *
 * @example Uso básico:
 * ```kotlin
 * var isSelected by remember { mutableStateOf(false) }
 * PocketFilterChip(
 *     selected = isSelected,
 *     onClick = { isSelected = !isSelected },
 *     label = { Text("Kotlin") }
 * )
 * ```
 *
 * @example Con icono:
 * ```kotlin
 * var showImages by remember { mutableStateOf(true) }
 * PocketFilterChip(
 *     selected = showImages,
 *     onClick = { showImages = !showImages },
 *     label = { Text("Imágenes") },
 *     leadingIcon = {
 *         Icon(
 *             imageVector = Icons.Default.Image,
 *             contentDescription = null,
 *             modifier = Modifier.size(FilterChipDefaults.IconSize)
 *         )
 *     }
 * )
 * ```
 *
 * @example Lista de filtros múltiples:
 * ```kotlin
 * val filters = listOf("Todos", "Kotlin", "Java", "Python", "JavaScript")
 * var selectedFilters by remember { mutableStateOf(setOf("Todos")) }
 *
 * FlowRow(
 *     horizontalArrangement = Arrangement.spacedBy(8.dp)
 * ) {
 *     filters.forEach { filter ->
 *         PocketFilterChip(
 *             selected = filter in selectedFilters,
 *             onClick = {
 *                 selectedFilters = if (filter in selectedFilters) {
 *                     selectedFilters - filter
 *                 } else {
 *                     selectedFilters + filter
 *                 }
 *             },
 *             label = { Text(filter) }
 *         )
 *     }
 * }
 * ```
 *
 * @example En pantalla de marketplace:
 * ```kotlin
 * @Composable
 * fun MarketplaceFilters(
 *     selectedCategories: Set<AssetCategory>,
 *     onCategoryToggle: (AssetCategory) -> Unit
 * ) {
 *     LazyRow(
 *         horizontalArrangement = Arrangement.spacedBy(8.dp),
 *         contentPadding = PaddingValues(horizontal = 16.dp)
 *     ) {
 *         items(AssetCategory.values()) { category ->
 *             PocketFilterChip(
 *                 selected = category in selectedCategories,
 *                 onClick = { onCategoryToggle(category) },
 *                 label = { Text(category.displayName) },
 *                 leadingIcon = {
 *                     Icon(
 *                         imageVector = category.icon,
 *                         contentDescription = null,
 *                         modifier = Modifier.size(FilterChipDefaults.IconSize)
 *                     )
 *                 }
 *             )
 *         }
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = if (selected && leadingIcon == null && trailingIcon == null) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Seleccionado",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            leadingIcon
        },
        trailingIcon = trailingIcon,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * Variante de PocketFilterChip que acepta texto simple.
 *
 * Simplifica la creación de chips cuando solo se necesita texto.
 *
 * @example
 * ```kotlin
 * PocketFilterChip(
 *     selected = isKotlinSelected,
 *     onClick = { isKotlinSelected = !isKotlinSelected },
 *     label = "Kotlin"
 * )
 * ```
 */
@Composable
fun PocketFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    PocketFilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        }
    )
}

/**
 * Chip de filtro con icono de cerrar (dismiss).
 *
 * Útil para tags que pueden ser removidos.
 *
 * @param label Texto del chip
 * @param onDismiss Callback cuando se hace clic en cerrar
 * @param modifier Modificador
 * @param enabled Si está habilitado
 *
 * @example Tags removibles:
 * ```kotlin
 * var tags by remember { mutableStateOf(listOf("Kotlin", "Android", "Compose")) }
 *
 * FlowRow {
 *     tags.forEach { tag ->
 *         DismissibleFilterChip(
 *             label = tag,
 *             onDismiss = { tags = tags - tag }
 *         )
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissibleFilterChip(
    label: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    FilterChip(
        selected = true,
        onClick = { /* No action, only dismiss */ },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Quitar",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )
}
