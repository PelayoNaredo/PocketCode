package com.pocketcode.core.api.dto

import kotlinx.serialization.Serializable

/**
 * This is a Data Transfer Object (DTO) for a Project.
 * DTOs are part of the `:core:api` module and define the data structures for
 * communication between the data layer and the domain layer, and often match
 * the structure of network API responses.
 *
 * Using a library like `kotlinx.serialization` allows for easy parsing from JSON.
 *
 * This file (and others like it in this module) is a contract. It is depended upon by:
 * - `:data` layer: To parse network responses or database entities.
 * - `:domain` layer: To understand the shape of data it will receive from repositories.
 */
@Serializable
data class ProjectDTO(
    val id: String,
    val name: String,
    val description: String,
    val sourceUrl: String
)
