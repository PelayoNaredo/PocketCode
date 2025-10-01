package com.pocketcode.features.editor.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketcode.features.editor.domain.model.CodeLanguage
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.core.ui.components.input.PocketTextField

/**
 * Editor status bar showing file info and cursor position
 */
@Composable
fun EditorStatusBar(
    fileName: String,
    language: CodeLanguage,
    cursorPosition: Pair<Int, Int>, // line, column
    selectionLength: Int? = null,
    isModified: Boolean = false,
    encoding: String = "UTF-8",
    lineEnding: String = "LF",
    tabSize: Int = 4,
    insertMode: Boolean = true,
    modifier: Modifier = Modifier,
    onLanguageClick: () -> Unit = {},
    onEncodingClick: () -> Unit = {},
    onLineEndingClick: () -> Unit = {},
    onPositionClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier.height(24.dp),
        color = ColorTokens.surfaceVariant,
        contentColor = ColorTokens.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
        ) {
            // File modification indicator
            if (isModified) {
                Text(
                    text = "●",
                    style = TypographyTokens.labelSmall,
                    color = ColorTokens.primary
                )
            }
            
            // Language
            StatusBarItem(
                text = language.displayName,
                onClick = onLanguageClick
            )
            
            StatusBarDivider()
            
            // Cursor position
            StatusBarItem(
                text = "Ln ${cursorPosition.first}, Col ${cursorPosition.second}",
                onClick = onPositionClick
            )
            
            // Selection length (if any)
            if (selectionLength != null && selectionLength > 0) {
                StatusBarItem(
                    text = "($selectionLength selected)",
                    onClick = {}
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Insert/Overwrite mode
            StatusBarItem(
                text = if (insertMode) "INS" else "OVR",
                onClick = {}
            )
            
            StatusBarDivider()
            
            // Tab size
            StatusBarItem(
                text = "Tab Size: $tabSize",
                onClick = {}
            )
            
            StatusBarDivider()
            
            // Encoding
            StatusBarItem(
                text = encoding,
                onClick = onEncodingClick
            )
            
            StatusBarDivider()
            
            // Line ending
            StatusBarItem(
                text = lineEnding,
                onClick = onLineEndingClick
            )
        }
    }
}

/**
 * Clickable status bar item
 */
@Composable
private fun StatusBarItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TypographyTokens.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        ),
        color = ColorTokens.onSurfaceVariant,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

/**
 * Status bar divider
 */
@Composable
private fun StatusBarDivider() {
    VerticalDivider(
        modifier = Modifier.height(12.dp),
        thickness = 1.dp,
        color = ColorTokens.outline.copy(alpha = 0.3f)
    )
}

/**
 * Editor minimap for navigation
 */
