package com.pocketcode.features.editor.ui.components.line_numbers

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketcode.features.editor.ui.components.FoldingRegion
import com.pocketcode.features.editor.ui.components.FoldingType
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens

/**
 * Line number state for tracking various indicators
 */
data class LineNumberState(
    val lineNumber: Int,
    val hasBreakpoint: Boolean = false,
    val hasError: Boolean = false,
    val hasWarning: Boolean = false,
    val hasInfo: Boolean = false,
    val isFoldingStart: Boolean = false,
    val isFoldingEnd: Boolean = false,
    val foldingLevel: Int = 0,
    val isCollapsed: Boolean = false,
    val gitBlameInfo: GitBlameInfo? = null
)

/**
 * Git blame information for a line
 */
data class GitBlameInfo(
    val author: String,
    val commitHash: String,
    val commitMessage: String,
    val timestamp: Long,
    val isModified: Boolean = false
)

/**
 * Breakpoint types
 */
enum class BreakpointType {
    REGULAR,
    CONDITIONAL,
    DISABLED
}

/**
 * Error indicator types
 */
enum class ErrorType {
    ERROR,
    WARNING,
    INFO,
    HINT
}

/**
 * Line number gutter with breakpoints, errors, folding, and git blame
 */
@Composable
fun LineNumbers(
    lineCount: Int,
    currentLine: Int,
    modifier: Modifier = Modifier,
    visibleRange: Pair<Int, Int> = 0 to lineCount,
    foldingRegions: List<FoldingRegion> = emptyList(),
    breakpoints: Map<Int, BreakpointType> = emptyMap(),
    errors: Map<Int, ErrorType> = emptyMap(),
    gitBlameData: Map<Int, GitBlameInfo> = emptyMap(),
    showBreakpoints: Boolean = true,
    showErrorIndicators: Boolean = true,
    showFoldingControls: Boolean = true,
    showGitBlame: Boolean = false,
    onBreakpointToggle: (Int) -> Unit = {},
    onToggleFolding: (Int) -> Unit = {},
    onLineClick: (Int) -> Unit = {},
    listState: LazyListState = rememberLazyListState()
) {
    val lineStates = remember(lineCount, foldingRegions, breakpoints, errors, gitBlameData) {
        buildLineStates(lineCount, foldingRegions, breakpoints, errors, gitBlameData)
    }
    
    val gutterWidth = remember(lineCount, showBreakpoints, showErrorIndicators) {
        calculateGutterWidth(lineCount, showBreakpoints, showErrorIndicators)
    }
    
    Surface(
        modifier = modifier.width(gutterWidth),
        color = ColorTokens.surfaceVariant
    ) {
        Row {
            // Breakpoint column
            if (showBreakpoints) {
                BreakpointColumn(
                    lineStates = lineStates,
                    visibleRange = visibleRange,
                    currentLine = currentLine,
                    onBreakpointToggle = onBreakpointToggle,
                    listState = listState,
                    modifier = Modifier.width(20.dp)
                )
            }
            
            // Error indicators column
            if (showErrorIndicators) {
                ErrorIndicatorColumn(
                    lineStates = lineStates,
                    visibleRange = visibleRange,
                    currentLine = currentLine,
                    listState = listState,
                    modifier = Modifier.width(16.dp)
                )
            }
            
            // Line numbers column
            LineNumberColumn(
                lineStates = lineStates,
                visibleRange = visibleRange,
                currentLine = currentLine,
                showFoldingControls = showFoldingControls,
                showGitBlame = showGitBlame,
                onToggleFolding = onToggleFolding,
                onLineClick = onLineClick,
                listState = listState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Breakpoint column
 */
@Composable
private fun BreakpointColumn(
    lineStates: List<LineNumberState>,
    visibleRange: Pair<Int, Int>,
    currentLine: Int,
    onBreakpointToggle: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxHeight(),
        userScrollEnabled = false
    ) {
        items(visibleRange.second - visibleRange.first) { index ->
            val lineNumber = visibleRange.first + index + 1
            val lineState = lineStates.getOrNull(lineNumber - 1)
            
            BreakpointIndicator(
                lineNumber = lineNumber,
                hasBreakpoint = lineState?.hasBreakpoint == true,
                isCurrentLine = lineNumber == currentLine,
                onToggle = { onBreakpointToggle(lineNumber) },
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Error indicator column
 */
@Composable
private fun ErrorIndicatorColumn(
    lineStates: List<LineNumberState>,
    visibleRange: Pair<Int, Int>,
    currentLine: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxHeight(),
        userScrollEnabled = false
    ) {
        items(visibleRange.second - visibleRange.first) { index ->
            val lineNumber = visibleRange.first + index + 1
            val lineState = lineStates.getOrNull(lineNumber - 1)
            
            ErrorIndicator(
                hasError = lineState?.hasError == true,
                hasWarning = lineState?.hasWarning == true,
                hasInfo = lineState?.hasInfo == true,
                isCurrentLine = lineNumber == currentLine,
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Line number column
 */
@Composable
private fun LineNumberColumn(
    lineStates: List<LineNumberState>,
    visibleRange: Pair<Int, Int>,
    currentLine: Int,
    showFoldingControls: Boolean,
    showGitBlame: Boolean,
    onToggleFolding: (Int) -> Unit,
    onLineClick: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxHeight(),
        userScrollEnabled = false
    ) {
        items(visibleRange.second - visibleRange.first) { index ->
            val lineNumber = visibleRange.first + index + 1
            val lineState = lineStates.getOrNull(lineNumber - 1)
            
            if (lineState?.isCollapsed != true) {
                LineNumberRow(
                    lineNumber = lineNumber,
                    lineState = lineState,
                    isCurrentLine = lineNumber == currentLine,
                    showFoldingControls = showFoldingControls,
                    showGitBlame = showGitBlame,
                    onToggleFolding = onToggleFolding,
                    onLineClick = onLineClick,
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Individual breakpoint indicator
 */
@Composable
private fun BreakpointIndicator(
    lineNumber: Int,
    hasBreakpoint: Boolean,
    isCurrentLine: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .semantics { contentDescription = "Breakpoint línea $lineNumber" }
            .clickable { onToggle() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasBreakpoint) {
            Surface(
                shape = CircleShape,
                color = ColorTokens.error,
                modifier = Modifier.size(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Breakpoint",
                    tint = Color.White,
                    modifier = Modifier.size(8.dp)
                )
            }
        } else if (isCurrentLine) {
            // Show a subtle indicator for current line
            Surface(
                shape = CircleShape,
                color = ColorTokens.outline.copy(alpha = 0.3f),
                modifier = Modifier.size(6.dp)
            ) {}
        }
    }
}

/**
 * Error/warning indicator
 */
@Composable
private fun ErrorIndicator(
    hasError: Boolean,
    hasWarning: Boolean,
    hasInfo: Boolean,
    isCurrentLine: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (isCurrentLine) 1f else 0.8f

    Box(
        modifier = modifier
            .semantics { contentDescription = "Estado línea: ${if (hasError) "error" else if (hasWarning) "aviso" else if (hasInfo) "info" else "sin incidencias"}" }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            hasError -> {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = ColorTokens.error.copy(alpha = alpha),
                    modifier = Modifier.size(12.dp)
                )
            }
            hasWarning -> {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    tint = ColorTokens.Semantic.warning500.copy(alpha = alpha),
                    modifier = Modifier.size(12.dp)
                )
            }
            hasInfo -> {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = ColorTokens.Semantic.info500.copy(alpha = alpha),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Line number row with folding and git blame
 */
@Composable
private fun LineNumberRow(
    lineNumber: Int,
    lineState: LineNumberState?,
    isCurrentLine: Boolean,
    showFoldingControls: Boolean,
    showGitBlame: Boolean,
    onToggleFolding: (Int) -> Unit,
    onLineClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onLineClick(lineNumber) }
            .background(
                if (isCurrentLine) ColorTokens.primaryContainer.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(horizontal = SpacingTokens.xsmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Folding control
        if (showFoldingControls && lineState?.isFoldingStart == true) {
            FoldingControl(
                isCollapsed = lineState.isCollapsed,
                foldingType = FoldingType.BLOCK, // This should come from the line state
                onClick = { onToggleFolding(lineNumber) },
                modifier = Modifier.size(12.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Line number
        Text(
            text = lineNumber.toString(),
            style = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentLine) ColorTokens.primary else ColorTokens.onSurfaceVariant
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        
        // Git blame info (if enabled)
        if (showGitBlame && lineState?.gitBlameInfo != null) {
            GitBlameIndicator(
                blameInfo = lineState.gitBlameInfo,
                modifier = Modifier.padding(start = SpacingTokens.xsmall)
            )
        }
    }
}

/**
 * Folding control button
 */
@Composable
private fun FoldingControl(
    isCollapsed: Boolean,
    foldingType: FoldingType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = foldingType.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = if (isCollapsed) "Expandir $label" else "Colapsar $label"
        }
    ) {
        Icon(
            imageVector = if (isCollapsed) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
            contentDescription = null,
            tint = ColorTokens.onSurfaceVariant,
            modifier = Modifier.size(10.dp)
        )
    }
}

/**
 * Git blame indicator
 */
@Composable
private fun GitBlameIndicator(
    blameInfo: GitBlameInfo,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = if (blameInfo.isModified) ColorTokens.Semantic.warning500 else ColorTokens.outline,
        modifier = modifier.size(4.dp)
    ) {}
}

/**
 * Build line states from various data sources
 */
private fun buildLineStates(
    lineCount: Int,
    foldingRegions: List<FoldingRegion>,
    breakpoints: Map<Int, BreakpointType>,
    errors: Map<Int, ErrorType>,
    gitBlameData: Map<Int, GitBlameInfo>
): List<LineNumberState> {
    return (1..lineCount).map { lineNumber ->
        val foldingRegion = foldingRegions.find { it.startLine == lineNumber - 1 }
        val isFoldingStart = foldingRegion != null
        val isFoldingEnd = foldingRegions.any { it.endLine == lineNumber - 1 }
        val foldingLevel = foldingRegions.count { lineNumber - 1 in it.startLine..it.endLine }
        val isCollapsed = foldingRegions.any { 
            it.isCollapsed && lineNumber - 1 in (it.startLine + 1)..it.endLine 
        }
        
        LineNumberState(
            lineNumber = lineNumber,
            hasBreakpoint = breakpoints.containsKey(lineNumber),
            hasError = errors[lineNumber] == ErrorType.ERROR,
            hasWarning = errors[lineNumber] == ErrorType.WARNING,
            hasInfo = errors[lineNumber] == ErrorType.INFO,
            isFoldingStart = isFoldingStart,
            isFoldingEnd = isFoldingEnd,
            foldingLevel = foldingLevel,
            isCollapsed = isCollapsed,
            gitBlameInfo = gitBlameData[lineNumber]
        )
    }
}

/**
 * Calculate the required gutter width based on content
 */
private fun calculateGutterWidth(
    lineCount: Int,
    showBreakpoints: Boolean,
    showErrorIndicators: Boolean
): androidx.compose.ui.unit.Dp {
    val lineNumberWidth = when {
        lineCount < 100 -> 24.dp
        lineCount < 1000 -> 32.dp
        lineCount < 10000 -> 40.dp
        else -> 48.dp
    }
    
    val breakpointWidth = if (showBreakpoints) 20.dp else 0.dp
    val errorWidth = if (showErrorIndicators) 16.dp else 0.dp
    
    return breakpointWidth + errorWidth + lineNumberWidth + 8.dp
}