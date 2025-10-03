package com.pocketcode.features.project.ui.explorer

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.usecase.ListFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    private val listFilesUseCase: ListFilesUseCase,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedProject = MutableStateFlow<com.pocketcode.features.project.ui.selection.Project?>(null)
    val selectedProject: StateFlow<com.pocketcode.features.project.ui.selection.Project?> = _selectedProject.asStateFlow()

    // Convert to domain Project when needed
    private val project: Project
        get() = _selectedProject.value?.let { selected ->
            Project(
                id = selected.id,
                name = selected.name,
                localPath = selected.localPath
            )
        } ?: Project(
            id = "default",
            name = "Default Project",
            localPath = File(context.filesDir, "projects/default").absolutePath
        )

    private val _uiState = MutableStateFlow<FileExplorerUiState>(FileExplorerUiState.Loading)
    val uiState: StateFlow<FileExplorerUiState> = _uiState.asStateFlow()

    private val _treeState = MutableStateFlow<Map<String, List<FileNode>>>(emptyMap())
    val treeState: StateFlow<Map<String, List<FileNode>>> = _treeState.asStateFlow()

    private val _expandedFolders = MutableStateFlow<Set<String>>(setOf(""))
    val expandedFolders: StateFlow<Set<String>> = _expandedFolders.asStateFlow()

    private val _createResult = MutableStateFlow<CreateResult?>(null)
    val createResult: StateFlow<CreateResult?> = _createResult.asStateFlow()

    init {
        // Load default project files
        loadFilesForPath("")
    }

    fun setSelectedProject(project: com.pocketcode.features.project.ui.selection.Project) {
        viewModelScope.launch {
            _selectedProject.value = project
            // Clear existing state and reload for new project
            _treeState.value = emptyMap()
            _expandedFolders.value = setOf("")
            loadFilesForPath("")
        }
    }

    fun toggleFolder(path: String) {
        val currentExpanded = _expandedFolders.value
        _expandedFolders.value = if (currentExpanded.contains(path)) {
            currentExpanded - path
        } else {
            currentExpanded + path
        }

        if (_expandedFolders.value.contains(path) && _treeState.value[path] == null) {
            loadFilesForPath(path)
        }
    }

    fun createFile(name: String, parentPath: String = "") {
        viewModelScope.launch {
            try {
                val fullPath = if (parentPath.isEmpty()) {
                    "${project.localPath}/$name"
                } else {
                    "${project.localPath}/$parentPath/$name"
                }
                
                val file = File(fullPath)
                
                // Ensure parent directory exists
                file.parentFile?.mkdirs()
                
                // Create the file
                if (file.createNewFile()) {
                    _createResult.value = CreateResult.Success("File created successfully")
                    // Reload the folder to show the new file
                    loadFilesForPath(parentPath)
                } else {
                    _createResult.value = CreateResult.Error("File already exists")
                }
            } catch (e: Exception) {
                _createResult.value = CreateResult.Error("Failed to create file: ${e.message}")
            }
        }
    }

    fun createFolder(name: String, parentPath: String = "") {
        viewModelScope.launch {
            try {
                val fullPath = if (parentPath.isEmpty()) {
                    "${project.localPath}/$name"
                } else {
                    "${project.localPath}/$parentPath/$name"
                }
                
                val folder = File(fullPath)
                
                if (folder.mkdirs() || folder.exists()) {
                    _createResult.value = CreateResult.Success("Folder created successfully")
                    // Reload the parent folder to show the new folder
                    loadFilesForPath(parentPath)
                } else {
                    _createResult.value = CreateResult.Error("Failed to create folder")
                }
            } catch (e: Exception) {
                _createResult.value = CreateResult.Error("Failed to create folder: ${e.message}")
            }
        }
    }

    fun clearCreateResult() {
        _createResult.value = null
    }

    private fun loadFilesForPath(path: String) {
        viewModelScope.launch {
            listFilesUseCase(project, path).onSuccess { files ->
                val nodes = files.map { FileNode(it) }
                _treeState.value = _treeState.value + (path to nodes)
                if (path == "") {
                    _uiState.value = FileExplorerUiState.Success
                }
            }.onFailure {
                _uiState.value = FileExplorerUiState.Error(it.message ?: "Unknown error")
            }
        }
    }
}

data class FileNode(
    val file: ProjectFile,
    val isExpanded: Boolean = false
)

sealed interface FileExplorerUiState {
    object Loading : FileExplorerUiState
    object Success : FileExplorerUiState
    data class Error(val message: String) : FileExplorerUiState
}

sealed interface CreateResult {
    data class Success(val message: String) : CreateResult
    data class Error(val message: String) : CreateResult
}
