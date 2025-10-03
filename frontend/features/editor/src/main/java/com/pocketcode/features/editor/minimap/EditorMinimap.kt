@file:OptIn(ExperimentalMaterial3Api::class)
package com.pocketcode.features.editor.minimap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import com.pocketcode.core.ui.components.input.PocketTextField
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Sistema avanzado de minimapa del editor:
 * - Syntax coloring (coloreado de sintaxis)
 * - Scroll navigation (navegación con scroll)
 * - Code overview (vista general del código)
 * - Clickable navigation (navegación clickeable)
 * - Performance optimization (optimización de rendimiento)
 * - Viewport indicator (indicador de ventana visible)
 * - Error/warning markers (marcadores de errores y advertencias)
 * - Search result highlights (destacados de resultados de búsqueda)
 * - Folded region indicators (indicadores de regiones plegadas)
 */

/**
 * Configuración del minimapa
 */
data class MinimapConfig(
    val width: Float = 120f,
    val showSyntaxHighlighting: Boolean = true,
    val showLineNumbers: Boolean = false,
    val showViewport: Boolean = true,
    val showScrollbar: Boolean = true,
    val showErrorMarkers: Boolean = true,
    val showSearchHighlights: Boolean = true,
    val showFoldedRegions: Boolean = true,
    val scale: Float = 0.1f, // Scale factor for minimap rendering
    val renderQuality: RenderQuality = RenderQuality.BALANCED,
    val refreshRate: RefreshRate = RefreshRate.MEDIUM,
    val colorScheme: MinimapColorScheme = MinimapColorScheme.AUTO
)

/**
 * Calidad de renderizado
 */
enum class RenderQuality(val displayName: String, val pixelDensity: Float) {
    LOW("Baja", 0.5f),
    BALANCED("Balanceada", 0.75f),
    HIGH("Alta", 1.0f),
    ULTRA("Ultra", 1.5f)
}

/**
 * Frecuencia de actualización
 */
enum class RefreshRate(val displayName: String, val intervalMs: Long) {
    LOW("Baja (500ms)", 500),
    MEDIUM("Media (200ms)", 200),
    HIGH("Alta (100ms)", 100),
    REALTIME("Tiempo real (50ms)", 50)
}

/**
 * Esquema de colores del minimapa
 */
enum class MinimapColorScheme(val displayName: String) {
    AUTO("Automático"),
    LIGHT("Claro"),
    DARK("Oscuro"),
    HIGH_CONTRAST("Alto contraste"),
    MONOCHROME("Monocromático")
}

/**
 * Token de sintaxis para el minimapa
 */
data class MinimapToken(
    val start: Int,
    val end: Int,
    val type: TokenType,
    val color: Color,
    val line: Int,
    val column: Int
)

/**
 * Tipos de tokens
 */
enum class TokenType {
    KEYWORD,
    STRING,
    COMMENT,
    NUMBER,
    IDENTIFIER,
    OPERATOR,
    PUNCTUATION,
    WHITESPACE,
    ERROR
}

/**
 * Marcador en el minimapa
 */
data class MinimapMarker(
    val line: Int,
    val type: MarkerType,
    val color: Color,
    val message: String = "",
    val severity: MarkerSeverity = MarkerSeverity.INFO
)

/**
 * Tipos de marcadores
 */
enum class MarkerType {
    ERROR,
    WARNING,
    INFO,
    SEARCH_RESULT,
    BREAKPOINT,
    BOOKMARK,
    FOLDED_REGION
}

/**
 * Severidad del marcador
 */
enum class MarkerSeverity {
    ERROR,
    WARNING,
    INFO,
    HINT
}

/**
 * Viewport del editor visible
 */
data class EditorViewport(
    val startLine: Int,
    val endLine: Int,
    val totalLines: Int,
    val scrollOffset: Float = 0f
) {
    val visibleLineCount: Int get() = endLine - startLine + 1
    val progressRatio: Float get() = if (totalLines > 0) startLine.toFloat() / totalLines else 0f
}

/**
 * Estado del minimapa
 */
@Stable
class MinimapState {
    private val _config = MutableStateFlow(MinimapConfig())
    val config: StateFlow<MinimapConfig> = _config.asStateFlow()
    
    private val _tokens = MutableStateFlow<List<MinimapToken>>(emptyList())
    val tokens: StateFlow<List<MinimapToken>> = _tokens.asStateFlow()
    
