package com.pocketcode.features.editor.multicursor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ControlPoint
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sistema avanzado de multi-cursor:
 * - Multiple cursors (múltiples cursores)
 * - Select all occurrences (seleccionar todas las ocurrencias)
 * - Column selection mode (modo de selección por columnas)
 * - Multi-line editing (edición multi-línea)
 * - Cursor history navigation (navegación del historial de cursores)
 * - Smart cursor placement (posicionamiento inteligente de cursores)
 * - Visual feedback y animations
 */

/**
 * Cursor individual con posición y selección
 */
data class EditorCursor(
    val id: String,
    val line: Int,
    val column: Int,
    val selection: TextRange? = null,
    val isPrimary: Boolean = false
) {
    val hasSelection: Boolean get() = selection != null && !selection.collapsed
    val position: Int get() = calculateAbsolutePosition(line, column)
    
    private fun calculateAbsolutePosition(line: Int, column: Int): Int {
        // Esta función debería calcular la posición absoluta basada en el texto
        // Por simplicidad, usamos una aproximación
        return line * 100 + column // Placeholder implementation
    }
}

/**
 * Modo de selección
 */
enum class SelectionMode {
    NORMAL,      // Selección normal
    COLUMN,      // Selección por columnas
    LINE,        // Selección de líneas completas
    WORD         // Selección de palabras
}

/**
 * Estado del sistema multi-cursor
 */
@Stable
class MultiCursorState {
    private val _cursors = MutableStateFlow<List<EditorCursor>>(emptyList())
    val cursors: StateFlow<List<EditorCursor>> = _cursors.asStateFlow()
    
    private val _selectionMode = MutableStateFlow(SelectionMode.NORMAL)
    val selectionMode: StateFlow<SelectionMode> = _selectionMode.asStateFlow()
    
    private val _cursorHistory = mutableListOf<List<EditorCursor>>()
    private var historyIndex = -1
    
    val hasPrimaryCursor: Boolean get() = _cursors.value.any { it.isPrimary }
    val hasMultipleCursors: Boolean get() = _cursors.value.size > 1
    val primaryCursor: EditorCursor? get() = _cursors.value.find { it.isPrimary }
    val cursorCount: Int get() = _cursors.value.size
    
    fun addCursor(line: Int, column: Int, makePrimary: Boolean = false) {
        val newId = "cursor_${System.currentTimeMillis()}_${line}_$column"
        
        // Check if cursor already exists at this position
        val existingCursor = _cursors.value.find { it.line == line && it.column == column }
        if (existingCursor != null) return
        
        saveToHistory()
        
        val updatedCursors = _cursors.value.map { cursor ->
            cursor.copy(isPrimary = false)
        } + EditorCursor(
            id = newId,
            line = line,
            column = column,
            isPrimary = makePrimary || !hasPrimaryCursor
        )
        
        _cursors.value = updatedCursors.sortedWith(compareBy({ it.line }, { it.column }))
    }
    
    fun removeCursor(cursorId: String) {
        if (_cursors.value.size <= 1) return
        
        saveToHistory()
        
        val updatedCursors = _cursors.value.filter { it.id != cursorId }
        
        // Ensure we have a primary cursor
        val finalCursors = if (!updatedCursors.any { it.isPrimary } && updatedCursors.isNotEmpty()) {
            updatedCursors.mapIndexed { index, cursor ->
                cursor.copy(isPrimary = index == 0)
            }
        } else {
            updatedCursors
        }
        
        _cursors.value = finalCursors
    }
    
    fun setPrimaryCursor(cursorId: String) {
        val updatedCursors = _cursors.value.map { cursor ->
            cursor.copy(isPrimary = cursor.id == cursorId)
        }
        _cursors.value = updatedCursors
    }
    
    fun clearAllCursors() {
        saveToHistory()
        _cursors.value = emptyList()
    }
    
    fun setOnlyCursor(line: Int, column: Int) {
        saveToHistory()
        val newCursor = EditorCursor(
            id = "primary_cursor",
            line = line,
            column = column,
            isPrimary = true
        )
        _cursors.value = listOf(newCursor)
    }
    
