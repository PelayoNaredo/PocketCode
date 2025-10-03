package com.pocketcode.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.auth.model.AuthFailure
import com.pocketcode.domain.auth.model.AuthUser
import com.pocketcode.domain.auth.model.SignUpRequest
import com.pocketcode.domain.auth.usecase.CreateLocalDeveloperKeyUseCase
import com.pocketcode.domain.auth.usecase.SendPasswordResetUseCase
import com.pocketcode.domain.auth.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val MIN_DEVELOPER_KEY_LENGTH = 24

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val createLocalDeveloperKeyUseCase: CreateLocalDeveloperKeyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = Channel<SignUpEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onFullNameChange(value: String) {
        updateState { it.copy(fullName = value) }
    }

    fun onEmailChange(value: String) {
        updateState { it.copy(email = value.trim()) }
    }

    fun onPasswordChange(value: String) {
        updateState { it.copy(password = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        updateState { it.copy(confirmPassword = value) }
    }

    fun onDeveloperKeyChange(value: String) {
        updateState {
            if (!it.useByok || it.byokGenerated) {
                it
            } else {
                it.copy(developerKey = value)
            }
        }
    }

    fun onAcceptTermsChange(accepted: Boolean) {
        updateState { it.copy(termsAccepted = accepted) }
    }

    fun toggleByokUsage() {
        updateState {
            if (it.useByok) {
                it.copy(useByok = false, developerKey = "", byokGenerated = false, developerKeyError = null)
            } else {
                it.copy(useByok = true)
            }
        }
    }

    fun generateDeveloperKey() {
        updateState {
            if (!it.useByok) return@updateState it
            val generated = createLocalDeveloperKeyUseCase()
            it.copy(
                developerKey = generated.value,
                byokGenerated = true,
                developerKeyError = null,
                lastGeneratedKey = generated.value
            )
        }
    }

    fun clearGeneratedDeveloperKey() {
        updateState {
            if (!it.useByok) return@updateState it
            it.copy(byokGenerated = false, developerKey = "", developerKeyError = null)
        }
    }

    fun submit() {
        val currentState = validateCurrentState()
        if (!currentState.canSubmit || currentState.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.emit(currentState.copy(isLoading = true, globalError = null))

            val request = SignUpRequest(
                fullName = currentState.fullName.trim(),
                email = currentState.email.trim(),
                password = currentState.password,
                byokKey = currentState.developerKey.takeIf { currentState.useByok && it.isNotBlank() },
                acceptTerms = currentState.termsAccepted
            )

            val result = signUpUseCase(request)
            result.onSuccess { user ->
                _uiState.emit(currentState.copy(isLoading = false, password = "", confirmPassword = ""))
                _events.send(SignUpEvent.Success(user, currentState.useByok.thenValue(currentState.developerKey)))
            }.onFailure { throwable ->
                val failure = signUpUseCase.mapError(throwable)
                _uiState.emit(currentState.copy(isLoading = false, globalError = failure.toMessage()))
            }
        }
    }

    fun sendPasswordReset() {
        val current = _uiState.value
        val email = current.email.trim()
        if (!emailRegex.matches(email)) {
            updateState { it.copy(globalError = "Introduce un correo válido antes de solicitar recuperación") }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isSendingReset = true, globalError = null) }
            sendPasswordResetUseCase(email)
                .onSuccess {
                    updateState { it.copy(isSendingReset = false, lastPasswordResetEmail = email) }
                    _events.send(SignUpEvent.PasswordResetSent(email))
                }
                .onFailure { throwable ->
                    val failure = signUpUseCase.mapError(throwable)
                    updateState { it.copy(isSendingReset = false, globalError = failure.toMessage()) }
                }
        }
    }

    private fun validateCurrentState(): SignUpUiState {
        val current = _uiState.value
        val nameError = when {
            current.fullName.isBlank() -> "El nombre es obligatorio"
            current.fullName.trim().length < 3 -> "Introduce al menos 3 caracteres"
            else -> null
        }
        val emailError = when {
            current.email.isBlank() -> "El correo es obligatorio"
            !emailRegex.matches(current.email.trim()) -> "Introduce un correo válido"
            else -> null
        }
        val passwordError = when {
            current.password.isBlank() -> "La contraseña es obligatoria"
            !passwordRegex.matches(current.password) -> "Debe incluir 8+ caracteres, mayúsculas, minúsculas y números"
            else -> null
        }
        val confirmPasswordError = when {
            current.confirmPassword.isBlank() -> "Confirma tu contraseña"
            current.confirmPassword != current.password -> "Las contraseñas no coinciden"
            else -> null
        }
        val developerKeyError = when {
            !current.useByok -> null
            current.developerKey.isBlank() -> "Introduce o genera una clave BYOK"
            current.developerKey.length < MIN_DEVELOPER_KEY_LENGTH -> "La clave debe tener al menos $MIN_DEVELOPER_KEY_LENGTH caracteres"
            else -> null
        }

        val canSubmit = listOf(nameError, emailError, passwordError, confirmPasswordError, developerKeyError)
            .all { it == null } && current.termsAccepted

        val validated = current.copy(
            fullNameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            developerKeyError = developerKeyError,
            canSubmit = canSubmit
        )
        _uiState.value = validated
        return validated
    }

    private fun updateState(transform: (SignUpUiState) -> SignUpUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun Boolean.thenValue(value: String): String? = if (this) value else null

    private fun AuthFailure.toMessage(): String = when (this) {
        is AuthFailure.EmailAlreadyInUse -> "El correo ya está registrado"
        is AuthFailure.InvalidCredentials -> "Credenciales inválidas"
        is AuthFailure.Network -> "Error de red. Inténtalo de nuevo"
        is AuthFailure.WeakPassword -> this.message ?: "La contraseña es demasiado débil"
        is AuthFailure.Unknown -> this.message ?: "Ha ocurrido un error inesperado"
    }

    companion object {
        private val emailRegex = Regex("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        private val passwordRegex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}")
    }
}

data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val developerKey: String = "",
    val byokGenerated: Boolean = false,
    val useByok: Boolean = false,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val isSendingReset: Boolean = false,
    val canSubmit: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val developerKeyError: String? = null,
    val globalError: String? = null,
    val lastPasswordResetEmail: String? = null,
    val lastGeneratedKey: String? = null
)

sealed interface SignUpEvent {
    data class Success(val user: AuthUser, val byokKey: String?) : SignUpEvent
    data class PasswordResetSent(val email: String) : SignUpEvent
}
