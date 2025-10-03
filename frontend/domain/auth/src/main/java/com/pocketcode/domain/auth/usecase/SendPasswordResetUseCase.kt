package com.pocketcode.domain.auth.usecase

import com.pocketcode.domain.auth.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.sendPasswordReset(email)
    }
}
