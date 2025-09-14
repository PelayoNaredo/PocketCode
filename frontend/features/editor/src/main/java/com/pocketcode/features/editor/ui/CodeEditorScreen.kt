package com.pocketcode.features.editor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.jetpackcamera.core.ui.theme.PocketCodeTheme
import io.github.vince_styl.codeview.CodeView
import io.github.vince_styl.codeview.CodeViewTheme

@Composable
fun CodeEditorScreen(
    viewModel: CodeEditorViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()

    PocketCodeTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Code Editor") },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.saveFile() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save File")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    is CodeEditorUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is CodeEditorUiState.Success -> {
                        CodeView(
                            code = fileContent,
                            theme = CodeViewTheme.DEFAULT.copy(
                                backgroundColor = MaterialTheme.colorScheme.background,
                                numberColor = MaterialTheme.colorScheme.secondary
                            ),
                            onCodeChange = {
                                viewModel.onContentChange(it)
                            }
                        )
                    }
                    is CodeEditorUiState.Error -> {
                        Text(
                            text = (uiState as CodeEditorUiState.Error).message,
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
