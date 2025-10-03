package com.pocketcode.domain.auth.usecase

import com.pocketcode.domain.auth.model.AuthFailure
import com.pocketcode.domain.auth.model.AuthUser
import com.pocketcode.domain.auth.model.AuthFailureException
import com.pocketcode.domain.auth.model.SignUpRequest
import com.pocketcode.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: SignUpRequest): Result<AuthUser> {
        if (!request.acceptTerms) {
            return Result.failure(IllegalStateException("terms_not_accepted"))
        }
        return repository.signUp(request)
    }

    fun mapError(error: Throwable): AuthFailure = when (error) {
        is AuthFailureException -> error.failure
        else -> repository.mapException(error)
    }
}
