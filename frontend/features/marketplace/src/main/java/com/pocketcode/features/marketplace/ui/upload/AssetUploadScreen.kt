package com.pocketcode.features.marketplace.ui.upload

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AssetUploadScreen(
    viewModel: AssetUploadViewModel = hiltViewModel(),
    onUploadSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onFileSelected(uri)
        }
    }

    if (uiState.uploadSuccess) {
        // Navigate back or show a success dialog
        LaunchedEffect(Unit) {
            onUploadSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Upload New Asset", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Asset Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 5
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { filePickerLauncher.launch("*/*") }) {
                Text("Select File")
            }
            Spacer(Modifier.width(16.dp))
            Text(uiState.selectedFileUri?.path ?: "No file selected")
        }

        if (uiState.isUploading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = viewModel::uploadAsset,
                enabled = uiState.name.isNotBlank() && uiState.description.isNotBlank() && uiState.selectedFileUri != null
            ) {
                Text("Upload")
            }
        }

        uiState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
