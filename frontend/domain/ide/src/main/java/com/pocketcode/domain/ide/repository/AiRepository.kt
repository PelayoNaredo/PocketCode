package com.pocketcode.domain.ide.repository

/**
 * This is a Repository Interface defined in the domain layer.
 * It defines a contract for what data operations are possible for a specific
 * domain concept (in this case, AI services).
 *
 * Responsibilities:
 * - Define the functions that the data layer must implement (e.g., `generateCode`).
 * - Expose data using domain models, not DTOs or database entities. This decouples
 *   the domain layer from the implementation details of the data layer.
 *
 * This interface acts as a boundary.
 *
 * Interacts with:
 * - Use Cases (e.g., `GenerateCodeUseCase`) within the same module will depend on this
 *   interface to get data.
 * - The `:data:ide` module will provide the concrete implementation of this interface.
 */
interface AiRepository {
    // suspend fun generateCode(prompt: String): Result<String>
}
