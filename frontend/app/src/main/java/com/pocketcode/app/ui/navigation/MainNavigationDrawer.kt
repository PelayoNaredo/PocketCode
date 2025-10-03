package com.pocketcode.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MainNavigationDrawer(
    currentScreen: String = "",
    onNavigateToScreen: (String) -> Unit,
    onItemFavorited: ((String, Boolean) -> Unit)? = null,
    searchQuery: String = "",
    onSearchQueryChanged: ((String) -> Unit)? = null,
    recentProjects: List<String> = emptyList(),
    favoriteItems: Set<String> = emptySet()
) {
    val navigationItems = remember(recentProjects, favoriteItems) {
        buildNavigationItems(recentProjects, favoriteItems)
    }

    val flattenedItems = remember(navigationItems, searchQuery) {
        val allItems = flattenDrawerItems(navigationItems)
        if (searchQuery.isBlank()) {
            allItems
        } else {
            allItems.filter { (item, _) ->
                item.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            if (onSearchQueryChanged != null) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    label = { Text(text = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = flattenedItems,
                    key = { (item, _) -> item.id }
                ) { (item, level) ->
                    DrawerItemRow(
                        item = item,
                        level = level,
                        isSelected = item.route == currentScreen,
                        onNavigate = onNavigateToScreen,
                        onItemFavorited = onItemFavorited
                    )
                }
            }
        }
    }
}

private data class DrawerItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String? = null,
    val badge: String? = null,
    val isFavorite: Boolean = false,
    val isRecent: Boolean = false,
    val children: List<DrawerItem> = emptyList()
)

private fun buildNavigationItems(
    recentProjects: List<String>,
    favoriteItems: Set<String>
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            id = "projects",
            title = "Projects",
            icon = Icons.Default.Folder,
            route = "projects",
            isFavorite = favoriteItems.contains("projects"),
            children = listOf(
                DrawerItem(
                    id = "new_project",
                    title = "New Project",
                    icon = Icons.Default.Add,
                    route = "projects/new"
                ),
                DrawerItem(
                    id = "recent_projects",
                    title = "Recent Projects",
                    icon = Icons.Default.History,
                    route = "projects/recent"
                ),
                DrawerItem(
                    id = "templates",
                    title = "Templates",
                    icon = Icons.Default.FileCopy,
                    route = "projects/templates"
                )
            )
        ),
        DrawerItem(
            id = "file_explorer",
            title = "File Explorer",
            icon = Icons.Default.FolderOpen,
            route = "files",
            isFavorite = favoriteItems.contains("file_explorer"),
            isRecent = recentProjects.contains("file_explorer"),
            children = listOf(
                DrawerItem(
                    id = "quick_open",
                    title = "Quick Open",
                    icon = Icons.Default.Search,
                    route = "files/quick_open"
                ),
                DrawerItem(
                    id = "bookmarks",
                    title = "Bookmarks",
                    icon = Icons.Default.Bookmark,
                    route = "files/bookmarks"
                )
            )
        ),
        DrawerItem(
            id = "code_editor",
            title = "Code Editor",
            icon = Icons.Default.Edit,
            route = "editor",
            isFavorite = favoriteItems.contains("code_editor"),
            isRecent = recentProjects.contains("code_editor"),
            children = listOf(
                DrawerItem(
                    id = "find_replace",
                    title = "Find & Replace",
                    icon = Icons.Default.FindReplace,
                    route = "editor/find_replace"
                ),
                DrawerItem(
                    id = "goto_line",
                    title = "Go to Line",
                    icon = Icons.Default.TrendingUp,
                    route = "editor/goto_line"
                ),
                DrawerItem(
                    id = "format_code",
                    title = "Format Code",
                    icon = Icons.Default.FormatAlignLeft,
                    route = "editor/format"
                )
            )
        ),
        DrawerItem(
            id = "ai_assistant",
            title = "AI Assistant",
            icon = Icons.Default.SmartToy,
            route = "ai",
            isFavorite = favoriteItems.contains("ai_assistant"),
            badge = "New",
            children = listOf(
                DrawerItem(
                    id = "code_suggestions",
                    title = "Code Suggestions",
                    icon = Icons.Default.Lightbulb,
                    route = "ai/suggestions"
                ),
                DrawerItem(
                    id = "code_review",
                    title = "Code Review",
                    icon = Icons.Default.RateReview,
                    route = "ai/review"
                ),
                DrawerItem(
                    id = "documentation",
                    title = "Generate Docs",
                    icon = Icons.Default.Description,
                    route = "ai/docs"
                )
            )
        ),
        DrawerItem(
            id = "preview",
            title = "Preview",
            icon = Icons.Default.Preview,
            route = "preview",
            isFavorite = favoriteItems.contains("preview"),
            children = listOf(
                DrawerItem(
                    id = "live_preview",
                    title = "Live Preview",
                    icon = Icons.Default.PlayArrow,
                    route = "preview/live"
                ),
                DrawerItem(
                    id = "device_preview",
                    title = "Device Preview",
                    icon = Icons.Default.PhoneAndroid,
                    route = "preview/device"
                )
            )
        ),
        DrawerItem(
            id = "tools",
            title = "Development Tools",
            icon = Icons.Default.Build,
            route = "tools",
            children = listOf(
                DrawerItem(
                    id = "terminal",
                    title = "Terminal",
                    icon = Icons.Default.Terminal,
                    route = "tools/terminal"
                ),
                DrawerItem(
                    id = "git",
                    title = "Git Integration",
                    icon = Icons.Default.Source,
                    route = "tools/git"
                ),
                DrawerItem(
                    id = "build",
                    title = "Build & Deploy",
                    icon = Icons.Default.Construction,
                    route = "tools/build"
                ),
                DrawerItem(
                    id = "debug",
                    title = "Debug Console",
                    icon = Icons.Default.BugReport,
                    route = "tools/debug"
                )
            )
        ),
        DrawerItem(
            id = "marketplace",
            title = "Marketplace",
            icon = Icons.Default.Store,
            route = "marketplace",
            children = listOf(
                DrawerItem(
                    id = "extensions",
                    title = "Extensions",
                    icon = Icons.Default.Extension,
                    route = "marketplace/extensions"
                ),
                DrawerItem(
                    id = "templates_market",
                    title = "Templates",
                    icon = Icons.Default.GetApp,
                    route = "marketplace/templates"
                ),
                DrawerItem(
                    id = "themes",
                    title = "Themes",
                    icon = Icons.Default.Palette,
                    route = "marketplace/themes"
                )
            )
        )
    )
}

private fun flattenDrawerItems(
    items: List<DrawerItem>,
    level: Int = 0
): List<Pair<DrawerItem, Int>> {
    return items.flatMap { item ->
        val current = listOf(item to level)
        val children = flattenDrawerItems(item.children, level + 1)
        current + children
    }
}

@Composable
private fun DrawerItemRow(
    item: DrawerItem,
    level: Int,
    isSelected: Boolean,
    onNavigate: (String) -> Unit,
    onItemFavorited: ((String, Boolean) -> Unit)?
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    val contentColor by rememberUpdatedState(
        if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(start = 16.dp + (level * 12).dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
            .then(
                if (item.route != null) {
                    Modifier.clickable { onNavigate(item.route) }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.badge != null) {
                    Spacer(modifier = Modifier.size(6.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(text = item.badge) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            if (item.isRecent) {
                Text(
                    text = "Recently opened",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (onItemFavorited != null && item.route != null) {
            val icon = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder
            IconButton(onClick = { onItemFavorited(item.id, !item.isFavorite) }) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (item.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}