@Composable
fun EditorMinimap(
    content: String,
    language: CodeLanguage,
    cursorPosition: Pair<Int, Int>,
    visibleRange: Pair<Int, Int>,
    onNavigateToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = remember(content) { content.lines() }
    val scrollState = rememberScrollState()
    
    Surface(
        modifier = modifier,
        color = ColorTokens.surfaceVariant.copy(alpha = 0.8f),
        contentColor = ColorTokens.onSurfaceVariant
    ) {
        Column {
            // Minimap header
            Text(
                text = "Minimap",
                style = TypographyTokens.labelSmall,
                modifier = Modifier.padding(SpacingTokens.xsmall),
                textAlign = TextAlign.Center
            )
            
            HorizontalDivider(
                thickness = 1.dp,
                color = ColorTokens.outline.copy(alpha = 0.3f)
            )
            
            // Minimap content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .clickable { /* Handle click to navigate */ }
            ) {
                Column {
                    lines.forEachIndexed { index, line ->
                        MinimapLine(
                            lineNumber = index + 1,
                            content = line,
                            isCurrentLine = index + 1 == cursorPosition.first,
                            isVisible = index + 1 in visibleRange.first..visibleRange.second,
                            onLineClick = { onNavigateToLine(index + 1) }
                        )
                    }
                }
                
                // Visible area indicator
                VisibleAreaIndicator(
                    visibleRange = visibleRange,
                    totalLines = lines.size,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Individual minimap line
 */
@Composable
private fun MinimapLine(
    lineNumber: Int,
    content: String,
    isCurrentLine: Boolean,
    isVisible: Boolean,
    onLineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .clickable { onLineClick() }
            .background(
                when {
                    isCurrentLine -> ColorTokens.primary.copy(alpha = 0.6f)
                    isVisible -> ColorTokens.primaryContainer.copy(alpha = 0.3f)
                    content.isNotBlank() -> ColorTokens.onSurfaceVariant.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
    ) {
        // Line representation (simplified)
        if (content.isNotBlank()) {
            Box(
                modifier = Modifier
                    .width((content.length.coerceAtMost(50) * 2).dp)
                    .height(1.dp)
                    .background(ColorTokens.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    }
}

/**
 * Visible area indicator in minimap
 */
@Composable
private fun VisibleAreaIndicator(
    visibleRange: Pair<Int, Int>,
    totalLines: Int,
    modifier: Modifier = Modifier
) {
    if (totalLines <= 0) return
    
    val startRatio = visibleRange.first.toFloat() / totalLines.toFloat()
    val endRatio = visibleRange.second.toFloat() / totalLines.toFloat()
    
    Canvas(modifier = modifier) {
        val height = size.height
        val startY = startRatio * height
        val endY = endRatio * height
        
        drawRect(
            color = ColorTokens.primary.copy(alpha = 0.2f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, startY),
            size = androidx.compose.ui.geometry.Size(size.width, endY - startY)
        )
    }
}

/**
 * Floating editor controls
 */
@Composable
fun EditorControls(
    state: EditorContainerState,
    editorState: Any, // Replace with actual editor state type
    onQuickAction: (EditorQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isFocused,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            // Quick actions
            QuickActionsPanel(
                onAction = onQuickAction
            )
            
            // Find/Replace panel (if visible)
            if (state.showFindReplace) {
                FindReplacePanel(
                    onFind = { query -> /* Handle find */ },
                    onReplace = { query, replacement -> /* Handle replace */ },
                    onClose = { /* Close find/replace */ }
                )
            }
        }
    }
}

/**
 * Quick actions floating panel
 */
@Composable
private fun QuickActionsPanel(
    onAction: (EditorQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ColorTokens.surfaceVariant,
            contentColor = ColorTokens.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.small),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall)
        ) {
            QuickActionButton(
                icon = Icons.Filled.AutoFixHigh,
                label = "Format",
                onClick = { onAction(EditorQuickAction.Format) }
            )
            
            QuickActionButton(
                icon = Icons.Filled.Comment,
                label = "Comment",
                onClick = { onAction(EditorQuickAction.Comment) }
            )
            
            QuickActionButton(
                icon = Icons.Filled.Search,
                label = "Find",
                onClick = { onAction(EditorQuickAction.Find) }
            )
            
            QuickActionButton(
                icon = Icons.Filled.FindReplace,
                label = "Replace",
                onClick = { onAction(EditorQuickAction.Replace) }
            )
        }
    }
}

/**
 * Quick action button
 */
@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Find and replace panel
 */
@Composable
private fun FindReplacePanel(
    onFind: (String) -> Unit,
    onReplace: (String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ColorTokens.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Find & Replace",
                    style = TypographyTokens.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.small))
            
            // Find field
            PocketTextField(
                value = findText,
                onValueChange = { findText = it },
                label = "Find",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.small))
            
            // Replace field
            PocketTextField(
                value = replaceText,
                onValueChange = { replaceText = it },
                label = "Replace",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.medium))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
            ) {
                Button(
                    onClick = { onFind(findText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Find")
                }
                
                Button(
                    onClick = { onReplace(findText, replaceText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Replace All")
                }
            }
        }
    }
}

/**
 * Extension properties
 */
private val CodeLanguage.displayName: String
    get() = when (this) {
        CodeLanguage.KOTLIN -> "Kotlin"
        CodeLanguage.JAVA -> "Java"
        CodeLanguage.JAVASCRIPT -> "JavaScript"
        CodeLanguage.TYPESCRIPT -> "TypeScript"
        CodeLanguage.PYTHON -> "Python"
        CodeLanguage.DART -> "Dart"
        CodeLanguage.XML -> "XML"
        CodeLanguage.HTML -> "HTML"
        CodeLanguage.CSS -> "CSS"
        CodeLanguage.JSON -> "JSON"
        CodeLanguage.YAML -> "YAML"
        CodeLanguage.MARKDOWN -> "Markdown"
        CodeLanguage.SQL -> "SQL"
        CodeLanguage.GRADLE -> "Gradle"
        else -> "Text"
    }

private val EditorContainerState.showFindReplace: Boolean
    get() = false // This should be a real property in the state