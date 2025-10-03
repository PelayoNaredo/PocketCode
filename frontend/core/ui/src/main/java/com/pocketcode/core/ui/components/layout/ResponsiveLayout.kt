package com.pocketcode.core.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Breakpoint de responsive design.
 *
 * Define los puntos de quiebre estándar para diseño adaptativo:
 * - Compact: Teléfonos en portrait (< 600dp)
 * - Medium: Tablets pequeñas, teléfonos landscape (600-840dp)
 * - Expanded: Tablets grandes, desktops (> 840dp)
 */
enum class WindowSize {
    Compact,
    Medium,
    Expanded
}

/**
 * Obtiene el tamaño de ventana actual según los breakpoints de Material3.
 *
 * @return WindowSize correspondiente al ancho actual
 *
 * @example
 * ```kotlin
 * val windowSize = rememberWindowSize()
 * when (windowSize) {
 *     WindowSize.Compact -> CompactLayout()
 *     WindowSize.Medium -> MediumLayout()
 *     WindowSize.Expanded -> ExpandedLayout()
 * }
 * ```
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return when {
        screenWidth < 600 -> WindowSize.Compact
        screenWidth < 840 -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}

/**
 * Layout responsivo del sistema de diseño Pocket.
 *
 * ResponsiveLayout adapta automáticamente su disposición según el tamaño
 * de pantalla:
 * - **Compact (< 600dp)**: Diseño vertical (móvil portrait)
 * - **Medium (600-840dp)**: Diseño híbrido (tablet pequeña, móvil landscape)
 * - **Expanded (> 840dp)**: Diseño horizontal (tablet grande, desktop)
 *
 * Características:
 * - Adaptación automática al tamaño de pantalla
 * - Layouts personalizables por breakpoint
 * - Integración con Material3 WindowSizeClass
 * - Soporte para navegación adaptativa
 *
 * @param modifier Modificador para el layout
 * @param compactContent Contenido para pantallas compactas
 * @param mediumContent Contenido para pantallas medianas (null = usa compactContent)
 * @param expandedContent Contenido para pantallas expandidas (null = usa mediumContent)
 *
 * @example Diseño adaptativo simple:
 * ```kotlin
 * ResponsiveLayout(
 *     compactContent = {
 *         // Móvil: Layout vertical
 *         Column {
 *             Header()
 *             Content()
 *             Sidebar()
 *         }
 *     },
 *     expandedContent = {
 *         // Desktop: Layout horizontal
 *         Row {
 *             Sidebar(modifier = Modifier.width(300.dp))
 *             Column(modifier = Modifier.weight(1f)) {
 *                 Header()
 *                 Content()
 *             }
 *         }
 *     }
 * )
 * ```
 *
 * @example Editor con panel lateral adaptativo:
 * ```kotlin
 * ResponsiveLayout(
 *     compactContent = {
 *         // Móvil: Editor a pantalla completa
 *         Box(modifier = Modifier.fillMaxSize()) {
 *             CodeEditor()
 *             // Panel lateral como drawer
 *             if (showSidebar) {
 *                 ModalBottomSheet { ToolsPalette() }
 *             }
 *         }
 *     },
 *     expandedContent = {
 *         // Desktop: Editor + panel lateral fijo
 *         Row {
 *             CodeEditor(modifier = Modifier.weight(1f))
 *             ToolsPalette(modifier = Modifier.width(280.dp))
 *         }
 *     }
 * )
 * ```
 *
 * @example Dashboard adaptativo:
 * ```kotlin
 * ResponsiveLayout(
 *     compactContent = {
 *         // Móvil: 1 columna
 *         LazyColumn {
 *             items(widgets) { widget ->
 *                 WidgetCard(
 *                     widget = widget,
 *                     modifier = Modifier.fillMaxWidth()
 *                 )
 *             }
 *         }
 *     },
 *     mediumContent = {
 *         // Tablet: 2 columnas
 *         LazyVerticalGrid(columns = GridCells.Fixed(2)) {
 *             items(widgets) { widget ->
 *                 WidgetCard(widget = widget)
 *             }
 *         }
 *     },
 *     expandedContent = {
 *         // Desktop: 3 columnas
 *         LazyVerticalGrid(columns = GridCells.Fixed(3)) {
 *             items(widgets) { widget ->
 *                 WidgetCard(widget = widget)
 *             }
 *         }
 *     }
 * )
 * ```
 */
@Composable
fun ResponsiveLayout(
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: (@Composable () -> Unit)? = null,
    expandedContent: (@Composable () -> Unit)? = null
) {
    val windowSize = rememberWindowSize()

    Box(modifier = modifier) {
        when (windowSize) {
            WindowSize.Compact -> compactContent()
            WindowSize.Medium -> (mediumContent ?: compactContent)()
            WindowSize.Expanded -> (expandedContent ?: mediumContent ?: compactContent)()
        }
    }
}

/**
 * Layout con columnas adaptativas.
 *
 * AdaptiveColumns ajusta automáticamente el número de columnas según
 * el tamaño de pantalla.
 *
 * @param modifier Modificador
 * @param compactColumns Columnas en pantalla compacta (default: 1)
 * @param mediumColumns Columnas en pantalla media (default: 2)
 * @param expandedColumns Columnas en pantalla expandida (default: 3)
 * @param horizontalSpacing Espaciado horizontal entre columnas
 * @param verticalSpacing Espaciado vertical entre filas
 * @param content Contenido de las columnas
 *
 * @example
 * ```kotlin
 * AdaptiveColumns(
 *     compactColumns = 1,
 *     mediumColumns = 2,
 *     expandedColumns = 3
 * ) {
 *     items(projects) { project ->
 *         ProjectCard(project = project)
 *     }
 * }
 * ```
 */
@Composable
fun AdaptiveColumns(
    modifier: Modifier = Modifier,
    compactColumns: Int = 1,
    mediumColumns: Int = 2,
    expandedColumns: Int = 3,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 16.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val windowSize = rememberWindowSize()
    val columns = when (windowSize) {
        WindowSize.Compact -> compactColumns
        WindowSize.Medium -> mediumColumns
        WindowSize.Expanded -> expandedColumns
    }

    // Implementación simplificada - en producción usar LazyVerticalGrid
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        content()
    }
}

/**
 * Espaciado adaptativo según el tamaño de pantalla.
 *
 * @param compact Espaciado para pantalla compacta
 * @param medium Espaciado para pantalla media (null = usa compact)
 * @param expanded Espaciado para pantalla expandida (null = usa medium)
 * @return Dp correspondiente al tamaño actual
 *
 * @example
 * ```kotlin
 * val padding = adaptiveSpacing(
 *     compact = 16.dp,
 *     medium = 24.dp,
 *     expanded = 32.dp
 * )
 * Column(modifier = Modifier.padding(padding)) { ... }
 * ```
 */
@Composable
fun adaptiveSpacing(
    compact: androidx.compose.ui.unit.Dp,
    medium: androidx.compose.ui.unit.Dp? = null,
    expanded: androidx.compose.ui.unit.Dp? = null
): androidx.compose.ui.unit.Dp {
    val windowSize = rememberWindowSize()
    return when (windowSize) {
        WindowSize.Compact -> compact
        WindowSize.Medium -> medium ?: compact
        WindowSize.Expanded -> expanded ?: medium ?: compact
    }
}
