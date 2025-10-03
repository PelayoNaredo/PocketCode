package com.pocketcode.core.ui.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.SpacingTokens

/**
 * Layout de sección del sistema de diseño Pocket.
 *
 * SectionLayout agrupa contenido relacionado bajo un título común,
 * proporcionando:
 * - Título de sección con estilo consistente
 * - Descripción opcional
 * - Espaciado estandarizado
 * - Separación visual clara entre secciones
 *
 * Se usa típicamente en:
 * - Pantallas de configuración
 * - Formularios largos
 * - Dashboards con múltiples áreas
 * - Páginas de información
 *
 * @param title Título de la sección
 * @param modifier Modificador para el layout
 * @param description Descripción opcional de la sección
 * @param content Contenido de la sección
 *
 * @example Uso básico en configuración:
 * ```kotlin
 * Column {
 *     SectionLayout(title = "Apariencia") {
 *         SimplePocketSwitch(
 *             label = "Modo oscuro",
 *             checked = darkMode,
 *             onCheckedChange = { darkMode = it }
 *         )
 *         SimplePocketSwitch(
 *             label = "Compacto",
 *             checked = compactMode,
 *             onCheckedChange = { compactMode = it }
 *         )
 *     }
 *
 *     SectionLayout(title = "Notificaciones") {
 *         SimplePocketSwitch(
 *             label = "Activadas",
 *             checked = notifications,
 *             onCheckedChange = { notifications = it }
 *         )
 *     }
 * }
 * ```
 *
 * @example Con descripción:
 * ```kotlin
 * SectionLayout(
 *     title = "Privacidad",
 *     description = "Controla qué información compartimos y con quién"
 * ) {
 *     SimplePocketSwitch(
 *         label = "Analytics",
 *         description = "Datos de uso anónimos",
 *         checked = analytics,
 *         onCheckedChange = { analytics = it }
 *     )
 *     SimplePocketSwitch(
 *         label = "Crash reports",
 *         description = "Informes de errores automáticos",
 *         checked = crashReports,
 *         onCheckedChange = { crashReports = it }
 *     )
 * }
 * ```
 *
 * @example En dashboard:
 * ```kotlin
 * LazyColumn {
 *     item {
 *         SectionLayout(title = "Proyectos Recientes") {
 *             RecentProjectsList(projects = recentProjects)
 *         }
 *     }
 *
 *     item {
 *         SectionLayout(
 *             title = "Estadísticas",
 *             description = "Tu actividad de esta semana"
 *         ) {
 *             StatsCards(stats = weeklyStats)
 *         }
 *     }
 *
 *     item {
 *         SectionLayout(title = "Marketplace Destacado") {
 *             FeaturedAssets(assets = featured)
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun SectionLayout(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.Semantic.contentPaddingNormal),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        // Cabecera de la sección
        Column(
            modifier = Modifier.padding(horizontal = SpacingTokens.Semantic.contentPaddingNormal),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingSmall)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Contenido de la sección
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingSmall)
        ) {
            content()
        }
    }
}

/**
 * Variante compacta de SectionLayout sin padding superior.
 *
 * Útil para secciones en cards o contenedores con padding propio.
 *
 * @example
 * ```kotlin
 * PocketCard {
 *     CompactSectionLayout(title = "Detalles") {
 *         Text("Nombre: Proyecto 1")
 *         Text("Fecha: 2025-01-02")
 *         Text("Estado: Activo")
 *     }
 * }
 * ```
 */
@Composable
fun CompactSectionLayout(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        // Cabecera
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Contenido
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}
