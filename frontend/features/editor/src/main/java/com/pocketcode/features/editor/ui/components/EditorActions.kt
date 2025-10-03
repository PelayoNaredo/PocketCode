package com.pocketcode.features.editor.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/**
 * Available quick actions in the editor
 */
enum class EditorQuickAction {
    Format,
    Comment,
    Uncomment,
    Find,
    Replace,
    GoToLine,
    Undo,
    Redo,
    SelectAll,
    Copy,
    Cut,
    Paste,
    Duplicate,
    Delete,
    ToggleFolding,
    ExpandAll,
    CollapseAll,
    ZoomIn,
    ZoomOut,
    ResetZoom,
    ToggleMinimap,
    ToggleCompletion,
    ToggleMultiCursor,
    ToggleLineNumbers,
    ToggleWordWrap
}

/**
 * Editor keyboard shortcuts configuration
 */
@Immutable
data class EditorKeyboardShortcuts(
    val save: String = "Ctrl+S",
    val undo: String = "Ctrl+Z",
    val redo: String = "Ctrl+Y",
    val copy: String = "Ctrl+C",
    val cut: String = "Ctrl+X",
    val paste: String = "Ctrl+V",
    val selectAll: String = "Ctrl+A",
    val find: String = "Ctrl+F",
    val replace: String = "Ctrl+H",
    val goToLine: String = "Ctrl+G",
    val comment: String = "Ctrl+/",
    val format: String = "Ctrl+Shift+F",
    val duplicate: String = "Ctrl+D",
    val delete: String = "Delete",
    val zoomIn: String = "Ctrl++",
    val zoomOut: String = "Ctrl+-",
    val resetZoom: String = "Ctrl+0"
)

/**
 * Editor configuration settings
 */
@Immutable
data class EditorConfig(
    val fontSize: Int = 14,
    val fontFamily: String = "JetBrains Mono",
    val tabSize: Int = 4,
    val insertSpaces: Boolean = true,
    val wordWrap: Boolean = false,
    val showLineNumbers: Boolean = true,
    val showMinimap: Boolean = true,
    val showWhitespace: Boolean = false,
    val highlightCurrentLine: Boolean = true,
    val autoCloseBrackets: Boolean = true,
    val autoIndent: Boolean = true,
    val enableCodeFolding: Boolean = true,
    val enableMultiCursor: Boolean = true,
    val scrollSensitivity: Float = 1.0f,
    val cursorBlinkRate: Long = 500L,
    val autoSaveDelay: Long = 3000L
)

/**
 * Editor text selection
 */
@Immutable
data class TextSelection(
    val start: Int,
    val end: Int
) {
    val isCollapsed: Boolean get() = start == end
    val length: Int get() = end - start
    
    fun contains(index: Int): Boolean = index in start..end
    
    companion object {
        val Empty = TextSelection(0, 0)
    }
}

/**
 * Editor cursor position
 */
@Immutable
data class CursorPosition(
    val line: Int,
    val column: Int,
    val offset: Int
) {
    companion object {
        val Start = CursorPosition(1, 1, 0)
    }
}

/**
 * Multi-cursor state for advanced editing
 */
@Immutable
data class MultiCursorState(
    val cursors: List<CursorPosition>,
    val selections: List<TextSelection>
) {
    val hasCursors: Boolean get() = cursors.isNotEmpty()
    val hasSelections: Boolean get() = selections.any { !it.isCollapsed }
    
    companion object {
        val Empty = MultiCursorState(emptyList(), emptyList())
    }
}

/**
 * Editor viewport information
 */
@Immutable
data class EditorViewport(
    val firstVisibleLine: Int,
    val lastVisibleLine: Int,
    val totalLines: Int,
    val scrollOffset: Float
) {
    val visibleLineCount: Int get() = lastVisibleLine - firstVisibleLine + 1
    val scrollPercentage: Float get() = if (totalLines > 0) firstVisibleLine.toFloat() / totalLines else 0f
    
    companion object {
        val Empty = EditorViewport(1, 1, 0, 0f)
    }
}

/**
 * Code folding region
 */
@Immutable
data class FoldingRegion(
    val startLine: Int,
    val endLine: Int,
    val isCollapsed: Boolean = false,
    val type: FoldingType = FoldingType.BLOCK
) {
    val lineCount: Int get() = endLine - startLine + 1
    
    fun contains(line: Int): Boolean = line in startLine..endLine
}

/**
 * Types of code folding
 */
