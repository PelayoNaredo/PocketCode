package com.pocketcode.domain.auth.model

/**
 * Excepción ligera que encapsula un [AuthFailure] para propagarlo a la capa de presentación
 * mediante `Result.failure` sin perder el contexto del error.
 */
class AuthFailureException(val failure: AuthFailure) : Exception(failure.message)
