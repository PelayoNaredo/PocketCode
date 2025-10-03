package com.pocketcode.features.editor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.feedback.ErrorDisplay
import com.pocketcode.core.ui.components.feedback.LoadingIndicator
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import com.pocketcode.core.ui.components.feedback.SmallLoadingIndicator
import com.pocketcode.core.ui.components.selection.PocketFilterChip
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.LocalGlobalToastDispatcher
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.domain.project.model.ProjectFile
import com.pocketcode.features.editor.findreplace.FindReplacePanel
import com.pocketcode.features.editor.findreplace.FindReplaceState
import com.pocketcode.features.editor.domain.model.CodeLanguage
import com.pocketcode.features.editor.findreplace.SearchOptions
import com.pocketcode.features.editor.findreplace.SearchResult
import com.pocketcode.features.editor.ui.CodeEditorViewModel
import com.pocketcode.features.editor.ui.SaveResult
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

data class EditorContainerState(
    val isFocused: Boolean = false,
    val showLineNumbers: Boolean = true,
    val showMinimap: Boolean = false,
    val showFindReplace: Boolean = false,
    val cursorPosition: Pair<Int, Int> = 1 to 1,
    val selectionRange: IntRange? = null
)

data class EditorContainerConfig(
    val enableLineNumbers: Boolean = true,
    val enableMinimap: Boolean = false,
    val enableCodeFolding: Boolean = true,
    val enableSyntaxHighlighting: Boolean = true,
    val enableFindReplace: Boolean = true,
    val enableCodeFormatter: Boolean = true,
    val enableCodeCompletion: Boolean = false,
    val enableAutoSave: Boolean = true,
    val enableStatusBar: Boolean = true,
    val autoSaveDelayMs: Long = 2000L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorContainer(
    file: ProjectFile,
    selectedProjectId: String?,
    selectedProjectName: String?,
    selectedProjectPath: String?,
    modifier: Modifier = Modifier,
    config: EditorContainerConfig = EditorContainerConfig(),
    viewModel: CodeEditorViewModel = hiltViewModel(),
    onFileChanged: (ProjectFile) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onShowSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var containerConfig by remember { mutableStateOf(config) }
    var containerState by remember {
        mutableStateOf(
            EditorContainerState(
                showLineNumbers = config.enableLineNumbers,
                showMinimap = config.enableMinimap
            )
        )
    }

    val findReplaceState = remember { FindReplaceState() }
    val language = remember(file.name) { CodeLanguage.fromFileName(file.name) }
    val toastDispatcher = LocalGlobalToastDispatcher.current

    LaunchedEffect(selectedProjectId, selectedProjectName, selectedProjectPath) {
        if (selectedProjectId != null && selectedProjectName != null) {
            viewModel.setProject(selectedProjectId, selectedProjectName, selectedProjectPath)
        }
    }

    LaunchedEffect(file) {
        onFileChanged(file)
        viewModel.loadFile(file)
    }

    LaunchedEffect(containerConfig.enableAutoSave, uiState.content, uiState.isModified) {
        if (containerConfig.enableAutoSave && uiState.isModified) {
            delay(containerConfig.autoSaveDelayMs)
            viewModel.saveFile()
        }
    }

    LaunchedEffect(findReplaceState.isVisible) {
        containerState = containerState.copy(showFindReplace = findReplaceState.isVisible)
    }

    LaunchedEffect(containerConfig.enableLineNumbers) {
        containerState = containerState.copy(showLineNumbers = containerConfig.enableLineNumbers)
    }

    LaunchedEffect(containerConfig.enableMinimap) {
        containerState = containerState.copy(showMinimap = containerConfig.enableMinimap)
    }

    LaunchedEffect(uiState.saveResult) {
        when (val result = uiState.saveResult) {
            SaveResult.Success -> {
                toastDispatcher.showMessage(
                    message = "Archivo guardado",
                    style = PocketToastStyle.Success,
                    origin = GlobalSnackbarOrigin.EDITOR
                )
                viewModel.clearSaveResult()
            }

            is SaveResult.Error -> {
                toastDispatcher.showMessage(
                    message = result.message,
                    style = PocketToastStyle.Error,
                    origin = GlobalSnackbarOrigin.EDITOR
                )
                viewModel.clearSaveResult()
            }

            null -> Unit
        }
    }

    val totalLines = remember(uiState.content) { max(uiState.content.lines().size, 1) }
    val cursorLine = containerState.cursorPosition.first.coerceIn(1, totalLines)
    val visibleWindow = 120
    val visibleRange = remember(totalLines, cursorLine) {
        val start = max(1, cursorLine - visibleWindow / 2)
        val end = min(totalLines, start + visibleWindow)
        start to end
    }

    val performSearch: (String, SearchOptions) -> Unit = remember(uiState.content) {
        { query, options ->
            val results = computeSearchResults(uiState.content, query, options)
            findReplaceState.searchResults = results
            findReplaceState.currentMatchIndex = if (results.isNotEmpty()) 0 else -1
            if (query.isNotBlank()) {
                findReplaceState.addToSearchHistory(query)
            }
        }
    }

    val replaceMatch: (SearchResult, String) -> Unit = { match, replacement ->
        val newContent = applyReplacement(
            content = uiState.content,
            result = match,
            replacement = replacement,
            preserveCase = findReplaceState.searchOptions.preserveCase
        )
        viewModel.updateContent(newContent)
        performSearch(findReplaceState.searchQuery, findReplaceState.searchOptions)
    }

    val replaceAll: (String, String, SearchOptions) -> Int = { query, replacement, options ->
        val results = computeSearchResults(uiState.content, query, options)
        if (results.isEmpty()) {
            0
        } else {
            val (updated, count) = applyReplaceAll(
                content = uiState.content,
                results = results,
                replacement = replacement,
                preserveCase = options.preserveCase
            )
            viewModel.updateContent(updated)
            performSearch(query, options)
            count
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingTokens.small, vertical = SpacingTokens.xsmall),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        EditorTopBar(
            file = file,
            projectName = selectedProjectName,
            isModified = uiState.isModified,
            canUndo = uiState.isModified,
            canRedo = false,
            config = containerConfig,
            onNavigateBack = onNavigateBack,
            onSave = viewModel::saveFile,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onToggleLineNumbers = { enabled ->
                containerConfig = containerConfig.copy(enableLineNumbers = enabled)
            },
            onToggleMinimap = { enabled ->
                containerConfig = containerConfig.copy(enableMinimap = enabled)
            },
            onShowSettings = onShowSettings,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.small),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            PocketFilterChip(
                selected = false,
                onClick = viewModel::formatCode,
                label = "Formato",
                leadingIcon = Icons.AutoMirrored.Filled.FormatAlignLeft
            )
            PocketFilterChip(
                selected = findReplaceState.isVisible && !findReplaceState.isReplaceMode,
                onClick = {
                    findReplaceState.show(replaceMode = false)
                    performSearch(findReplaceState.searchQuery, findReplaceState.searchOptions)
                },
                label = "Buscar",
                leadingIcon = Icons.Filled.Search
            )
            PocketFilterChip(
                selected = findReplaceState.isVisible && findReplaceState.isReplaceMode,
                onClick = {
                    findReplaceState.show(replaceMode = true)
                    performSearch(findReplaceState.searchQuery, findReplaceState.searchOptions)
                },
                label = "Reemplazar",
                leadingIcon = Icons.Filled.FindReplace
            )
        }

        val errorMessage = uiState.error

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(
                        text = "Cargando archivo…",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                errorMessage != null -> {
                    ErrorDisplay(
                        error = errorMessage,
                        onRetry = { viewModel.loadFile(file) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            EditorContent(
                                content = uiState.content,
                                language = language,
                                showLineNumbers = containerState.showLineNumbers,
                                config = containerConfig,
                                state = containerState,
                                onContentChanged = viewModel::updateContent,
                                onCursorPositionChanged = { line, column ->
                                    containerState = containerState.copy(cursorPosition = line to column)
                                },
                                onSelectionChanged = { start, end ->
                                    val safeRange = if (start != end) {
                                        if (start <= end) start..end else end..start
                                    } else {
                                        null
                                    }
                                    containerState = containerState.copy(selectionRange = safeRange)
                                },
                                onFocusChanged = { focused ->
                                    containerState = containerState.copy(isFocused = focused)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (containerState.showMinimap && containerConfig.enableMinimap) {
                            Spacer(modifier = Modifier.width(SpacingTokens.small))
                            EditorMinimap(
                                content = uiState.content,
                                language = language,
                                cursorPosition = containerState.cursorPosition,
                                visibleRange = visibleRange,
                                onNavigateToLine = viewModel::navigateToLine,
                                modifier = Modifier
                                    .width(120.dp)
                                    .fillMaxSize()
                            )
                        }
                    }

                    if (containerState.showFindReplace && containerConfig.enableFindReplace) {
                        FindReplacePanel(
                            state = findReplaceState,
                            onSearch = performSearch,
                            onReplace = replaceMatch,
                            onReplaceAll = replaceAll,
                            onNavigateToMatch = { match ->
                                containerState = containerState.copy(
                                    cursorPosition = match.lineNumber to match.columnNumber,
                                    selectionRange = match.startIndex..match.endIndex
                                )
                                viewModel.navigateToLine(match.lineNumber)
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(SpacingTokens.small)
                        )
                    }

                    if (uiState.isSaving) {
                        PocketCard(
                            variant = CardVariant.Elevated,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(SpacingTokens.small)
                        ) {
                            SmallLoadingIndicator(
                                modifier = Modifier.padding(
                                    horizontal = SpacingTokens.small,
                                    vertical = SpacingTokens.xsmall
                                ),
                                text = "Guardando cambios…"
                            )
                        }
                    }
                }
            }
        }

        if (containerConfig.enableStatusBar) {
            EditorStatusBar(
                fileName = file.name,
                language = language,
                cursorPosition = containerState.cursorPosition,
                selectionLength = containerState.selectionRange?.let { it.last - it.first },
                isModified = uiState.isModified,
                encoding = "UTF-8",
                lineEnding = "LF",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun computeSearchResults(
    content: String,
    query: String,
    options: SearchOptions
): List<SearchResult> {
    if (query.isBlank()) return emptyList()

    return if (options.useRegex) {
        val pattern = buildRegexPattern(query, options.wholeWord)
        runCatching {
            val regexOptions = if (options.caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
            pattern.toRegex(regexOptions).findAll(content).map { matchResult ->
                val start = matchResult.range.first
                val end = matchResult.range.last + 1
                val (line, column) = content.getLineAndColumn(start)
                SearchResult(
                    startIndex = start,
                    endIndex = end,
                    matchText = matchResult.value,
                    lineNumber = line,
                    columnNumber = column,
                    contextBefore = content.safeSubstring(max(0, start - 20), start),
                    contextAfter = content.safeSubstring(end, min(content.length, end + 20))
                )
            }.toList()
        }.getOrElse { emptyList() }
    } else {
        findPlainTextMatches(content, query, options)
    }
}

private fun buildRegexPattern(query: String, wholeWord: Boolean): String {
    return if (wholeWord) "\\b$query\\b" else query
}

private fun findPlainTextMatches(
    content: String,
    query: String,
    options: SearchOptions
): List<SearchResult> {
    val searchText = if (options.caseSensitive) content else content.lowercase()
    val searchQuery = if (options.caseSensitive) query else query.lowercase()
    val results = mutableListOf<SearchResult>()
    var index = searchText.indexOf(searchQuery)

    while (index >= 0) {
        val end = index + searchQuery.length
        if (!options.wholeWord || isWholeWordMatch(content, index, end)) {
            val (line, column) = content.getLineAndColumn(index)
            results += SearchResult(
                startIndex = index,
                endIndex = end,
                matchText = content.substring(index, end),
                lineNumber = line,
                columnNumber = column,
                contextBefore = content.safeSubstring(max(0, index - 20), index),
                contextAfter = content.safeSubstring(end, min(content.length, end + 20))
            )
        }
        index = searchText.indexOf(searchQuery, index + 1)
    }

    return results
}

private fun isWholeWordMatch(text: String, start: Int, end: Int): Boolean {
    val before = if (start > 0) text[start - 1] else ' '
    val after = if (end < text.length) text[end] else ' '
    return !before.isLetterOrDigit() && !after.isLetterOrDigit() && before != '_' && after != '_'
}

private fun applyReplacement(
    content: String,
    result: SearchResult,
    replacement: String,
    preserveCase: Boolean
): String {
    val adjusted = adjustReplacementCase(replacement, result.matchText, preserveCase)
    return buildString {
        append(content.substring(0, result.startIndex))
        append(adjusted)
        append(content.substring(result.endIndex))
    }
}

private fun applyReplaceAll(
    content: String,
    results: List<SearchResult>,
    replacement: String,
    preserveCase: Boolean
): Pair<String, Int> {
    if (results.isEmpty()) return content to 0

    val sortedResults = results.sortedBy { it.startIndex }
    val builder = StringBuilder()
    var lastIndex = 0

    sortedResults.forEach { result ->
        if (result.startIndex >= lastIndex) {
            builder.append(content.substring(lastIndex, result.startIndex))
            builder.append(adjustReplacementCase(replacement, result.matchText, preserveCase))
            lastIndex = result.endIndex
        }
    }

    if (lastIndex < content.length) {
        builder.append(content.substring(lastIndex))
    }

    return builder.toString() to sortedResults.size
}

private fun adjustReplacementCase(replacement: String, match: String, preserveCase: Boolean): String {
    if (!preserveCase || replacement.isEmpty()) return replacement

    return when {
        match.all { it.isUpperCase() } -> replacement.uppercase()
        match.all { it.isLowerCase() } -> replacement.lowercase()
        match.firstOrNull()?.isUpperCase() == true -> replacement.replaceFirstChar { it.uppercase() }
        else -> replacement
    }
}

private fun String.safeSubstring(start: Int, end: Int): String {
    if (isEmpty()) return ""
    val safeStart = start.coerceIn(0, length)
    val safeEnd = end.coerceIn(safeStart, length)
    return substring(safeStart, safeEnd)
}