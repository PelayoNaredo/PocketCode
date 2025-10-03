package com.pocketcode.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.pocketcode.core.ui.tokens.MotionTokens
import kotlin.math.abs

/**
 * Navigation transition types
 */
enum class NavigationTransitionType {
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL,
    FADE,
    SCALE,
    SLIDE_FADE,
    PARALLAX,
    MATERIAL_SHARED_AXIS
}

/**
 * Navigation transition configuration
 */
data class NavigationTransitionConfig(
    val type: NavigationTransitionType = NavigationTransitionType.SLIDE_FADE,
    val duration: Int = MotionTokens.Duration.pageTransition,
    val easing: Easing = MotionTokens.Easing.emphasizedDecelerate,
    val enableParallax: Boolean = true,
    val enablePreview: Boolean = true
)

/**
 * Enhanced navigation manager with advanced transitions
 */
@Stable
@OptIn(ExperimentalFoundationApi::class)
class EnhancedNavigationManager(
    private val pagerState: PagerState,
    private val transitionConfig: NavigationTransitionConfig = NavigationTransitionConfig()
) {
    
    private var _isTransitioning by mutableStateOf(false)
    val isTransitioning: Boolean get() = _isTransitioning
    
    private var _transitionProgress by mutableStateOf(0f)
    val transitionProgress: Float get() = _transitionProgress
    
    private var _pendingDestination by mutableStateOf<AppDestination?>(null)
    val pendingDestination: AppDestination? get() = _pendingDestination
    
    /**
     * Calculate page offset for transition effects
     */
    fun getPageOffset(pageIndex: Int): Float {
        return pagerState.currentPage - pageIndex + pagerState.currentPageOffsetFraction
    }
    
    /**
     * Get transition alpha for page
     */
    fun getTransitionAlpha(pageIndex: Int): Float {
        val offset = abs(getPageOffset(pageIndex))
        return when (transitionConfig.type) {
            NavigationTransitionType.FADE,
            NavigationTransitionType.SLIDE_FADE -> {
                1f - (offset * 0.3f).coerceIn(0f, 1f)
            }
            NavigationTransitionType.MATERIAL_SHARED_AXIS -> {
                if (offset < 1f) 1f - offset * 0.4f else 0.6f
            }
            else -> 1f
        }
    }
    
    /**
     * Get transition scale for page
     */
    fun getTransitionScale(pageIndex: Int): Float {
        val offset = abs(getPageOffset(pageIndex))
        return when (transitionConfig.type) {
            NavigationTransitionType.SCALE,
            NavigationTransitionType.MATERIAL_SHARED_AXIS -> {
                1f - (offset * 0.1f).coerceIn(0f, 0.1f)
            }
            NavigationTransitionType.PARALLAX -> {
                1f - (offset * 0.05f).coerceIn(0f, 0.05f)
            }
            else -> 1f
        }
    }
    
    /**
     * Get translation X for parallax effect
     */
    fun getTranslationX(pageIndex: Int, pageWidth: Float): Float {
        val offset = getPageOffset(pageIndex)
        return when (transitionConfig.type) {
            NavigationTransitionType.PARALLAX -> {
                offset * pageWidth * 0.3f
            }
            NavigationTransitionType.MATERIAL_SHARED_AXIS -> {
                if (abs(offset) > 1f) {
                    if (offset > 0) pageWidth * 0.2f else -pageWidth * 0.2f
                } else {
                    offset * pageWidth * 0.1f
                }
            }
            else -> 0f
        }
    }
    
    /**
     * Get elevation for layered effect
     */
    fun getElevation(pageIndex: Int): Float {
        val offset = abs(getPageOffset(pageIndex))
        return when (transitionConfig.type) {
            NavigationTransitionType.MATERIAL_SHARED_AXIS,
            NavigationTransitionType.PARALLAX -> {
                (1f - offset) * 8f
            }
            else -> 0f
        }
    }
    
    /**
     * Enhanced navigation with transition feedback
     */
    suspend fun navigateToDestinationWithTransition(destination: AppDestination) {
        _isTransitioning = true
        _pendingDestination = destination
        
        try {
            // Start transition
            _transitionProgress = 0f
            
            // Animate transition progress
            val progressAnimation = Animatable(0f)
            progressAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = transitionConfig.duration,
                    easing = transitionConfig.easing
                )
            ) {
                _transitionProgress = value
            }
            
            // Perform actual navigation
            pagerState.animateScrollToPage(
                page = destination.index,
                animationSpec = tween(
                    durationMillis = transitionConfig.duration,
                    easing = transitionConfig.easing
                )
            )
            
            // Small delay for visual completion
            delay(50)
            
        } finally {
            _isTransitioning = false
            _pendingDestination = null
            _transitionProgress = 0f
        }
    }
    
    /**
     * Get transition modifier for page content
     */
    @Composable
    fun getPageTransitionModifier(pageIndex: Int): Modifier {
        return Modifier
            .alpha(getTransitionAlpha(pageIndex))
            .scale(getTransitionScale(pageIndex))
            .graphicsLayer {
                val pageWidth = size.width
                translationX = getTranslationX(pageIndex, pageWidth)
                
                // Add depth effect
                if (transitionConfig.enableParallax) {
                    shadowElevation = getElevation(pageIndex)
                    
                    // Subtle rotation for depth
                    if (transitionConfig.type == NavigationTransitionType.PARALLAX) {
                        rotationY = getPageOffset(pageIndex) * 2f
                    }
                }
            }
    }
    
    /**
     * Create enter transition for new content
     */
    fun getEnterTransition(fromIndex: Int, toIndex: Int): EnterTransition {
        val isForward = toIndex > fromIndex
        
        return when (transitionConfig.type) {
            NavigationTransitionType.SLIDE_HORIZONTAL -> {
                slideInHorizontally(
                    initialOffsetX = { if (isForward) it else -it },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.SLIDE_VERTICAL -> {
                slideInVertically(
                    initialOffsetY = { if (isForward) it else -it },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.FADE -> {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.SCALE -> {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.SLIDE_FADE -> {
                slideInHorizontally(
                    initialOffsetX = { if (isForward) it / 2 else -it / 2 },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.MATERIAL_SHARED_AXIS -> {
                slideInHorizontally(
                    initialOffsetX = { if (isForward) it / 3 else -it / 3 },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = MotionTokens.Easing.emphasizedDecelerate
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration / 2,
                        delayMillis = transitionConfig.duration / 4,
                        easing = LinearEasing
                    )
                )
            }
            
            NavigationTransitionType.PARALLAX -> {
                slideInHorizontally(
                    initialOffsetX = { if (isForward) it else -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }
    
    /**
     * Create exit transition for old content
     */
    fun getExitTransition(fromIndex: Int, toIndex: Int): ExitTransition {
        val isForward = toIndex > fromIndex
        
        return when (transitionConfig.type) {
            NavigationTransitionType.SLIDE_HORIZONTAL -> {
                slideOutHorizontally(
                    targetOffsetX = { if (isForward) -it else it },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.SLIDE_VERTICAL -> {
                slideOutVertically(
                    targetOffsetY = { if (isForward) -it else it },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.FADE -> {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.SCALE -> {
                scaleOut(
                    targetScale = 1.1f,
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.SLIDE_FADE -> {
                slideOutHorizontally(
                    targetOffsetX = { if (isForward) -it / 2 else it / 2 },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = transitionConfig.easing
                    )
                )
            }
            
            NavigationTransitionType.MATERIAL_SHARED_AXIS -> {
                slideOutHorizontally(
                    targetOffsetX = { if (isForward) -it / 3 else it / 3 },
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration,
                        easing = MotionTokens.Easing.emphasizedAccelerate
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = transitionConfig.duration / 2,
                        easing = LinearEasing
                    )
                )
            }
            
            NavigationTransitionType.PARALLAX -> {
                slideOutHorizontally(
                    targetOffsetX = { if (isForward) -it else it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + scaleOut(
                    targetScale = 1.05f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }
}

/**
 * Remember enhanced navigation manager
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberEnhancedNavigationManager(
    pagerState: PagerState,
    transitionConfig: NavigationTransitionConfig = NavigationTransitionConfig()
): EnhancedNavigationManager {
    return remember(pagerState, transitionConfig) {
        EnhancedNavigationManager(pagerState, transitionConfig)
    }
}