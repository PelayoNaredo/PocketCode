package com.pocketcode.domain.auth.repository

import com.pocketcode.domain.auth.model.AuthFailure
import com.pocketcode.domain.auth.model.AuthUser
import com.pocketcode.domain.auth.model.DeveloperKey
import com.pocketcode.domain.auth.model.SignUpRequest

/**
 * Contrato de la capa de dominio para todas las operaciones de autenticación.
 */
interface AuthRepository {
    suspend fun signUp(request: SignUpRequest): Result<AuthUser>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun upsertDeveloperKey(userId: String, developerKey: String): Result<DeveloperKey>

    suspend fun generateDeveloperKey(userId: String): Result<DeveloperKey>

    fun mapException(throwable: Throwable): AuthFailure
}
