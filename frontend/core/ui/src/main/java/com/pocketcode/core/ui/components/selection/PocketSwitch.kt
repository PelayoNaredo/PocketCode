package com.pocketcode.core.ui.components.selection

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.SpacingTokens

/**
 * Switch (interruptor) del sistema de diseño Pocket.
 *
 * PocketSwitch es un wrapper de Switch de Material3 que proporciona:
 * - Estilos consistentes con el design system
 * - Soporte para etiquetas y descripciones
 * - Estados enabled/disabled
 * - Integración con formularios
 *
 * @param checked Si el switch está activado
 * @param onCheckedChange Callback cuando cambia el estado
 * @param modifier Modificador para el switch
 * @param enabled Si el switch está habilitado
 * @param colors Colores personalizados (null = usa defaults)
 * @param interactionSource Source de interacciones para animaciones
 * @param thumbContent Contenido opcional dentro del thumb (icono)
 *
 * @example Uso básico:
 * ```kotlin
 * var isEnabled by remember { mutableStateOf(false) }
 * PocketSwitch(
 *     checked = isEnabled,
 *     onCheckedChange = { isEnabled = it }
 * )
 * ```
 *
 * @example Con etiqueta (usar SimplePocketSwitch):
 * ```kotlin
 * var notifications by remember { mutableStateOf(true) }
 * SimplePocketSwitch(
 *     label = "Notificaciones",
 *     description = "Recibir alertas de nuevos mensajes",
 *     checked = notifications,
 *     onCheckedChange = { notifications = it }
 * )
 * ```
 */
@Composable
fun PocketSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    thumbContent: (@Composable () -> Unit)? = null
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        thumbContent = thumbContent
    )
}

/**
 * Switch simple con etiqueta y descripción.
 *
 * SimplePocketSwitch es una variante de PocketSwitch que incluye:
 * - Etiqueta a la izquierda del switch
 * - Descripción opcional debajo de la etiqueta
 * - Layout consistente para configuraciones
 * - Clickeable en toda el área (no solo el switch)
 *
 * @param label Etiqueta principal del switch
 * @param checked Si el switch está activado
 * @param onCheckedChange Callback cuando cambia el estado
 * @param modifier Modificador para el contenedor
 * @param description Descripción opcional debajo de la etiqueta
 * @param enabled Si el switch está habilitado
 * @param colors Colores personalizados del switch
 *
 * @example En pantalla de configuración:
 * ```kotlin
 * var darkMode by remember { mutableStateOf(false) }
 * var notifications by remember { mutableStateOf(true) }
 * var analytics by remember { mutableStateOf(false) }
 *
 * Column {
 *     SimplePocketSwitch(
 *         label = "Modo oscuro",
 *         description = "Usar tema oscuro en toda la aplicación",
 *         checked = darkMode,
 *         onCheckedChange = { darkMode = it }
 *     )
 *
 *     SimplePocketSwitch(
 *         label = "Notificaciones",
 *         description = "Recibir alertas y actualizaciones",
 *         checked = notifications,
 *         onCheckedChange = { notifications = it }
 *     )
 *
 *     SimplePocketSwitch(
 *         label = "Analytics",
 *         description = "Ayúdanos a mejorar compartiendo datos de uso anónimos",
 *         checked = analytics,
 *         onCheckedChange = { analytics = it }
 *     )
 * }
 * ```
 *
 * @example Con estado deshabilitado:
 * ```kotlin
 * SimplePocketSwitch(
 *     label = "Función premium",
 *     description = "Disponible solo para usuarios premium",
 *     checked = false,
 *     onCheckedChange = null,
 *     enabled = false
 * )
 * ```
 */
/**
 * Sobrecarga de PocketSwitch que acepta label y description (redirección a SimplePocketSwitch).
 *
 * Permite usar PocketSwitch con la misma API que SimplePocketSwitch para compatibilidad.
 */
@Composable
fun PocketSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    SimplePocketSwitch(
        label = label,
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        description = description,
        enabled = enabled,
        colors = colors
    )
}

@Composable
fun SimplePocketSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    Surface(
        onClick = { onCheckedChange?.invoke(!checked) },
        modifier = modifier
            .fillMaxWidth()
            .semantics { role = Role.Switch },
        enabled = enabled && onCheckedChange != null,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = SpacingTokens.Semantic.contentPaddingNormal,
                    vertical = SpacingTokens.Semantic.contentPaddingSmall
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Etiqueta y descripción
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = SpacingTokens.Semantic.contentSpacingNormal),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }
                    )
                }
            }

            // Switch
            PocketSwitch(
                checked = checked,
                onCheckedChange = null, // El click se maneja en el Surface
                enabled = enabled,
                colors = colors
            )
        }
    }
}

/**
 * Grupo de switches relacionados.
 *
 * SwitchGroup organiza múltiples switches con un título común.
 *
 * @param title Título del grupo
 * @param modifier Modificador para el grupo
 * @param content Switches del grupo
 *
 * @example
 * ```kotlin
 * SwitchGroup(title = "Notificaciones") {
 *     SimplePocketSwitch(
 *         label = "Mensajes",
 *         checked = notifyMessages,
 *         onCheckedChange = { notifyMessages = it }
 *     )
 *     SimplePocketSwitch(
 *         label = "Actualizaciones",
 *         checked = notifyUpdates,
 *         onCheckedChange = { notifyUpdates = it }
 *     )
 *     SimplePocketSwitch(
 *         label = "Promociones",
 *         checked = notifyPromos,
 *         onCheckedChange = { notifyPromos = it }
 *     )
 * }
 * ```
 */
@Composable
fun SwitchGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingSmall)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                horizontal = SpacingTokens.Semantic.contentPaddingNormal,
                vertical = SpacingTokens.Semantic.contentPaddingSmall
            )
        )
        content()
    }
}
