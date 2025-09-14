package com.pocketcode.features.project.ui.explorer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.domain.project.model.ProjectFile

@Composable
fun FileExplorer(
    viewModel: FileExplorerViewModel = hiltViewModel(),
    onFileClick: (ProjectFile) -> Unit
) {
    val treeState by viewModel.treeState.collectAsState()
    val expandedFolders by viewModel.expandedFolders.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            FileNodeRow(
                name = "root",
                icon = Icons.Default.Folder,
                isExpanded = expandedFolders.contains(""),
                isDirectory = true,
                level = 0,
                onClick = { viewModel.toggleFolder("") }
            )
        }
        if (expandedFolders.contains("")) {
            treeState[""]?.let { rootNodes ->
                items(rootNodes.size) { index ->
                    val node = rootNodes[index]
                    FileTree(
                        node = node,
                        level = 1,
                        treeState = treeState,
                        expandedFolders = expandedFolders,
                        viewModel = viewModel,
                        onFileClick = onFileClick
                    )
                }
            }
        }
    }
}

@Composable
fun FileTree(
    node: FileNode,
    level: Int,
    treeState: Map<String, List<FileNode>>,
    expandedFolders: Set<String>,
    viewModel: FileExplorerViewModel,
    onFileClick: (ProjectFile) -> Unit
) {
    val isExpanded = expandedFolders.contains(node.file.path)
    FileNodeRow(
        name = node.file.name,
        icon = if (node.file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
        isExpanded = isExpanded,
        isDirectory = node.file.isDirectory,
        level = level,
        onClick = {
            if (node.file.isDirectory) {
                viewModel.toggleFolder(node.file.path)
            } else {
                onFileClick(node.file)
            }
        }
    )

    if (node.file.isDirectory && isExpanded) {
        treeState[node.file.path]?.forEach { childNode ->
            FileTree(
                node = childNode,
                level = level + 1,
                treeState = treeState,
                expandedFolders = expandedFolders,
                viewModel = viewModel,
                onFileClick = onFileClick
            )
        }
    }
}

@Composable
fun FileNodeRow(
    name: String,
    icon: ImageVector,
    isExpanded: Boolean,
    isDirectory: Boolean,
    level: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (level * 16).dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDirectory) {
            val arrowIcon = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight
            Icon(imageVector = arrowIcon, contentDescription = null, modifier = Modifier.size(24.dp))
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }

        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp).padding(start = 4.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}
