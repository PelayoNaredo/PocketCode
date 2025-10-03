@file:OptIn(ExperimentalMaterial3Api::class)

package com.pocketcode.features.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.form.FormContainer
import com.pocketcode.core.ui.components.form.FormField
import com.pocketcode.core.ui.components.form.FormScope
import com.pocketcode.core.ui.components.form.ValidationDisplay
import com.pocketcode.core.ui.components.form.ValidationSeverity
import com.pocketcode.core.ui.components.form.validation
import com.pocketcode.core.ui.components.input.PocketPasswordField
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.selection.SimplePocketSwitch
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.LocalGlobalSnackbarDispatcher
import com.pocketcode.core.ui.theme.PocketTheme
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonSize
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.domain.auth.model.AuthUser
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit = {},
    onSignUpSuccess: (AuthUser, String?) -> Unit = { _, _ -> },
    onAlreadyHaveAccount: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarDispatcher = LocalGlobalSnackbarDispatcher.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SignUpEvent.Success -> {
                    snackbarDispatcher.dispatch(
                        GlobalSnackbarEvent(
                            message = "Cuenta creada correctamente",
                            origin = GlobalSnackbarOrigin.AUTH,
                            severity = GlobalSnackbarSeverity.SUCCESS
                        )
                    )
                    onSignUpSuccess(event.user, event.byokKey)
                }
                is SignUpEvent.PasswordResetSent -> {
                    snackbarDispatcher.dispatch(
                        GlobalSnackbarEvent(
                            message = "Enviamos un correo a ${event.email} para restablecer tu contraseña",
                            origin = GlobalSnackbarOrigin.AUTH,
                            severity = GlobalSnackbarSeverity.INFO
                        )
                    )
                }
            }
        }
    }

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
                title = "Crear cuenta",
                subtitle = "Sincroniza tus proyectos en cualquier dispositivo",
                navigationIcon = PocketIcons.ChevronLeft,
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        FormContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            title = "Regístrate en PocketCode",
            description = "Crea una cuenta gratuita para activar respaldos y colaboración en tiempo real.",
            isLoading = uiState.isLoading,
            showSubmitButton = false,
            scrollable = false
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
            ) {
                SignUpPlanSummary()
                SignUpFormFields(uiState = uiState, viewModel = viewModel)
                SignUpActions(
                    uiState = uiState,
                    onSubmit = viewModel::submit,
                    onAlreadyHaveAccount = onAlreadyHaveAccount,
                    onForgotPassword = viewModel::sendPasswordReset
                )
            }
        }
    }
}

@Composable
private fun SignUpPlanSummary() {
    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = ComponentTokens.CardVariant.Filled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Semantic.contentSpacingNormal),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
            ) {
                PocketButton(
                    text = "Plan Starter",
                    onClick = {},
                    size = ButtonSize.Small,
                    enabled = false
                )
                PocketButton(
                    text = "Sin costo",
                    onClick = {},
                    size = ButtonSize.Small,
                    enabled = false,
                    variant = ButtonVariant.Secondary
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
            ) {
                PocketBenefitItem(text = "Sincronización multidispositivo")
                PocketBenefitItem(text = "Versionado automático de proyectos")
                PocketBenefitItem(text = "Invitaciones ilimitadas a colaboradores")
            }
        }
    }
}

