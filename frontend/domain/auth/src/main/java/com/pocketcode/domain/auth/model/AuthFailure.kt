package com.pocketcode.domain.auth.model

/**
 * Errores de autenticación más frecuentes que la capa de UI puede representar.
 */
sealed class AuthFailure(open val message: String? = null) {
    data class InvalidCredentials(override val message: String? = null) : AuthFailure(message)
    data class EmailAlreadyInUse(override val message: String? = null) : AuthFailure(message)
    data class WeakPassword(override val message: String? = null) : AuthFailure(message)
    data class Network(override val message: String? = null) : AuthFailure(message)
    data class Unknown(override val message: String? = null) : AuthFailure(message)
}
