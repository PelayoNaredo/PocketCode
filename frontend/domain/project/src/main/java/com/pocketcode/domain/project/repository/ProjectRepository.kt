package com.pocketcode.domain.project.repository

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectImportRequest
import com.pocketcode.domain.project.model.ProjectFile
import kotlinx.coroutines.flow.Flow

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

    fun getProjects(): Flow<List<Project>>

    suspend fun getProject(id: String): Project?

    suspend fun createProject(name: String): Result<Project>

    suspend fun importProject(request: ProjectImportRequest): Result<Project>

    suspend fun deleteProject(id: String): Result<Unit>

    suspend fun getFileContent(project: Project, filePath: String): Result<String>

    suspend fun saveFileContent(project: Project, filePath: String, content: String): Result<Unit>

    suspend fun listFiles(project: Project, path: String): Result<List<ProjectFile>>

    // Git Operations
    suspend fun initGitRepo(project: Project): Result<Unit>
    suspend fun addFileToGit(project: Project, filePattern: String): Result<Unit>
    suspend fun commitChanges(project: Project, message: String): Result<Unit>
}
