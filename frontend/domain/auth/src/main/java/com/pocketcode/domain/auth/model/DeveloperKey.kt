package com.pocketcode.domain.auth.model

import java.time.Instant

/**
 * Representa una clave BYOK (Bring Your Own Key) que permite habilitar
 * integraciones avanzadas en la plataforma.
 */
data class DeveloperKey(
    val value: String,
    val generatedAt: Instant = Instant.now()
)
