package com.pocketcode.domain.project.usecase

import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.repository.ProjectRepository
import javax.inject.Inject

class CommitChangesUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project, message: String): Result<Unit> {
        return projectRepository.commitChanges(project, message)
    }
}
