package com.pocketcode.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores oscuro de PocketCode
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF004A77),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFFB8C8DA),
    onSecondary = Color(0xFF23323F),
    secondaryContainer = Color(0xFF394857),
    onSecondaryContainer = Color(0xFFD4E4F6),
    tertiary = Color(0xFFD3BEE3),
    onTertiary = Color(0xFF392948),
    tertiaryContainer = Color(0xFF503F5F),
    onTertiaryContainer = Color(0xFFEFDBFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    outline = Color(0xFF8C9198),
    outlineVariant = Color(0xFF42474E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF2E3133),
    inversePrimary = Color(0xFF00639B),
    surfaceTint = Color(0xFF90CAF9),
)

/**
 * Esquema de colores claro de PocketCode
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00639B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF51606F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E4F6),
    onSecondaryContainer = Color(0xFF0D1D2A),
    tertiary = Color(0xFF685779),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEFDBFF),
    onTertiaryContainer = Color(0xFF231533),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFCFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72787E),
    outlineVariant = Color(0xFFC2C7CF),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2E3133),
    inverseOnSurface = Color(0xFFF0F0F4),
    inversePrimary = Color(0xFF90CAF9),
    surfaceTint = Color(0xFF00639B),
)

/**
 * Tema principal de PocketCode que envuelve MaterialTheme con colores personalizados.
 *
 * Este wrapper permite:
 * - Aplicar colores consistentes de PocketCode
 * - Gestionar modo oscuro/claro
 * - Configurar barra de estado del sistema
 * - Mantener compatibilidad con Material3
 *
 * @param darkTheme Si debe usar el tema oscuro. Por defecto detecta automáticamente.
 * @param dynamicColor Si debe usar colores dinámicos de Android 12+ (no implementado aún)
 * @param content Contenido Composable a renderizar dentro del tema
 *
 * @example
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     PocketTheme {
 *         // Tu contenido aquí
 *         PocketScaffold { ... }
 *     }
 * }
 * ```
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun PocketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // TODO: Implementar colores dinámicos de Android 12+
    content: @Composable () -> Unit
) {
    // Seleccionar esquema de colores según modo
    val colorScheme = when {
        // TODO: Implementar soporte para colores dinámicos en Android 12+
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Configurar color de barra de estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
