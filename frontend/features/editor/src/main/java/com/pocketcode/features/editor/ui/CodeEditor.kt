package com.pocketcode.features.editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.features.editor.ui.components.EditorContainer
import com.pocketcode.features.editor.ui.components.EditorContainerConfig

@Composable
fun CodeEditor(
    file: ProjectFile,
    selectedProjectId: String?,
    selectedProjectName: String?,
    selectedProjectPath: String?,
    modifier: Modifier = Modifier,
    config: EditorContainerConfig = EditorContainerConfig(),
    viewModel: CodeEditorViewModel = hiltViewModel(),
    onFileChanged: (ProjectFile) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onShowSettings: () -> Unit = {}
) {
    EditorContainer(
        file = file,
        selectedProjectId = selectedProjectId,
        selectedProjectName = selectedProjectName,
        selectedProjectPath = selectedProjectPath,
        modifier = modifier,
        config = config,
        viewModel = viewModel,
        onFileChanged = onFileChanged,
        onNavigateBack = onNavigateBack,
        onShowSettings = onShowSettings
    )
}
