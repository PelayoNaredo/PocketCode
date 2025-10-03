package com.pocketcode.core.ui.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.pocketcode.core.ui.icons.PocketIcons

/**
 * Acción para la barra superior (TopBar).
 *
 * Representa un botón de acción en la barra superior, típicamente mostrado
 * como un icono con una etiqueta de accesibilidad.
 *
 * @param icon Icono de la acción
 * @param contentDescription Descripción para accesibilidad
 * @param onClick Callback cuando se hace clic
 * @param enabled Si la acción está habilitada
 *
 * @example
 * ```kotlin
 * val actions = listOf(
 *     TopBarAction(
 *         icon = Icons.Default.Search,
 *         contentDescription = "Buscar",
 *         onClick = { viewModel.openSearch() }
 *     ),
 *     TopBarAction(
 *         icon = Icons.Default.Settings,
 *         contentDescription = "Configuración",
 *         onClick = { navController.navigate("settings") }
 *     )
 * )
 * ```
 */
data class TopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/**
 * Barra superior (Top App Bar) del sistema de diseño Pocket.
 *
 * PocketTopBar es una barra de navegación superior que incluye:
 * - Título de la pantalla
 * - Botón de navegación (atrás/menú)
 * - Acciones contextuales (búsqueda, configuración, etc.)
 * - Soporte para scroll behavior
 *
 * Características:
 * - Integración con sistema de navegación
 * - Acciones configurables
 * - Soporte para colores personalizados
 * - Compatibilidad con Material3 TopAppBar
 *
 * @param title Título mostrado en la barra
 * @param modifier Modificador para la barra
 * @param navigationIcon Icono de navegación (por defecto: flecha atrás)
 * @param navigationIconContentDescription Descripción del icono de navegación
 * @param onNavigationClick Callback cuando se hace clic en el icono de navegación
 * @param actions Lista de acciones a mostrar en la barra
 * @param colors Colores personalizados (null = usa defaults)
 * @param scrollBehavior Comportamiento de scroll (null = fixed)
 *
 * @example Top bar básica con botón atrás:
 * ```kotlin
 * PocketTopBar(
 *     title = "Mi Pantalla",
 *     onNavigationClick = { navController.popBackStack() }
 * )
 * ```
 *
 * @example Con acciones:
 * ```kotlin
 * PocketTopBar(
 *     title = "Proyectos",
 *     onNavigationClick = { navController.popBackStack() },
 *     actions = listOf(
 *         TopBarAction(
 *             icon = Icons.Default.Search,
 *             contentDescription = "Buscar",
 *             onClick = { showSearchDialog = true }
 *         ),
 *         TopBarAction(
 *             icon = Icons.Default.FilterList,
 *             contentDescription = "Filtrar",
 *             onClick = { showFilterMenu = true }
 *         )
 *     )
 * )
 * ```
 *
 * @example Con scroll behavior:
 * ```kotlin
 * val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
 * PocketScaffold(
 *     config = PocketScaffoldConfig(
 *         topBar = {
 *             PocketTopBar(
 *                 title = "Dashboard",
 *                 scrollBehavior = scrollBehavior
 *             )
 *         }
 *     )
 * ) { paddingValues ->
 *     LazyColumn(
 *         modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
 *     ) {
 *         // Contenido scrolleable
 *     }
 * }
 * ```
 *
 * @example Sin botón de navegación:
 * ```kotlin
 * PocketTopBar(
 *     title = "Pantalla Principal",
 *     onNavigationClick = null // Sin botón de navegación
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketTopBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconContentDescription: String = "Navegar atrás",
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopBarAction> = emptyList(),
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            if (subtitle != null) {
                Column {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconContentDescription
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(
                    onClick = action.onClick,
                    enabled = action.enabled
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription
                    )
                }
            }
        },
        colors = colors ?: TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Variante de PocketTopBar para pantallas de chat.
 *
 * ChatTopBar incluye características específicas para chat:
 * - Avatar o icono del contacto
 * - Estado del chat (online, escribiendo, etc.)
 * - Acciones específicas de chat (llamar, videollamada, info)
 *
 * @param title Nombre del chat o contacto
 * @param subtitle Estado del chat (opcional)
 * @param modifier Modificador para la barra
 * @param onNavigationClick Callback para el botón atrás
 * @param actions Acciones específicas del chat
 *
 * @example
 * ```kotlin
 * ChatTopBar(
 *     title = "AI Assistant",
 *     subtitle = "Online",
 *     onNavigationClick = { navController.popBackStack() },
 *     actions = listOf(
 *         TopBarAction(
 *             icon = Icons.Default.MoreVert,
 *             contentDescription = "Más opciones",
 *             onClick = { showChatMenu = true }
 *         )
 *     )
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    isTyping: Boolean = false,
    onNavigationClick: (() -> Unit)? = null,
    onClearChat: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    actions: List<TopBarAction> = emptyList()
) {
    TopAppBar(
        title = {
            val statusText = when {
                isTyping -> "Escribiendo..."
                subtitle != null -> subtitle
                else -> null
            }

            if (statusText != null) {
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = statusText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        },
        actions = {
            actions.forEach { action ->
                IconButton(
                    onClick = action.onClick,
                    enabled = action.enabled
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription
                    )
                }
            }
            if (onSettings != null) {
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = PocketIcons.Settings,
                        contentDescription = "Configuración del chat"
                    )
                }
            }
            if (onClearChat != null) {
                IconButton(onClick = onClearChat) {
                    Icon(
                        imageVector = PocketIcons.Delete,
                        contentDescription = "Limpiar conversación"
                    )
                }
            }
        }
    )
}
