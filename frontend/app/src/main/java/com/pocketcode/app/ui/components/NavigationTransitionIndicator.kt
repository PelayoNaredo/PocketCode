package com.pocketcode.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.pocketcode.core.ui.components.card.PocketCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pocketcode.app.navigation.AppDestination
import com.pocketcode.app.navigation.NavigationTransitionType
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.MotionTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant

/**
 * Navigation transition indicator component
 */
@Composable
fun NavigationTransitionIndicator(
    isTransitioning: Boolean,
    pendingDestination: AppDestination?,
    transitionProgress: Float,
    transitionType: NavigationTransitionType,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isTransitioning && pendingDestination != null,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.fast,
                easing = MotionTokens.Easing.linearOutSlowIn
            )
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.fast,
                easing = MotionTokens.Easing.linearOutSlowIn
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.fast,
                easing = MotionTokens.Easing.fastOutLinearIn
            )
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.fast,
                easing = MotionTokens.Easing.fastOutLinearIn
            )
        ),
        modifier = modifier
    ) {
        PocketCard(
            modifier = Modifier.wrapContentSize(),
            variant = CardVariant.Elevated
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = SpacingTokens.medium,
                    vertical = SpacingTokens.small
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
            ) {
                TransitionTypeIcon(
                    transitionType = transitionType,
                    progress = transitionProgress
                )
                
                // Destination info
                Column {
                    Text(
                        text = "Navigating to",
                        style = TypographyTokens.labelSmall,
                        color = ColorTokens.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = pendingDestination?.title ?: "",
                        style = TypographyTokens.labelMedium,
                        color = ColorTokens.onSurface
                    )
                }
                
                // Progress indicator
                CircularProgressIndicator(
                    progress = { transitionProgress },
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = ColorTokens.primary,
                    trackColor = ColorTokens.outline.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Icon that represents the transition type with animation
 */
@Composable
private fun TransitionTypeIcon(
    transitionType: NavigationTransitionType,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val icon = when (transitionType) {
    NavigationTransitionType.SLIDE_HORIZONTAL -> Icons.AutoMirrored.Filled.ArrowForward
    NavigationTransitionType.SLIDE_VERTICAL -> Icons.Filled.ArrowUpward
    NavigationTransitionType.FADE -> Icons.Filled.Visibility
    NavigationTransitionType.SCALE -> Icons.Filled.ZoomIn
    NavigationTransitionType.SLIDE_FADE -> Icons.Filled.Slideshow
    NavigationTransitionType.PARALLAX -> Icons.Filled.Layers
    NavigationTransitionType.MATERIAL_SHARED_AXIS -> Icons.Filled.SwapHoriz
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = 1f + (progress * 0.2f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = progress * 360f,
        animationSpec = tween(
            durationMillis = MotionTokens.Duration.pageTransition,
            easing = MotionTokens.Easing.linearOutSlowIn
        ),
        label = "iconRotation"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = "Transition type: ${transitionType.name}",
        modifier = modifier
            .size(24.dp)
            .scale(animatedScale)
            .then(
                if (transitionType == NavigationTransitionType.MATERIAL_SHARED_AXIS) {
                    Modifier.graphicsLayer {
                        rotationZ = animatedRotation
                    }
                } else Modifier
            ),
        tint = ColorTokens.primary
    )
}

/**
 * Enhanced page indicator with transition feedback
 */
@Composable
fun EnhancedPageIndicator(
    currentPage: Int,
    pageCount: Int,
    transitionProgress: Float,
    pendingPage: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            PageIndicatorDot(
                isActive = index == currentPage,
                isPending = index == pendingPage,
                transitionProgress = if (index == pendingPage) transitionProgress else 0f
            )
        }
    }
}

/**
 * Individual page indicator dot with enhanced animations
 */
@Composable
private fun PageIndicatorDot(
    isActive: Boolean,
    isPending: Boolean,
    transitionProgress: Float,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = when {
            isActive -> 1.2f
            isPending -> 1f + (transitionProgress * 0.3f)
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dotScale"
    )
    
    val animatedColor by animateColorAsState(
        targetValue = when {
            isActive -> ColorTokens.primary
            isPending -> ColorTokens.primary.copy(alpha = 0.7f + (transitionProgress * 0.3f))
            else -> ColorTokens.outline.copy(alpha = 0.4f)
        },
        animationSpec = tween(
            durationMillis = MotionTokens.Duration.medium,
            easing = MotionTokens.Easing.linearOutSlowIn
        ),
        label = "dotColor"
    )
    
    val animatedWidth by animateDpAsState(
        targetValue = when {
            isActive -> 16.dp
            isPending -> 8.dp + (transitionProgress * 4).dp
            else -> 8.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dotWidth"
    )
    
    Box(
        modifier = modifier
            .size(
                width = animatedWidth,
                height = 8.dp
            )
            .scale(animatedScale)
            .clip(RoundedCornerShape(4.dp))
            .background(animatedColor)
    )
}

/**
 * Navigation gesture hint overlay
 */
@Composable
fun NavigationGestureHint(
    show: Boolean,
    direction: String, // "left", "right", "up", "down"
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.medium,
                easing = MotionTokens.Easing.linearOutSlowIn
            )
        ) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.medium,
                easing = MotionTokens.Easing.emphasizedDecelerate
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.fast,
                easing = MotionTokens.Easing.fastOutLinearIn
            )
        ) + slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = MotionTokens.Duration.fast,
                easing = MotionTokens.Easing.emphasizedAccelerate
            )
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = SpacingTokens.medium,
                    vertical = SpacingTokens.small
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall)
            ) {
                val gestureIcon = when (direction.lowercase()) {
                    "left" -> Icons.AutoMirrored.Filled.ArrowBack
                    "right" -> Icons.AutoMirrored.Filled.ArrowForward
                    "up" -> Icons.Filled.ArrowUpward
                    "down" -> Icons.Filled.ArrowDownward
                    else -> Icons.Filled.TouchApp
                }
                
                Icon(
                    imageVector = gestureIcon,
                    contentDescription = "Swipe $direction",
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = "Swipe $direction",
                    style = TypographyTokens.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}