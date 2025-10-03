package com.pocketcode.features.editor.completion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Sistema avanzado de autocompletado de código:
 * - Autocompletado básico de palabras
 * - Context-aware suggestions (sugerencias contextuales)
 * - Snippet support (fragmentos de código)
 * - Custom completion providers (proveedores personalizados)
 * - LSP integration foundation (base para integración LSP)
 * - Fuzzy matching para búsqueda inteligente
 */

/**
 * Tipos de completion disponibles
 */
enum class CompletionItemType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    VARIABLE("Variable", Icons.Default.Code, Color(0xFF4CAF50)),
    FUNCTION("Function", Icons.Default.Functions, Color(0xFF2196F3)),
    CLASS("Class", Icons.Default.Class, Color(0xFF9C27B0)),
    METHOD("Method", Icons.Default.Extension, Color(0xFF00BCD4)),
    PROPERTY("Property", Icons.Default.Settings, Color(0xFF795548)),
    KEYWORD("Keyword", Icons.Default.Key, Color(0xFFE91E63)),
    SNIPPET("Snippet", Icons.Default.ContentPaste, Color(0xFFFF9800)),
    CONSTANT("Constant", Icons.Default.Lock, Color(0xFF607D8B)),
    INTERFACE("Interface", Icons.Default.Api, Color(0xFF8BC34A)),
    ENUM("Enum", Icons.AutoMirrored.Filled.List, Color(0xFFCDDC39))
}

/**
 * Item de completion con información detallada
 */
data class CompletionItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val insertText: String = label,
    val type: CompletionItemType,
    val detail: String? = null,
    val documentation: String? = null,
    val sortText: String = label,
    val filterText: String = label,
    val priority: Int = 0,
    val snippet: CodeSnippet? = null,
    val additionalTextEdits: List<TextEdit> = emptyList()
)

/**
 * Snippet de código con placeholders
 */
data class CodeSnippet(
    val template: String,
    val placeholders: List<SnippetPlaceholder> = emptyList(),
    val tabStops: List<Int> = emptyList()
)

data class SnippetPlaceholder(
    val id: Int,
    val text: String,
    val startIndex: Int,
    val endIndex: Int,
    val choices: List<String> = emptyList()
)

/**
 * Edit de texto para modificaciones adicionales
 */
data class TextEdit(
    val range: TextRange,
    val newText: String
)

data class TextRange(
    val start: Int,
    val end: Int
)

/**
 * Proveedor de completion items
 */
interface CompletionProvider {
    suspend fun provideCompletions(
        text: String,
        cursorPosition: Int,
        context: CompletionContext
    ): List<CompletionItem>
    
    fun canProvideCompletions(context: CompletionContext): Boolean
    fun getPriority(): Int
}

/**
 * Contexto para completion
 */
data class CompletionContext(
    val language: String,
    val fileName: String,
    val lineText: String,
    val wordUnderCursor: String,
    val previousWord: String? = null,
    val indentLevel: Int = 0,
    val isInString: Boolean = false,
    val isInComment: Boolean = false
)

/**
 * Componente principal de completion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeCompletionPopup(
    visible: Boolean,
    items: List<CompletionItem>,
    selectedIndex: Int,
    onItemSelected: (CompletionItem) -> Unit,
    onDismiss: () -> Unit,
    offset: DpOffset = DpOffset.Zero,
    maxHeight: androidx.compose.ui.unit.Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    if (!visible || items.isEmpty()) return

    val listState = rememberLazyListState()
    
    // Scroll to selected item
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0 && selectedIndex < items.size) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    DropdownMenu(
        expanded = visible,
        onDismissRequest = onDismiss,
        offset = offset,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = false
        ),
        modifier = modifier
            .heightIn(max = maxHeight)
            .widthIn(min = 300.dp, max = 500.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(items) { index, item ->
                CompletionItemRow(
                    item = item,
                    isSelected = index == selectedIndex,
                    onClick = { onItemSelected(item) }
                )
            }
        }
    }
}

@Composable
private fun CompletionItemRow(
    item: CompletionItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Type icon
        Icon(
            imageVector = item.type.icon,
            contentDescription = item.type.displayName,
            tint = item.type.color,
            modifier = Modifier.size(16.dp)
        )
        
        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Label
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Detail
            item.detail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Type badge
        Badge(
            containerColor = item.type.color.copy(alpha = 0.2f),
            contentColor = item.type.color
        ) {
            Text(
                text = item.type.displayName.first().toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * Engine de completion que maneja múltiples providers
 */
class CodeCompletionEngine {
    private val providers = mutableListOf<CompletionProvider>()
    
