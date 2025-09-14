package com.pocketcode.domain.project.repository

import com.pocketcode.domain.project.model.Project

/**
 * This is the Repository Interface for project-related data operations.
 * It defines the contract that the `:data:project` module must implement.
 *
 * Responsibilities:
 * - Define functions for accessing and manipulating project data, such as `getProjects`,
 *   `createProject`, `saveFile`, etc.
 * - All functions must use domain models (e.g., `Project`) in their signatures.
 *
 * Interacts with:
 * - Use Cases within the `:domain` layer, which will depend on this interface.
 * - The `:data:project` module, which provides the concrete implementation.
 */
interface ProjectRepository {
    // suspend fun getProjects(): List<Project>
    // suspend fun createProject(name: String): Result<Project>
}
