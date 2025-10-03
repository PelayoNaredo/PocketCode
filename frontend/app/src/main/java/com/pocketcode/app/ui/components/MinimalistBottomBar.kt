package com.pocketcode.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.ElevationTokens

/**
 * Minimalist bottom navigation bar with enhanced transitions
 */
@Composable
fun MinimalistBottomBar(
    onSettingsClick: () -> Unit,
    onUserClick: () -> Unit,
    currentPage: Int,
    pageCount: Int = 5,
    transitionProgress: Float = 0f,
    pendingPage: Int? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = ColorTokens.surface,
        shadowElevation = ElevationTokens.level2
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Settings button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ColorTokens.onSurface,
                    modifier = Modifier.size(SpacingTokens.large)
                )
            }
            
            // Enhanced page indicator
            EnhancedPageIndicator(
                currentPage = currentPage,
                pageCount = pageCount,
                transitionProgress = transitionProgress,
                pendingPage = pendingPage
            )
            
            // User profile button
            IconButton(
                onClick = onUserClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Profile",
                    tint = ColorTokens.onSurface,
                    modifier = Modifier.size(SpacingTokens.large)
                )
            }
        }
    }
}

/**
 * Minimalist page indicator dots
 */
@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Surface(
                modifier = Modifier
                    .size(
                        width = if (isSelected) SpacingTokens.large else SpacingTokens.small,
                        height = SpacingTokens.small
                    ),
                shape = MaterialTheme.shapes.small,
                color = if (isSelected) {
                    ColorTokens.primary
                } else {
                    ColorTokens.outline.copy(alpha = 0.3f)
                }
            ) {}
        }
    }
}