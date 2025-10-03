package com.pocketcode.core.ui.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.pocketcode.core.ui.components.navigation.PocketTabs
import com.pocketcode.core.ui.components.navigation.TabItem

private val EmptyComposable: @Composable () -> Unit = {}

/**
 * Configuración para PocketScaffold.
 *
 * Esta data class encapsula todas las configuraciones posibles de un scaffold,
 * permitiendo pasar un solo objeto en lugar de múltiples parámetros.
 *
 * @param topBar Composable para la barra superior (normalmente PocketTopBar)
 * @param bottomBar Composable para la barra inferior
 * @param floatingActionButton FAB opcional
 * @param floatingActionButtonPosition Posición del FAB
 * @param containerColor Color de fondo del contenedor
 * @param contentColor Color del contenido
 * @param contentWindowInsets Insets para el contenido
 *
 * @example
 * ```kotlin
 * val config = PocketScaffoldConfig(
 *     topBar = { PocketTopBar(title = "Mi App") }
 * )
 * PocketScaffold(config = config) {
 *     // Contenido
 * }
 * ```
 */
data class PocketScaffoldConfig(
    val topBar: @Composable () -> Unit = EmptyComposable,
    val bottomBar: @Composable () -> Unit = EmptyComposable,
    val snackbarHost: @Composable () -> Unit = EmptyComposable,
    val floatingActionButton: @Composable () -> Unit = EmptyComposable,
    val floatingActionButtonPosition: FabPosition = FabPosition.End,
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val contentWindowInsets: WindowInsets? = null,
    val hasTopBar: Boolean = false,
    val hasBottomBar: Boolean = false,
    val hasTabs: Boolean = false,
    val isScrollable: Boolean = true,
    val paddingValues: PaddingValues = PaddingValues(),
    val backgroundColor: Color = Color.Unspecified,
    val tabs: List<TabItem> = emptyList(),
    val selectedTabIndex: Int = 0,
    val onTabSelected: (Int) -> Unit = {}
)

