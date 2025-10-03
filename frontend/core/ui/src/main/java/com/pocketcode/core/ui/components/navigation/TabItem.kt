package com.pocketcode.core.ui.components.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import com.pocketcode.core.ui.icons.PocketIcons

/**
 * Item de tab para navegación por pestañas.
 *
 * TabItem representa una pestaña en un sistema de navegación horizontal.
 *
 * @param title Texto de la pestaña
 * @param icon Icono de la pestaña (opcional)
 * @param badge Contador de notificaciones (opcional)
 */
data class TabItem(
    val title: String,
    val icon: ImageVector? = null,
    val badge: Int? = null,
    val id: String = title,
    val closeable: Boolean = false,
    val enabled: Boolean = true
)

/**
 * Tabs de navegación del sistema de diseño Pocket.
 *
 * PocketTabs es un contenedor de pestañas para navegación horizontal.
 *
 * @param tabs Lista de TabItem a mostrar
 * @param selectedIndex Índice de la pestaña seleccionada
 * @param onTabSelected Callback cuando se selecciona una pestaña
 * @param modifier Modificador
 *
 * @example
 * ```kotlin
 * val tabs = listOf(
 *     TabItem("General", Icons.Default.Settings),
 *     TabItem("Apariencia", Icons.Default.Palette),
 *     TabItem("Privacidad", Icons.Default.Lock)
 * )
 * var selectedTab by remember { mutableStateOf(0) }
 *
 * PocketTabs(
 *     tabs = tabs,
 *     selectedIndex = selectedTab,
 *     onTabSelected = { selectedTab = it }
 * )
 * ```
 */
@Composable
fun PocketTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    onTabClosed: ((Int) -> Unit)? = null
) {
    if (tabs.isEmpty()) {
        return
    }

    val tabRowContent: @Composable () -> Unit = {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = tab.enabled,
                text = {
                    TabLabel(
                        tab = tab,
                        onClose = onTabClosed?.takeIf { tab.closeable }?.let {
                            { it(index) }
                        }
                    )
                }
            )
        }
    }

    if (scrollable) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier
        ) {
            tabRowContent()
        }
    } else {
        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier
        ) {
            tabRowContent()
        }
    }
}

@Composable
private fun TabLabel(
    tab: TabItem,
    onClose: (() -> Unit)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tab.icon?.let { icon ->
            Icon(imageVector = icon, contentDescription = null)
        }

        val badgeCount = tab.badge
        if (badgeCount != null && badgeCount > 0) {
            BadgedBox(
                badge = { Badge { Text(badgeCount.toString()) } }
            ) {
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = tab.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (onClose != null) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = PocketIcons.Close,
                    contentDescription = "Cerrar pestaña ${tab.title}",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScrollableTabIndicator(
    tabs: List<TabItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PocketTabs(
        tabs = tabs,
        selectedIndex = selectedTabIndex,
        onTabSelected = onTabSelected,
        onTabClosed = onTabClosed,
        scrollable = true,
        modifier = modifier
    )
}
