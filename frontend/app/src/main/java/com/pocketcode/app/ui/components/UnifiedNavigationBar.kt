package com.pocketcode.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.pocketcode.app.navigation.AppDestination
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.ElevationTokens

/**
 * Unified navigation bar that combines bottom navigation with top bar actions.
 * 
 * Features:
 * - Page indicator in the center
 * - Quick actions (Chat, Settings, User) on the right
 * - Haptic feedback on navigation
 * - Accessibility optimized (48dp minimum touch target)
 * - Camera/notch safe area handling
 */
@Composable
fun UnifiedNavigationBar(
    currentPage: Int,
    pageCount: Int = 5,
    transitionProgress: Float = 0f,
    pendingPage: Int? = null,
    onAiChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            ),
        color = ColorTokens.surface,
        shadowElevation = ElevationTokens.level2,
        tonalElevation = ElevationTokens.level1
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer for balance (left side)
            Spacer(modifier = Modifier.width(48.dp))
            
            // Center: Enhanced page indicator
            EnhancedPageIndicator(
                currentPage = currentPage,
                pageCount = pageCount,
                transitionProgress = transitionProgress,
                pendingPage = pendingPage,
                modifier = Modifier.weight(1f)
            )
            
            // Right side: Quick action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Chat button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAiChatClick()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = PocketIcons.Chat,
                        contentDescription = "Abrir chat de IA",
                        tint = ColorTokens.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Settings button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSettingsClick()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = PocketIcons.Settings,
                        contentDescription = "Abrir configuración",
                        tint = ColorTokens.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // User profile button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUserClick()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = PocketIcons.Person,
                        contentDescription = "Ver perfil de usuario",
                        tint = ColorTokens.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
