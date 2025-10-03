package com.pocketcode.domain.auth.model

import java.time.Instant

/**
 * Representa a un usuario autenticado tras completar el registro o el inicio de sesión.
 */
data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Instant = Instant.now()
)
