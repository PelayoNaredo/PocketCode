package com.pocketcode.features.editor.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.features.editor.domain.model.CodeLanguage
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

/**
 * Editor action for the top bar
 */
data class EditorAction(
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean = true,
    val isVisible: Boolean = true,
    val showBadge: Boolean = false,
    val badgeContent: String? = null,
    val onClick: () -> Unit
)

/**
 * File type specific actions
 */
object FileTypeActions {
    fun getActionsForLanguage(language: CodeLanguage): List<EditorAction> {
        return when (language) {
            CodeLanguage.KOTLIN -> listOf(
                EditorAction(
                    icon = Icons.Filled.PlayArrow,
                    label = "Run",
                    onClick = { /* Run Kotlin code */ }
                ),
                EditorAction(
                    icon = Icons.Filled.Build,
                    label = "Build",
                    onClick = { /* Build project */ }
                ),
                EditorAction(
                    icon = Icons.Filled.BugReport,
                    label = "Debug",
                    onClick = { /* Start debugging */ }
                )
            )
            
            CodeLanguage.JAVA -> listOf(
                EditorAction(
                    icon = Icons.Filled.PlayArrow,
                    label = "Run",
                    onClick = { /* Run Java code */ }
                ),
                EditorAction(
                    icon = Icons.Filled.Build,
                    label = "Compile",
                    onClick = { /* Compile Java */ }
                )
            )
            
            CodeLanguage.XML -> listOf(
                EditorAction(
                    icon = Icons.Filled.Visibility,
                    label = "Preview",
                    onClick = { /* Preview layout */ }
                ),
                EditorAction(
                    icon = Icons.Filled.Check,
                    label = "Validate",
                    onClick = { /* Validate XML */ }
                )
            )
            
            CodeLanguage.JSON -> listOf(
                EditorAction(
                    icon = Icons.Filled.Check,
                    label = "Validate",
                    onClick = { /* Validate JSON */ }
                ),
                EditorAction(
                    icon = Icons.Filled.AutoFixHigh,
                    label = "Format",
                    onClick = { /* Format JSON */ }
                )
            )
            
            CodeLanguage.HTML -> listOf(
                EditorAction(
                    icon = Icons.Filled.Visibility,
                    label = "Preview",
                    onClick = { /* Preview HTML */ }
                ),
                EditorAction(
                    icon = Icons.Filled.Check,
                    label = "Validate",
                    onClick = { /* Validate HTML */ }
                )
            )
            
            else -> emptyList()
        }
    }
}

/**
 * Specialized editor top bar with contextual actions based on file type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTopBar(
    file: ProjectFile,
    projectName: String?,
    isModified: Boolean = false,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    config: EditorContainerConfig,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onToggleLineNumbers: (Boolean) -> Unit = {},
    onToggleMinimap: (Boolean) -> Unit = {},
    onShowSettings: () -> Unit = {}
) {
    val language = remember(file.name) {
        CodeLanguage.fromFileName(file.name)
    }
    
    var showOverflowMenu by remember { mutableStateOf(false) }
    
    // Get file type specific actions
    val fileTypeActions = remember(language) {
        FileTypeActions.getActionsForLanguage(language)
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ColorTokens.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.small, vertical = SpacingTokens.xsmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = ColorTokens.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(SpacingTokens.small))
            
            // File info section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // File name with modification indicator
                    Text(
                        text = if (isModified) "● ${file.name}" else file.name,
                        style = TypographyTokens.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isModified) ColorTokens.primary else ColorTokens.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.width(SpacingTokens.xsmall))
                    
                    // Language indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ColorTokens.primaryContainer,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = language.displayName,
                            style = TypographyTokens.labelSmall,
                            color = ColorTokens.onPrimaryContainer,
                            modifier = Modifier.padding(
                                horizontal = SpacingTokens.small,
                                vertical = 2.dp
                            )
                        )
                    }
                }
                
                // Project name
                if (projectName != null) {
                    Text(
                        text = projectName,
                        style = TypographyTokens.bodySmall,
                        color = ColorTokens.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(SpacingTokens.small))
            
            // Editor actions row
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val defaultIconButtonColors = IconButtonDefaults.iconButtonColors(
                    contentColor = ColorTokens.onSurface
                )
                val saveButtonColors = if (isModified) {
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = ColorTokens.primary,
                        contentColor = ColorTokens.onPrimary
                    )
                } else {
                    IconButtonDefaults.iconButtonColors(contentColor = ColorTokens.onSurface)
                }
                
                // Undo/Redo buttons
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(36.dp),
                    colors = defaultIconButtonColors
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(36.dp),
                    colors = defaultIconButtonColors
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Save button
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(36.dp),
                    colors = saveButtonColors
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // File type specific actions (only show first 2)
                fileTypeActions.take(2).forEach { action ->
                    AnimatedVisibility(
                        visible = action.isVisible,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(
                            onClick = action.onClick,
                            enabled = action.enabled,
                            modifier = Modifier.size(36.dp),
                            colors = defaultIconButtonColors
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Overflow menu button
                Box {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        // View options
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (config.enableLineNumbers) 
                                            Icons.Filled.Check else Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(SpacingTokens.small))
                                    Text("Line Numbers")
                                }
                            },
                            onClick = {
                                onToggleLineNumbers(!config.enableLineNumbers)
                                showOverflowMenu = false
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (config.enableMinimap) 
                                            Icons.Filled.Check else Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(SpacingTokens.small))
                                    Text("Minimap")
                                }
                            },
                            onClick = {
                                onToggleMinimap(!config.enableMinimap)
                                showOverflowMenu = false
                            }
                        )
                        
                        // Remaining file type actions
                        if (fileTypeActions.size > 2) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = SpacingTokens.xsmall))
                            
                            fileTypeActions.drop(2).forEach { action ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(SpacingTokens.small))
                                            Text(action.label)
                                        }
                                    },
                                    onClick = {
                                        action.onClick()
                                        showOverflowMenu = false
                                    },
                                    enabled = action.enabled
                                )
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = SpacingTokens.xsmall))
                        
                        // Settings
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(SpacingTokens.small))
                                    Text("Editor Settings")
                                }
                            },
                            onClick = {
                                onShowSettings()
                                showOverflowMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status indicator for file modification state
 */
@Composable
private fun ModificationIndicator(
    isModified: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isModified,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = ColorTokens.primary,
            modifier = Modifier.size(8.dp)
        ) {}
    }
}

