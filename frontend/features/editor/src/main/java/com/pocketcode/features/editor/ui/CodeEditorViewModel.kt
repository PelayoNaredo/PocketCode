package com.pocketcode.features.editor.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.core.ui.providers.EditorConfiguration
import com.pocketcode.domain.project.model.Project
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.domain.project.usecase.GetFileContentUseCase
import com.pocketcode.domain.project.usecase.SaveFileContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class CodeEditorUiState(
    val content: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isModified: Boolean = false,
    val currentFile: ProjectFile? = null,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
    val editorConfig: EditorConfiguration = EditorConfiguration(),
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val cursorPosition: Int = 0
)

sealed class SaveResult {
    object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
}

@HiltViewModel
class CodeEditorViewModel @Inject constructor(
    private val getFileContentUseCase: GetFileContentUseCase,
    private val saveFileContentUseCase: SaveFileContentUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodeEditorUiState())
    val uiState: StateFlow<CodeEditorUiState> = _uiState.asStateFlow()

    private var currentProject: Project? = null
    private var originalContent: String = ""

    fun setProject(projectId: String, projectName: String, projectPath: String?) {
        val resolvedPath = projectPath ?: File(context.filesDir, "projects/$projectName").absolutePath
        currentProject = Project(
            id = projectId,
            name = projectName,
            localPath = resolvedPath
        )
    }

    fun loadFile(file: ProjectFile) {
        if (_uiState.value.currentFile?.path == file.path) return // Already loaded
        
        _uiState.value = _uiState.value.copy(
            currentFile = file,
            isLoading = true,
            error = null,
            saveResult = null
        )
        
        viewModelScope.launch {
            val project = currentProject ?: Project(
                id = "default",
                name = "Default Project",
                localPath = File(context.filesDir, "projects/default").absolutePath
            )
            
            getFileContentUseCase(project, file.path).onSuccess { fileContent ->
                originalContent = fileContent
                _uiState.value = _uiState.value.copy(
                    content = fileContent,
                    isLoading = false,
                    isModified = false,
                    error = null
                )
            }.onFailure { _ ->
                // If file doesn't exist or can't be read, show empty content for new files
                originalContent = getDefaultContent(file.name)
                _uiState.value = _uiState.value.copy(
                    content = originalContent,
                    isLoading = false,
                    isModified = false,
                    error = null // Don't show error for new files
                )
            }
        }
    }

    private fun getDefaultContent(fileName: String): String {
        return when {
            fileName.endsWith(".kt") -> """
                package com.example
                
                fun main() {
                    println("Hello, World!")
                }
            """.trimIndent()
            fileName.endsWith(".java") -> """
                package com.example;
                
                public class ${fileName.substringBeforeLast(".")} {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }
            """.trimIndent()
            fileName.endsWith(".xml") -> """
                <?xml version="1.0" encoding="utf-8"?>
                <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical">

                </LinearLayout>
            """.trimIndent()
            fileName.endsWith(".json") -> """
                {
                    "name": "example",
                    "version": "1.0.0"
                }
            """.trimIndent()
            fileName.endsWith(".md") -> """
                # ${fileName.substringBeforeLast(".")}
                
                Welcome to your new file!
            """.trimIndent()
            else -> "// New file: $fileName\n"
        }
    }

    fun updateContent(newContent: String) {
        if (_uiState.value.currentFile == null) return

        _uiState.value = _uiState.value.copy(
            content = newContent,
            isModified = newContent != originalContent,
            saveResult = null // Clear previous save results when content changes
        )
    }

    fun saveFile() {
        val currentFile = _uiState.value.currentFile ?: return
        val project = currentProject ?: return
        val content = _uiState.value.content
        
        if (!_uiState.value.isModified) return // No changes to save
        
        _uiState.value = _uiState.value.copy(isSaving = true, saveResult = null)
        
        viewModelScope.launch {
            saveFileContentUseCase(project, currentFile.path, content)
                .onSuccess {
                    originalContent = content
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isModified = false,
                        saveResult = SaveResult.Success,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveResult = SaveResult.Error(exception.message ?: "Error saving file"),
                        error = exception.message
                    )
                }
        }
    }

    fun clearSaveResult() {
        _uiState.value = _uiState.value.copy(saveResult = null)
    }

    fun formatCode() {
        val currentContent = _uiState.value.content
        val currentFile = _uiState.value.currentFile ?: return
        
        // Basic formatting based on file type
        val formattedContent = when {
            currentFile.name.endsWith(".kt") -> formatKotlinCode(currentContent)
            currentFile.name.endsWith(".java") -> formatJavaCode(currentContent)
            currentFile.name.endsWith(".xml") -> formatXmlCode(currentContent)
            currentFile.name.endsWith(".json") -> formatJsonCode(currentContent)
            else -> currentContent
        }
        
        if (formattedContent != currentContent) {
            updateContent(formattedContent)
        }
    }

    private fun formatKotlinCode(content: String): String {
        // Basic Kotlin formatting
        return content
            .lines()
            .map { line -> line.trimEnd() }
            .joinToString("\n")
            .replace(Regex("\\{\\s*\\n\\s*\\}"), " { }")
            .replace(Regex("\\}\\s*\\n\\s*else"), "} else")
    }

    private fun formatJavaCode(content: String): String {
        // Basic Java formatting
        return content
            .lines()
            .map { line -> line.trimEnd() }
            .joinToString("\n")
    }

    private fun formatXmlCode(content: String): String {
        // Basic XML formatting - ensure proper indentation
        val lines = content.lines()
        val formatted = mutableListOf<String>()
        var indentLevel = 0
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                if (trimmed.startsWith("</") && !trimmed.startsWith("<!--")) {
                    indentLevel = maxOf(0, indentLevel - 1)
                }
                
                formatted.add("    ".repeat(indentLevel) + trimmed)
                
                if (trimmed.startsWith("<") && 
                    !trimmed.startsWith("</") && 
                    !trimmed.startsWith("<!--") &&
                    !trimmed.endsWith("/>") &&
                    !trimmed.endsWith("?>")) {
                    indentLevel++
                }
            } else {
                formatted.add("")
            }
        }
        
        return formatted.joinToString("\n")
    }

    private fun formatJsonCode(content: String): String {
        // Basic JSON formatting
        return try {
            // Simple indentation for JSON
            content
                .replace(Regex("\\{\\s*"), "{\n    ")
                .replace(Regex("\\}\\s*"), "\n}")
                .replace(Regex(",\\s*"), ",\n    ")
        } catch (e: Exception) {
            content
        }
    }

    fun undo() {
        // Basic undo functionality - revert to original content
        // Note: Advanced undo/redo with history stack is a future enhancement
        if (_uiState.value.isModified) {
            _uiState.value = _uiState.value.copy(
                content = originalContent,
                isModified = false,
                saveResult = null
            )
        }
    }

    fun redo() {
        // Note: Advanced undo/redo with history stack is a future enhancement
        // Current implementation focuses on core editing functionality
    }

    fun discardChanges() {
        _uiState.value = _uiState.value.copy(
            content = originalContent,
            isModified = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // Selection management
    fun updateSelection(start: Int, end: Int) {
        _uiState.value = _uiState.value.copy(
            selectionStart = start,
            selectionEnd = end
        )
    }
    
    fun updateCursorPosition(position: Int) {
        _uiState.value = _uiState.value.copy(
            cursorPosition = position.coerceIn(0, _uiState.value.content.length)
        )
    }
    
    fun getSelectedText(): String {
        val state = _uiState.value
        if (state.selectionStart == state.selectionEnd) return ""
        
        val start = minOf(state.selectionStart, state.selectionEnd).coerceIn(0, state.content.length)
        val end = maxOf(state.selectionStart, state.selectionEnd).coerceIn(0, state.content.length)
        
        return state.content.substring(start, end)
    }
    
    // New editor enhancement functions
    fun copySelection(clipboardText: (String) -> Unit) {
        val selectedText = getSelectedText()
        if (selectedText.isNotEmpty()) {
            clipboardText(selectedText)
        }
    }
    
    fun cutSelection(clipboardText: (String) -> Unit) {
        val selectedText = getSelectedText()
        if (selectedText.isNotEmpty()) {
            clipboardText(selectedText)
            deleteSelection()
        }
    }
    
    fun paste(clipboardText: String) {
        if (clipboardText.isNotEmpty()) {
            insertTextAtCursor(clipboardText)
        }
    }
    
    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectionStart = 0,
            selectionEnd = _uiState.value.content.length
        )
    }
    
    private fun deleteSelection() {
        val state = _uiState.value
        if (state.selectionStart == state.selectionEnd) return
        
        val start = minOf(state.selectionStart, state.selectionEnd).coerceIn(0, state.content.length)
        val end = maxOf(state.selectionStart, state.selectionEnd).coerceIn(0, state.content.length)
        
        val newContent = state.content.removeRange(start, end)
        val newCursor = start
        
        _uiState.value = state.copy(
            content = newContent,
            isModified = newContent != originalContent,
            selectionStart = newCursor,
            selectionEnd = newCursor,
            cursorPosition = newCursor
        )
    }
    
    private fun insertTextAtCursor(text: String) {
        val state = _uiState.value
        
        // If there's a selection, delete it first
        if (state.selectionStart != state.selectionEnd) {
            deleteSelection()
        }
        
        val currentState = _uiState.value
        val cursor = currentState.cursorPosition.coerceIn(0, currentState.content.length)
        
        val newContent = StringBuilder(currentState.content)
            .insert(cursor, text)
            .toString()
        
        val newCursor = cursor + text.length
        
        _uiState.value = currentState.copy(
            content = newContent,
            isModified = newContent != originalContent,
            cursorPosition = newCursor,
            selectionStart = newCursor,
            selectionEnd = newCursor
        )
    }
    
    fun toggleComment() {
        val currentContent = _uiState.value.content
        val currentFile = _uiState.value.currentFile ?: return
        
        val commentedContent = when {
            currentFile.name.endsWith(".kt") || 
            currentFile.name.endsWith(".java") -> toggleLineComment(currentContent, "//")
            currentFile.name.endsWith(".xml") -> toggleLineComment(currentContent, "<!--", "-->")
            currentFile.name.endsWith(".py") -> toggleLineComment(currentContent, "#")
            else -> currentContent
        }
        
        if (commentedContent != currentContent) {
            updateContent(commentedContent)
        }
    }
    
    fun duplicateLine() {
        val state = _uiState.value
        val cursor = state.cursorPosition.coerceIn(0, state.content.length)
        val lines = state.content.lines()
        
        // Find which line the cursor is on
        var currentPos = 0
        var currentLine = 0
        
        for ((index, line) in lines.withIndex()) {
            val lineEndPos = currentPos + line.length
            if (cursor <= lineEndPos) {
                currentLine = index
                break
            }
            currentPos = lineEndPos + 1 // +1 for newline character
        }
        
        // Duplicate the current line
        val mutableLines = lines.toMutableList()
        if (currentLine < mutableLines.size) {
            mutableLines.add(currentLine + 1, mutableLines[currentLine])
            val newContent = mutableLines.joinToString("\n")
            
            // Move cursor to the beginning of the duplicated line
            val newCursorPos = (0..currentLine).sumOf { lines[it].length + 1 } - 1
            
            _uiState.value = state.copy(
                content = newContent,
                isModified = newContent != originalContent,
                cursorPosition = newCursorPos,
                selectionStart = newCursorPos,
                selectionEnd = newCursorPos
            )
        }
    }
    
    fun deleteLine() {
        val state = _uiState.value
        val cursor = state.cursorPosition.coerceIn(0, state.content.length)
        val lines = state.content.lines()
        
        if (lines.isEmpty()) return
        
        // Find which line the cursor is on
        var currentPos = 0
        var currentLine = 0
        
        for ((index, line) in lines.withIndex()) {
            val lineEndPos = currentPos + line.length
            if (cursor <= lineEndPos) {
                currentLine = index
                break
            }
            currentPos = lineEndPos + 1 // +1 for newline character
        }
        
        // Delete the current line
        val mutableLines = lines.toMutableList()
        if (mutableLines.size > 1) {
            mutableLines.removeAt(currentLine)
            val newContent = mutableLines.joinToString("\n")
            
            // Keep cursor at same position or move to end if necessary
            val newCursorPos = cursor.coerceIn(0, newContent.length)
            
            _uiState.value = state.copy(
                content = newContent,
                isModified = newContent != originalContent,
                cursorPosition = newCursorPos,
                selectionStart = newCursorPos,
                selectionEnd = newCursorPos
            )
        } else if (mutableLines.size == 1) {
            // If only one line, just clear it
            val newContent = ""
            _uiState.value = state.copy(
                content = newContent,
                isModified = newContent != originalContent,
                cursorPosition = 0,
                selectionStart = 0,
                selectionEnd = 0
            )
        }
    }
    
    fun insertCompletion(text: String) {
        val currentContent = _uiState.value.content
        // Simple implementation - append the completion
        updateContent(currentContent + text)
    }
    
    fun navigateToLine(lineNumber: Int) {
        val state = _uiState.value
        val lines = state.content.lines()
        
        // Validate line number (1-indexed for user, 0-indexed internally)
        val targetLine = (lineNumber - 1).coerceIn(0, lines.size - 1)
        
        // Calculate the position at the start of the target line
        var position = 0
        for (i in 0 until targetLine) {
            position += lines[i].length + 1 // +1 for newline
        }
        
        // Skip leading whitespace on the target line to position cursor at first non-whitespace char
        val targetLineContent = lines.getOrNull(targetLine) ?: ""
        val firstNonWhitespace = targetLineContent.indexOfFirst { !it.isWhitespace() }
        if (firstNonWhitespace >= 0) {
            position += firstNonWhitespace
        }
        
        _uiState.value = state.copy(
            cursorPosition = position,
            selectionStart = position,
            selectionEnd = position
        )
    }
    
    fun getCurrentLineNumber(): Int {
        val state = _uiState.value
        val cursor = state.cursorPosition.coerceIn(0, state.content.length)
        val lines = state.content.substring(0, cursor).lines()
        return lines.size
    }
    
    fun retryLoadFile() {
        val currentFile = _uiState.value.currentFile
        if (currentFile != null) {
            loadFile(currentFile)
        }
    }
    
    // New state flows for editor enhancements
    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()
    
    private fun toggleLineComment(content: String, singleComment: String, endComment: String? = null): String {
        val lines = content.lines().toMutableList()
        
        for (i in lines.indices) {
            val line = lines[i].trimStart()
            if (line.isNotEmpty()) {
                if (endComment != null) {
                    // Block comment style (XML)
                    if (line.startsWith(singleComment) && line.endsWith(endComment)) {
                        lines[i] = line.substring(singleComment.length, line.length - endComment.length).trim()
                    } else {
                        lines[i] = "$singleComment ${line.trim()} $endComment"
                    }
                } else {
                    // Line comment style
                    if (line.startsWith(singleComment)) {
                        lines[i] = lines[i].replaceFirst(singleComment, "").trimStart()
                    } else {
                        val indent = lines[i].takeWhile { it.isWhitespace() }
                        lines[i] = "$indent$singleComment ${lines[i].trimStart()}"
                    }
                }
            }
        }
        
        return lines.joinToString("\n")
    }
    
    /**
     * Actualiza la configuración del editor desde SharedStateProvider
     */
    fun updateEditorConfiguration(config: EditorConfiguration) {
        _uiState.value = _uiState.value.copy(editorConfig = config)
    }
    
    /**
     * Aplica la configuración del editor al estado actual
     */
    fun applyEditorConfiguration() {
        val config = _uiState.value.editorConfig
        _uiState.value = _uiState.value.copy(editorConfig = config)
    }
}

/**
 * Enhanced editor state for new features
 */
data class EditorState(
    val content: String = "",
    val language: String = "plain_text",
    val isModified: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val cursorPosition: Pair<Int, Int> = 0 to 0,
    val selectionRange: Pair<Int, Int>? = null
)