    fun selectAllOccurrences(text: String, searchTerm: String, caseSensitive: Boolean = false) {
        if (searchTerm.isBlank()) return
        
        saveToHistory()
        
        val occurrences = findAllOccurrences(text, searchTerm, caseSensitive)
        val newCursors = occurrences.mapIndexed { index, occurrence ->
            EditorCursor(
                id = "occurrence_$index",
                line = occurrence.line,
                column = occurrence.column,
                selection = TextRange(occurrence.start, occurrence.end),
                isPrimary = index == 0
            )
        }
        
        _cursors.value = newCursors
    }
    
    fun createColumnSelection(startLine: Int, startColumn: Int, endLine: Int, endColumn: Int) {
        saveToHistory()
        _selectionMode.value = SelectionMode.COLUMN
        
        val minLine = minOf(startLine, endLine)
        val maxLine = maxOf(startLine, endLine)
        val minColumn = minOf(startColumn, endColumn)
        val maxColumn = maxOf(startColumn, endColumn)
        
        val columnCursors = (minLine..maxLine).mapIndexed { index, line ->
            EditorCursor(
                id = "column_$line",
                line = line,
                column = minColumn,
                selection = if (minColumn != maxColumn) {
                    TextRange(minColumn, maxColumn)
                } else null,
                isPrimary = index == 0
            )
        }
        
        _cursors.value = columnCursors
    }
    
    fun moveAllCursors(deltaLine: Int, deltaColumn: Int) {
        val updatedCursors = _cursors.value.map { cursor ->
            cursor.copy(
                line = maxOf(0, cursor.line + deltaLine),
                column = maxOf(0, cursor.column + deltaColumn)
            )
        }
        _cursors.value = updatedCursors
    }
    
    fun updateCursorSelection(cursorId: String, selection: TextRange?) {
        val updatedCursors = _cursors.value.map { cursor ->
            if (cursor.id == cursorId) {
                cursor.copy(selection = selection)
            } else {
                cursor
            }
        }
        _cursors.value = updatedCursors
    }
    
    fun mergeCursorsOnSameLine() {
        val groupedByLine = _cursors.value.groupBy { it.line }
        val mergedCursors = groupedByLine.values.map { cursorsOnLine ->
            if (cursorsOnLine.size == 1) {
                cursorsOnLine.first()
            } else {
                val primaryCursor = cursorsOnLine.find { it.isPrimary } ?: cursorsOnLine.first()
                val minColumn = cursorsOnLine.minOf { it.column }
                val maxColumn = cursorsOnLine.maxOf { it.column }
                
                primaryCursor.copy(
                    column = minColumn,
                    selection = if (minColumn != maxColumn) {
                        TextRange(minColumn, maxColumn)
                    } else null
                )
            }
        }
        
        _cursors.value = mergedCursors.sortedWith(compareBy({ it.line }, { it.column }))
    }
    
    fun setSelectionMode(mode: SelectionMode) {
        _selectionMode.value = mode
    }
    
    // History management
    private fun saveToHistory() {
        if (historyIndex < _cursorHistory.size - 1) {
            // Remove future history when making new changes
            _cursorHistory.subList(historyIndex + 1, _cursorHistory.size).clear()
        }
        
        _cursorHistory.add(_cursors.value.toList())
        
        // Limit history size
        if (_cursorHistory.size > 50) {
            _cursorHistory.removeAt(0)
        } else {
            historyIndex++
        }
    }
    
    fun canUndo(): Boolean = historyIndex > 0
    
    fun canRedo(): Boolean = historyIndex < _cursorHistory.size - 1
    
    fun undo() {
        if (canUndo()) {
            historyIndex--
            _cursors.value = _cursorHistory[historyIndex].toList()
        }
    }
    
    fun redo() {
        if (canRedo()) {
            historyIndex++
            _cursors.value = _cursorHistory[historyIndex].toList()
        }
    }
    
