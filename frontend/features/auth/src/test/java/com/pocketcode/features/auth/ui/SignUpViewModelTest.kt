package com.pocketcode.features.auth.ui

import com.pocketcode.domain.auth.model.AuthFailure
import com.pocketcode.domain.auth.model.AuthUser
import com.pocketcode.domain.auth.model.DeveloperKey
import com.pocketcode.domain.auth.model.SignUpRequest
import com.pocketcode.domain.auth.repository.AuthRepository
import com.pocketcode.domain.auth.usecase.CreateLocalDeveloperKeyUseCase
import com.pocketcode.domain.auth.usecase.SendPasswordResetUseCase
import com.pocketcode.domain.auth.usecase.SignUpUseCase
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is empty and disabled`() {
        val viewModel = buildViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.fullName.isEmpty())
        assertTrue(state.email.isEmpty())
        assertTrue(state.password.isEmpty())
        assertTrue(state.confirmPassword.isEmpty())
        assertFalse(state.canSubmit)
        assertFalse(state.termsAccepted)
        assertFalse(state.useByok)
    }

    @Test
    fun `submit success clears passwords and emits event`() = runTest(dispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val expectedUser = AuthUser(
            id = "user-123",
            email = "user@example.com",
            displayName = "User",
            createdAt = Instant.now()
        )
        repository.signUpResult = Result.success(expectedUser)

        val viewModel = buildViewModel(repository)
        viewModel.onFullNameChange("User Example")
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("Password1")
        viewModel.onConfirmPasswordChange("Password1")
        viewModel.onAcceptTermsChange(true)
        viewModel.toggleByokUsage()
        viewModel.generateDeveloperKey()

        val eventDeferred = async { viewModel.events.first() }

        viewModel.submit()
        advanceUntilIdle()

        val event = eventDeferred.await()
        assertTrue(event is SignUpEvent.Success)
        event as SignUpEvent.Success
        assertEquals(expectedUser, event.user)
        assertNotNull(event.byokKey)
        assertEquals(repository.capturedSignUpRequest?.byokKey, event.byokKey)

        val state = viewModel.uiState.value
        assertTrue(state.password.isEmpty())
        assertTrue(state.confirmPassword.isEmpty())
        assertFalse(state.isLoading)
        assertEquals(1, repository.signUpCalls)
    }

    @Test
    fun `submit without accepting terms does not call repository`() = runTest(dispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = buildViewModel(repository)

        viewModel.onFullNameChange("User Example")
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("Password1")
        viewModel.onConfirmPasswordChange("Password1")
        // terms not accepted

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(0, repository.signUpCalls)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `send password reset with invalid email updates global error`() {
        val viewModel = buildViewModel()

        viewModel.onEmailChange("invalid-email")
        viewModel.sendPasswordReset()

        val state = viewModel.uiState.value
        assertEquals(
            "Introduce un correo válido antes de solicitar recuperación",
            state.globalError
        )
    }

    @Test
    fun `send password reset success emits event`() = runTest(dispatcherRule.dispatcher) {
        val repository = FakeAuthRepository()
        repository.sendPasswordResetResult = Result.success(Unit)
        val viewModel = buildViewModel(repository)

        viewModel.onEmailChange("user@example.com")

        val eventDeferred = async { viewModel.events.first { it is SignUpEvent.PasswordResetSent } }

        viewModel.sendPasswordReset()
        advanceUntilIdle()

        val event = eventDeferred.await() as SignUpEvent.PasswordResetSent
        assertEquals("user@example.com", event.email)
        assertEquals("user@example.com", viewModel.uiState.value.lastPasswordResetEmail)
        assertFalse(viewModel.uiState.value.isSendingReset)
        assertEquals(1, repository.passwordResetCalls)
    }

    private fun buildViewModel(
        repository: FakeAuthRepository = FakeAuthRepository()
    ): SignUpViewModel {
        val signUpUseCase = SignUpUseCase(repository)
        val sendPasswordResetUseCase = SendPasswordResetUseCase(repository)
        val createLocalDeveloperKeyUseCase = CreateLocalDeveloperKeyUseCase()
        return SignUpViewModel(
            signUpUseCase = signUpUseCase,
            sendPasswordResetUseCase = sendPasswordResetUseCase,
            createLocalDeveloperKeyUseCase = createLocalDeveloperKeyUseCase
        )
    }

    private class FakeAuthRepository : AuthRepository {
        var signUpResult: Result<AuthUser> = Result.failure(IllegalStateException("not implemented"))
        var sendPasswordResetResult: Result<Unit> = Result.failure(IllegalStateException("not implemented"))

        var signUpCalls: Int = 0
            private set
        var passwordResetCalls: Int = 0
            private set

        var capturedSignUpRequest: SignUpRequest? = null
        var capturedPasswordResetEmail: String? = null

        override suspend fun signUp(request: SignUpRequest): Result<AuthUser> {
            signUpCalls += 1
            capturedSignUpRequest = request
            return signUpResult
        }

        override suspend fun sendPasswordReset(email: String): Result<Unit> {
            passwordResetCalls += 1
            capturedPasswordResetEmail = email
            return sendPasswordResetResult
        }

        override suspend fun upsertDeveloperKey(userId: String, developerKey: String): Result<DeveloperKey> {
            return Result.failure(UnsupportedOperationException())
        }

        override suspend fun generateDeveloperKey(userId: String): Result<DeveloperKey> {
            return Result.failure(UnsupportedOperationException())
        }

        override fun mapException(throwable: Throwable): AuthFailure {
            return AuthFailure.Unknown(throwable.message)
        }
    }
}
