package com.pocketcode.features.editor.folding

import com.pocketcode.features.editor.ui.components.*
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sistema avanzado de plegado de código:
 * - Function/class folding (plegado de funciones/clases)
 * - Comment block folding (plegado de bloques de comentarios)
 * - Custom fold regions (regiones de plegado personalizadas)
 * - Persist fold state (persistir estado de plegado)
 * - Visual fold indicators (indicadores visuales de plegado)
 * - Smart folding based on indentation (plegado inteligente basado en indentación)
 * - Nested folding support (soporte para plegado anidado)
 */

/**
 * Tipos de regiones plegables
 */
enum class FoldType {
    FUNCTION,
    CLASS,
    INTERFACE,
    METHOD,
    PROPERTY,
    COMMENT_BLOCK,
    IMPORT_BLOCK,
    CUSTOM_REGION,
    CONTROL_STRUCTURE, // if, for, while, etc.
    LAMBDA,
    INITIALIZER_BLOCK
}

/**
 * Región plegable
 */
data class FoldRegion(
    val id: String,
    val type: FoldType,
    val startLine: Int,
    val endLine: Int,
    val startColumn: Int = 0,
    val endColumn: Int = 0,
    val headerText: String,
    val placeholderText: String = "...",
    val isCollapsed: Boolean = false,
    val level: Int = 0, // Nesting level
    val canFold: Boolean = true,
    val description: String? = null
) {
    val lineCount: Int get() = endLine - startLine + 1
    val isEmpty: Boolean get() = startLine >= endLine
}

/**
 * Estado del sistema de plegado
 */
@Stable
class CodeFoldingState {
    private val _foldRegions = MutableStateFlow<List<FoldRegion>>(emptyList())
    val foldRegions: StateFlow<List<FoldRegion>> = _foldRegions.asStateFlow()
    
    private val _persistedState = mutableStateMapOf<String, Boolean>()
    
    fun updateFoldRegions(regions: List<FoldRegion>) {
        // Restaurar estados persistidos
        val restoredRegions = regions.map { region ->
            val isCollapsed = _persistedState[region.id] ?: region.isCollapsed
            region.copy(isCollapsed = isCollapsed)
        }
        _foldRegions.value = restoredRegions
    }
    
    fun toggleFold(regionId: String) {
        val currentRegions = _foldRegions.value
        val updatedRegions = currentRegions.map { region ->
            if (region.id == regionId) {
                val newCollapsed = !region.isCollapsed
                _persistedState[regionId] = newCollapsed
                region.copy(isCollapsed = newCollapsed)
            } else {
                region
            }
        }
        _foldRegions.value = updatedRegions
    }
    
    fun foldRegion(regionId: String) {
        setFoldState(regionId, true)
    }
    
    fun unfoldRegion(regionId: String) {
        setFoldState(regionId, false)
    }
    
    fun foldAll() {
        val currentRegions = _foldRegions.value
        val updatedRegions = currentRegions.map { region ->
            _persistedState[region.id] = true
            region.copy(isCollapsed = true)
        }
        _foldRegions.value = updatedRegions
    }
    
    fun unfoldAll() {
        val currentRegions = _foldRegions.value
        val updatedRegions = currentRegions.map { region ->
            _persistedState[region.id] = false
            region.copy(isCollapsed = false)
        }
        _foldRegions.value = updatedRegions
    }
    
    fun foldByType(type: FoldType) {
        val currentRegions = _foldRegions.value
        val updatedRegions = currentRegions.map { region ->
            if (region.type == type) {
                _persistedState[region.id] = true
                region.copy(isCollapsed = true)
            } else {
                region
            }
        }
        _foldRegions.value = updatedRegions
    }
    
    fun unfoldByType(type: FoldType) {
        val currentRegions = _foldRegions.value
        val updatedRegions = currentRegions.map { region ->
            if (region.type == type) {
                _persistedState[region.id] = false
                region.copy(isCollapsed = false)
            } else {
                region
            }
        }
        _foldRegions.value = updatedRegions
    }
    