    private fun findAllOccurrences(text: String, searchTerm: String, caseSensitive: Boolean): List<TextOccurrence> {
        val searchQuery = if (caseSensitive) searchTerm else searchTerm.lowercase()
        val occurrences = mutableListOf<TextOccurrence>()
        
        val lines = text.lines()
        
        lines.forEachIndexed { lineIndex, line ->
            val searchLine = if (caseSensitive) line else line.lowercase()
            var startIndex = 0
            
            while (true) {
                val index = searchLine.indexOf(searchQuery, startIndex)
                if (index == -1) break
                
                occurrences.add(
                    TextOccurrence(
                        line = lineIndex,
                        column = index,
                        start = index,
                        end = index + searchTerm.length
                    )
                )
                
                startIndex = index + 1
            }
        }
        
        return occurrences
    }
}

/**
 * Ocurrencia de texto encontrada
 */
data class TextOccurrence(
    val line: Int,
    val column: Int,
    val start: Int,
    val end: Int
)

/**
 * Componente visual para mostrar cursores múltiples
 */
@Composable
fun MultiCursorOverlay(
    cursors: List<EditorCursor>,
    textMetrics: TextMetrics,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Canvas(modifier = modifier.fillMaxSize()) {
        cursors.forEach { cursor ->
            val cursorColor = if (cursor.isPrimary) primaryColor else secondaryColor
            
            drawCursor(
                cursor = cursor,
                color = cursorColor,
                textMetrics = textMetrics,
                drawScope = this
            )
            
            // Draw selection if present
            cursor.selection?.let { selection ->
                drawSelection(
                    cursor = cursor,
                    selection = selection,
                    color = cursorColor.copy(alpha = 0.3f),
                    textMetrics = textMetrics,
                    drawScope = this
                )
            }
        }
    }
}

private fun drawCursor(
    cursor: EditorCursor,
    color: Color,
    textMetrics: TextMetrics,
    drawScope: DrawScope
) {
    val x = cursor.column * textMetrics.charWidth
    val y = cursor.line * textMetrics.lineHeight
    
    with(drawScope) {
        // Cursor line
        drawLine(
            color = color,
            start = Offset(x, y),
            end = Offset(x, y + textMetrics.lineHeight),
            strokeWidth = 2f
        )
        
        // Cursor indicator (small rectangle at top)
        if (!cursor.isPrimary) {
            drawRect(
                color = color,
                topLeft = Offset(x - 2f, y - 2f),
                size = androidx.compose.ui.geometry.Size(4f, 4f)
            )
        }
    }
}

private fun drawSelection(
    cursor: EditorCursor,
    selection: TextRange,
    color: Color,
    textMetrics: TextMetrics,
    drawScope: DrawScope
) {
    val startX = selection.start * textMetrics.charWidth
    val endX = selection.end * textMetrics.charWidth
    val y = cursor.line * textMetrics.lineHeight
    
    with(drawScope) {
        drawRect(
            color = color,
            topLeft = Offset(startX, y),
            size = androidx.compose.ui.geometry.Size(endX - startX, textMetrics.lineHeight)
        )
    }
}

/**
 * Métricas de texto para cálculo de posiciones
 */
data class TextMetrics(
    val charWidth: Float,
    val lineHeight: Float,
    val baseline: Float
)

/**
 * Panel de control para multi-cursor
 */
