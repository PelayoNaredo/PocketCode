package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(name: String, @Suppress("UNUSED_PARAMETER") description: String = ""): Result<Project> {
        return projectRepository.createProject(name)
    }
}
