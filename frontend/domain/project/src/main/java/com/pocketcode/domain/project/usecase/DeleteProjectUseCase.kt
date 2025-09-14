package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class DeleteProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return projectRepository.deleteProject(id)
    }
}
