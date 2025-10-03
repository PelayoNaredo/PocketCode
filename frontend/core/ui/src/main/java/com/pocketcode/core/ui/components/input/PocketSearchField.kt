package com.pocketcode.core.ui.components.input

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Campo de búsqueda del sistema de diseño Pocket.
 *
 * PocketSearchField es un campo de texto especializado para búsquedas que incluye:
 * - Icono de búsqueda (leading)
 * - Botón de limpiar (trailing, aparece cuando hay texto)
 * - Placeholder sugerente
 * - Optimizaciones para búsqueda
 *
 * @param value Valor actual del campo
 * @param onValueChange Callback cuando cambia el valor
 * @param modifier Modificador
 * @param placeholder Texto de placeholder
 * @param enabled Si está habilitado
 * @param onClear Callback cuando se limpia (opcional)
 *
 * @example Búsqueda en marketplace:
 * ```kotlin
 * var searchQuery by remember { mutableStateOf("") }
 *
 * PocketSearchField(
 *     value = searchQuery,
 *     onValueChange = { searchQuery = it },
 *     placeholder = "Buscar recursos...",
 *     onClear = {
 *         searchQuery = ""
 *         viewModel.clearSearch()
 *     }
 * )
 * ```
 *
 * @example Con búsqueda en tiempo real:
 * ```kotlin
 * var searchQuery by remember { mutableStateOf("") }
 *
 * LaunchedEffect(searchQuery) {
 *     delay(300) // Debounce
 *     if (searchQuery.isNotEmpty()) {
 *         viewModel.search(searchQuery)
 *     }
 * }
 *
 * PocketSearchField(
 *     value = searchQuery,
 *     onValueChange = { searchQuery = it },
 *     placeholder = "Buscar proyectos..."
 * )
 * ```
 */
@Composable
fun PocketSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar...",
    enabled: Boolean = true,
    onClear: (() -> Unit)? = null
) {
    PocketTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = "",
        placeholder = placeholder,
        enabled = enabled,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(
                    onClick = {
                        onValueChange("")
                        onClear?.invoke()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        } else null
    )
}
