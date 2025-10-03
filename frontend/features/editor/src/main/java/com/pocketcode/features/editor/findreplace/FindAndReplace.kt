package com.pocketcode.features.editor.findreplace

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import com.pocketcode.core.ui.components.input.PocketTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Sistema avanzado de búsqueda y reemplazo:
 * - Regex support (soporte de expresiones regulares)
 * - Case sensitivity options (opciones de sensibilidad a mayúsculas)
 * - Replace all functionality (funcionalidad de reemplazar todo)
 * - Search history (historial de búsquedas)
 * - Cross-file search (búsqueda entre archivos) - base para futuro
 * - Highlight matches (resaltar coincidencias)
 * - Navigation entre matches
 */

/**
 * Configuración de búsqueda
 */
data class SearchOptions(
    val caseSensitive: Boolean = false,
    val wholeWord: Boolean = false,
    val useRegex: Boolean = false,
    val searchInSelection: Boolean = false,
    val preserveCase: Boolean = false
)

/**
 * Resultado de búsqueda
 */
data class SearchResult(
    val startIndex: Int,
    val endIndex: Int,
    val matchText: String,
    val lineNumber: Int,
    val columnNumber: Int,
    val contextBefore: String = "",
    val contextAfter: String = ""
)

/**
 * Estado del Find & Replace
 */
@Stable
class FindReplaceState {
    var isVisible by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var replaceText by mutableStateOf("")
    var currentMatchIndex by mutableStateOf(-1)
    var searchResults by mutableStateOf<List<SearchResult>>(emptyList())
    var searchOptions by mutableStateOf(SearchOptions())
    var searchHistory by mutableStateOf<List<String>>(emptyList())
    var replaceHistory by mutableStateOf<List<String>>(emptyList())
    var isReplaceMode by mutableStateOf(false)
    
    val totalMatches: Int get() = searchResults.size
    val hasMatches: Boolean get() = searchResults.isNotEmpty()
    val currentMatch: SearchResult? get() = 
        if (currentMatchIndex >= 0 && currentMatchIndex < searchResults.size) {
            searchResults[currentMatchIndex]
        } else null
    
    fun show(replaceMode: Boolean = false) {
        isVisible = true
        isReplaceMode = replaceMode
    }
    
    fun hide() {
        isVisible = false
        searchQuery = ""
        replaceText = ""
        searchResults = emptyList()
        currentMatchIndex = -1
    }
    
    fun nextMatch() {
        if (searchResults.isNotEmpty()) {
            currentMatchIndex = (currentMatchIndex + 1) % searchResults.size
        }
    }
    
    fun previousMatch() {
        if (searchResults.isNotEmpty()) {
            currentMatchIndex = if (currentMatchIndex <= 0) {
                searchResults.size - 1
            } else {
                currentMatchIndex - 1
            }
        }
    }
    
    fun addToSearchHistory(query: String) {
        if (query.isNotBlank() && !searchHistory.contains(query)) {
            searchHistory = (listOf(query) + searchHistory).take(20)
        }
    }
    
    fun addToReplaceHistory(text: String) {
        if (text.isNotBlank() && !replaceHistory.contains(text)) {
            replaceHistory = (listOf(text) + replaceHistory).take(20)
        }
    }
}

