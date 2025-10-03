package com.pocketcode.core.ui.components.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Indicador de pasos para navegación secuencial (stepper).
 *
 * StepperIndicator muestra el progreso a través de una serie de pasos,
 * típicamente usado en:
 * - Onboarding
 * - Formularios multi-paso
 * - Asistentes (wizards)
 * - Tutoriales
 *
 * @param totalSteps Número total de pasos
 * @param currentStep Paso actual (0-indexed)
 * @param modifier Modificador
 * @param activeColor Color del paso activo
 * @param inactiveColor Color de pasos inactivos
 * @param completedColor Color de pasos completados
 * @param dotSize Tamaño de cada punto
 * @param spacing Espaciado entre puntos
 *
 * @example En onboarding:
 * ```kotlin
 * val steps = 4
 * var currentStep by remember { mutableStateOf(0) }
 *
 * Column {
 *     HorizontalPager(
 *         count = steps,
 *         state = pagerState
 *     ) { page ->
 *         OnboardingPage(page)
 *     }
 *
 *     StepperIndicator(
 *         totalSteps = steps,
 *         currentStep = currentStep,
 *         modifier = Modifier.align(Alignment.CenterHorizontally)
 *     )
 *
 *     Row {
 *         if (currentStep > 0) {
 *             PocketButton("Anterior") { currentStep-- }
 *         }
 *         PocketButton("Siguiente") {
 *             if (currentStep < steps - 1) currentStep++
 *         }
 *     }
 * }
 * ```
 *
 * @example En formulario multi-paso:
 * ```kotlin
 * StepperIndicator(
 *     totalSteps = 3,
 *     currentStep = formStep,
 *     completedColor = MaterialTheme.colorScheme.primary
 * )
 * ```
 */
@Composable
fun StepperIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    completedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    dotSize: Dp = 8.dp,
    spacing: Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            val color = when {
                step == currentStep -> activeColor
                step < currentStep -> completedColor
                else -> inactiveColor
            }

            Canvas(
                modifier = Modifier.size(dotSize)
            ) {
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2
                )
            }
        }
    }
}

/**
 * Indicador de pasos lineal (con líneas entre puntos).
 *
 * LinearStepperIndicator muestra el progreso con líneas conectando los pasos.
 *
 * @example
 * ```kotlin
 * LinearStepperIndicator(
 *     totalSteps = 5,
 *     currentStep = 2
 * )
 * ```
 */
@Composable
fun LinearStepperIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    dotSize: Dp = 10.dp,
    lineThickness: Dp = 2.dp,
    lineLength: Dp = 24.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { step ->
            // Punto
            val dotColor = if (step <= currentStep) activeColor else inactiveColor
            Canvas(
                modifier = Modifier.size(dotSize)
            ) {
                drawCircle(
                    color = dotColor,
                    radius = size.minDimension / 2
                )
            }

            // Línea (excepto después del último paso)
            if (step < totalSteps - 1) {
                val lineColor = if (step < currentStep) activeColor else inactiveColor
                Canvas(
                    modifier = Modifier
                        .width(lineLength)
                        .height(lineThickness)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height
                    )
                }
            }
        }
    }
}
