package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class SaveFileContentUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project, filePath: String, content: String): Result<Unit> {
        return projectRepository.saveFileContent(project, filePath, content)
    }
}
