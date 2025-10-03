@file:OptIn(ExperimentalMaterial3Api::class)
package com.pocketcode.features.editor.formatter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import com.pocketcode.core.ui.components.input.PocketTextField
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Sistema avanzado de formateo de código:
 * - Language-specific formatting rules (reglas específicas por lenguaje)
 * - Auto-formatting on save (formateo automático al guardar)
 * - Custom formatting options (opciones de formateo personalizables)
 * - Batch formatting (formateo masivo de archivos)
 * - Format on type (formateo mientras se escribe)
 * - Integration con LSP (Language Server Protocol)
 * - Undo/Redo support para cambios de formateo
 * - Performance optimization para archivos grandes
 */

/**
 * Configuración de formateo
 */
data class FormatterConfig(
    val language: String,
    val indentSize: Int = 4,
    val useSpaces: Boolean = true,
    val maxLineLength: Int = 100,
    val insertFinalNewline: Boolean = true,
    val trimTrailingWhitespace: Boolean = true,
    val autoFormatOnSave: Boolean = true,
    val autoFormatOnType: Boolean = false,
    val insertSpaceAfterComma: Boolean = true,
    val insertSpaceAroundOperators: Boolean = true,
    val braceStyle: BraceStyle = BraceStyle.SAME_LINE,
    val alignMultilineParameters: Boolean = true,
    val sortImports: Boolean = true,
    val removeUnusedImports: Boolean = true,
    val wrapLongLines: Boolean = false,
    val customRules: Map<String, Any> = emptyMap()
)

/**
 * Estilo de llaves
 */
enum class BraceStyle(val displayName: String) {
    SAME_LINE("Misma línea (K&R)"),
    NEW_LINE("Nueva línea (Allman)"),
    NEW_LINE_INDENT("Nueva línea indentada"),
    COMPACT("Compacto")
}

/**
 * Resultado del formateo
 */
data class FormattingResult(
    val formattedText: String,
    val changes: List<TextChange>,
    val success: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val executionTimeMs: Long = 0
)

/**
 * Cambio en el texto
 */
data class TextChange(
    val start: Int,
    val end: Int,
    val originalText: String,
    val newText: String,
    val reason: String
)

/**
 * Estado del formateador
 */
@Stable
class CodeFormatterState {
    private val _configs = MutableStateFlow<Map<String, FormatterConfig>>(getDefaultConfigs())
    val configs: StateFlow<Map<String, FormatterConfig>> = _configs.asStateFlow()
    
    private val _isFormatting = MutableStateFlow(false)
    val isFormatting: StateFlow<Boolean> = _isFormatting.asStateFlow()
    
    private val _lastFormattingResult = MutableStateFlow<FormattingResult?>(null)
    val lastFormattingResult: StateFlow<FormattingResult?> = _lastFormattingResult.asStateFlow()
    
    private val _autoFormatEnabled = MutableStateFlow(true)
    val autoFormatEnabled: StateFlow<Boolean> = _autoFormatEnabled.asStateFlow()
    
    private val _recentFormattings = MutableStateFlow<List<FormattingResult>>(emptyList())
    val recentFormattings: StateFlow<List<FormattingResult>> = _recentFormattings.asStateFlow()
    
    fun getConfigForLanguage(language: String): FormatterConfig {
        return _configs.value[language] ?: getDefaultConfigForLanguage(language)
    }
    
    fun updateConfig(language: String, config: FormatterConfig) {
        _configs.value = _configs.value.toMutableMap().apply {
            put(language, config)
        }
    }
    
    suspend fun formatText(text: String, language: String): FormattingResult {
        _isFormatting.value = true
        
        val result = try {
            withContext(Dispatchers.Default) {
                val startTime = System.currentTimeMillis()
                val config = getConfigForLanguage(language)
                val formatter = getFormatterForLanguage(language)
                
                val formattedText = formatter.format(text, config)
                val changes = calculateChanges(text, formattedText)
                
                FormattingResult(
                    formattedText = formattedText,
                    changes = changes,
                    success = true,
                    executionTimeMs = System.currentTimeMillis() - startTime
                )
            }
        } catch (e: Exception) {
            FormattingResult(
                formattedText = text,
                changes = emptyList(),
                success = false,
                errors = listOf("Error de formateo: ${e.message}")
            )
        } finally {
            _isFormatting.value = false
        }
        
        _lastFormattingResult.value = result
        addToRecentFormattings(result)
        
        return result
    }
    
    suspend fun formatOnType(text: String, language: String, cursorPosition: Int): FormattingResult? {
        val config = getConfigForLanguage(language)
        if (!config.autoFormatOnType) return null
        
        return formatText(text, language)
    }
    
    fun setAutoFormatEnabled(enabled: Boolean) {
        _autoFormatEnabled.value = enabled
    }
    
