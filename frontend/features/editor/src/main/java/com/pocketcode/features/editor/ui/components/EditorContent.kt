package com.pocketcode.features.editor.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.pocketcode.features.editor.domain.model.CodeLanguage
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.features.editor.ui.components.line_numbers.LineNumbers

/**
 * Editor text state for managing large files efficiently
 */
class EditorTextState(
    initialContent: String = "",
    private val maxVisibleLines: Int = 100
) {
    private val _lines = mutableStateListOf<String>()
    private val _content = MutableStateFlow(initialContent)
    
    val content: StateFlow<String> = _content
    val lines: List<String> get() = _lines
    val lineCount: Int get() = _lines.size
    
    private var _cursorPosition by mutableStateOf(TextRange.Zero)
    val cursorPosition: TextRange get() = _cursorPosition
    
    private var _selection by mutableStateOf(TextRange.Zero)
    val selection: TextRange get() = _selection
    
    init {
        updateLines(initialContent)
    }
    
    fun updateContent(newContent: String) {
        _content.value = newContent
        updateLines(newContent)
    }
    
    private fun updateLines(content: String) {
        _lines.clear()
        _lines.addAll(content.lines())
    }
    
    fun getVisibleLines(startLine: Int): List<IndexedValue<String>> {
        val endLine = minOf(startLine + maxVisibleLines, _lines.size)
        return _lines.subList(startLine, endLine).withIndex().map { 
            IndexedValue(startLine + it.index, it.value) 
        }
    }
    
    fun updateCursorPosition(position: TextRange) {
        _cursorPosition = position
    }
    
    fun updateSelection(selection: TextRange) {
        _selection = selection
    }
    
    fun getLineAtOffset(offset: Int): Int {
        var currentOffset = 0
        for (i in _lines.indices) {
            val lineLength = _lines[i].length + 1 // +1 for newline
            if (currentOffset + lineLength > offset) {
                return i
            }
            currentOffset += lineLength
        }
        return maxOf(0, _lines.size - 1)
    }
    
    fun getOffsetAtLine(line: Int): Int {
        if (line >= _lines.size) return content.value.length
        
        var offset = 0
        for (i in 0 until line) {
            offset += _lines[i].length + 1 // +1 for newline
        }
        return offset
    }
}

/**
 * Multi-cursor support for advanced editing
 */
data class EditorCursor(
    val position: Int,
    val selection: TextRange? = null
)


/**
 * Advanced editor content with virtualization and multi-cursor support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorContent(
    content: String,
    language: CodeLanguage,
    showLineNumbers: Boolean = true,
    config: EditorContainerConfig,
    state: EditorContainerState,
    modifier: Modifier = Modifier,
    onContentChanged: (String) -> Unit = {},
    onCursorPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    onSelectionChanged: (Int, Int) -> Unit = { _, _ -> },
    onFocusChanged: (Boolean) -> Unit = {}
) {
    val editorTextState = remember(content) { EditorTextState(content) }
    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val shouldShowLineNumbers = showLineNumbers && state.showLineNumbers

    // Folding regions state
    var foldingRegions by remember { mutableStateOf<List<FoldingRegion>>(emptyList()) }

    // Current text field value for the active editing
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(content))
    }
    
    // Calculate visible line range
    val visibleLineRange = remember(lazyListState.firstVisibleItemIndex, lazyListState.layoutInfo.visibleItemsInfo.size) {
        val start = maxOf(0, lazyListState.firstVisibleItemIndex - 10) // Buffer for smooth scrolling
        val end = minOf(
            editorTextState.lineCount,
            lazyListState.firstVisibleItemIndex + lazyListState.layoutInfo.visibleItemsInfo.size + 10
        )
        start to end
    }
    
    // Update editor state when content changes
    LaunchedEffect(textFieldValue.text) {
        if (textFieldValue.text != content) {
            editorTextState.updateContent(textFieldValue.text)
            onContentChanged(textFieldValue.text)
        }
    }
    
    // Update cursor position
    LaunchedEffect(textFieldValue.selection) {
        editorTextState.updateSelection(textFieldValue.selection)
        val line = editorTextState.getLineAtOffset(textFieldValue.selection.start)
        val column = textFieldValue.selection.start - editorTextState.getOffsetAtLine(line)
        onCursorPositionChanged(line + 1, column + 1) // 1-based for display
        onSelectionChanged(textFieldValue.selection.start, textFieldValue.selection.end)
    }
    
    // Auto-detect folding regions
    LaunchedEffect(content, language) {
        foldingRegions = detectFoldingRegions(editorTextState.lines, language)
    }

    LaunchedEffect(state.selectionRange) {
        val range = state.selectionRange ?: return@LaunchedEffect
        val clampedStart = range.first.coerceIn(0, textFieldValue.text.length)
        val clampedEnd = range.last.coerceIn(0, textFieldValue.text.length)
        textFieldValue = textFieldValue.copy(selection = TextRange(clampedStart, clampedEnd))
    }

    LaunchedEffect(state.isFocused) {
        if (state.isFocused) {
            focusRequester.requestFocus()
        }
    }
    
    Row(modifier = modifier.fillMaxSize()) {
        // Line numbers column
        if (shouldShowLineNumbers) {
            LineNumbers(
                lineCount = editorTextState.lineCount,
                currentLine = editorTextState.getLineAtOffset(textFieldValue.selection.start) + 1,
                foldingRegions = foldingRegions,
                visibleRange = visibleLineRange,
                onToggleFolding = { lineNumber ->
                    val region = foldingRegions.find { it.startLine == lineNumber - 1 }
                    if (region != null) {
                        foldingRegions = foldingRegions.map {
                            if (it == region) it.copy(isCollapsed = !it.isCollapsed)
                            else it
                        }
                    }
                },
                modifier = Modifier.width(60.dp)
            )
            
            // Vertical divider
            VerticalDivider(
                color = ColorTokens.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
        
        // Main editor area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            if (config.enableSyntaxHighlighting) {
                // Syntax highlighted editor with virtualization
                SyntaxHighlightedEditor(
                    textState = editorTextState,
                    language = language,
                    textFieldValue = textFieldValue,
                    lazyListState = lazyListState,
                    foldingRegions = foldingRegions,
                    visibleRange = visibleLineRange,
                    config = config,
                    focusRequester = focusRequester,
                    onTextChanged = { newValue ->
                        textFieldValue = newValue
                    },
                    onFocusChanged = onFocusChanged,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Plain text editor for better performance with large files
                PlainTextEditor(
                    textFieldValue = textFieldValue,
                    lazyListState = lazyListState,
                    config = config,
                    focusRequester = focusRequester,
                    onTextChanged = { newValue ->
                        textFieldValue = newValue
                    },
                    onFocusChanged = onFocusChanged,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Code folding indicators overlay
            if (config.enableCodeFolding) {
                CodeFoldingOverlay(
                    foldingRegions = foldingRegions,
                    visibleRange = visibleLineRange,
                    lineHeight = 20.dp, // Should match editor line height
                    onToggleFolding = { region ->
                        foldingRegions = foldingRegions.map {
                            if (it == region) it.copy(isCollapsed = !it.isCollapsed)
                            else it
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Request focus when first composed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

/**
 * Syntax highlighted editor implementation
 */
