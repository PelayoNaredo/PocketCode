package com.pocketcode.features.project.ui.explorer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.core.ui.components.feedback.EmptyState
import com.pocketcode.core.ui.components.feedback.PocketDialog
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.navigation.PocketTopBar
import com.pocketcode.core.ui.components.navigation.TopBarAction
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.LocalGlobalToastDispatcher
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorer(
    selectedProject: com.pocketcode.features.project.ui.selection.Project? = null,
    viewModel: FileExplorerViewModel = hiltViewModel(),
    onFileClick: (ProjectFile) -> Unit
) {
    val treeState by viewModel.treeState.collectAsState()
    val expandedFolders by viewModel.expandedFolders.collectAsState()
    val createResult by viewModel.createResult.collectAsState()
    val toastDispatcher = LocalGlobalToastDispatcher.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var createFileType by remember { mutableStateOf<String?>(null) }
    var currentParentPath by remember { mutableStateOf("") }

    // Update project when selection changes
    LaunchedEffect(selectedProject) {
        selectedProject?.let {
            viewModel.setSelectedProject(it)
        }
    }

    PocketScaffold(
        topBar = {
            PocketTopBar(
                title = "Files",
                subtitle = "Project Explorer",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.CreateNewFolder,
                        contentDescription = "Create folder",
                        onClick = {
                            createFileType = "folder"
                            showCreateDialog = true
                        }
                    ),
                    TopBarAction(
                        icon = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = "Create file",
                        onClick = {
                            createFileType = "file"
                            showCreateDialog = true
                        }
                    )
                )
            )
        },
        content = { paddingValues ->
            FileExplorerContent(
                treeState = treeState,
                expandedFolders = expandedFolders,
                viewModel = viewModel,
                onFileClick = onFileClick,
                paddingValues = paddingValues
            )
        }
    )

    // Create File/Folder Dialog
    if (showCreateDialog) {
        CreateItemDialog(
            itemType = createFileType ?: "file",
            onDismiss = { 
                showCreateDialog = false
                createFileType = null
            },
            onConfirm = { name ->
                when (createFileType) {
                    "file" -> viewModel.createFile(name, currentParentPath)
                    "folder" -> viewModel.createFolder(name, currentParentPath)
                }
                showCreateDialog = false
                createFileType = null
            }
        )
    }

    // Show results using ErrorDisplay component
    LaunchedEffect(createResult) {
        val result = createResult ?: return@LaunchedEffect
        when (result) {
            is CreateResult.Success -> toastDispatcher.showMessage(
                message = result.message,
                style = PocketToastStyle.Success,
                origin = GlobalSnackbarOrigin.PROJECTS
            )
            is CreateResult.Error -> toastDispatcher.showMessage(
                message = result.message,
                style = PocketToastStyle.Error,
                origin = GlobalSnackbarOrigin.PROJECTS
            )
        }
        viewModel.clearCreateResult()
    }
}