/**
 * Layout principal de PocketCode que envuelve Scaffold de Material3.
 *
 * PocketScaffold proporciona la estructura base para las pantallas de la aplicación,
 * incluyendo:
 * - Barra superior (top bar)
 * - Barra inferior (bottom bar)
 * - Floating Action Button (FAB)
 * - Snackbar host
 * - Sistema de padding automático para contenido
 *
 * Ventajas sobre Scaffold directo:
 * - Configuración unificada mediante PocketScaffoldConfig
 * - Valores predeterminados consistentes
 * - Fácil de mantener y actualizar
 * - Integración con otros componentes Pocket
 *
 * @param modifier Modificador para el scaffold
 * @param config Configuración del scaffold (topBar, bottomBar, etc.)
 * @param content Contenido de la pantalla (recibe PaddingValues para safe areas)
 *
 * @example Uso básico con top bar:
 * ```kotlin
 * PocketScaffold(
 *     config = PocketScaffoldConfig(
 *         topBar = {
 *             PocketTopBar(
 *                 title = "Dashboard",
 *                 onNavigationClick = { navController.popBackStack() }
 *             )
 *         }
 *     )
 * ) { paddingValues ->
 *     Column(modifier = Modifier.padding(paddingValues)) {
 *         Text("Contenido")
 *     }
 * }
 * ```
 *
 * @example Con FAB y bottom bar:
 * ```kotlin
 * PocketScaffold(
 *     config = PocketScaffoldConfig(
 *         topBar = { PocketTopBar(title = "Editor") },
 *         floatingActionButton = {
 *             FloatingActionButton(onClick = { /* ... */ }) {
 *                 Icon(Icons.Default.Add, "Añadir")
 *             }
 *         },
 *         bottomBar = {
 *             BottomAppBar { /* Navigation items */ }
 *         }
 *     )
 * ) { paddingValues ->
 *     // Contenido con padding
 * }
 * ```
 *
 * @example Configuración avanzada:
 * ```kotlin
 * val config = PocketScaffoldConfig(
 *     topBar = { PocketTopBar(title = "Settings") },
 *     snackbarHost = { SnackbarHost(snackbarHostState) },
 *     floatingActionButton = { /* FAB */ },
 *     floatingActionButtonPosition = FabPosition.Center,
 *     containerColor = MaterialTheme.colorScheme.surface,
 *     contentColor = MaterialTheme.colorScheme.onSurface
 * )
 * PocketScaffold(config = config) { paddingValues ->
 *     // Contenido
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketScaffold(
    modifier: Modifier = Modifier,
    config: PocketScaffoldConfig = PocketScaffoldConfig(),
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    floatingActionButtonPosition: FabPosition = config.floatingActionButtonPosition,
    tabs: List<TabItem> = emptyList(),
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val topBarContent = topBar ?: config.topBar
    val bottomBarContent = bottomBar ?: config.bottomBar
    val snackbarHostContent = snackbarHost ?: config.snackbarHost
    val floatingActionButtonContent = floatingActionButton ?: config.floatingActionButton

    val resolvedTabs = if (tabs.isNotEmpty()) tabs else config.tabs
    val resolvedTabIndex = resolvedTabs.takeIf { it.isNotEmpty() }?.let { tabItems ->
        val index = if (tabs.isNotEmpty()) selectedTabIndex else config.selectedTabIndex
        index.coerceIn(0, tabItems.lastIndex)
    } ?: 0
    val resolvedOnTabSelected = if (tabs.isNotEmpty()) onTabSelected else config.onTabSelected

    val hasTopBarContent = config.hasTopBar || topBarContent !== EmptyComposable
    val hasBottomBarContent = config.hasBottomBar || bottomBarContent !== EmptyComposable
    val showTabs = (config.hasTabs || resolvedTabs.isNotEmpty()) && resolvedTabs.isNotEmpty()

    val containerColor = when {
        config.backgroundColor != Color.Unspecified -> config.backgroundColor
        config.containerColor != Color.Unspecified -> config.containerColor
        else -> MaterialTheme.colorScheme.background
    }
    val contentColor = if (config.contentColor == Color.Unspecified) {
        MaterialTheme.colorScheme.onBackground
    } else {
        config.contentColor
    }
    val contentInsets = config.contentWindowInsets ?: ScaffoldDefaults.contentWindowInsets

    Scaffold(
        modifier = modifier,
        topBar = {
            if (hasTopBarContent || showTabs) {
                Column {
                    if (hasTopBarContent && topBarContent !== EmptyComposable) {
                        topBarContent()
                    }
                    if (showTabs) {
                        PocketTabs(
                            tabs = resolvedTabs,
                            selectedIndex = resolvedTabIndex,
                            onTabSelected = resolvedOnTabSelected,
                            scrollable = resolvedTabs.size > 3
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (hasBottomBarContent && bottomBarContent !== EmptyComposable) {
                bottomBarContent()
            }
        },
        snackbarHost = {
            if (snackbarHostContent !== EmptyComposable) {
                snackbarHostContent()
            }
        },
        floatingActionButton = {
            if (floatingActionButtonContent !== EmptyComposable) {
                floatingActionButtonContent()
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentInsets
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val combinedPadding = combinePaddingValues(innerPadding, config.paddingValues, layoutDirection)
        content(combinedPadding)
    }
}

/**
 * Sobrecarga de PocketScaffold sin config para uso rápido.
 *
 * Útil cuando solo necesitas un scaffold simple sin configuración especial.
 *
 * @example
 * ```kotlin
 * PocketScaffold { paddingValues ->
 *     Box(modifier = Modifier.padding(paddingValues)) {
 *         Text("Contenido simple")
 *     }
 * }
 * ```
 */
@Composable
fun PocketScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (PaddingValues) -> Unit
) {
    PocketScaffold(
        modifier = modifier,
        config = PocketScaffoldConfig(
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            containerColor = containerColor,
            contentColor = contentColor
        ),
        content = content
    )
}

private fun combinePaddingValues(
    first: PaddingValues,
    second: PaddingValues,
    layoutDirection: LayoutDirection
): PaddingValues {
    val firstLeft = first.calculateLeftPadding(layoutDirection)
    val firstRight = first.calculateRightPadding(layoutDirection)
    val secondLeft = second.calculateLeftPadding(layoutDirection)
    val secondRight = second.calculateRightPadding(layoutDirection)

    val start = if (layoutDirection == LayoutDirection.Ltr) {
        firstLeft + secondLeft
    } else {
        firstRight + secondRight
    }
    val end = if (layoutDirection == LayoutDirection.Ltr) {
        firstRight + secondRight
    } else {
        firstLeft + secondLeft
    }

    return PaddingValues(
        start = start,
        top = first.calculateTopPadding() + second.calculateTopPadding(),
        end = end,
        bottom = first.calculateBottomPadding() + second.calculateBottomPadding()
    )
}
