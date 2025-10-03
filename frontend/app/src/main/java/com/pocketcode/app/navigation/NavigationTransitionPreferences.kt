package com.pocketcode.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.pocketcode.core.ui.tokens.MotionTokens

/**
 * Navigation transition preferences manager
 */
private val android.content.Context.transitionPreferencesDataStore by preferencesDataStore(
    name = "navigation_transition_preferences"
)

/**
 * Navigation transition preferences
 */
class NavigationTransitionPreferences(
    private val context: android.content.Context
) {
    companion object {
        private val TRANSITION_TYPE_KEY = stringPreferencesKey("transition_type")
        private val TRANSITION_DURATION_KEY = intPreferencesKey("transition_duration")
        private val ENABLE_PARALLAX_KEY = booleanPreferencesKey("enable_parallax")
        private val ENABLE_PREVIEW_KEY = booleanPreferencesKey("enable_preview")
        private val ENABLE_HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("enable_haptic_feedback")
    }
    
    /**
     * Get transition configuration flow
     */
    val transitionConfigFlow: Flow<NavigationTransitionConfig> = 
        context.transitionPreferencesDataStore.data.map { preferences ->
            val transitionTypeString = preferences[TRANSITION_TYPE_KEY] 
                ?: NavigationTransitionType.MATERIAL_SHARED_AXIS.name
            val transitionType = try {
                NavigationTransitionType.valueOf(transitionTypeString)
            } catch (e: IllegalArgumentException) {
                NavigationTransitionType.MATERIAL_SHARED_AXIS
            }
            
            NavigationTransitionConfig(
                type = transitionType,
                duration = preferences[TRANSITION_DURATION_KEY] ?: MotionTokens.Duration.pageTransition,
                enableParallax = preferences[ENABLE_PARALLAX_KEY] ?: true,
                enablePreview = preferences[ENABLE_PREVIEW_KEY] ?: true
            )
        }
    
    /**
     * Save transition type preference
     */
    suspend fun setTransitionType(type: NavigationTransitionType) {
        context.transitionPreferencesDataStore.edit { preferences ->
            preferences[TRANSITION_TYPE_KEY] = type.name
        }
    }
    
    /**
     * Save transition duration preference
     */
    suspend fun setTransitionDuration(duration: Int) {
        context.transitionPreferencesDataStore.edit { preferences ->
            preferences[TRANSITION_DURATION_KEY] = duration.coerceIn(100, 1000)
        }
    }
    
    /**
     * Save parallax preference
     */
    suspend fun setEnableParallax(enabled: Boolean) {
        context.transitionPreferencesDataStore.edit { preferences ->
            preferences[ENABLE_PARALLAX_KEY] = enabled
        }
    }
    
    /**
     * Save preview preference
     */
    suspend fun setEnablePreview(enabled: Boolean) {
        context.transitionPreferencesDataStore.edit { preferences ->
            preferences[ENABLE_PREVIEW_KEY] = enabled
        }
    }
    
    /**
     * Save haptic feedback preference
     */
    suspend fun setEnableHapticFeedback(enabled: Boolean) {
        context.transitionPreferencesDataStore.edit { preferences ->
            preferences[ENABLE_HAPTIC_FEEDBACK_KEY] = enabled
        }
    }
    
    /**
     * Get haptic feedback preference flow
     */
    val hapticFeedbackFlow: Flow<Boolean> = 
        context.transitionPreferencesDataStore.data.map { preferences ->
            preferences[ENABLE_HAPTIC_FEEDBACK_KEY] ?: true
        }
}

/**
 * Composable to provide transition preferences
 */
@Composable
fun rememberNavigationTransitionPreferences(): NavigationTransitionPreferences {
    val context = LocalContext.current
    return remember(context) {
        NavigationTransitionPreferences(context)
    }
}

/**
 * Navigation transition settings data class for UI
 */
data class NavigationTransitionSettings(
    val availableTypes: List<NavigationTransitionType> = NavigationTransitionType.values().toList(),
    val availableDurations: List<Pair<String, Int>> = listOf(
        "Fast" to MotionTokens.Duration.fast,
        "Medium" to MotionTokens.Duration.medium,
        "Slow" to MotionTokens.Duration.slow,
        "Custom" to MotionTokens.Duration.pageTransition
    ),
    val typeDescriptions: Map<NavigationTransitionType, String> = mapOf(
        NavigationTransitionType.SLIDE_HORIZONTAL to "Classic horizontal slide",
        NavigationTransitionType.SLIDE_VERTICAL to "Vertical slide transition",
        NavigationTransitionType.FADE to "Simple fade in/out",
        NavigationTransitionType.SCALE to "Scale and fade effect",
        NavigationTransitionType.SLIDE_FADE to "Slide with fade overlay",
        NavigationTransitionType.PARALLAX to "3D parallax effect",
        NavigationTransitionType.MATERIAL_SHARED_AXIS to "Material Design shared axis"
    )
)

/**
 * Preview configurations for different transition types
 */
object TransitionPreviews {
    fun getPreviewConfig(type: NavigationTransitionType): NavigationTransitionConfig {
        return NavigationTransitionConfig(
            type = type,
            duration = MotionTokens.Duration.fast, // Faster for previews
            enableParallax = type == NavigationTransitionType.PARALLAX,
            enablePreview = true
        )
    }
    
    fun getTypeDisplayName(type: NavigationTransitionType): String {
        return when (type) {
            NavigationTransitionType.SLIDE_HORIZONTAL -> "Slide"
            NavigationTransitionType.SLIDE_VERTICAL -> "Vertical Slide"
            NavigationTransitionType.FADE -> "Fade"
            NavigationTransitionType.SCALE -> "Scale"
            NavigationTransitionType.SLIDE_FADE -> "Slide & Fade"
            NavigationTransitionType.PARALLAX -> "Parallax"
            NavigationTransitionType.MATERIAL_SHARED_AXIS -> "Material Design"
        }
    }
}