@Composable
private fun FormScope.SignUpFormFields(
    uiState: SignUpUiState,
    viewModel: SignUpViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        FormField(
            key = "fullName",
            isValid = uiState.fullNameError == null,
            errorMessage = uiState.fullNameError
        ) {
            PocketTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Nombre completo",
                placeholder = "Nombre y apellidos",
                isError = !uiState.fullNameError.isNullOrBlank(),
                errorMessage = uiState.fullNameError,
                leadingIcon = { IconWrapper(PocketIcons.Person, "Nombre completo") }
            )
        }

        FormField(
            key = "email",
            isValid = uiState.emailError == null,
            errorMessage = uiState.emailError
        ) {
            PocketTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo electrónico",
                placeholder = "ejemplo@pocketcode.dev",
                isError = !uiState.emailError.isNullOrBlank(),
                errorMessage = uiState.emailError,
                leadingIcon = { IconWrapper(PocketIcons.Email, "Correo electrónico") }
            )
        }

        FormField(
            key = "password",
            isValid = uiState.passwordError == null,
            errorMessage = uiState.passwordError
        ) {
            PocketPasswordField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                placeholder = "Al menos 8 caracteres",
                error = uiState.passwordError
            )
        }

        FormField(
            key = "confirmPassword",
            isValid = uiState.confirmPasswordError == null,
            errorMessage = uiState.confirmPasswordError
        ) {
            PocketPasswordField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar contraseña",
                placeholder = "Repite tu contraseña",
                error = uiState.confirmPasswordError
            )
        }

        SimplePocketSwitch(
            label = "Acepto términos y privacidad",
            description = "Requerido para crear tu cuenta",
            checked = uiState.termsAccepted,
            onCheckedChange = viewModel::onAcceptTermsChange
        )

        SimplePocketSwitch(
            label = "Quiero usar una clave BYOK",
            description = "Habilita integraciones avanzadas",
            checked = uiState.useByok,
            onCheckedChange = { viewModel.toggleByokUsage() }
        )

        if (uiState.useByok) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
            ) {
                FormField(
                    key = "developerKey",
                    isValid = uiState.developerKeyError == null,
                    errorMessage = uiState.developerKeyError
                ) {
                    PocketTextField(
                        value = uiState.developerKey,
                        onValueChange = viewModel::onDeveloperKeyChange,
                        label = "Clave BYOK",
                        placeholder = "Introduce o genera tu clave",
                        readOnly = uiState.byokGenerated,
                        isError = !uiState.developerKeyError.isNullOrBlank(),
                        errorMessage = uiState.developerKeyError,
                        helperText = if (uiState.byokGenerated) "Generada automáticamente" else null
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
                ) {
                    PocketButton(
                        text = if (uiState.byokGenerated) "Regenerar clave" else "Generar clave BYOK",
                        onClick = viewModel::generateDeveloperKey,
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.Outline
                    )
                    if (uiState.byokGenerated) {
                        PocketButton(
                            text = "Editar manualmente",
                            onClick = viewModel::clearGeneratedDeveloperKey,
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.Text
                        )
                    }
                }

                uiState.lastGeneratedKey?.takeIf { uiState.byokGenerated }?.let { key ->
                    PocketCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = ComponentTokens.CardVariant.Outlined
                    ) {
                        Column(
                            modifier = Modifier.padding(SpacingTokens.Semantic.contentSpacingNormal),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
                        ) {
                            Text(
                                text = "Tu nueva clave",
                                style = TypographyTokens.Body.large,
                                color = ColorTokens.onSurface
                            )
                            Text(
                                text = key,
                                style = TypographyTokens.Code.medium,
                                color = ColorTokens.Primary.primary500
                            )
                        }
                    }
                }
            }
        }

        uiState.globalError?.let { errorMessage ->
            ValidationDisplay(
                validations = listOf(
                    validation {
                        message(errorMessage)
                        type(ValidationSeverity.ERROR)
                        field("signup")
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SignUpActions(
    uiState: SignUpUiState,
    onSubmit: () -> Unit,
    onAlreadyHaveAccount: () -> Unit,
    onForgotPassword: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)) {
        PocketButton(
            text = "Crear cuenta",
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            loading = uiState.isLoading,
            enabled = uiState.canSubmit && !uiState.isLoading,
            size = ButtonSize.Large
        )

        PocketButton(
            text = "¿Olvidaste tu contraseña?",
            onClick = onForgotPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSendingReset,
            loading = uiState.isSendingReset,
            variant = ButtonVariant.Text
        )

        PocketButton(
            text = "Ya tengo una cuenta",
            onClick = onAlreadyHaveAccount,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.Text,
            enabled = !uiState.isLoading
        )
    }
}

@Composable
private fun IconWrapper(icon: ImageVector, contentDescription: String) {
    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = ColorTokens.Primary.primary600
    )
}

@Composable
private fun PocketBenefitItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingTight)
    ) {
        androidx.compose.material3.Icon(
            imageVector = PocketIcons.CheckCircle,
            contentDescription = null,
            tint = ColorTokens.Primary.primary500
        )
        Text(
            text = text,
            style = TypographyTokens.Body.medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    PocketTheme {
        SignUpScreen()
    }
}
