package com.pocketcode.features.project.ui.dashboard

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.core.ui.snackbar.GlobalSnackbarDuration
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectImportRequest
import com.pocketcode.domain.project.model.ProjectImportSource
import com.pocketcode.domain.project.usecase.CreateProjectUseCase
import com.pocketcode.domain.project.usecase.GetProjectsUseCase
import com.pocketcode.domain.project.usecase.ImportProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val projects: List<Project> = emptyList(),
    val errorMessage: String? = null,
    val isCreatingProject: Boolean = false,
    val isImportingProject: Boolean = false
)

sealed interface DashboardEvent {
    data class ShowSnackbar(val event: GlobalSnackbarEvent) : DashboardEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val importProjectUseCase: ImportProjectUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events = _events.asSharedFlow()

    private var projectsJob: Job? = null

    init {
        loadProjects()
    }

    private fun loadProjects() {
        projectsJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        projectsJob = getProjectsUseCase()
            .onEach { projects ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        projects = projects,
                        errorMessage = null
                    )
                }
            }
            .catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "No fue posible cargar los proyectos"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        loadProjects()
    }

    fun createProject(rawName: String) {
        val name = rawName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                emitSnackbar(
                    message = "Escribe un nombre para crear el proyecto",
                    severity = GlobalSnackbarSeverity.WARNING
                )
            }
            return
        }

        if (_uiState.value.isCreatingProject) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingProject = true) }

            createProjectUseCase(name)
                .onSuccess { project ->
                    emitSnackbar(
                        message = "Proyecto '${project.name}' creado",
                        severity = GlobalSnackbarSeverity.SUCCESS
                    )
                }
                .onFailure { error ->
                    emitSnackbar(
                        message = error.message ?: "No fue posible crear el proyecto",
                        severity = GlobalSnackbarSeverity.ERROR
                    )
                }

            _uiState.update { it.copy(isCreatingProject = false) }
        }
    }

    fun importProject(uri: Uri, suggestedName: String? = null) {
        if (_uiState.value.isImportingProject) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImportingProject = true) }

            val requestResult = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = readBytesFromUri(context.contentResolver, uri)
                    ProjectImportRequest(
                        source = ProjectImportSource.Archive(
                            bytes = bytes,
                            suggestedName = suggestedName
                        )
                    )
                }
            }

            val request = requestResult.getOrElse { error ->
                emitSnackbar(
                    message = error.message ?: "No pudimos leer el archivo seleccionado",
                    severity = GlobalSnackbarSeverity.ERROR
                )
                _uiState.update { it.copy(isImportingProject = false) }
                return@launch
            }

            importProjectUseCase(request)
                .onSuccess { project ->
                    emitSnackbar(
                        message = "Proyecto '${project.name}' importado",
                        severity = GlobalSnackbarSeverity.SUCCESS
                    )
                }
                .onFailure { error ->
                    emitSnackbar(
                        message = error.message ?: "No fue posible importar el proyecto",
                        severity = GlobalSnackbarSeverity.ERROR
                    )
                }

            _uiState.update { it.copy(isImportingProject = false) }
        }
    }

    private suspend fun emitSnackbar(
        message: String,
        severity: GlobalSnackbarSeverity,
        supportingText: String? = null,
        duration: GlobalSnackbarDuration = GlobalSnackbarDuration.SHORT
    ) {
        _events.emit(
            DashboardEvent.ShowSnackbar(
                GlobalSnackbarEvent(
                    message = message,
                    supportingText = supportingText,
                    duration = duration,
                    severity = severity,
                    origin = GlobalSnackbarOrigin.PROJECTS,
                    analyticsId = "dashboard_${severity.name.lowercase()}"
                )
            )
        )
    }

    private fun readBytesFromUri(contentResolver: ContentResolver, uri: Uri): ByteArray {
        return contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: throw IllegalArgumentException("El archivo seleccionado no es válido")
    }
}