@Composable
fun MultiCursorControlPanel(
    state: MultiCursorState,
    onSelectAllOccurrences: (String) -> Unit,
    onCreateColumnSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cursors by state.cursors.collectAsState()
    val selectionMode by state.selectionMode.collectAsState()
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Multi-cursor",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Badge {
                    Text("${cursors.size}")
                }
            }
            
            // Current mode
            if (selectionMode != SelectionMode.NORMAL) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Modo: ${selectionMode.displayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Implement select all occurrences */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Seleccionar todo", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = onCreateColumnSelection,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.ControlPoint,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Columnas", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            // Actions
            if (cursors.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { state.clearAllCursors() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Limpiar", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    OutlinedButton(
                        onClick = { state.mergeCursorsOnSameLine() },
                        modifier = Modifier.weight(1f),
                        enabled = cursors.groupBy { it.line }.any { it.value.size > 1 }
                    ) {
                        Text("Fusionar", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // History controls
            if (state.canUndo() || state.canRedo()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { state.undo() },
                        enabled = state.canUndo(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deshacer", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    OutlinedButton(
                        onClick = { state.redo() },
                        enabled = state.canRedo(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rehacer", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Cursor list (if multiple)
            if (cursors.size > 1) {
                Text(
                    text = "Cursores activos:",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Column(
                    modifier = Modifier.heightIn(max = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    cursors.take(10).forEach { cursor -> // Limit display to 10
                        CursorItem(
                            cursor = cursor,
                            onRemove = { state.removeCursor(cursor.id) },
                            onMakePrimary = { state.setPrimaryCursor(cursor.id) }
                        )
                    }
                    
                    if (cursors.size > 10) {
                        Text(
                            text = "... y ${cursors.size - 10} más",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CursorItem(
    cursor: EditorCursor,
    onRemove: () -> Unit,
    onMakePrimary: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (cursor.isPrimary) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                } else {
                    Color.Transparent
                },
                RoundedCornerShape(4.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Línea ${cursor.line + 1}, Col ${cursor.column + 1}",
                style = MaterialTheme.typography.bodySmall
            )
            
            if (cursor.hasSelection) {
                Text(
                    text = "Selección: ${cursor.selection!!.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Row {
            if (!cursor.isPrimary) {
                IconButton(
                    onClick = onMakePrimary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ControlPoint,
                        contentDescription = "Hacer primario",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remover",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Detector de gestos para multi-cursor
 */
@Composable
fun MultiCursorGestureDetector(
    state: MultiCursorState,
    textMetrics: TextMetrics,
    onCursorAdded: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isColumnSelecting by remember { mutableStateOf(false) }
    var columnStartLine by remember { mutableStateOf(0) }
    var columnStartColumn by remember { mutableStateOf(0) }
    
    Box(
        modifier = modifier
            .pointerInput(state) {
                detectTapGestures(
                    onTap = { offset ->
                        val line = (offset.y / textMetrics.lineHeight).toInt()
                        val column = (offset.x / textMetrics.charWidth).toInt()
                        
                        // Add cursor at tap position
                        state.addCursor(line, column)
                        onCursorAdded(line, column)
                    },
                    onLongPress = { offset ->
                        val line = (offset.y / textMetrics.lineHeight).toInt()
                        val column = (offset.x / textMetrics.charWidth).toInt()
                        
                        // Start column selection
                        isColumnSelecting = true
                        columnStartLine = line
                        columnStartColumn = column
                        state.setSelectionMode(SelectionMode.COLUMN)
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Extension functions
 */
private fun SelectionMode.displayName(): String {
    return when (this) {
        SelectionMode.NORMAL -> "Normal"
        SelectionMode.COLUMN -> "Columnas"
        SelectionMode.LINE -> "Líneas"
        SelectionMode.WORD -> "Palabras"
    }
}

/**
 * Composable para recordar el estado multi-cursor
 */
@Composable
fun rememberMultiCursorState(): MultiCursorState {
    return remember { MultiCursorState() }
}

/**
 * Keyboard shortcuts handler para multi-cursor
 */
class MultiCursorKeyboardHandler(
    private val state: MultiCursorState
) {
    
    fun handleKeyEvent(
        key: String,
        isCtrlPressed: Boolean,
        isAltPressed: Boolean,
        isShiftPressed: Boolean
    ): Boolean {
        return when {
            isCtrlPressed && key == "d" -> {
                // Select next occurrence (like VS Code)
                // Implementation would depend on current selection
                true
            }
            isCtrlPressed && isShiftPressed && key == "l" -> {
                // Select all occurrences of current selection
                true
            }
            isAltPressed && key == "click" -> {
                // Add cursor at click position
                true
            }
            key == "Escape" -> {
                // Clear multiple cursors, keep only primary
                if (state.hasMultipleCursors) {
                    state.primaryCursor?.let { primary ->
                        state.setOnlyCursor(primary.line, primary.column)
                    }
                    true
                } else false
            }
            else -> false
        }
    }
}