package com.pocketcode.features.project.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.usecase.CreateProjectUseCase
import com.pocketcode.domain.project.usecase.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    private fun loadProjects() {
        getProjectsUseCase().onEach { projects ->
            _uiState.value = DashboardUiState.Success(projects)
        }.launchIn(viewModelScope)
    }

    fun createProject(name: String) {
        viewModelScope.launch {
            createProjectUseCase(name).onSuccess {
                // The flow should automatically update the list
            }.onFailure {
                // Handle error state
            }
        }
    }
}

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val projects: List<Project>) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
