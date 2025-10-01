package com.pocketcode.core.ui.components.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

/**
 * PocketTextField - Campo de texto estilizado con tokens del sistema de diseño.
 *
 * Wrapper de OutlinedTextField que aplica los tokens de Pocket para garantizar
 * consistencia visual en toda la aplicación.
 *
 * @param value Texto actual del campo
 * @param onValueChange Callback cuando cambia el texto
 * @param label Etiqueta del campo
 * @param modifier Modificador para customizar el layout
 * @param enabled Si el campo está habilitado para edición
 * @param readOnly Si el campo es solo lectura
 * @param isError Si el campo está en estado de error
 * @param errorMessage Mensaje de error a mostrar debajo del campo
 * @param helperText Texto de ayuda a mostrar debajo del campo
 * @param placeholder Texto placeholder cuando el campo está vacío
 * @param leadingIcon Ícono al inicio del campo
 * @param trailingIcon Ícono al final del campo
 * @param prefix Texto prefijo dentro del campo
 * @param suffix Texto sufijo dentro del campo
 * @param maxLines Número máximo de líneas (para multiline)
 * @param singleLine Si el campo es de una sola línea
 * @param visualTransformation Transformación visual (ej: password)
 * @param keyboardOptions Opciones del teclado
 * @param keyboardActions Acciones del teclado
 * @param interactionSource Source de interacciones para el campo
 *
 * @sample PocketTextFieldSamples
 */
@Composable
fun PocketTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    maxLines: Int = 1,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            isError = isError,
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = {
                when {
                    isError && errorMessage != null -> {
                        Text(
                            text = errorMessage,
                            style = TypographyTokens.Body.small,
                            color = ColorTokens.error
                        )
                    }
                    helperText != null -> {
                        Text(
                            text = helperText,
                            style = TypographyTokens.Body.small,
                            color = ColorTokens.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            maxLines = maxLines,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.primary,
                unfocusedBorderColor = ColorTokens.outline,
                errorBorderColor = ColorTokens.error,
                focusedLabelColor = ColorTokens.primary,
                unfocusedLabelColor = ColorTokens.onSurface.copy(alpha = 0.6f),
                errorLabelColor = ColorTokens.error,
                cursorColor = ColorTokens.primary,
                errorCursorColor = ColorTokens.error,
                focusedTextColor = ColorTokens.onSurface,
                unfocusedTextColor = ColorTokens.onSurface,
                disabledTextColor = ColorTokens.onSurface.copy(alpha = 0.38f),
                errorTextColor = ColorTokens.onSurface
            )
        )
    }
}

/**
 * Ejemplos de uso de PocketTextField
 */
private object PocketTextFieldSamples {
    
    @Composable
    fun BasicUsage() {
        var text by remember { mutableStateOf("") }
        
        PocketTextField(
            value = text,
            onValueChange = { text = it },
            label = "Nombre"
        )
    }
    
    @Composable
    fun WithError() {
        var email by remember { mutableStateOf("") }
        val isValid = email.contains("@")
        
        PocketTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            isError = !isValid && email.isNotEmpty(),
            errorMessage = if (!isValid && email.isNotEmpty()) "Email inválido" else null,
            helperText = "Ingresa tu correo electrónico"
        )
    }
    
    @Composable
    fun Multiline() {
        var description by remember { mutableStateOf("") }
        
        PocketTextField(
            value = description,
            onValueChange = { description = it },
            label = "Descripción",
            maxLines = 5,
            singleLine = false,
            helperText = "${description.length}/500 caracteres"
        )
    }
    
    @Composable
    fun WithIcons() {
        var search by remember { mutableStateOf("") }
        
        PocketTextField(
            value = search,
            onValueChange = { search = it },
            label = "Buscar",
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                    }
                }
            }
        )
    }
}
