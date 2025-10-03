package com.pocketcode.core.ui.components.input

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto especializado para contraseñas del sistema de diseño Pocket.
 *
 * PocketPasswordField es un wrapper de PocketTextField optimizado para entrada
 * de contraseñas que incluye:
 * - Ocultación/visualización de contraseña mediante toggle
 * - Transformación visual automática
 * - Icono de visibilidad integrado
 * - Validación de seguridad (opcional)
 *
 * @param value Valor actual del campo
 * @param onValueChange Callback cuando cambia el valor
 * @param modifier Modificador para el campo
 * @param label Etiqueta del campo (opcional)
 * @param placeholder Placeholder cuando está vacío (opcional)
 * @param error Mensaje de error (null = sin error)
 * @param helperText Texto de ayuda mostrado debajo del campo
 * @param enabled Si el campo está habilitado
 * @param readOnly Si el campo es solo lectura
 * @param required Si el campo es obligatorio
 * @param showPasswordByDefault Si debe mostrar la contraseña inicialmente
 *
 * @example Uso básico:
 * ```kotlin
 * var password by remember { mutableStateOf("") }
 * PocketPasswordField(
 *     value = password,
 *     onValueChange = { password = it },
 *     label = "Contraseña"
 * )
 * ```
 *
 * @example Con validación:
 * ```kotlin
 * var password by remember { mutableStateOf("") }
 * var error by remember { mutableStateOf<String?>(null) }
 *
 * PocketPasswordField(
 *     value = password,
 *     onValueChange = {
 *         password = it
 *         error = when {
 *             it.length < 8 -> "Mínimo 8 caracteres"
 *             !it.any { c -> c.isDigit() } -> "Debe contener al menos un número"
 *             !it.any { c -> c.isUpperCase() } -> "Debe contener al menos una mayúscula"
 *             else -> null
 *         }
 *     },
 *     label = "Contraseña",
 *     error = error,
 *     helperText = "Mínimo 8 caracteres, una mayúscula y un número"
 * )
 * ```
 *
 * @example En formulario de registro:
 * ```kotlin
 * FormContainer(
 *     title = "Crear cuenta",
 *     submitText = "Registrarse"
 * ) {
 *     val passwordState = rememberFieldState()
 *     val confirmPasswordState = rememberFieldState()
 *
 *     FormField("Contraseña", passwordState) {
 *         PocketPasswordField(
 *             value = passwordState.value,
 *             onValueChange = { passwordState.value = it },
 *             error = passwordState.error
 *         )
 *     }
 *
 *     FormField("Confirmar contraseña", confirmPasswordState) {
 *         PocketPasswordField(
 *             value = confirmPasswordState.value,
 *             onValueChange = {
 *                 confirmPasswordState.value = it
 *                 if (it != passwordState.value) {
 *                     confirmPasswordState.error = "Las contraseñas no coinciden"
 *                 } else {
 *                     confirmPasswordState.error = null
 *                 }
 *             },
 *             error = confirmPasswordState.error
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun PocketPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    required: Boolean = false,
    showPasswordByDefault: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(showPasswordByDefault) }

    PocketTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label ?: "",
        placeholder = placeholder,
        isError = !error.isNullOrEmpty(),
        errorMessage = error,
        helperText = helperText,
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible }
            ) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = if (passwordVisible) {
                        "Ocultar contraseña"
                    } else {
                        "Mostrar contraseña"
                    }
                )
            }
        }
    )
}

/**
 * Validador de fortaleza de contraseña.
 *
 * Utilidad para evaluar la seguridad de una contraseña según criterios comunes.
 *
 * @param password Contraseña a validar
 * @return Par de (esValida, mensajeError)
 *
 * @example
 * ```kotlin
 * val (isValid, errorMessage) = validatePasswordStrength(password)
 * if (!isValid) {
 *     passwordError = errorMessage
 * }
 * ```
 */
fun validatePasswordStrength(password: String): Pair<Boolean, String?> {
    return when {
        password.length < 8 -> false to "La contraseña debe tener al menos 8 caracteres"
        !password.any { it.isDigit() } -> false to "Debe contener al menos un número"
        !password.any { it.isUpperCase() } -> false to "Debe contener al menos una mayúscula"
        !password.any { it.isLowerCase() } -> false to "Debe contener al menos una minúscula"
        else -> true to null
    }
}

/**
 * Nivel de fortaleza de contraseña.
 */
enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}

/**
 * Calcula el nivel de fortaleza de una contraseña.
 *
 * @param password Contraseña a evaluar
 * @return PasswordStrength indicando la fortaleza
 *
 * @example
 * ```kotlin
 * val strength = calculatePasswordStrength(password)
 * val strengthColor = when (strength) {
 *     PasswordStrength.WEAK -> Color.Red
 *     PasswordStrength.MEDIUM -> Color.Orange
 *     PasswordStrength.STRONG -> Color.Green
 *     PasswordStrength.VERY_STRONG -> Color.Blue
 * }
 * ```
 */
fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0

    // Longitud
    when {
        password.length >= 12 -> score += 2
        password.length >= 8 -> score += 1
    }

    // Caracteres variados
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        0, 1, 2 -> PasswordStrength.WEAK
        3, 4 -> PasswordStrength.MEDIUM
        5, 6 -> PasswordStrength.STRONG
        else -> PasswordStrength.VERY_STRONG
    }
}
