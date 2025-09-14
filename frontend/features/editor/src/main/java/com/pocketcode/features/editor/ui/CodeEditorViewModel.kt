package com.pocketcode.features.editor.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.usecase.GetFileContentUseCase
import com.pocketcode.domain.project.usecase.SaveFileContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CodeEditorViewModel @Inject constructor(
    private val getFileContentUseCase: GetFileContentUseCase,
    private val saveFileContentUseCase: SaveFileContentUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // In a real app, these would be passed via navigation
    private val project: Project = savedStateHandle.get<Project>("project")!!
    private val filePath: String = savedStateHandle.get<String>("filePath")!!

    private val _uiState = MutableStateFlow<CodeEditorUiState>(CodeEditorUiState.Loading)
    val uiState: StateFlow<CodeEditorUiState> = _uiState.asStateFlow()

    private val _fileContent = MutableStateFlow("")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    init {
        loadFile()
    }

    private fun loadFile() {
        viewModelScope.launch {
            _uiState.value = CodeEditorUiState.Loading
            getFileContentUseCase(project, filePath)
                .onSuccess { content ->
                    _fileContent.value = content
                    _uiState.value = CodeEditorUiState.Success
                }
                .onFailure {
                    _uiState.value = CodeEditorUiState.Error(it.message ?: "Failed to load file")
                }
        }
    }

    fun onContentChange(newContent: String) {
        _fileContent.value = newContent
    }

    fun saveFile() {
        viewModelScope.launch {
            saveFileContentUseCase(project, filePath, _fileContent.value)
                .onSuccess {
                    // Optionally show a success message
                }
                .onFailure {
                    // Optionally show an error message
                }
        }
    }
}

sealed interface CodeEditorUiState {
    object Loading : CodeEditorUiState
    object Success : CodeEditorUiState
    data class Error(val message: String) : CodeEditorUiState
}
