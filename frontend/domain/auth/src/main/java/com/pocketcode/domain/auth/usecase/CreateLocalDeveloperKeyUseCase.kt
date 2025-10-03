package com.pocketcode.domain.auth.usecase

import com.pocketcode.domain.auth.model.DeveloperKey
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import javax.inject.Inject

class CreateLocalDeveloperKeyUseCase @Inject constructor() {

    private val random = SecureRandom()

    operator fun invoke(): DeveloperKey {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return DeveloperKey(value = value, generatedAt = Instant.now())
    }
}