    fun addProvider(provider: CompletionProvider) {
        providers.add(provider)
        providers.sortByDescending { it.getPriority() }
    }
    
    fun removeProvider(provider: CompletionProvider) {
        providers.remove(provider)
    }
    
    suspend fun getCompletions(
        text: String,
        cursorPosition: Int,
        context: CompletionContext
    ): List<CompletionItem> {
        val allCompletions = mutableListOf<CompletionItem>()
        
        providers.forEach { provider ->
            if (provider.canProvideCompletions(context)) {
                try {
                    val completions = provider.provideCompletions(text, cursorPosition, context)
                    allCompletions.addAll(completions)
                } catch (e: Exception) {
                    // Log error but continue with other providers
                }
            }
        }
        
        return allCompletions
            .sortedWith(compareBy({ -it.priority }, { it.sortText }))
            .distinctBy { it.label }
    }
}

/**
 * Provider básico de palabras
 */
class BasicWordCompletionProvider : CompletionProvider {
    override suspend fun provideCompletions(
        text: String,
        cursorPosition: Int,
        context: CompletionContext
    ): List<CompletionItem> {
        val words = extractWords(text)
        val currentWord = context.wordUnderCursor.lowercase()
        
        return words
            .filter { word ->
                word.lowercase().startsWith(currentWord) && 
                word.lowercase() != currentWord
            }
            .map { word ->
                CompletionItem(
                    label = word,
                    type = CompletionItemType.VARIABLE,
                    priority = calculateWordPriority(word, currentWord)
                )
            }
            .distinctBy { it.label }
            .take(20)
    }
    
    override fun canProvideCompletions(context: CompletionContext): Boolean {
        return context.wordUnderCursor.length >= 2 && 
               !context.isInString && 
               !context.isInComment
    }
    
    override fun getPriority(): Int = 0
    
    private fun extractWords(text: String): List<String> {
        return text.split(Regex("\\W+"))
            .filter { it.isNotBlank() && it.length > 2 }
            .distinct()
    }
    
    private fun calculateWordPriority(word: String, currentWord: String): Int {
        return when {
            word.startsWith(currentWord, ignoreCase = true) -> 10
            word.contains(currentWord, ignoreCase = true) -> 5
            else -> 1
        }
    }
}

/**
 * Provider de keywords específico del lenguaje
 */
class LanguageKeywordProvider(
    private val language: String,
    private val keywords: List<String>
) : CompletionProvider {
    
    override suspend fun provideCompletions(
        text: String,
        cursorPosition: Int,
        context: CompletionContext
    ): List<CompletionItem> {
        val currentWord = context.wordUnderCursor.lowercase()
        
        return keywords
            .filter { keyword ->
                keyword.lowercase().startsWith(currentWord) &&
                keyword.lowercase() != currentWord
            }
            .map { keyword ->
                CompletionItem(
                    label = keyword,
                    type = CompletionItemType.KEYWORD,
                    priority = 15,
                    detail = "Keyword"
                )
            }
    }
    
    override fun canProvideCompletions(context: CompletionContext): Boolean {
        return context.language.equals(language, ignoreCase = true) &&
               context.wordUnderCursor.isNotEmpty() &&
               !context.isInString &&
               !context.isInComment
    }
    
    override fun getPriority(): Int = 10
}

/**
 * Provider de snippets
 */
class SnippetCompletionProvider(
    private val snippets: List<SnippetDefinition>
) : CompletionProvider {
    
    override suspend fun provideCompletions(
        text: String,
        cursorPosition: Int,
        context: CompletionContext
    ): List<CompletionItem> {
        val currentWord = context.wordUnderCursor.lowercase()
        
        return snippets
            .filter { snippet ->
                snippet.trigger.lowercase().startsWith(currentWord) &&
                snippet.language.equals(context.language, ignoreCase = true)
            }
            .map { snippet ->
                CompletionItem(
                    label = snippet.trigger,
                    insertText = snippet.body,
                    type = CompletionItemType.SNIPPET,
                    detail = snippet.description,
                    documentation = snippet.documentation,
                    priority = 20,
                    snippet = CodeSnippet(
                        template = snippet.body,
                        placeholders = snippet.placeholders,
                        tabStops = snippet.tabStops
                    )
                )
            }
    }
    
    override fun canProvideCompletions(context: CompletionContext): Boolean {
        return context.wordUnderCursor.isNotEmpty() &&
               !context.isInString &&
               !context.isInComment
    }
    
    override fun getPriority(): Int = 20
}

/**
 * Definición de snippet
 */