/**
 * Componente principal de Find & Replace
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FindReplacePanel(
    state: FindReplaceState,
    onSearch: (String, SearchOptions) -> Unit,
    onReplace: (SearchResult, String) -> Unit,
    onReplaceAll: (String, String, SearchOptions) -> Int,
    onNavigateToMatch: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchFocusRequester = remember { FocusRequester() }
    val replaceFocusRequester = remember { FocusRequester() }
    
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically() + expandVertically() + fadeIn(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        PocketCard(
            modifier = Modifier.fillMaxWidth(),
            variant = ComponentTokens.CardVariant.Elevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.medium),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
            ) {
                // Header
                FindReplaceHeader(
                    state = state,
                    onClose = { state.hide() },
                    onToggleReplace = { state.isReplaceMode = !state.isReplaceMode }
                )
                
                // Search input
                SearchInputSection(
                    state = state,
                    searchFocusRequester = searchFocusRequester,
                    onSearch = onSearch,
                    onNavigateToMatch = onNavigateToMatch
                )
                
                // Replace input (if in replace mode)
                if (state.isReplaceMode) {
                    ReplaceInputSection(
                        state = state,
                        replaceFocusRequester = replaceFocusRequester,
                        onReplace = onReplace,
                        onReplaceAll = onReplaceAll
                    )
                }
                
                // Options
                SearchOptionsSection(
                    options = state.searchOptions,
                    onOptionsChange = { options ->
                        state.searchOptions = options
                        if (state.searchQuery.isNotBlank()) {
                            onSearch(state.searchQuery, options)
                        }
                    }
                )
                
                // Results summary
                if (state.hasMatches) {
                    SearchResultsSummary(state = state)
                }
            }
        }
    }
    
    // Auto-focus search input when visible
    LaunchedEffect(state.isVisible) {
        if (state.isVisible) {
            searchFocusRequester.requestFocus()
        }
    }
}

@Composable
private fun FindReplaceHeader(
    state: FindReplaceState,
    onClose: () -> Unit,
    onToggleReplace: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (state.isReplaceMode) "Buscar y Reemplazar" else "Buscar",
            style = MaterialTheme.typography.titleMedium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall)
        ) {
            IconButton(
                onClick = onToggleReplace,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (state.isReplaceMode) Icons.Default.FindInPage else Icons.Default.FindReplace,
                    contentDescription = if (state.isReplaceMode) "Solo buscar" else "Buscar y reemplazar",
                    modifier = Modifier.size(16.dp)
                )
            }
            
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchInputSection(
    state: FindReplaceState,
    searchFocusRequester: FocusRequester,
    onSearch: (String, SearchOptions) -> Unit,
    onNavigateToMatch: (SearchResult) -> Unit
) {
    var showSearchHistory by remember { mutableStateOf(false) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        // Search input with history
        Box(modifier = Modifier.weight(1f)) {
            PocketTextField(
                value = state.searchQuery,
                onValueChange = { query ->
                    state.searchQuery = query
                    if (query.isNotBlank()) {
                        onSearch(query, state.searchOptions)
                    } else {
                        state.searchResults = emptyList()
                        state.currentMatchIndex = -1
                    }
                },
                label = "Buscar",
                placeholder = "Buscar...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    Row {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    state.searchQuery = ""
                                    state.searchResults = emptyList()
                                    state.currentMatchIndex = -1
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (state.searchHistory.isNotEmpty()) {
                            IconButton(
                                onClick = { showSearchHistory = !showSearchHistory },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = "Historial",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (state.searchQuery.isNotBlank()) {
                            state.addToSearchHistory(state.searchQuery)
                            onSearch(state.searchQuery, state.searchOptions)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester)
            )
            
            // Search history dropdown
            DropdownMenu(
                expanded = showSearchHistory,
                onDismissRequest = { showSearchHistory = false }
            ) {
                state.searchHistory.forEach { historyItem ->
                    DropdownMenuItem(
                        text = { Text(historyItem, fontFamily = FontFamily.Monospace) },
                        onClick = {
                            state.searchQuery = historyItem
                            onSearch(historyItem, state.searchOptions)
                            showSearchHistory = false
                        }
                    )
                }
            }
        }
        
        // Navigation buttons
        if (state.hasMatches) {
            NavigationButtons(
                state = state,
                onNavigateToMatch = onNavigateToMatch
            )
        }
    }
}

@Composable
private fun ReplaceInputSection(
    state: FindReplaceState,
    replaceFocusRequester: FocusRequester,
    onReplace: (SearchResult, String) -> Unit,
    onReplaceAll: (String, String, SearchOptions) -> Int
) {
    var showReplaceHistory by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        // Replace input with history
        Box(modifier = Modifier.weight(1f)) {
            PocketTextField(
                value = state.replaceText,
                onValueChange = { state.replaceText = it },
                label = "Reemplazar",
                placeholder = "Reemplazar con...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Reemplazar",
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (state.replaceHistory.isNotEmpty()) {
                        IconButton(
                            onClick = { showReplaceHistory = !showReplaceHistory },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = "Historial",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        state.currentMatch?.let { match ->
                            onReplace(match, state.replaceText)
                            state.addToReplaceHistory(state.replaceText)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(replaceFocusRequester)
            )
            
            // Replace history dropdown
            DropdownMenu(
                expanded = showReplaceHistory,
                onDismissRequest = { showReplaceHistory = false }
            ) {
                state.replaceHistory.forEach { historyItem ->
                    DropdownMenuItem(
                        text = { Text(historyItem, fontFamily = FontFamily.Monospace) },
                        onClick = {
                            state.replaceText = historyItem
                            showReplaceHistory = false
                        }
                    )
                }
            }
        }
        
        // Replace buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall)
        ) {
            // Replace current
            IconButton(
                onClick = {
                    state.currentMatch?.let { match ->
                        onReplace(match, state.replaceText)
                        state.addToReplaceHistory(state.replaceText)
                    }
                },
                enabled = state.currentMatch != null
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = "Reemplazar actual"
                )
            }
            
            // Replace all
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        onReplaceAll(state.searchQuery, state.replaceText, state.searchOptions)
                        state.addToReplaceHistory(state.replaceText)
                        // Show snackbar with result count
                    }
                },
                enabled = state.hasMatches && state.searchQuery.isNotBlank()
            ) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = "Reemplazar todo"
                )
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    state: FindReplaceState,
    onNavigateToMatch: (SearchResult) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Previous match
        IconButton(
            onClick = {
                state.previousMatch()
                state.currentMatch?.let(onNavigateToMatch)
            },
            enabled = state.hasMatches
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Anterior"
            )
        }
        
        // Next match
        IconButton(
            onClick = {
                state.nextMatch()
                state.currentMatch?.let(onNavigateToMatch)
            },
            enabled = state.hasMatches
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Siguiente"
            )
        }
    }
}

@Composable
private fun SearchOptionsSection(
    options: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        // Toggle button
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Opciones de búsqueda")
        }
        
        // Options content
        AnimatedVisibility(visible = expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Case sensitive
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = options.caseSensitive,
                            onCheckedChange = { 
                                onOptionsChange(options.copy(caseSensitive = it))
                            }
                        )
                        Text("Sensible a mayúsculas", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    // Whole word
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = options.wholeWord,
                            onCheckedChange = { 
                                onOptionsChange(options.copy(wholeWord = it))
                            }
                        )
                        Text("Palabra completa", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Use regex
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = options.useRegex,
                            onCheckedChange = { 
                                onOptionsChange(options.copy(useRegex = it))
                            }
                        )
                        Text("Expresiones regulares", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    // Preserve case in replace
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = options.preserveCase,
                            onCheckedChange = { 
                                onOptionsChange(options.copy(preserveCase = it))
                            }
                        )
                        Text("Preservar mayúsculas", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSummary(
    state: FindReplaceState
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.currentMatchIndex + 1} de ${state.totalMatches} coincidencias",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
                    if (state.totalMatches > 1) {
                        Text(
                            text = "Navegar con ↑ ↓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
        }
    }
}

/**
 * Motor de búsqueda
 */
