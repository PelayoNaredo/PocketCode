package com.pocketcode.core.ui.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing as AnimationEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme

/**
 * Centralised collection of design tokens used across PocketCode.
 * These tokens intentionally mirror Material3 semantics so feature modules can
 * remain lightweight while still benefiting from consistent styling.
 */
object ColorTokens {
    val primary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    val onPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onPrimary

    val primaryContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primaryContainer

    val onPrimaryContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onPrimaryContainer

    val secondary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.secondary

    val onSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSecondary

    val background: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.background

    val surface: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface

    val surfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceVariant

    val onSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurfaceVariant

    val outline: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline

    val error: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.error

    val onError: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onError

    val success: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFF2E7D32)

    val warning: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFFF2994A)

    val info: Color
        @Composable
        @ReadOnlyComposable
        get() = Color(0xFF2962FF)

    object Primary {
        val primary400: Color = Color(0xFF7F67BE)
        val primary500: Color = Color(0xFF6750A4)
        val primary600: Color = Color(0xFF5B4893)
    }

    object Semantic {
        val success500: Color = Color(0xFF2E7D32)
        val warning500: Color = Color(0xFFF2994A)
        val info500: Color = Color(0xFF2962FF)
        val danger500: Color = Color(0xFFB3261E)
    }
}

object ElevationTokens {
    val level0 = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
    val level4 = 12.dp
}

object SpacingTokens {
    val xsmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val xlarge: Dp = 24.dp
    val xxlarge: Dp = 32.dp

    object Semantic {
        val screenPaddingHorizontal: Dp = 24.dp
        val screenPaddingVertical: Dp = 24.dp
        val contentSpacingLoose: Dp = 24.dp
        val contentSpacingRelaxed: Dp = 20.dp
        val contentSpacingNormal: Dp = 16.dp
        val contentSpacingTight: Dp = 12.dp
        val contentSpacingSmall: Dp = 8.dp
        val contentSpacingLarge: Dp = 24.dp
        val contentPaddingNormal: Dp = 16.dp
        val contentPaddingSmall: Dp = 12.dp
        val contentPaddingLarge: Dp = 24.dp
        val layoutMarginMedium: Dp = 32.dp
        val layoutMarginLarge: Dp = 48.dp
    }
}

object MotionTokens {
    object Duration {
        const val fast: Int = 150
        const val medium: Int = 250
        const val slow: Int = 400
        const val pageTransition: Int = 320
    }

    object Easing {
        val linear: AnimationEasing = LinearEasing
        val linearOutSlowIn: AnimationEasing = LinearOutSlowInEasing
        val fastOutLinearIn: AnimationEasing = FastOutLinearInEasing
        val fastOutSlowIn: AnimationEasing = FastOutSlowInEasing
        val emphasizedDecelerate: AnimationEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        val emphasizedAccelerate: AnimationEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    }

    @Deprecated("Use MotionTokens.Duration.fast", ReplaceWith("MotionTokens.Duration.fast"))
    const val durationShort: Int = Duration.fast

    @Deprecated("Use MotionTokens.Duration.medium", ReplaceWith("MotionTokens.Duration.medium"))
    const val durationMedium: Int = Duration.medium

    @Deprecated("Use MotionTokens.Duration.slow", ReplaceWith("MotionTokens.Duration.slow"))
    const val durationLong: Int = Duration.slow

    @Deprecated("Use MotionTokens.Easing.emphasizedDecelerate", ReplaceWith("MotionTokens.Easing.emphasizedDecelerate"))
    const val easingEmphasized: Int = 0
}

object TypographyTokens {
    object Display {
        val large: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.displayLarge

        val medium: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.displayMedium

        val small: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.displaySmall
    }

    object Headline {
        val large: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.headlineLarge

        val medium: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.headlineMedium

        val small: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.headlineSmall
    }

    object Title {
        val large: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.titleLarge

        val medium: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.titleMedium

        val small: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.titleSmall
    }

    object Body {
        val large: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.bodyLarge

        val medium: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.bodyMedium

        val small: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.bodySmall
    }

    object Label {
        val large: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.labelLarge

        val medium: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.labelMedium

        val small: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.labelSmall
    }

    object Code {
        val medium: TextStyle
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
    }

    val labelSmall: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.labelSmall

    val labelMedium: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.labelMedium

    val titleMedium: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.titleMedium

    val bodySmall: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.bodySmall

    val titleSmall: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography.titleSmall
}

object ComponentTokens {
    @Immutable
    enum class ButtonSize(val height: Dp, val horizontalPadding: Dp, val textStyle: TextStyle) {
        Small(40.dp, 12.dp, TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)),
        Medium(48.dp, 16.dp, TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)),
        Large(56.dp, 20.dp, TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold))
    }

    @Immutable
    enum class ButtonVariant {
        Primary,
        Secondary,
        Outline,
        Text,
        Danger
    }

    @Immutable
    enum class CardVariant {
        Filled,
        Elevated,
        Outlined
    }

    object ShapeTokens {
        val extraSmall: Shape
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.shapes.extraSmall

        val small: Shape
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.shapes.small

        val medium: Shape
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.shapes.medium

        val large: Shape
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.shapes.large

        val extraLarge: Shape
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.shapes.extraLarge
    }
}