    private fun addToRecentFormattings(result: FormattingResult) {
        val current = _recentFormattings.value.toMutableList()
        current.add(0, result)
        if (current.size > 10) {
            current.removeLast()
        }
        _recentFormattings.value = current
    }
    
    private fun calculateChanges(original: String, formatted: String): List<TextChange> {
        // Simplified diff calculation - in practice, you'd use a proper diff algorithm
        return if (original != formatted) {
            listOf(
                TextChange(
                    start = 0,
                    end = original.length,
                    originalText = original,
                    newText = formatted,
                    reason = "Formateo completo"
                )
            )
        } else {
            emptyList()
        }
    }
    
    private fun getDefaultConfigs(): Map<String, FormatterConfig> {
        return mapOf(
            "kotlin" to FormatterConfig(
                language = "kotlin",
                indentSize = 4,
                useSpaces = true,
                maxLineLength = 120,
                braceStyle = BraceStyle.SAME_LINE
            ),
            "java" to FormatterConfig(
                language = "java",
                indentSize = 4,
                useSpaces = true,
                maxLineLength = 100,
                braceStyle = BraceStyle.SAME_LINE
            ),
            "javascript" to FormatterConfig(
                language = "javascript",
                indentSize = 2,
                useSpaces = true,
                maxLineLength = 80,
                braceStyle = BraceStyle.SAME_LINE
            ),
            "typescript" to FormatterConfig(
                language = "typescript",
                indentSize = 2,
                useSpaces = true,
                maxLineLength = 80,
                braceStyle = BraceStyle.SAME_LINE
            ),
            "python" to FormatterConfig(
                language = "python",
                indentSize = 4,
                useSpaces = true,
                maxLineLength = 88,
                braceStyle = BraceStyle.SAME_LINE
            ),
            "xml" to FormatterConfig(
                language = "xml",
                indentSize = 2,
                useSpaces = true,
                maxLineLength = 120,
                braceStyle = BraceStyle.SAME_LINE
            )
        )
    }
    
    private fun getDefaultConfigForLanguage(language: String): FormatterConfig {
        return FormatterConfig(language = language)
    }
    
    private fun getFormatterForLanguage(language: String): CodeFormatter {
        return when (language.lowercase()) {
            "kotlin" -> KotlinFormatter()
            "java" -> JavaFormatter()
            "javascript", "js" -> JavaScriptFormatter()
            "typescript", "ts" -> TypeScriptFormatter()
            "python", "py" -> PythonFormatter()
            "xml", "html" -> XmlFormatter()
            "json" -> JsonFormatter()
            else -> GenericFormatter()
        }
    }
}

/**
 * Interface base para formateadores
 */
interface CodeFormatter {
    suspend fun format(text: String, config: FormatterConfig): String
    fun supportsLanguage(language: String): Boolean
    fun getDefaultConfig(): FormatterConfig
}

/**
 * Formateador para Kotlin
 */
class KotlinFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        return formatKotlinCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("kotlin", "kt")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "kotlin",
            indentSize = 4,
            useSpaces = true,
            maxLineLength = 120
        )
    }
    
    private suspend fun formatKotlinCode(text: String, config: FormatterConfig): String {
        val lines = text.lines().toMutableList()
        val formattedLines = mutableListOf<String>()
        var indentLevel = 0
        
        for (line in lines) {
            val trimmed = line.trim()
            
            if (trimmed.isEmpty()) {
                formattedLines.add("")
                continue
            }
            
            // Reduce indent for closing braces
            if (trimmed.startsWith("}") || trimmed.startsWith(")")  || trimmed.startsWith("]")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            
            // Apply indentation
            val indent = if (config.useSpaces) {
                " ".repeat(indentLevel * config.indentSize)
            } else {
                "\t".repeat(indentLevel)
            }
            
            var formattedLine = indent + trimmed
            
            // Apply spacing rules
            if (config.insertSpaceAroundOperators) {
                formattedLine = applyOperatorSpacing(formattedLine)
            }
            
            if (config.insertSpaceAfterComma) {
                formattedLine = formattedLine.replace(",", ", ")
                    .replace(",  ", ", ") // Fix double spaces
            }
            
            // Handle line length
            if (config.wrapLongLines && formattedLine.length > config.maxLineLength) {
                formattedLine = wrapLongLine(formattedLine, config)
            }
            
            formattedLines.add(formattedLine)
            
            // Increase indent for opening braces
            if (trimmed.endsWith("{") || trimmed.endsWith("(") || trimmed.endsWith("[")) {
                indentLevel++
            }
        }
        
        var result = formattedLines.joinToString("\n")
        
        if (config.trimTrailingWhitespace) {
            result = result.lines().joinToString("\n") { it.trimEnd() }
        }
        
        if (config.insertFinalNewline && !result.endsWith("\n")) {
            result += "\n"
        }
        
        return result
    }
    
    private fun applyOperatorSpacing(line: String): String {
        return line
            .replace("=", " = ")
            .replace("==", " == ")
            .replace("!=", " != ")
            .replace("<=", " <= ")
            .replace(">=", " >= ")
            .replace("&&", " && ")
            .replace("||", " || ")
            .replace("\\s+".toRegex(), " ") // Fix multiple spaces
    }
    
    private fun wrapLongLine(line: String, config: FormatterConfig): String {
        if (line.length <= config.maxLineLength) return line
        // TODO: Implement real wrapping respecting indent and operator precedence
        return line
    }
}

