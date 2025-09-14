package com.pocketcode.features.project.ui.explorer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.usecase.ListFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    private val listFilesUseCase: ListFilesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // A real implementation would get the project from navigation args
    private val project: Project = savedStateHandle.get<Project>("project")!!

    private val _uiState = MutableStateFlow<FileExplorerUiState>(FileExplorerUiState.Loading)
    val uiState: StateFlow<FileExplorerUiState> = _uiState.asStateFlow()

    private val _treeState = MutableStateFlow<Map<String, List<FileNode>>>(emptyMap())
    val treeState: StateFlow<Map<String, List<FileNode>>> = _treeState.asStateFlow()

    private val _expandedFolders = MutableStateFlow<Set<String>>(setOf(""))
    val expandedFolders: StateFlow<Set<String>> = _expandedFolders.asStateFlow()

    init {
        loadFilesForPath("")
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