@Composable
private fun FileExplorerContent(
    treeState: Map<String, List<FileNode>>,
    expandedFolders: Set<String>,
    viewModel: FileExplorerViewModel,
    onFileClick: (ProjectFile) -> Unit,
    paddingValues: PaddingValues
) {
    if (treeState.isEmpty()) {
        EmptyState(
            title = "No Files Found",
            description = "This project doesn't contain any files yet. Create your first file or folder to get started.",
            icon = Icons.Default.FolderOpen,
            actionText = "Create File",
            onAction = { /* Will be handled by create dialog */ }
        )
        return
    }

    val flattenedTree = remember(treeState, expandedFolders) {
        buildFileTreeList(treeState, expandedFolders)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 8.dp)
    ) {
        items(
            items = flattenedTree,
            key = { node -> node.file.path.ifEmpty { "project_root" } }
        ) { fileNode ->
            when {
                fileNode.file.path.isEmpty() -> {
                    ModernFileNodeRow(
                        name = "Project Root",
                        icon = Icons.Default.Folder,
                        isExpanded = expandedFolders.contains(""),
                        isDirectory = true,
                        level = 0,
                        onClick = { viewModel.toggleFolder("") }
                    )
                }

                else -> {
                    ModernFileTree(
                        node = fileNode,
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

/**
 * Construye una lista plana del árbol de archivos para optimizar el lazy loading
 */
private fun buildFileTreeList(
    treeState: Map<String, List<FileNode>>,
    expandedFolders: Set<String>
): List<FileNodeWithLevel> {
    val result = mutableListOf<FileNodeWithLevel>()
    
    // Agregar root
    result.add(
        FileNodeWithLevel(
            file = ProjectFile(name = "Project Root", path = "", isDirectory = true),
            level = 0
        )
    )
    
    if (expandedFolders.contains("")) {
        treeState[""]?.let { rootNodes ->
            addNodesToList(result, rootNodes, 1, treeState, expandedFolders)
        }
    }
    
    return result
}

private fun addNodesToList(
    result: MutableList<FileNodeWithLevel>,
    nodes: List<FileNode>,
    level: Int,
    treeState: Map<String, List<FileNode>>,
    expandedFolders: Set<String>
) {
    nodes.forEach { node ->
        result.add(FileNodeWithLevel(node.file, level))
        
        if (node.file.isDirectory && expandedFolders.contains(node.file.path)) {
            treeState[node.file.path]?.let { childNodes ->
                addNodesToList(result, childNodes, level + 1, treeState, expandedFolders)
            }
        }
    }
}

data class FileNodeWithLevel(
    val file: ProjectFile,
    val level: Int
)
 
@Composable
private fun ModernFileTree(
    node: FileNodeWithLevel,
    treeState: Map<String, List<FileNode>>,
    expandedFolders: Set<String>,
    viewModel: FileExplorerViewModel,
    onFileClick: (ProjectFile) -> Unit
) {
    val isExpanded = expandedFolders.contains(node.file.path)
    val level = node.level
    
    Column {
        ModernFileNodeRow(
            name = node.file.name,
            icon = getFileIcon(node.file),
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
        
        AnimatedVisibility(
            visible = node.file.isDirectory && isExpanded,
            enter = expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
        ) {
            Column {
                treeState[node.file.path]?.forEach { childNode ->
                    ModernFileTree(
                        node = FileNodeWithLevel(childNode.file, level + 1),
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
fun ModernFileNodeRow(
    name: String,
    icon: ImageVector,
    isExpanded: Boolean,
    isDirectory: Boolean,
    level: Int,
    onClick: () -> Unit
) {
    PocketCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp)
            .clickable(onClick = onClick),
        variant = if (level == 0) CardVariant.Elevated else CardVariant.Outlined,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expand/Collapse arrow for directories
                if (isDirectory) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }
                
                // File/Folder icon
                Surface(
                    color = if (isDirectory) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDirectory) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // File/Folder name
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (level == 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // File extension badge for files
                if (!isDirectory) {
                    val extension = name.substringAfterLast(".", "").lowercase()
                    if (extension.isNotEmpty()) {
                        Surface(
                            color = getExtensionColor(extension),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = extension.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CreateItemDialog(
    itemType: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("") }
    
    val fileTemplates = mapOf(
        "Kotlin File" to ".kt",
        "Java File" to ".java",
        "XML File" to ".xml",
        "JSON File" to ".json",
        "Text File" to ".txt",
        "Markdown File" to ".md"
    )
    
    val capitalizedType = itemType.replaceFirstChar { it.uppercase() }
    PocketDialog(
        title = "Create $capitalizedType",
        onDismissRequest = onDismiss,
        confirmText = "Create",
        confirmEnabled = name.isNotBlank(),
        onConfirm = {
            val finalName = if (itemType == "file" && selectedTemplate.isNotEmpty() && !name.contains(".")) {
                name + selectedTemplate
            } else {
                name
            }
            if (finalName.isNotBlank()) {
                onConfirm(finalName)
            }
        },
        dismissText = "Cancel",
        onDismiss = onDismiss,
        icon = if (itemType == "folder") {
            Icons.Default.CreateNewFolder
        } else {
            Icons.AutoMirrored.Filled.NoteAdd
        },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Enter $itemType name:",
                    style = MaterialTheme.typography.bodyMedium
                )

                PocketTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = if (itemType == "file" && selectedTemplate.isNotEmpty()) {
                        {
                            Text(
                                text = selectedTemplate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    } else null
                )

                if (itemType == "file") {
                    Text(
                        text = "File Template:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        fileTemplates.keys.take(3).forEach { template ->
                            @OptIn(ExperimentalMaterial3Api::class)
                            FilterChip(
                                selected = selectedTemplate == fileTemplates[template],
                                onClick = {
                                    selectedTemplate = fileTemplates[template] ?: ""
                                    if (name.isEmpty()) {
                                        name = "New${template.replace(" File", "")}"
                                    }
                                },
                                label = { Text(template) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (fileTemplates.size > 3) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fileTemplates.keys.drop(3).forEach { template ->
                                @OptIn(ExperimentalMaterial3Api::class)
                                FilterChip(
                                    selected = selectedTemplate == fileTemplates[template],
                                    onClick = {
                                        selectedTemplate = fileTemplates[template] ?: ""
                                        if (name.isEmpty()) {
                                            name = "New${template.replace(" File", "")}"
                                        }
                                    },
                                    label = { Text(template) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

fun getFileIcon(file: ProjectFile): ImageVector {
    return if (file.isDirectory) {
        Icons.Default.Folder
    } else {
        when (file.name.substringAfterLast(".", "").lowercase()) {
            "kt", "java" -> Icons.Default.Code
            "xml" -> Icons.Default.Description
            "json" -> Icons.Default.DataObject
            "md" -> Icons.AutoMirrored.Filled.Article
            "png", "jpg", "jpeg" -> Icons.Default.Image
            else -> Icons.AutoMirrored.Filled.InsertDriveFile
        }
    }
}

@Composable
fun getExtensionColor(extension: String): androidx.compose.ui.graphics.Color {
    return when (extension) {
        "kt" -> MaterialTheme.colorScheme.primary
        "java" -> MaterialTheme.colorScheme.secondary
        "xml" -> MaterialTheme.colorScheme.tertiary
        "json" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        "md" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.outline
    }
}
