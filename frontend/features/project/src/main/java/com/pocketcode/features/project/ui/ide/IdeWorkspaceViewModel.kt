package com.pocketcode.features.project.ui.ide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.usecase.GetFileContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IdeWorkspaceViewModel @Inject constructor(
    private val getFileContentUseCase: GetFileContentUseCase
) : ViewModel() {

    private val _openFiles = MutableStateFlow<List<OpenFile>>(emptyList())
    val openFiles: StateFlow<List<OpenFile>> = _openFiles.asStateFlow()

    private val _currentFile = MutableStateFlow<OpenFile?>(null)
    val currentFile: StateFlow<OpenFile?> = _currentFile.asStateFlow()

    fun openFile(file: ProjectFile) {
        // Check if file is already open
        val existingFile = _openFiles.value.find { it.file.path == file.path }
        if (existingFile != null) {
            _currentFile.value = existingFile
            return
        }

        // If it's a directory, don't open it
        if (file.isDirectory) return

        // Create new open file and load content
        val openFile = OpenFile(file = file, content = "Loading...")
        _openFiles.value = _openFiles.value + openFile
        _currentFile.value = openFile

        // Load file content
        viewModelScope.launch {
            // Note: Project context integration is handled by the parent navigation layer
            // getFileContentUseCase(project, file.path).onSuccess { content ->
            //     val updatedFile = openFile.copy(content = content)
            //     _openFiles.value = _openFiles.value.map { 
            //         if (it.file.path == file.path) updatedFile else it 
            //     }
            //     if (_currentFile.value?.file?.path == file.path) {
            //         _currentFile.value = updatedFile
            //     }
            // }
        }
    }

    fun closeFile(file: ProjectFile) {
        _openFiles.value = _openFiles.value.filter { it.file.path != file.path }
        if (_currentFile.value?.file?.path == file.path) {
            _currentFile.value = _openFiles.value.firstOrNull()
        }
    }

    fun switchToFile(openFile: OpenFile) {
        _currentFile.value = openFile
    }
}

data class OpenFile(
    val file: ProjectFile,
    val content: String,
    val isModified: Boolean = false
)
