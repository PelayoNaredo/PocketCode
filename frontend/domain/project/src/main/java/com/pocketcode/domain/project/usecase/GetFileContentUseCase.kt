package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class GetFileContentUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project, filePath: String): Result<String> {
        return projectRepository.getFileContent(project, filePath)
    }
}
