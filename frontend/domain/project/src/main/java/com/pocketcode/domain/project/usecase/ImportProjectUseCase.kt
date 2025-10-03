package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectImportRequest
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class ImportProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(request: ProjectImportRequest): Result<Project> {
        return projectRepository.importProject(request)
    }
}