data class SnippetDefinition(
    val trigger: String,
    val body: String,
    val description: String,
    val language: String,
    val documentation: String? = null,
    val placeholders: List<SnippetPlaceholder> = emptyList(),
    val tabStops: List<Int> = emptyList()
)

/**
 * Fuzzy matcher para búsqueda inteligente
 */
object FuzzyMatcher {
    fun matches(query: String, target: String): Boolean {
        if (query.isEmpty()) return true
        if (target.isEmpty()) return false
        
        val queryLower = query.lowercase()
        val targetLower = target.lowercase()
        
        var queryIndex = 0
        
        for (char in targetLower) {
            if (queryIndex < queryLower.length && char == queryLower[queryIndex]) {
                queryIndex++
            }
        }
        
        return queryIndex == queryLower.length
    }
    
    fun score(query: String, target: String): Int {
        if (!matches(query, target)) return 0
        
        val queryLower = query.lowercase()
        val targetLower = target.lowercase()
        
        var score = 0
        var queryIndex = 0
        var lastMatchIndex = -1
        
        for (i in targetLower.indices) {
            if (queryIndex < queryLower.length && targetLower[i] == queryLower[queryIndex]) {
                score += if (i == lastMatchIndex + 1) 10 else 1
                lastMatchIndex = i
                queryIndex++
            }
        }
        
        // Bonus for exact prefix match
        if (targetLower.startsWith(queryLower)) {
            score += 100
        }
        
        // Penalty for longer targets
        score -= target.length
        
        return score
    }
}

/**
 * Snippets predefinidos para Kotlin
 */
object KotlinSnippets {
    val snippets = listOf(
        SnippetDefinition(
            trigger = "fun",
            body = "fun \${1:functionName}(\${2:parameters}): \${3:ReturnType} {\n    \${4:// implementation}\n    return \${5:defaultValue}\n}",
            description = "Function declaration",
            language = "kotlin"
        ),
        SnippetDefinition(
            trigger = "class",
            body = "class \${1:ClassName}(\${2:parameters}) {\n    \${3:// implementation}\n}",
            description = "Class declaration",
            language = "kotlin"
        ),
        SnippetDefinition(
            trigger = "if",
            body = "if (\${1:condition}) {\n    \${2:// implementation}\n}",
            description = "If statement",
            language = "kotlin"
        ),
        SnippetDefinition(
            trigger = "for",
            body = "for (\${1:item} in \${2:collection}) {\n    \${3:// implementation}\n}",
            description = "For loop",
            language = "kotlin"
        ),
        SnippetDefinition(
            trigger = "when",
            body = "when (\${1:expression}) {\n    \${2:condition} -> \${3:result}\n    else -> \${4:defaultResult}\n}",
            description = "When expression",
            language = "kotlin"
        )
    )
}

/**
 * Keywords para diferentes lenguajes
 */
object LanguageKeywords {
    val kotlin = listOf(
        "abstract", "actual", "annotation", "as", "break", "by", "catch", "class",
        "companion", "const", "constructor", "continue", "crossinline", "data",
        "delegate", "do", "dynamic", "else", "enum", "expect", "external", "false",
        "final", "finally", "for", "fun", "get", "if", "import", "in", "infix",
        "init", "inline", "inner", "interface", "internal", "is", "lateinit",
        "null", "object", "open", "operator", "out", "override", "package",
        "private", "protected", "public", "reified", "return", "sealed", "set",
        "super", "suspend", "tailrec", "this", "throw", "true", "try", "typealias",
        "typeof", "val", "var", "vararg", "when", "where", "while"
    )
    
    val java = listOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
        "class", "const", "continue", "default", "do", "double", "else", "enum",
        "extends", "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native", "new",
        "package", "private", "protected", "public", "return", "short", "static",
        "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
        "transient", "try", "void", "volatile", "while"
    )
}

/**
 * Factory para crear completion engine configurado
 */
object CompletionEngineFactory {
    fun createForLanguage(language: String): CodeCompletionEngine {
        val engine = CodeCompletionEngine()
        
        // Add basic word completion
        engine.addProvider(BasicWordCompletionProvider())
        
        // Add language-specific keywords
        when (language.lowercase()) {
            "kotlin" -> {
                engine.addProvider(LanguageKeywordProvider("kotlin", LanguageKeywords.kotlin))
                engine.addProvider(SnippetCompletionProvider(KotlinSnippets.snippets))
            }
            "java" -> {
                engine.addProvider(LanguageKeywordProvider("java", LanguageKeywords.java))
            }
        }
        
        return engine
    }
}