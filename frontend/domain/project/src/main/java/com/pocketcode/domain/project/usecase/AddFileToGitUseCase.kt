package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class AddFileToGitUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project, filePattern: String): Result<Unit> {
        return projectRepository.addFileToGit(project, filePattern)
    }
}
