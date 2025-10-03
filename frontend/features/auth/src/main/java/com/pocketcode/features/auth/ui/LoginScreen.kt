@file:OptIn(ExperimentalMaterial3Api::class)

package com.pocketcode.features.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.form.FormContainer
import com.pocketcode.core.ui.components.form.FormField
import com.pocketcode.core.ui.components.form.ValidationDisplay
import com.pocketcode.core.ui.components.form.ValidationSeverity
import com.pocketcode.core.ui.components.form.rememberFieldState
import com.pocketcode.core.ui.components.form.validation
import com.pocketcode.core.ui.components.input.PocketPasswordField
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonSize
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.theme.PocketTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    val emailRegex = remember { Regex("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}") }

    PocketScaffold(
        config = PocketScaffoldConfig(
            paddingValues = PaddingValues(
                horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
                vertical = SpacingTokens.Semantic.screenPaddingVertical
            ),
            isScrollable = false,
            backgroundColor = ColorTokens.background
        ),
        topBar = {
            PocketTopBar(
                title = "Acceso a PocketCode",
                subtitle = "Sincroniza tus proyectos en la nube",
                actions = listOf(
                    TopBarAction(
                        icon = PocketIcons.Clear,
                        contentDescription = "Continuar sin cuenta",
                        onClick = onSkip
                    )
                )
            )
        }
    ) { paddingValues ->
        FormContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            title = "Iniciar sesión",
            description = "Introduce tus credenciales para desbloquear sincronización y respaldos automáticos.",
            isLoading = isLoading,
            showSubmitButton = false,
            scrollable = true
        ) {
            val emailState = rememberFieldState()
            val passwordState = rememberFieldState()

            val emailValue = emailState.value.trim()
            val passwordValue = passwordState.value

            val emailError = remember(emailValue) {
                when {
                    emailValue.isEmpty() -> "El correo es obligatorio"
                    !emailRegex.matches(emailValue) -> "Introduce un correo válido"
                    else -> null
                }
            }

            val passwordError = remember(passwordValue) {
                when {
                    passwordValue.isEmpty() -> "La contraseña es obligatoria"
                    passwordValue.length < 8 -> "Debe tener al menos 8 caracteres"
                    else -> null
                }
            }

            val loginEnabled = !isLoading &&
                emailError == null &&
                passwordError == null &&
                emailValue.isNotBlank() &&
                passwordValue.isNotBlank()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PocketCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = ComponentTokens.CardVariant.Filled
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
                    ) {
                        Text(
                            text = "🔐",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = "Bienvenido nuevamente",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Conecta tu cuenta para acceder a sincronización multidispositivo y respaldos en la nube.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                FormField(
                    key = "email",
                    isValid = emailError == null,
                    errorMessage = emailError
                ) {
                    PocketTextField(
                        value = emailState.value,
                        onValueChange = { emailState.value = it },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@pocketcode.dev",
                        isError = !emailError.isNullOrBlank(),
                        errorMessage = emailError,
                        leadingIcon = {
                            Icon(
                                imageVector = PocketIcons.Email,
                                contentDescription = "Correo electrónico"
                            )
                        }
                    )
                }

                FormField(
                    key = "password",
                    isValid = passwordError == null,
                    errorMessage = passwordError
                ) {
                    PocketPasswordField(
                        value = passwordState.value,
                        onValueChange = { passwordState.value = it },
                        label = "Contraseña",
                        placeholder = "Introduce tu contraseña",
                        error = passwordError
                    )
                }

                authError?.let { errorMessage ->
                    ValidationDisplay(
                        validations = listOf(
                            validation {
                                message(errorMessage)
                                type(ValidationSeverity.ERROR)
                                field("authentication")
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                PocketButton(
                    text = "Iniciar sesión",
                    onClick = {
                        authError = null
                        isLoading = true
                        coroutineScope.launch {
                            delay(1500)
                            isLoading = false
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    loading = isLoading,
                    enabled = loginEnabled,
                    size = ButtonSize.Large
                )

                PocketButton(
                    text = "Continuar sin cuenta",
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Outline,
                    enabled = !isLoading
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PocketButton(
                        text = "¿Olvidaste tu contraseña?",
                        onClick = { /* TODO: recuperar contraseña */ },
                        variant = ButtonVariant.Text,
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    PocketTheme {
        LoginScreen()
    }
}
