package com.pocketcode.domain.auth.usecase

import com.pocketcode.domain.auth.model.DeveloperKey
import com.pocketcode.domain.auth.repository.AuthRepository
import javax.inject.Inject

class GenerateDeveloperKeyUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Result<DeveloperKey> {
        return repository.generateDeveloperKey(userId)
    }
}
