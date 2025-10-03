package com.pocketcode.features.project.ui.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.project.usecase.GetProjectsUseCase
import com.pocketcode.domain.project.usecase.CreateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class ProjectSelectionUiState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createProjectResult: CreateProjectResult? = null
)

sealed class CreateProjectResult {
    data class Success(val project: Project) : CreateProjectResult()
    data class Error(val message: String) : CreateProjectResult()
}

@HiltViewModel
class ProjectSelectionViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProjectSelectionUiState())
    val uiState: StateFlow<ProjectSelectionUiState> = _uiState.asStateFlow()
    
    init {
        loadProjects()
    }
    
    private fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                getProjectsUseCase().collect { domainProjects ->
                    // Convert domain projects to UI projects
                    val uiProjects = domainProjects.map { domainProject ->
                        Project(
                            id = domainProject.id,
                            name = domainProject.name,
                            localPath = domainProject.localPath,
                            description = "Android project",
                            lastModified = "Recently",
                            icon = Icons.Default.Folder,
                            isRecent = true
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        projects = uiProjects,
                        isLoading = false
                    )
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun createProject(name: String, description: String = "New Android project") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            createProjectUseCase(name, description).fold(
                onSuccess = { domainProject: com.pocketcode.domain.project.model.Project ->
                    val newProject = Project(
                        id = domainProject.id,
                        name = domainProject.name,
                        localPath = domainProject.localPath,
                        description = description,
                        lastModified = "Just now",
                        icon = Icons.Default.FolderOpen,
                        isRecent = true
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createProjectResult = CreateProjectResult.Success(newProject)
                    )
                    
                    // Reload projects to include the new one
                    loadProjects()
                },
                onFailure = { error: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createProjectResult = CreateProjectResult.Error(
                            error.message ?: "Failed to create project"
                        )
                    )
                }
            )
        }
    }
    
    fun clearCreateResult() {
        _uiState.value = _uiState.value.copy(createProjectResult = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshProjects() {
        loadProjects()
    }
}