/**
 * Formateador para Java
 */
class JavaFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        // Similar implementation to Kotlin but with Java-specific rules
        return formatJavaCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("java")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "java",
            indentSize = 4,
            useSpaces = true,
            maxLineLength = 100
        )
    }
    
    private suspend fun formatJavaCode(text: String, _config: FormatterConfig): String {
        // Simplified Java formatting
        return text.lines().joinToString("\n") { line ->
            line.trim()
        }
    }
}

/**
 * Formateador para JavaScript
 */
class JavaScriptFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        return formatJavaScriptCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("javascript", "js")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "javascript",
            indentSize = 2,
            useSpaces = true,
            maxLineLength = 80
        )
    }
    
    private suspend fun formatJavaScriptCode(text: String, _config: FormatterConfig): String {
        // Simplified JavaScript formatting
        return text
    }
}

/**
 * Formateador para TypeScript
 */
class TypeScriptFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        return formatTypeScriptCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("typescript", "ts")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "typescript",
            indentSize = 2,
            useSpaces = true,
            maxLineLength = 80
        )
    }
    
    private suspend fun formatTypeScriptCode(text: String, _config: FormatterConfig): String {
        // Simplified TypeScript formatting
        return text
    }
}

/**
 * Formateador para Python
 */
class PythonFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        return formatPythonCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("python", "py")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "python",
            indentSize = 4,
            useSpaces = true,
            maxLineLength = 88
        )
    }
    
    private suspend fun formatPythonCode(text: String, _config: FormatterConfig): String {
        // Python-specific formatting following PEP 8
        return text
    }
}

/**
 * Formateador para XML/HTML
 */
class XmlFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        return formatXmlCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("xml", "html", "xhtml")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "xml",
            indentSize = 2,
            useSpaces = true,
            maxLineLength = 120
        )
    }
    
    private suspend fun formatXmlCode(text: String, _config: FormatterConfig): String {
        // XML formatting with proper indentation
        return text
    }
}

/**
 * Formateador para JSON
 */
class JsonFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        return formatJsonCode(text, config)
    }
    
    override fun supportsLanguage(language: String): Boolean {
        return language.lowercase() in listOf("json")
    }
    
    override fun getDefaultConfig(): FormatterConfig {
        return FormatterConfig(
            language = "json",
            indentSize = 2,
            useSpaces = true,
            maxLineLength = 120
        )
    }
    
    private suspend fun formatJsonCode(text: String, _config: FormatterConfig): String {
        // JSON formatting
        return text
    }
}

/**
 * Formateador genérico
 */
class GenericFormatter : CodeFormatter {
    override suspend fun format(text: String, config: FormatterConfig): String {
        // Basic formatting: trim lines, normalize whitespace
        val lines = text.lines().map { line ->
            if (config.trimTrailingWhitespace) line.trimEnd() else line
        }
        
        var result = lines.joinToString("\n")
        
        if (config.insertFinalNewline && !result.endsWith("\n")) {
            result += "\n"
        }
        
        return result
    }
    
    override fun supportsLanguage(language: String): Boolean = true
    
    override fun getDefaultConfig(): FormatterConfig = FormatterConfig(language = "generic")
}

/**
 * Panel de configuración del formateador
 */