    private val _markers = MutableStateFlow<List<MinimapMarker>>(emptyList())
    val markers: StateFlow<List<MinimapMarker>> = _markers.asStateFlow()
    
    private val _viewport = MutableStateFlow(EditorViewport(0, 10, 100))
    val viewport: StateFlow<EditorViewport> = _viewport.asStateFlow()
    
    private val _isVisible = MutableStateFlow(true)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()
    
    private val _isHovered = MutableStateFlow(false)
    val isHovered: StateFlow<Boolean> = _isHovered.asStateFlow()
    
    private val _contentHeight = MutableStateFlow(0f)
    val contentHeight: StateFlow<Float> = _contentHeight.asStateFlow()
    
    private val _renderedLines = MutableStateFlow<Map<Int, String>>(emptyMap())
    val renderedLines: StateFlow<Map<Int, String>> = _renderedLines.asStateFlow()
    
    fun updateConfig(config: MinimapConfig) {
        _config.value = config
    }
    
    fun updateTokens(tokens: List<MinimapToken>) {
        _tokens.value = tokens
    }
    
    fun updateMarkers(markers: List<MinimapMarker>) {
        _markers.value = markers
    }
    
    fun updateViewport(viewport: EditorViewport) {
        _viewport.value = viewport
        _contentHeight.value = viewport.totalLines * getLineHeight()
    }
    
    fun setVisible(visible: Boolean) {
        _isVisible.value = visible
    }
    
    fun setHovered(hovered: Boolean) {
        _isHovered.value = hovered
    }
    
    fun updateRenderedLines(lines: Map<Int, String>) {
        _renderedLines.value = lines
    }
    
    fun scrollToLine(line: Int): Boolean {
        val currentViewport = _viewport.value
        if (line < 0 || line >= currentViewport.totalLines) return false
        
        val newStartLine = maxOf(0, line - currentViewport.visibleLineCount / 2)
        val newEndLine = minOf(currentViewport.totalLines - 1, newStartLine + currentViewport.visibleLineCount - 1)
        
        _viewport.value = currentViewport.copy(
            startLine = newStartLine,
            endLine = newEndLine
        )
        
        return true
    }
    
    fun addMarker(marker: MinimapMarker) {
        val currentMarkers = _markers.value.toMutableList()
        currentMarkers.add(marker)
        _markers.value = currentMarkers
    }
    
    fun removeMarker(line: Int, type: MarkerType) {
        val currentMarkers = _markers.value.toMutableList()
        currentMarkers.removeAll { it.line == line && it.type == type }
        _markers.value = currentMarkers
    }
    
    fun clearMarkers(type: MarkerType? = null) {
        if (type == null) {
            _markers.value = emptyList()
        } else {
            _markers.value = _markers.value.filter { it.type != type }
        }
    }
    
    fun getMarkersForLine(line: Int): List<MinimapMarker> {
        return _markers.value.filter { it.line == line }
    }
    
    private fun getLineHeight(): Float {
        return _config.value.scale * 20f // Base line height scaled
    }
    
    fun calculateLineFromPosition(y: Float): Int {
        val lineHeight = getLineHeight()
        val line = (y / lineHeight).toInt()
        return line.coerceIn(0, _viewport.value.totalLines - 1)
    }
    
    fun calculatePositionFromLine(line: Int): Float {
        val lineHeight = getLineHeight()
        return line * lineHeight
    }
}

/**
 * Analizador de sintaxis para el minimapa
 */