@Composable
private fun SyntaxHighlightedEditor(
    textState: EditorTextState,
    language: CodeLanguage,
    textFieldValue: TextFieldValue,
    lazyListState: LazyListState,
    foldingRegions: List<FoldingRegion>,
    visibleRange: Pair<Int, Int>,
    config: EditorContainerConfig,
    focusRequester: FocusRequester,
    onTextChanged: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleLines = remember(visibleRange) {
        textState.getVisibleLines(visibleRange.first)
    }
    
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
    ) {
        itemsIndexed(
            items = visibleLines,
            key = { _, item -> item.index }
        ) { _, lineData ->
            val (lineNumber, lineContent) = lineData
            
            // Check if this line is in a collapsed folding region
            val isCollapsed = foldingRegions.any { region ->
                region.isCollapsed && lineNumber in (region.startLine + 1)..region.endLine
            }
            
            if (!isCollapsed) {
                SyntaxHighlightedLine(
                    lineNumber = lineNumber,
                    content = lineContent,
                    language = language,
                    isCurrentLine = lineNumber == textState.getLineAtOffset(textFieldValue.selection.start),
                    config = config,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Plain text editor for better performance
 */
@Composable
private fun PlainTextEditor(
    textFieldValue: TextFieldValue,
    lazyListState: LazyListState,
    config: EditorContainerConfig,
    focusRequester: FocusRequester,
    onTextChanged: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = textFieldValue,
        onValueChange = onTextChanged,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = ColorTokens.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Default
        ),
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .padding(SpacingTokens.small)
    )
}

/**
 * Individual syntax highlighted line
 */
@Composable
private fun SyntaxHighlightedLine(
    lineNumber: Int,
    content: String,
    language: CodeLanguage,
    isCurrentLine: Boolean,
    config: EditorContainerConfig,
    modifier: Modifier = Modifier
) {
    val highlightedText = remember(content, language) {
        applySyntaxHighlighting(content, language)
    }
    
    Row(
        modifier = modifier
            .height(20.dp)
            .background(
                if (isCurrentLine) ColorTokens.primaryContainer.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .padding(horizontal = SpacingTokens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = highlightedText,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

/**
 * Code folding overlay
 */
@Composable
private fun CodeFoldingOverlay(
    foldingRegions: List<FoldingRegion>,
    visibleRange: Pair<Int, Int>,
    lineHeight: androidx.compose.ui.unit.Dp,
    onToggleFolding: (FoldingRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation for folding indicators and collapsed region indicators
    // This would draw folding icons and handle clicks
}

/**
 * Auto-detect folding regions based on language syntax
 */
private fun detectFoldingRegions(lines: List<String>, language: CodeLanguage): List<FoldingRegion> {
    val regions = mutableListOf<FoldingRegion>()
    
    when (language) {
        CodeLanguage.KOTLIN, CodeLanguage.JAVA -> {
            detectBraceFoldingRegions(lines, regions)
            detectFunctionFoldingRegions(lines, regions, language)
            detectClassFoldingRegions(lines, regions, language)
        }
        CodeLanguage.XML, CodeLanguage.HTML -> {
            detectXmlFoldingRegions(lines, regions)
        }
        CodeLanguage.JSON -> {
            detectJsonFoldingRegions(lines, regions)
        }
        else -> {
            detectBraceFoldingRegions(lines, regions)
        }
    }
    
    return regions
}

private fun detectBraceFoldingRegions(lines: List<String>, regions: MutableList<FoldingRegion>) {
    val stack = mutableListOf<Int>()
    
    lines.forEachIndexed { index, line ->
        val openBraces = line.count { it == '{' }
        val closeBraces = line.count { it == '}' }
        
        repeat(openBraces) {
            stack.add(index)
        }
        
        repeat(closeBraces) {
            if (stack.isNotEmpty()) {
                val startLine = stack.removeLastOrNull()
                if (startLine != null && index > startLine) {
                    regions.add(FoldingRegion(startLine, index, type = FoldingType.BLOCK))
                }
            }
        }
    }
}

private fun detectFunctionFoldingRegions(lines: List<String>, regions: MutableList<FoldingRegion>, language: CodeLanguage) {
    val functionPattern = when (language) {
        CodeLanguage.KOTLIN -> Regex("""^\s*(private\s+|public\s+|internal\s+|protected\s+)?(suspend\s+)?fun\s+\w+.*\{?\s*$""")
        CodeLanguage.JAVA -> Regex("""^\s*(private\s+|public\s+|protected\s+)?(static\s+)?\w+\s+\w+\s*\([^)]*\)\s*\{?\s*$""")
        else -> return
    }
    
    lines.forEachIndexed { index, line ->
        if (functionPattern.matches(line.trim())) {
            // Find the closing brace for this function
            var braceCount = line.count { it == '{' } - line.count { it == '}' }
            var endLine = index
            
            for (i in (index + 1) until lines.size) {
                braceCount += lines[i].count { it == '{' } - lines[i].count { it == '}' }
                if (braceCount == 0) {
                    endLine = i
                    break
                }
            }
            
            if (endLine > index) {
                regions.add(FoldingRegion(index, endLine, type = FoldingType.FUNCTION))
            }
        }
    }
}

private fun detectClassFoldingRegions(lines: List<String>, regions: MutableList<FoldingRegion>, language: CodeLanguage) {
    val classPattern = when (language) {
        CodeLanguage.KOTLIN -> Regex("""^\s*(private\s+|public\s+|internal\s+)?(data\s+|sealed\s+|abstract\s+)?class\s+\w+.*\{?\s*$""")
        CodeLanguage.JAVA -> Regex("""^\s*(private\s+|public\s+|protected\s+)?(abstract\s+|final\s+)?class\s+\w+.*\{?\s*$""")
        else -> return
    }
    
    lines.forEachIndexed { index, line ->
        if (classPattern.matches(line.trim())) {
            // Find the closing brace for this class
            var braceCount = line.count { it == '{' } - line.count { it == '}' }
            var endLine = index
            
            for (i in (index + 1) until lines.size) {
                braceCount += lines[i].count { it == '{' } - lines[i].count { it == '}' }
                if (braceCount == 0) {
                    endLine = i
                    break
                }
            }
            
            if (endLine > index) {
                regions.add(FoldingRegion(index, endLine, type = FoldingType.CLASS))
            }
        }
    }
}

private fun detectXmlFoldingRegions(lines: List<String>, regions: MutableList<FoldingRegion>) {
    // Implementation for XML tag folding
}

private fun detectJsonFoldingRegions(lines: List<String>, regions: MutableList<FoldingRegion>) {
    // Implementation for JSON object/array folding
}

/**
 * Apply syntax highlighting - this should be moved to the SyntaxHighlighter component later
 */
private fun applySyntaxHighlighting(text: String, language: CodeLanguage): AnnotatedString {
    return buildAnnotatedString {
        append(text)
        // Basic highlighting - will be replaced by proper SyntaxHighlighter
        when (language) {
            CodeLanguage.KOTLIN -> {
                // Apply Kotlin-specific highlighting
                addStyle(
                    style = SpanStyle(color = Color(0xFF1976D2), fontWeight = FontWeight.Bold),
                    start = 0,
                    end = minOf(text.length, 10)
                )
            }
            else -> {
                // Default styling
            }
        }
    }
}