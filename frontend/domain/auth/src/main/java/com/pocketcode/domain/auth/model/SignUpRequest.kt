package com.pocketcode.domain.auth.model

/**
 * Datos requeridos para crear una nueva cuenta en PocketCode.
 */
data class SignUpRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val byokKey: String? = null,
    val acceptTerms: Boolean
)