class MinimapSyntaxAnalyzer(
    private val language: String,
    private val colorScheme: MinimapColorScheme
) {
    
    fun analyzeCode(text: String): List<MinimapToken> {
        return when (language.lowercase()) {
            "kotlin" -> analyzeKotlinCode(text)
            "java" -> analyzeJavaCode(text)
            "javascript", "js" -> analyzeJavaScriptCode(text)
            "python" -> analyzePythonCode(text)
            "xml", "html" -> analyzeXmlCode(text)
            else -> analyzeGenericCode(text)
        }
    }
    
    private fun analyzeKotlinCode(text: String): List<MinimapToken> {
        val tokens = mutableListOf<MinimapToken>()
        val lines = text.lines()
        
        val keywords = setOf(
            "fun", "class", "interface", "object", "val", "var", "if", "else", "when", "for", "while",
            "try", "catch", "finally", "return", "break", "continue", "import", "package", "private",
            "protected", "internal", "public", "abstract", "final", "open", "override", "data",
            "sealed", "enum", "annotation", "companion", "init", "constructor", "this", "super"
        )
        
        lines.forEachIndexed { lineIndex, line ->
            analyzeLineForTokens(line, lineIndex, keywords, tokens)
        }
        
        return tokens
    }
    
    private fun analyzeJavaCode(text: String): List<MinimapToken> {
        val tokens = mutableListOf<MinimapToken>()
        val lines = text.lines()
        
        val keywords = setOf(
            "public", "private", "protected", "static", "final", "abstract", "class", "interface",
            "extends", "implements", "import", "package", "if", "else", "for", "while", "do",
            "switch", "case", "default", "try", "catch", "finally", "throw", "throws", "return",
            "break", "continue", "new", "this", "super", "null", "true", "false"
        )
        
        lines.forEachIndexed { lineIndex, line ->
            analyzeLineForTokens(line, lineIndex, keywords, tokens)
        }
        
        return tokens
    }
    
    private fun analyzeJavaScriptCode(text: String): List<MinimapToken> {
        val tokens = mutableListOf<MinimapToken>()
        val lines = text.lines()
        
        val keywords = setOf(
            "function", "var", "let", "const", "if", "else", "for", "while", "do", "switch",
            "case", "default", "try", "catch", "finally", "throw", "return", "break", "continue",
            "new", "this", "class", "extends", "import", "export", "from", "async", "await"
        )
        
        lines.forEachIndexed { lineIndex, line ->
            analyzeLineForTokens(line, lineIndex, keywords, tokens)
        }
        
        return tokens
    }
    
    private fun analyzePythonCode(text: String): List<MinimapToken> {
        val tokens = mutableListOf<MinimapToken>()
        val lines = text.lines()
        
        val keywords = setOf(
            "def", "class", "if", "elif", "else", "for", "while", "try", "except", "finally",
            "with", "as", "import", "from", "return", "break", "continue", "pass", "raise",
            "yield", "lambda", "and", "or", "not", "in", "is", "True", "False", "None"
        )
        
        lines.forEachIndexed { lineIndex, line ->
            analyzeLineForTokens(line, lineIndex, keywords, tokens)
        }
        
        return tokens
    }
    
    private fun analyzeXmlCode(text: String): List<MinimapToken> {
        val tokens = mutableListOf<MinimapToken>()
        val lines = text.lines()
        
        lines.forEachIndexed { lineIndex, line ->
            var currentPos = 0
            
            while (currentPos < line.length) {
                when {
                    line.startsWith("<!--", currentPos) -> {
                        // Comment
                        val endPos = line.indexOf("-->", currentPos + 4)
                        val end = if (endPos != -1) endPos + 3 else line.length
                        tokens.add(
                            MinimapToken(
                                start = currentPos,
                                end = end,
                                type = TokenType.COMMENT,
                                color = getColorForTokenType(TokenType.COMMENT),
                                line = lineIndex,
                                column = currentPos
                            )
                        )
                        currentPos = end
                    }
                    line[currentPos] == '<' -> {
                        // Tag
                        val endPos = line.indexOf('>', currentPos)
                        val end = if (endPos != -1) endPos + 1 else currentPos + 1
                        tokens.add(
                            MinimapToken(
                                start = currentPos,
                                end = end,
                                type = TokenType.KEYWORD,
                                color = getColorForTokenType(TokenType.KEYWORD),
                                line = lineIndex,
                                column = currentPos
                            )
                        )
                        currentPos = end
                    }
                    else -> {
                        currentPos++
                    }
                }
            }
        }
        
        return tokens
    }
    
    private fun analyzeGenericCode(text: String): List<MinimapToken> {
        val tokens = mutableListOf<MinimapToken>()
        val lines = text.lines()
        
        lines.forEachIndexed { lineIndex, line ->
            // Basic token recognition for generic code
            var currentPos = 0
            while (currentPos < line.length) {
                when {
                    line[currentPos].isWhitespace() -> {
                        currentPos++
                    }
                    line[currentPos].isDigit() -> {
                        // Number
                        val start = currentPos
                        while (currentPos < line.length && (line[currentPos].isDigit() || line[currentPos] == '.')) {
                            currentPos++
                        }
                        tokens.add(
                            MinimapToken(
                                start = start,
                                end = currentPos,
                                type = TokenType.NUMBER,
                                color = getColorForTokenType(TokenType.NUMBER),
                                line = lineIndex,
                                column = start
                            )
                        )
                    }
                    line[currentPos] in "\"'" -> {
                        // String
                        val quote = line[currentPos]
                        val start = currentPos
                        currentPos++
                        while (currentPos < line.length && line[currentPos] != quote) {
                            if (line[currentPos] == '\\') currentPos++ // Skip escaped characters
                            currentPos++
                        }
                        if (currentPos < line.length) currentPos++ // Skip closing quote
                        
                        tokens.add(
                            MinimapToken(
                                start = start,
                                end = currentPos,
                                type = TokenType.STRING,
                                color = getColorForTokenType(TokenType.STRING),
                                line = lineIndex,
                                column = start
                            )
                        )
                    }
                    else -> {
                        currentPos++
                    }
                }
            }
        }
        
        return tokens
    }
    
    private fun analyzeLineForTokens(
        line: String,
        lineIndex: Int,
        keywords: Set<String>,
        tokens: MutableList<MinimapToken>
    ) {
        var currentPos = 0
        
        while (currentPos < line.length) {
            when {
                line[currentPos].isWhitespace() -> {
                    currentPos++
                }
                
                line.startsWith("//", currentPos) || line.startsWith("#", currentPos) -> {
                    // Single line comment
                    tokens.add(
                        MinimapToken(
                            start = currentPos,
                            end = line.length,
                            type = TokenType.COMMENT,
                            color = getColorForTokenType(TokenType.COMMENT),
                            line = lineIndex,
                            column = currentPos
                        )
                    )
                    break
                }
                
                line.startsWith("/*", currentPos) -> {
                    // Multi-line comment start
                    val endPos = line.indexOf("*/", currentPos + 2)
                    val end = if (endPos != -1) endPos + 2 else line.length
                    tokens.add(
                        MinimapToken(
                            start = currentPos,
                            end = end,
                            type = TokenType.COMMENT,
                            color = getColorForTokenType(TokenType.COMMENT),
                            line = lineIndex,
                            column = currentPos
                        )
                    )
                    currentPos = end
                }
                
                line[currentPos] in "\"'" -> {
                    // String literal
                    val quote = line[currentPos]
                    val start = currentPos
                    currentPos++
                    while (currentPos < line.length && line[currentPos] != quote) {
                        if (line[currentPos] == '\\') currentPos++ // Skip escaped characters
                        currentPos++
                    }
                    if (currentPos < line.length) currentPos++ // Skip closing quote
                    
                    tokens.add(
                        MinimapToken(
                            start = start,
                            end = currentPos,
                            type = TokenType.STRING,
                            color = getColorForTokenType(TokenType.STRING),
                            line = lineIndex,
                            column = start
                        )
                    )
                }
                
                line[currentPos].isDigit() -> {
                    // Number
                    val start = currentPos
                    while (currentPos < line.length && (line[currentPos].isDigit() || line[currentPos] == '.')) {
                        currentPos++
                    }
                    tokens.add(
                        MinimapToken(
                            start = start,
                            end = currentPos,
                            type = TokenType.NUMBER,
                            color = getColorForTokenType(TokenType.NUMBER),
                            line = lineIndex,
                            column = start
                        )
                    )
                }
                
                line[currentPos].isLetter() || line[currentPos] == '_' -> {
                    // Identifier or keyword
                    val start = currentPos
                    while (currentPos < line.length && (line[currentPos].isLetterOrDigit() || line[currentPos] == '_')) {
                        currentPos++
                    }
                    
                    val word = line.substring(start, currentPos)
                    val tokenType = if (keywords.contains(word)) TokenType.KEYWORD else TokenType.IDENTIFIER
                    
                    tokens.add(
                        MinimapToken(
                            start = start,
                            end = currentPos,
                            type = tokenType,
                            color = getColorForTokenType(tokenType),
                            line = lineIndex,
                            column = start
                        )
                    )
                }
                
                else -> {
                    // Operator or punctuation
                    val start = currentPos
                    currentPos++
                    tokens.add(
                        MinimapToken(
                            start = start,
                            end = currentPos,
                            type = TokenType.OPERATOR,
                            color = getColorForTokenType(TokenType.OPERATOR),
                            line = lineIndex,
                            column = start
                        )
                    )
                }
            }
        }
    }
    
    private fun getColorForTokenType(tokenType: TokenType): Color {
        return when (colorScheme) {
            MinimapColorScheme.LIGHT -> getLightThemeColor(tokenType)
            MinimapColorScheme.DARK -> getDarkThemeColor(tokenType)
            MinimapColorScheme.HIGH_CONTRAST -> getHighContrastColor(tokenType)
            MinimapColorScheme.MONOCHROME -> getMonochromeColor(tokenType)
            MinimapColorScheme.AUTO -> getDarkThemeColor(tokenType) // Default to dark theme
        }
    }
    
    private fun getLightThemeColor(tokenType: TokenType): Color {
        return when (tokenType) {
            TokenType.KEYWORD -> Color(0xFF0000FF)        // Blue
            TokenType.STRING -> Color(0xFF008000)         // Green
            TokenType.COMMENT -> Color(0xFF808080)        // Gray
            TokenType.NUMBER -> Color(0xFF800080)         // Purple
            TokenType.IDENTIFIER -> Color(0xFF000000)     // Black
            TokenType.OPERATOR -> Color(0xFF800000)       // Maroon
            TokenType.PUNCTUATION -> Color(0xFF000000)    // Black
            TokenType.WHITESPACE -> Color.Transparent
            TokenType.ERROR -> Color(0xFFFF0000)          // Red
        }
    }
    
    private fun getDarkThemeColor(tokenType: TokenType): Color {
        return when (tokenType) {
            TokenType.KEYWORD -> Color(0xFF569CD6)        // Light Blue
            TokenType.STRING -> Color(0xFFCE9178)         // Light Orange
            TokenType.COMMENT -> Color(0xFF6A9955)        // Green
            TokenType.NUMBER -> Color(0xFFB5CEA8)         // Light Green
            TokenType.IDENTIFIER -> Color(0xFFD4D4D4)     // Light Gray
            TokenType.OPERATOR -> Color(0xFFD4D4D4)       // Light Gray
            TokenType.PUNCTUATION -> Color(0xFFD4D4D4)    // Light Gray
            TokenType.WHITESPACE -> Color.Transparent
            TokenType.ERROR -> Color(0xFFF44747)          // Light Red
        }
    }
    
    private fun getHighContrastColor(tokenType: TokenType): Color {
        return when (tokenType) {
            TokenType.KEYWORD -> Color(0xFF00FFFF)        // Cyan
            TokenType.STRING -> Color(0xFF00FF00)         // Bright Green
            TokenType.COMMENT -> Color(0xFF808080)        // Gray
            TokenType.NUMBER -> Color(0xFFFF00FF)         // Magenta
            TokenType.IDENTIFIER -> Color(0xFFFFFFFF)     // White
            TokenType.OPERATOR -> Color(0xFFFFFF00)       // Yellow
            TokenType.PUNCTUATION -> Color(0xFFFFFFFF)    // White
            TokenType.WHITESPACE -> Color.Transparent
            TokenType.ERROR -> Color(0xFFFF0000)          // Red
        }
    }
    
    private fun getMonochromeColor(tokenType: TokenType): Color {
        return when (tokenType) {
            TokenType.KEYWORD -> Color(0xFF000000)        // Black
            TokenType.STRING -> Color(0xFF404040)         // Dark Gray
            TokenType.COMMENT -> Color(0xFF808080)        // Gray
            TokenType.NUMBER -> Color(0xFF202020)         // Very Dark Gray
            TokenType.IDENTIFIER -> Color(0xFF606060)     // Medium Gray
            TokenType.OPERATOR -> Color(0xFF000000)       // Black
            TokenType.PUNCTUATION -> Color(0xFF000000)    // Black
            TokenType.WHITESPACE -> Color.Transparent
            TokenType.ERROR -> Color(0xFF000000)          // Black
        }
    }
}

