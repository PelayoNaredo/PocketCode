package com.pocketcode.features.marketplace.ui.upload

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.marketplace.usecase.UploadAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class AssetUploadViewModel @Inject constructor(
    private val app: Application,
    private val uploadAssetUseCase: UploadAssetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetUploadState())
    val uiState: StateFlow<AssetUploadState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onFileSelected(uri: Uri) {
        _uiState.update { it.copy(selectedFileUri = uri) }
    }

    fun uploadAsset() {
        val currentState = _uiState.value
        val fileUri = currentState.selectedFileUri ?: return

        // In a real app, authorId would come from the logged-in user's profile
        val authorId = "temp-user-id"

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }

            // Create a temporary file from the Uri
            val tempFile = File(app.cacheDir, "upload_temp_file")
            app.contentResolver.openInputStream(fileUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            uploadAssetUseCase(
                name = currentState.name,
                description = currentState.description,
                authorId = authorId,
                file = tempFile
            ).onSuccess {
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isUploading = false, error = error.message) }
            }

            tempFile.delete() // Clean up the temp file
        }
    }
}
