package com.pocketcode.domain.project.model

/**
 * This is a Domain Model. It represents a core entity within the application's business logic.
 *
 * Responsibilities:
 * - Define the structure and behavior of a business object (e.g., a Project).
 * - It is a plain Kotlin data class, free from any framework or library-specific annotations
 *   (unlike DTOs or database entities).
 * - It represents the "truth" for the application's domain.
 *
 * This model is used by Use Cases and exposed by Repository interfaces within the domain layer.
 * The `:data` layer is responsible for mapping its data-specific models (DTOs, entities)
 * to this domain model before passing the data to the domain layer.
 *
 * Interacts with:
 * - Use Cases and Repository interfaces in the `:domain` layer.
 * - ViewModels in the `:features` layer will consume this model to display its data.
 */
data class Project(
    val id: String,
    val name: String,
    val localPath: String
)