class SearchEngine {
    
    fun search(
        text: String,
        query: String,
        options: SearchOptions
    ): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        
        return try {
            if (options.useRegex) {
                searchWithRegex(text, query, options)
            } else {
                searchPlainText(text, query, options)
            }
        } catch (e: PatternSyntaxException) {
            // Invalid regex, return empty results
            emptyList()
        }
    }
    
    private fun searchPlainText(
        text: String,
        query: String,
        options: SearchOptions
    ): List<SearchResult> {
        val searchText = if (options.caseSensitive) text else text.lowercase()
        val searchQuery = if (options.caseSensitive) query else query.lowercase()
        
        val results = mutableListOf<SearchResult>()
        var startIndex = 0
        
        while (true) {
            val index = searchText.indexOf(searchQuery, startIndex)
            if (index == -1) break
            
            // Check whole word constraint
            if (options.wholeWord && !isWholeWordMatch(text, index, query.length)) {
                startIndex = index + 1
                continue
            }
            
            val lineNumber = getLineNumber(text, index)
            val columnNumber = getColumnNumber(text, index)
            val matchText = text.substring(index, index + query.length)
            val (contextBefore, contextAfter) = getContext(text, index, query.length)
            
            results.add(
                SearchResult(
                    startIndex = index,
                    endIndex = index + query.length,
                    matchText = matchText,
                    lineNumber = lineNumber,
                    columnNumber = columnNumber,
                    contextBefore = contextBefore,
                    contextAfter = contextAfter
                )
            )
            
            startIndex = index + 1
        }
        
        return results
    }
    
    private fun searchWithRegex(
        text: String,
        pattern: String,
        options: SearchOptions
    ): List<SearchResult> {
        var flags = 0
        if (!options.caseSensitive) {
            flags = flags or Pattern.CASE_INSENSITIVE
        }
        
        val compiledPattern = Pattern.compile(pattern, flags)
        val matcher = compiledPattern.matcher(text)
        
        val results = mutableListOf<SearchResult>()
        
        while (matcher.find()) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            val matchText = matcher.group()
            val lineNumber = getLineNumber(text, startIndex)
            val columnNumber = getColumnNumber(text, startIndex)
            val (contextBefore, contextAfter) = getContext(text, startIndex, endIndex - startIndex)
            
            results.add(
                SearchResult(
                    startIndex = startIndex,
                    endIndex = endIndex,
                    matchText = matchText,
                    lineNumber = lineNumber,
                    columnNumber = columnNumber,
                    contextBefore = contextBefore,
                    contextAfter = contextAfter
                )
            )
        }
        
        return results
    }
    
    private fun isWholeWordMatch(text: String, index: Int, length: Int): Boolean {
        val beforeChar = if (index > 0) text[index - 1] else ' '
        val afterChar = if (index + length < text.length) text[index + length] else ' '
        
        return !beforeChar.isLetterOrDigit() && !afterChar.isLetterOrDigit()
    }
    
    private fun getLineNumber(text: String, index: Int): Int {
        return text.substring(0, index).count { it == '\n' } + 1
    }
    
    private fun getColumnNumber(text: String, index: Int): Int {
        val lineStart = text.lastIndexOf('\n', index - 1) + 1
        return index - lineStart + 1
    }
    
    private fun getContext(text: String, index: Int, length: Int): Pair<String, String> {
        val contextLength = 20
        
        val beforeStart = maxOf(0, index - contextLength)
        val beforeEnd = index
        val contextBefore = if (beforeStart < beforeEnd) {
            text.substring(beforeStart, beforeEnd)
        } else ""
        
        val afterStart = index + length
        val afterEnd = minOf(text.length, afterStart + contextLength)
        val contextAfter = if (afterStart < afterEnd) {
            text.substring(afterStart, afterEnd)
        } else ""
        
        return contextBefore to contextAfter
    }
}

/**
 * Función de utilidad para aplicar preservación de mayúsculas
 */
fun preserveCase(original: String, replacement: String): String {
    if (original.isEmpty() || replacement.isEmpty()) return replacement
    
    return when {
        original.all { it.isUpperCase() } -> replacement.uppercase()
        original.first().isUpperCase() && original.drop(1).all { it.isLowerCase() } -> 
            replacement.lowercase().replaceFirstChar { it.uppercase() }
        else -> replacement
    }
}