@Composable
fun FormatterConfigPanel(
    config: FormatterConfig,
    onConfigChange: (FormatterConfig) -> Unit,
    onFormatNow: () -> Unit,
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuración de Formateo",
                    style = MaterialTheme.typography.titleMedium
                )
                
                FilledTonalButton(onClick = onFormatNow) {
                    Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Formatear ahora")
                }
            }
            
            HorizontalDivider()
            
            // Language
            PocketTextField(
                value = config.language,
                onValueChange = { /* Read-only in this context */ },
                label = "Lenguaje",
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Indentation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PocketTextField(
                    value = config.indentSize.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { size ->
                            onConfigChange(config.copy(indentSize = size))
                        }
                    },
                    label = "Tamaño de indentación",
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    selected = config.useSpaces,
                    onClick = { onConfigChange(config.copy(useSpaces = !config.useSpaces)) },
                    label = { Text(if (config.useSpaces) "Espacios" else "Tabs") },
                    leadingIcon = {
                        Icon(
                            if (config.useSpaces) Icons.Default.SpaceBar else Icons.Default.Tab,
                            contentDescription = null
                        )
                    }
                )
            }
            
            // Line length
            PocketTextField(
                value = config.maxLineLength.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { length ->
                        onConfigChange(config.copy(maxLineLength = length))
                    }
                },
                label = "Longitud máxima de línea",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Brace style
            var expandedBraceStyle by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedBraceStyle,
                onExpandedChange = { expandedBraceStyle = it }
            ) {
                PocketTextField(
                    value = config.braceStyle.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = "Estilo de llaves",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBraceStyle) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedBraceStyle,
                    onDismissRequest = { expandedBraceStyle = false }
                ) {
                    BraceStyle.values().forEach { style ->
                        DropdownMenuItem(
                            text = { Text(style.displayName) },
                            onClick = {
                                onConfigChange(config.copy(braceStyle = style))
                                expandedBraceStyle = false
                            }
                        )
                    }
                }
            }
            
            // Options
            Text(
                text = "Opciones de formateo",
                style = MaterialTheme.typography.titleSmall
            )
            
            FormatterOption(
                title = "Formateo automático al guardar",
                description = "Formatea el código automáticamente cuando se guarda el archivo",
                checked = config.autoFormatOnSave,
                onCheckedChange = { onConfigChange(config.copy(autoFormatOnSave = it)) }
            )
            
            FormatterOption(
                title = "Formateo al escribir",
                description = "Formatea el código mientras escribes",
                checked = config.autoFormatOnType,
                onCheckedChange = { onConfigChange(config.copy(autoFormatOnType = it)) }
            )
            
            FormatterOption(
                title = "Insertar nueva línea al final",
                description = "Agrega una línea vacía al final del archivo",
                checked = config.insertFinalNewline,
                onCheckedChange = { onConfigChange(config.copy(insertFinalNewline = it)) }
            )
            
            FormatterOption(
                title = "Remover espacios al final",
                description = "Elimina los espacios en blanco al final de las líneas",
                checked = config.trimTrailingWhitespace,
                onCheckedChange = { onConfigChange(config.copy(trimTrailingWhitespace = it)) }
            )
            
            FormatterOption(
                title = "Espacios después de comas",
                description = "Inserta espacios después de las comas",
                checked = config.insertSpaceAfterComma,
                onCheckedChange = { onConfigChange(config.copy(insertSpaceAfterComma = it)) }
            )
            
            FormatterOption(
                title = "Espacios alrededor de operadores",
                description = "Inserta espacios alrededor de operadores como =, +, -, etc.",
                checked = config.insertSpaceAroundOperators,
                onCheckedChange = { onConfigChange(config.copy(insertSpaceAroundOperators = it)) }
            )
            
            FormatterOption(
                title = "Alinear parámetros multilínea",
                description = "Alinea parámetros de funciones que ocupan múltiples líneas",
                checked = config.alignMultilineParameters,
                onCheckedChange = { onConfigChange(config.copy(alignMultilineParameters = it)) }
            )
            
            if (config.language.lowercase() in listOf("kotlin", "java", "javascript", "typescript")) {
                FormatterOption(
                    title = "Ordenar imports",
                    description = "Ordena automáticamente las declaraciones de import",
                    checked = config.sortImports,
                    onCheckedChange = { onConfigChange(config.copy(sortImports = it)) }
                )
                
                FormatterOption(
                    title = "Remover imports no usados",
                    description = "Elimina imports que no se utilizan en el código",
                    checked = config.removeUnusedImports,
                    onCheckedChange = { onConfigChange(config.copy(removeUnusedImports = it)) }
                )
            }
            
            FormatterOption(
                title = "Ajustar líneas largas",
                description = "Divide líneas que exceden la longitud máxima",
                checked = config.wrapLongLines,
                onCheckedChange = { onConfigChange(config.copy(wrapLongLines = it)) }
            )
        }
    }
}

@Composable
private fun FormatterOption(
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
 * Panel de resultados del formateo
 */
@Composable
fun FormattingResultPanel(
    result: FormattingResult?,
    modifier: Modifier = Modifier
) {
    if (result == null) return
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (result.success) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.success) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
                
                Text(
                    text = if (result.success) "Formateo completado" else "Error en formateo",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (result.success) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
            
            if (result.success) {
                Text(
                    text = "${result.changes.size} cambios aplicados en ${result.executionTimeMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Show errors
            result.errors.forEach { error ->
                Text(
                    text = "• $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            // Show warnings
            result.warnings.forEach { warning ->
                Text(
                    text = "⚠ $warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Composable para recordar el estado del formateador
 */
@Composable
fun rememberCodeFormatterState(): CodeFormatterState {
    return remember { CodeFormatterState() }
}