    private fun setFoldState(regionId: String, collapsed: Boolean) {
        val currentRegions = _foldRegions.value
        val updatedRegions = currentRegions.map { region ->
            if (region.id == regionId) {
                _persistedState[regionId] = collapsed
                region.copy(isCollapsed = collapsed)
            } else {
                region
            }
        }
        _foldRegions.value = updatedRegions
    }
    
    fun getRegionAt(line: Int): FoldRegion? {
        return _foldRegions.value.find { region ->
            line in region.startLine..region.endLine
        }
    }
    
    fun isLineVisible(line: Int): Boolean {
        val region = _foldRegions.value.find { region ->
            region.isCollapsed && line in (region.startLine + 1)..region.endLine
        }
        return region == null
    }
    
    fun getCollapsedRegions(): List<FoldRegion> {
        return _foldRegions.value.filter { it.isCollapsed }
    }
}

/**
 * Componente de indicador de plegado
 */
@Composable
fun FoldIndicator(
    region: FoldRegion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (region.isCollapsed) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (region.isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
            contentDescription = if (region.isCollapsed) "Expandir" else "Colapsar",
            modifier = Modifier.size(12.dp),
            tint = if (region.isCollapsed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    }
}

/**
 * Componente de región colapsada
 */
@Composable
fun CollapsedRegion(
    region: FoldRegion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = region.headerText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = region.placeholderText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        
        if (region.lineCount > 1) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "${region.lineCount}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp)
                )
            }
        }
    }
}

/**
 * Panel de control de plegado
 */