/**
 * Componente principal del minimapa
 */
@Composable
fun EditorMinimap(
    text: String,
    language: String,
    state: MinimapState,
    onLineClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val config by state.config.collectAsState()
    val isVisible by state.isVisible.collectAsState()
    val tokens by state.tokens.collectAsState()
    val markers by state.markers.collectAsState()
    val viewport by state.viewport.collectAsState()
    
    // Update tokens when text or config changes
    LaunchedEffect(text, language, config.colorScheme) {
        val analyzer = MinimapSyntaxAnalyzer(language, config.colorScheme)
        val newTokens = analyzer.analyzeCode(text)
        state.updateTokens(newTokens)
    }
    
    if (!isVisible) return
    
    Card(
        modifier = modifier
            .width(config.width.dp)
            .fillMaxHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Minimap header
            MinimapHeader(
                state = state,
                onToggleVisibility = { state.setVisible(!isVisible) }
            )
            
            // Minimap content
            Box(modifier = Modifier.weight(1f)) {
                MinimapCanvas(
                    tokens = tokens,
                    markers = markers,
                    viewport = viewport,
                    config = config,
                    onLineClick = onLineClick,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Scrollbar indicator
                if (config.showScrollbar) {
                    MinimapScrollIndicator(
                        viewport = viewport,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimapHeader(
    state: MinimapState,
    onToggleVisibility: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Minimapa",
            style = MaterialTheme.typography.labelMedium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Settings button
            IconButton(
                onClick = { /* Open settings */ },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Configuración",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Hide button
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = "Ocultar",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MinimapCanvas(
    tokens: List<MinimapToken>,
    markers: List<MinimapMarker>,
    viewport: EditorViewport,
    config: MinimapConfig,
    onLineClick: (Int) -> Unit,
    modifier: Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val line = (offset.y / (config.scale * 20f)).toInt()
                    onLineClick(line)
                }
            }
    ) {
        drawMinimapContent(
            tokens = tokens,
            markers = markers,
            viewport = viewport,
            config = config,
            canvasSize = size
        )
    }
}

private fun DrawScope.drawMinimapContent(
    tokens: List<MinimapToken>,
    markers: List<MinimapMarker>,
    viewport: EditorViewport,
    config: MinimapConfig,
    canvasSize: Size
) {
    val lineHeight = config.scale * 20f
    val charWidth = config.scale * 8f
    
    // Draw background
    drawRect(
        color = Color(0xFF1E1E1E),
        size = canvasSize
    )
    
    // Draw syntax highlighted content
    if (config.showSyntaxHighlighting) {
        drawSyntaxTokens(tokens, lineHeight, charWidth, config)
    }
    
    // Draw markers
    drawMarkers(markers, lineHeight, canvasSize.width, config)
    
    // Draw viewport indicator
    if (config.showViewport) {
        drawViewportIndicator(viewport, lineHeight, canvasSize, config)
    }
    
    // Draw folded regions
    if (config.showFoldedRegions) {
        drawFoldedRegions(markers.filter { it.type == MarkerType.FOLDED_REGION }, lineHeight, canvasSize.width)
    }
}

private fun DrawScope.drawSyntaxTokens(
    tokens: List<MinimapToken>,
    lineHeight: Float,
    charWidth: Float,
    _config: MinimapConfig
) {
    tokens.forEach { token ->
        val y = token.line * lineHeight
        val x = token.column * charWidth
        val width = (token.end - token.start) * charWidth
        
        if (width > 0 && token.color != Color.Transparent) {
            drawRect(
                color = token.color.copy(alpha = 0.8f),
                topLeft = Offset(x, y),
                size = Size(width, lineHeight * 0.8f)
            )
        }
    }
}

private fun DrawScope.drawMarkers(
    markers: List<MinimapMarker>,
    lineHeight: Float,
    canvasWidth: Float,
    _config: MinimapConfig
) {
    markers.forEach { marker ->
        val y = marker.line * lineHeight
        val markerSize = when (marker.type) {
            MarkerType.ERROR, MarkerType.WARNING -> 4f
            MarkerType.INFO -> 3f
            MarkerType.SEARCH_RESULT -> 2f
            MarkerType.BREAKPOINT -> 6f
            MarkerType.BOOKMARK -> 3f
            MarkerType.FOLDED_REGION -> 8f
        }
        
        when (marker.type) {
            MarkerType.ERROR, MarkerType.WARNING -> {
                // Draw error/warning as circle on the right edge
                drawCircle(
                    color = marker.color,
                    radius = markerSize,
                    center = Offset(canvasWidth - 8f, y + lineHeight / 2)
                )
            }

            MarkerType.INFO -> {
                // Draw info as circle on the right edge
                drawCircle(
                    color = marker.color,
                    radius = markerSize,
                    center = Offset(canvasWidth - 8f, y + lineHeight / 2)
                )
            }

            MarkerType.SEARCH_RESULT -> {
                // Draw search result as rectangle across the line
                drawRect(
                    color = marker.color.copy(alpha = 0.6f),
                    topLeft = Offset(0f, y),
                    size = Size(canvasWidth, lineHeight)
                )
            }
            
            MarkerType.BREAKPOINT -> {
                // Draw breakpoint as filled circle on the left edge
                drawCircle(
                    color = marker.color,
                    radius = markerSize,
                    center = Offset(8f, y + lineHeight / 2)
                )
            }
            
            MarkerType.BOOKMARK -> {
                // Draw bookmark as triangle
                val center = Offset(canvasWidth - 12f, y + lineHeight / 2)
                // Simplified triangle drawing
                drawRect(
                    color = marker.color,
                    topLeft = Offset(center.x - markerSize, center.y - markerSize / 2),
                    size = Size(markerSize * 2, markerSize)
                )
            }
            
            MarkerType.FOLDED_REGION -> {
                // Draw folded region as a line
                drawRect(
                    color = marker.color.copy(alpha = 0.5f),
                    topLeft = Offset(0f, y + lineHeight / 2 - 1f),
                    size = Size(canvasWidth, 2f)
                )
            }
        }
    }
}

private fun DrawScope.drawViewportIndicator(
    viewport: EditorViewport,
    lineHeight: Float,
    canvasSize: Size,
    _config: MinimapConfig
) {
    val startY = viewport.startLine * lineHeight
    val endY = viewport.endLine * lineHeight + lineHeight
    val height = endY - startY
    
    // Draw viewport background
    drawRect(
        color = Color(0x33FFFFFF),
        topLeft = Offset(0f, startY),
        size = Size(canvasSize.width, height)
    )
    
    // Draw viewport border
    drawRect(
        color = Color(0xFF007ACC),
        topLeft = Offset(0f, startY),
        size = Size(canvasSize.width, 2f) // Top border
    )
    
    drawRect(
        color = Color(0xFF007ACC),
        topLeft = Offset(0f, endY - 2f),
        size = Size(canvasSize.width, 2f) // Bottom border
    )
}

private fun DrawScope.drawFoldedRegions(
    foldedMarkers: List<MinimapMarker>,
    lineHeight: Float,
    canvasWidth: Float
) {
    foldedMarkers.forEach { marker ->
        val y = marker.line * lineHeight
        
        // Draw folded region indicator
        drawRect(
            color = Color(0xFFFFFFFF).copy(alpha = 0.3f),
            topLeft = Offset(canvasWidth - 16f, y + lineHeight / 4),
            size = Size(12f, lineHeight / 2)
        )
        
        // Draw plus sign
        val centerX = canvasWidth - 10f
        val centerY = y + lineHeight / 2
        
        // Horizontal line
        drawRect(
            color = Color(0xFF000000),
            topLeft = Offset(centerX - 3f, centerY - 0.5f),
            size = Size(6f, 1f)
        )
        
        // Vertical line
        drawRect(
            color = Color(0xFF000000),
            topLeft = Offset(centerX - 0.5f, centerY - 3f),
            size = Size(1f, 6f)
        )
    }
}

@Composable
private fun MinimapScrollIndicator(
    viewport: EditorViewport,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .background(
                Color(0x44000000),
                RoundedCornerShape(4.dp)
            )
    ) {
        // Scroll thumb
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(viewport.visibleLineCount.toFloat() / viewport.totalLines)
                .align(Alignment.TopStart)
                .offset(y = (viewport.progressRatio * 100).dp) // Approximate positioning
                .background(
                    Color(0xFF007ACC),
                    RoundedCornerShape(4.dp)
                )
        )
    }
}

/**
 * Panel de configuración del minimapa
 */
@Composable
fun MinimapConfigPanel(
    config: MinimapConfig,
    onConfigChange: (MinimapConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Configuración del Minimapa",
                style = MaterialTheme.typography.titleMedium
            )
            
            HorizontalDivider()
            
            // Width
            Text(
                text = "Ancho: ${config.width.toInt()}px",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = config.width,
                onValueChange = { onConfigChange(config.copy(width = it)) },
                valueRange = 80f..200f,
                steps = 12
            )
            
            // Scale
            Text(
                text = "Escala: ${(config.scale * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = config.scale,
                onValueChange = { onConfigChange(config.copy(scale = it)) },
                valueRange = 0.05f..0.3f,
                steps = 25
            )
            
            // Quality
            var expandedQuality by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedQuality,
                onExpandedChange = { expandedQuality = it }
            ) {
                PocketTextField(
                    value = config.renderQuality.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = "Calidad de renderizado",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedQuality) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedQuality,
                    onDismissRequest = { expandedQuality = false }
                ) {
                    RenderQuality.values().forEach { quality ->
                        DropdownMenuItem(
                            text = { Text(quality.displayName) },
                            onClick = {
                                onConfigChange(config.copy(renderQuality = quality))
                                expandedQuality = false
                            }
                        )
                    }
                }
            }
            
            // Refresh rate
            var expandedRefreshRate by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedRefreshRate,
                onExpandedChange = { expandedRefreshRate = it }
            ) {
                PocketTextField(
                    value = config.refreshRate.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = "Frecuencia de actualización",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRefreshRate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedRefreshRate,
                    onDismissRequest = { expandedRefreshRate = false }
                ) {
                    RefreshRate.values().forEach { rate ->
                        DropdownMenuItem(
                            text = { Text(rate.displayName) },
                            onClick = {
                                onConfigChange(config.copy(refreshRate = rate))
                                expandedRefreshRate = false
                            }
                        )
                    }
                }
            }
            
            // Color scheme
            var expandedColorScheme by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedColorScheme,
                onExpandedChange = { expandedColorScheme = it }
            ) {
                PocketTextField(
                    value = config.colorScheme.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = "Esquema de colores",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColorScheme) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedColorScheme,
                    onDismissRequest = { expandedColorScheme = false }
                ) {
                    MinimapColorScheme.values().forEach { scheme ->
                        DropdownMenuItem(
                            text = { Text(scheme.displayName) },
                            onClick = {
                                onConfigChange(config.copy(colorScheme = scheme))
                                expandedColorScheme = false
                            }
                        )
                    }
                }
            }
            
            // Boolean options
            Text(
                text = "Opciones de visualización",
                style = MaterialTheme.typography.titleSmall
            )
            
            MinimapOption(
                title = "Resaltado de sintaxis",
                description = "Muestra colores de sintaxis en el minimapa",
                checked = config.showSyntaxHighlighting,
                onCheckedChange = { onConfigChange(config.copy(showSyntaxHighlighting = it)) }
            )
            
            MinimapOption(
                title = "Números de línea",
                description = "Muestra números de línea en el minimapa",
                checked = config.showLineNumbers,
                onCheckedChange = { onConfigChange(config.copy(showLineNumbers = it)) }
            )
            
            MinimapOption(
                title = "Indicador de ventana",
                description = "Muestra la ventana visible del editor principal",
                checked = config.showViewport,
                onCheckedChange = { onConfigChange(config.copy(showViewport = it)) }
            )
            
            MinimapOption(
                title = "Barra de desplazamiento",
                description = "Muestra barra de desplazamiento en el minimapa",
                checked = config.showScrollbar,
                onCheckedChange = { onConfigChange(config.copy(showScrollbar = it)) }
            )
            
            MinimapOption(
                title = "Marcadores de errores",
                description = "Muestra errores y advertencias en el minimapa",
                checked = config.showErrorMarkers,
                onCheckedChange = { onConfigChange(config.copy(showErrorMarkers = it)) }
            )
            
            MinimapOption(
                title = "Resultados de búsqueda",
                description = "Resalta resultados de búsqueda en el minimapa",
                checked = config.showSearchHighlights,
                onCheckedChange = { onConfigChange(config.copy(showSearchHighlights = it)) }
            )
            
            MinimapOption(
                title = "Regiones plegadas",
                description = "Muestra indicadores de código plegado",
                checked = config.showFoldedRegions,
                onCheckedChange = { onConfigChange(config.copy(showFoldedRegions = it)) }
            )
        }
    }
}

@Composable
private fun MinimapOption(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Composable para recordar el estado del minimapa
 */
@Composable
fun rememberMinimapState(): MinimapState {
    return remember { MinimapState() }
}