enum class FoldingType {
    BLOCK,      // { } blocks
    FUNCTION,   // Function/method definitions
    CLASS,      // Class definitions
    COMMENT,    // Multi-line comments
    IMPORT,     // Import statements
    REGION      // Custom regions
}

/**
 * Error marker in editor
 */
@Immutable
data class ErrorMarker(
    val line: Int,
    val column: Int,
    val length: Int,
    val message: String,
    val severity: ErrorSeverity
)

/**
 * Error severity levels
 */
enum class ErrorSeverity {
    ERROR,
    WARNING,
    INFO,
    HINT
}

/**
 * Breakpoint information
 */
@Immutable
data class Breakpoint(
    val line: Int,
    val isEnabled: Boolean = true,
    val condition: String? = null,
    val hitCount: Int = 0
)

/**
 * Git blame information
 */
@Immutable
data class GitBlameInfo(
    val line: Int,
    val commit: String,
    val author: String,
    val date: String,
    val message: String
)

/**
 * Find/Replace state
 */
@Immutable
data class FindReplaceState(
    val isVisible: Boolean = false,
    val findText: String = "",
    val replaceText: String = "",
    val caseSensitive: Boolean = false,
    val wholeWord: Boolean = false,
    val useRegex: Boolean = false,
    val currentMatch: Int = 0,
    val totalMatches: Int = 0
) {
    val hasMatches: Boolean get() = totalMatches > 0
    val hasCurrentMatch: Boolean get() = currentMatch > 0 && currentMatch <= totalMatches
}

/**
 * Editor keyboard options for compose
 */
fun editorKeyboardOptions(): KeyboardOptions {
    return KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.None
    )
}

/**
 * Editor keyboard actions for compose
 */
fun editorKeyboardActions(
    onAction: (EditorQuickAction) -> Unit
): KeyboardActions {
    return KeyboardActions(
        onAny = { onAction(EditorQuickAction.ToggleCompletion) }
    )
}

/**
 * Extension functions for editor operations
 */
fun String.getLineAtIndex(index: Int): String {
    val lines = this.lines()
    return if (index in lines.indices) lines[index] else ""
}

fun String.getLineAndColumn(offset: Int): Pair<Int, Int> {
    val lines = this.lines()
    var currentOffset = 0
    
    lines.forEachIndexed { lineIndex, line ->
        if (offset <= currentOffset + line.length) {
            val column = offset - currentOffset + 1
            return Pair(lineIndex + 1, column)
        }
        currentOffset += line.length + 1 // +1 for newline
    }
    
    return Pair(lines.size, lines.lastOrNull()?.length ?: 0)
}

fun String.getOffsetFromLineColumn(line: Int, column: Int): Int {
    val lines = this.lines()
    if (line < 1 || line > lines.size) return 0
    
    var offset = 0
    for (i in 0 until line - 1) {
        offset += lines[i].length + 1 // +1 for newline
    }
    
    val targetLine = lines[line - 1]
    val targetColumn = column.coerceIn(1, targetLine.length + 1)
    
    return offset + targetColumn - 1
}

/**
 * Editor action handlers interface
 */
interface EditorActionHandler {
    fun handleQuickAction(action: EditorQuickAction)
    fun handleKeyboardShortcut(shortcut: String)
    fun handleTextChange(newText: String)
    fun handleSelectionChange(selection: TextSelection)
    fun handleCursorPositionChange(position: CursorPosition)
    fun handleScrollChange(viewport: EditorViewport)
}

/**
 * Default implementation of editor action handler
 */
class DefaultEditorActionHandler : EditorActionHandler {
    override fun handleQuickAction(action: EditorQuickAction) {
        // Default implementation - can be overridden
        when (action) {
            EditorQuickAction.Format -> { /* Format code */ }
            EditorQuickAction.Comment -> { /* Toggle comment */ }
            EditorQuickAction.Find -> { /* Show find dialog */ }
            EditorQuickAction.Replace -> { /* Show find/replace dialog */ }
            else -> { /* Handle other actions */ }
        }
    }
    
    override fun handleKeyboardShortcut(shortcut: String) {
        // Handle keyboard shortcuts
    }
    
    override fun handleTextChange(newText: String) {
        // Handle text changes
    }
    
    override fun handleSelectionChange(selection: TextSelection) {
        // Handle selection changes
    }
    
    override fun handleCursorPositionChange(position: CursorPosition) {
        // Handle cursor position changes
    }
    
    override fun handleScrollChange(viewport: EditorViewport) {
        // Handle scroll changes
    }
}