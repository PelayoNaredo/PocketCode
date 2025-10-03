package com.pocketcode.core.ui.components.form

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.tokens.SpacingTokens

/**
 * Estado de un campo de formulario.
 *
 * Gestiona el valor y estado de validación de un campo.
 *
 * @param initialValue Valor inicial del campo
 */
class FieldState(initialValue: String = "") {
	var value by mutableStateOf(initialValue)
	var error by mutableStateOf<String?>(null)
	var isValid by mutableStateOf(true)
}

@Composable
fun rememberFieldState(initialValue: String = ""): FieldState {
	return remember { FieldState(initialValue) }
}

@Stable
interface FormScope {
	fun validate(fieldState: FieldState, vararg validations: Validation)
}

private class FormScopeImpl : FormScope {
	override fun validate(fieldState: FieldState, vararg validations: Validation) {
		validations.forEach { validation ->
			if (!validation.isValid(fieldState.value)) {
				fieldState.error = validation.errorMessage
				fieldState.isValid = false
				return
			}
		}
		fieldState.error = null
		fieldState.isValid = true
	}
}

data class Validation(
	val errorMessage: String,
	val isValid: (String) -> Boolean
)

enum class ValidationType {
	REQUIRED,
	EMAIL,
	MIN_LENGTH,
	MAX_LENGTH,
	NUMERIC,
	ALPHANUMERIC
}

fun validation(
	type: ValidationType,
	errorMessage: String? = null
): Validation {
	return when (type) {
		ValidationType.REQUIRED -> Validation(
			errorMessage = errorMessage ?: "Este campo es obligatorio",
			isValid = { it.isNotBlank() }
		)
		ValidationType.EMAIL -> Validation(
			errorMessage = errorMessage ?: "Email inválido",
			isValid = { it.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) }
		)
		ValidationType.NUMERIC -> Validation(
			errorMessage = errorMessage ?: "Solo números permitidos",
			isValid = { it.all(Char::isDigit) }
		)
		ValidationType.ALPHANUMERIC -> Validation(
			errorMessage = errorMessage ?: "Solo letras y números permitidos",
			isValid = { it.all { char -> char.isLetterOrDigit() } }
		)
		ValidationType.MIN_LENGTH -> Validation(
			errorMessage = errorMessage ?: "Mínimo 3 caracteres",
			isValid = { it.length >= 3 }
		)
		ValidationType.MAX_LENGTH -> Validation(
			errorMessage = errorMessage ?: "Máximo 50 caracteres",
			isValid = { it.length <= 50 }
		)
	}
}

enum class ValidationSeverity {
	INFO,
	SUCCESS,
	WARNING,
	ERROR
}

data class ValidationMessage(
	val message: String,
	val type: ValidationSeverity = ValidationSeverity.INFO,
	val field: String? = null
)

class ValidationMessageBuilder {
	private var message: String? = null
	private var type: ValidationSeverity = ValidationSeverity.INFO
	private var field: String? = null

	fun message(value: String) {
		message = value
	}

	fun type(value: ValidationSeverity) {
		type = value
	}

	fun field(value: String) {
		field = value
	}

	fun build(): ValidationMessage = ValidationMessage(
		message = message ?: "",
		type = type,
		field = field
	)
}

fun validation(builder: ValidationMessageBuilder.() -> Unit): ValidationMessage {
	val validationBuilder = ValidationMessageBuilder()
	validationBuilder.builder()
	return validationBuilder.build()
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun FormField(
	key: String? = null,
	label: String? = null,
	fieldState: FieldState? = null,
	isValid: Boolean = true,
	errorMessage: String? = null,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	val resolvedError = when {
		!isValid && !errorMessage.isNullOrBlank() -> errorMessage
		!isValid && !fieldState?.error.isNullOrBlank() -> fieldState?.error
		fieldState?.isValid == false -> fieldState?.error
		else -> errorMessage ?: fieldState?.error
	}

	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		if (!label.isNullOrBlank()) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Medium
			)
		}
		content()
		if (!resolvedError.isNullOrBlank()) {
			Text(
				text = resolvedError,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.error
			)
		}
	}
}

@Composable
fun ValidationDisplay(
	validations: List<ValidationMessage>,
	modifier: Modifier = Modifier
) {
	if (validations.isEmpty()) return

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		validations.forEach { validation ->
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				val color = when (validation.type) {
					ValidationSeverity.INFO -> MaterialTheme.colorScheme.primary
					ValidationSeverity.SUCCESS -> MaterialTheme.colorScheme.tertiary
					ValidationSeverity.WARNING -> MaterialTheme.colorScheme.secondary
					ValidationSeverity.ERROR -> MaterialTheme.colorScheme.error
				}
				Canvas(
					modifier = Modifier
						.size(10.dp)
				) {
					drawCircle(color = color)
				}
				Text(
					text = validation.message,
					style = MaterialTheme.typography.bodySmall,
					color = color
				)
			}
		}
	}
}

@Composable
fun FormContainer(
	modifier: Modifier = Modifier,
	title: String? = null,
	description: String? = null,
	isLoading: Boolean = false,
	submitText: String = "Continuar",
	onSubmit: () -> Unit = {},
	submitEnabled: Boolean = true,
	showSubmitButton: Boolean = true,
	scrollable: Boolean = true,
	content: @Composable FormScope.() -> Unit
) {
	val scrollState = rememberScrollState()
	val containerModifier = modifier
		.fillMaxWidth()
		.then(if (scrollable) Modifier.verticalScroll(scrollState) else Modifier)
		.padding(SpacingTokens.Semantic.contentPaddingLarge)

	Column(
		modifier = containerModifier,
		verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingLarge)
	) {
		if (title != null || description != null) {
			Column(
				verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingSmall)
			) {
				if (title != null) {
					Text(
						text = title,
						style = MaterialTheme.typography.headlineSmall,
						fontWeight = FontWeight.Bold
					)
				}
				if (description != null) {
					Text(
						text = description,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}

		val formScope = remember { FormScopeImpl() }
		Column(
			verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
		) {
			formScope.content()
		}

		if (showSubmitButton) {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				if (isLoading) {
					CircularProgressIndicator()
				} else {
					PocketButton(
						text = submitText,
						onClick = onSubmit,
						enabled = submitEnabled,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}
}