@Composable
fun FoldingControlPanel(
    state: CodeFoldingState,
    modifier: Modifier = Modifier
) {
    val regions by state.foldRegions.collectAsState()
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Plegado de código",
                style = MaterialTheme.typography.titleSmall
            )
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { state.foldAll() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Plegar todo", style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedButton(
                    onClick = { state.unfoldAll() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Expandir todo", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            // Fold by type
            if (regions.isNotEmpty()) {
                val groupedRegions = regions.groupBy { it.type }
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(groupedRegions.entries.toList()) { (type, typeRegions) ->
                        FoldTypeControl(
                            type = type,
                            regions = typeRegions,
                            onFoldAll = { state.foldByType(type) },
                            onUnfoldAll = { state.unfoldByType(type) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoldTypeControl(
    type: FoldType,
    regions: List<FoldRegion>,
    onFoldAll: () -> Unit,
    onUnfoldAll: () -> Unit
) {
    val collapsedCount = regions.count { it.isCollapsed }
    val totalCount = regions.size
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = type.displayName(),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "$collapsedCount de $totalCount colapsados",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onFoldAll,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ExpandLess,
                    contentDescription = "Plegar ${type.displayName()}",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            IconButton(
                onClick = onUnfoldAll,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = "Expandir ${type.displayName()}",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Canvas para dibujar indicadores de plegado en el gutter
 */
@Composable
fun FoldingGutter(
    regions: List<FoldRegion>,
    visibleLineRange: IntRange,
    lineHeight: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    
    Canvas(
        modifier = modifier.fillMaxHeight()
    ) {
        val gutterWidth = size.width
        val startY = 0f
        
        regions.forEach { region ->
            if (region.startLine in visibleLineRange) {
                val y = startY + (region.startLine - visibleLineRange.first) * lineHeight
                
                drawFoldIndicator(
                    region = region,
                    centerX = gutterWidth / 2,
                    centerY = y + lineHeight / 2,
                    primaryColor = primaryColor,
                    outlineColor = outlineColor
                )
                
                // Draw folding lines for nested regions
                if (region.level > 0 && !region.isCollapsed) {
                    drawFoldingLines(
                        startY = y,
                        endY = startY + (region.endLine - region.startLine) * lineHeight,
                        centerX = gutterWidth / 2 - (region.level * 8),
                        color = outlineColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFoldIndicator(
    region: FoldRegion,
    centerX: Float,
    centerY: Float,
    primaryColor: Color,
    outlineColor: Color
) {
    val radius = 6f
    val strokeWidth = 1.5f
    
    // Background circle
    drawCircle(
        color = if (region.isCollapsed) primaryColor.copy(alpha = 0.1f) else outlineColor.copy(alpha = 0.1f),
        radius = radius,
        center = Offset(centerX, centerY)
    )
    
    // Border circle
    drawCircle(
        color = if (region.isCollapsed) primaryColor else outlineColor,
        radius = radius,
        center = Offset(centerX, centerY),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )
    
    // Icon (- or +)
    val iconSize = 3f
    val iconColor = if (region.isCollapsed) primaryColor else outlineColor
    
    // Horizontal line (always present)
    drawLine(
        color = iconColor,
        start = Offset(centerX - iconSize, centerY),
        end = Offset(centerX + iconSize, centerY),
        strokeWidth = strokeWidth
    )
    
    // Vertical line (only when collapsed)
    if (region.isCollapsed) {
        drawLine(
            color = iconColor,
            start = Offset(centerX, centerY - iconSize),
            end = Offset(centerX, centerY + iconSize),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawFoldingLines(
    startY: Float,
    endY: Float,
    centerX: Float,
    color: Color
) {
    drawLine(
        color = color,
        start = Offset(centerX, startY),
        end = Offset(centerX, endY),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
    )
}

/**
 * Parser de regiones plegables para Kotlin
 */
class KotlinFoldingParser {
    
    fun parseText(text: String): List<FoldRegion> {
        val lines = text.lines()
        val regions = mutableListOf<FoldRegion>()
        
        // Parse functions, classes, etc.
        parseFunctionsAndClasses(lines, regions)
        
        // Parse comment blocks
        parseCommentBlocks(lines, regions)
        
        // Parse import blocks
        parseImportBlocks(lines, regions)
        
        // Parse custom regions
        parseCustomRegions(lines, regions)
        
        return regions.sortedBy { it.startLine }
    }
    
    private fun parseFunctionsAndClasses(lines: List<String>, regions: MutableList<FoldRegion>) {
        val stack = mutableListOf<Pair<Int, String>>() // (line, type)
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            // Detect class/interface/object declarations
            when {
                trimmed.matches(Regex("^(class|interface|object|enum class)\\s+\\w+.*\\{.*")) -> {
                    stack.add(index to "class")
                }
                trimmed.matches(Regex("^(fun|suspend fun)\\s+\\w+.*\\{.*")) -> {
                    stack.add(index to "function")
                }
                trimmed.contains("{") -> {
                    stack.add(index to "block")
                }
            }
            
            // Detect closing braces
            if (trimmed.contains("}") && stack.isNotEmpty()) {
                val (startLine, type) = stack.removeLastOrNull() ?: return@forEachIndexed
                
                if (index > startLine) {
                    val foldType = when (type) {
                        "class" -> FoldType.CLASS
                        "function" -> FoldType.FUNCTION
                        else -> FoldType.CONTROL_STRUCTURE
                    }
                    
                    val headerText = lines[startLine].trim().take(50)
                    
                    regions.add(
                        FoldRegion(
                            id = "fold_${startLine}_$index",
                            type = foldType,
                            startLine = startLine,
                            endLine = index,
                            headerText = headerText,
                            level = stack.size
                        )
                    )
                }
            }
        }
    }
    
    private fun parseCommentBlocks(lines: List<String>, regions: MutableList<FoldRegion>) {
        var blockStart = -1
        var inBlockComment = false
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            when {
                trimmed.startsWith("/*") -> {
                    blockStart = index
                    inBlockComment = true
                }
                trimmed.endsWith("*/") && inBlockComment -> {
                    if (index > blockStart && index - blockStart >= 2) {
                        regions.add(
                            FoldRegion(
                                id = "comment_${blockStart}_$index",
                                type = FoldType.COMMENT_BLOCK,
                                startLine = blockStart,
                                endLine = index,
                                headerText = "/* ${lines[blockStart].trim().removePrefix("/*").take(30)} */",
                                placeholderText = "/* ... */"
                            )
                        )
                    }
                    inBlockComment = false
                }
            }
        }
        
        // Parse consecutive line comments
        parseLineCommentBlocks(lines, regions)
    }
    
    private fun parseLineCommentBlocks(lines: List<String>, regions: MutableList<FoldRegion>) {
        var blockStart = -1
        var consecutiveComments = 0
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            if (trimmed.startsWith("//")) {
                if (consecutiveComments == 0) {
                    blockStart = index
                }
                consecutiveComments++
            } else {
                if (consecutiveComments >= 3) {
                    regions.add(
                        FoldRegion(
                            id = "linecomment_${blockStart}_${index - 1}",
                            type = FoldType.COMMENT_BLOCK,
                            startLine = blockStart,
                            endLine = index - 1,
                            headerText = lines[blockStart].trim().take(50),
                            placeholderText = "// ..."
                        )
                    )
                }
                consecutiveComments = 0
            }
        }
        
        // Handle comment block at end of file
        if (consecutiveComments >= 3) {
            regions.add(
                FoldRegion(
                    id = "linecomment_${blockStart}_${lines.size - 1}",
                    type = FoldType.COMMENT_BLOCK,
                    startLine = blockStart,
                    endLine = lines.size - 1,
                    headerText = lines[blockStart].trim().take(50),
                    placeholderText = "// ..."
                )
            )
        }
    }
    
    private fun parseImportBlocks(lines: List<String>, regions: MutableList<FoldRegion>) {
        var importStart = -1
        var importCount = 0
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            if (trimmed.startsWith("import ")) {
                if (importCount == 0) {
                    importStart = index
                }
                importCount++
            } else if (importCount > 0) {
                if (importCount >= 3) {
                    regions.add(
                        FoldRegion(
                            id = "imports_${importStart}_${index - 1}",
                            type = FoldType.IMPORT_BLOCK,
                            startLine = importStart,
                            endLine = index - 1,
                            headerText = "imports ($importCount)",
                            placeholderText = "import ..."
                        )
                    )
                }
                importCount = 0
            }
        }
    }
    
    private fun parseCustomRegions(lines: List<String>, regions: MutableList<FoldRegion>) {
        val regionStack = mutableListOf<Pair<Int, String>>()
        
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            
            // Custom region markers: // region Name or /* region Name */
            when {
                trimmed.matches(Regex("^//\\s*region\\s+(.+)")) -> {
                    val name = trimmed.replace(Regex("^//\\s*region\\s+"), "")
                    regionStack.add(index to name)
                }
                trimmed.matches(Regex("^//\\s*endregion")) -> {
                    if (regionStack.isNotEmpty()) {
                        val (startLine, name) = regionStack.removeLastOrNull() ?: return@forEachIndexed
                        if (index > startLine) {
                            regions.add(
                                FoldRegion(
                                    id = "region_${startLine}_$index",
                                    type = FoldType.CUSTOM_REGION,
                                    startLine = startLine,
                                    endLine = index,
                                    headerText = "region $name",
                                    placeholderText = "...",
                                    level = regionStack.size
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Extension functions
 */
private fun FoldType.displayName(): String {
    return when (this) {
        FoldType.FUNCTION -> "Funciones"
        FoldType.CLASS -> "Clases"
        FoldType.INTERFACE -> "Interfaces"
        FoldType.METHOD -> "Métodos"
        FoldType.PROPERTY -> "Propiedades"
        FoldType.COMMENT_BLOCK -> "Comentarios"
        FoldType.IMPORT_BLOCK -> "Imports"
        FoldType.CUSTOM_REGION -> "Regiones"
        FoldType.CONTROL_STRUCTURE -> "Estructuras de Control"
        FoldType.LAMBDA -> "Lambdas"
        FoldType.INITIALIZER_BLOCK -> "Bloques Init"
    }
}

/**
 * Composable para recordar el estado de plegado
 */
@Composable
fun rememberCodeFoldingState(): CodeFoldingState {
    return remember { CodeFoldingState() }
}