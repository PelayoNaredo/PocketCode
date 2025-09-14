package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class ListFilesUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project, path: String): Result<List<ProjectFile>> {
        return projectRepository.listFiles(project, path)
    }
}
