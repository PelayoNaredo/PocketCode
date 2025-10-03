package com.pocketcode.features.editor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.features.editor.domain.model.CodeLanguage
import kotlin.math.max
import kotlin.math.min

@Composable
fun EditorStatusBar(
    fileName: String,
    language: CodeLanguage,
    cursorPosition: Pair<Int, Int>,
    selectionLength: Int?,
    isModified: Boolean,
    encoding: String,
    lineEnding: String,
    tabSize: Int = 4,
    onEncodingClick: () -> Unit = {},
    onLineEndingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = ColorTokens.surfaceVariant,
        contentColor = ColorTokens.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.small, vertical = SpacingTokens.xsmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
        ) {
            val fileLabel = buildString {
                append(fileName)
                if (isModified) append(" *")
            }
            StatusBarItem(text = fileLabel)

            StatusBarDivider()
            StatusBarItem(text = language.displayName)

            StatusBarDivider()
            StatusBarItem(text = "Ln ${cursorPosition.first}, Col ${cursorPosition.second}")

            selectionLength?.let {
                StatusBarDivider()
                StatusBarItem(text = "Sel $it")
            }

            StatusBarDivider()
            StatusBarItem(text = "Tab Size: $tabSize")

            StatusBarDivider()
            StatusBarItem(text = encoding, onClick = onEncodingClick)

            StatusBarDivider()
            StatusBarItem(text = lineEnding, onClick = onLineEndingClick)
        }
    }
}

@Composable
private fun StatusBarItem(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TypographyTokens.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        ),
        color = ColorTokens.onSurfaceVariant,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun StatusBarDivider() {
    VerticalDivider(
        modifier = Modifier.height(12.dp),
        thickness = 1.dp,
        color = ColorTokens.outline.copy(alpha = 0.3f)
    )
}

@Composable
fun EditorMinimap(
    content: String,
    language: CodeLanguage,
    cursorPosition: Pair<Int, Int>,
    visibleRange: Pair<Int, Int>,
    onNavigateToLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lines = remember(content) { content.lines() }
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier,
        color = ColorTokens.surfaceVariant.copy(alpha = 0.85f),
        contentColor = ColorTokens.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Minimap (${language.displayName})",
                style = TypographyTokens.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.xsmall),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = ColorTokens.outline.copy(alpha = 0.3f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .clickable { /* Placeholder */ }
            ) {
                Column {
                    lines.forEachIndexed { index, line ->
                        MinimapLine(
                            lineNumber = index + 1,
                            content = line,
                            isCurrentLine = index + 1 == cursorPosition.first,
                            isVisible = index + 1 in visibleRange.first..visibleRange.second,
                            onLineClick = { onNavigateToLine(index + 1) }
                        )
                    }
                }

                VisibleAreaIndicator(
                    visibleRange = visibleRange,
                    totalLines = lines.size,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun MinimapLine(
    lineNumber: Int,
    content: String,
    isCurrentLine: Boolean,
    isVisible: Boolean,
    onLineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .semantics { contentDescription = "Línea $lineNumber" }
            .clickable { onLineClick() }
            .background(
                when {
                    isCurrentLine -> ColorTokens.primary.copy(alpha = 0.6f)
                    isVisible -> ColorTokens.primaryContainer.copy(alpha = 0.3f)
                    content.isNotBlank() -> ColorTokens.onSurfaceVariant.copy(alpha = 0.25f)
                    else -> Color.Transparent
                }
            )
    ) {
        if (content.isNotBlank()) {
            Box(
                modifier = Modifier
                    .width((min(content.length, 50) * 2).dp)
                    .height(1.dp)
                    .background(ColorTokens.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
private fun VisibleAreaIndicator(
    visibleRange: Pair<Int, Int>,
    totalLines: Int,
    modifier: Modifier = Modifier
) {
    if (totalLines <= 0) return

    val startRatio = visibleRange.first.toFloat() / totalLines.toFloat()
    val endRatio = visibleRange.second.toFloat() / totalLines.toFloat()
    val highlightColor = ColorTokens.primary.copy(alpha = 0.18f)

    Canvas(modifier = modifier) {
        val height = size.height
        val startY = startRatio * height
        val endY = endRatio * height

        drawRect(
            color = highlightColor,
            topLeft = Offset(0f, startY),
            size = Size(size.width, max(2f, endY - startY))
        )
    }
}

@Composable
fun EditorControls(
    state: EditorContainerState,
    onQuickAction: (EditorQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isFocused,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
    ) {
        QuickActionsPanel(onAction = onQuickAction)
    }
}

@Composable
private fun QuickActionsPanel(
    onAction: (EditorQuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ColorTokens.surfaceVariant,
            contentColor = ColorTokens.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.small),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall)
        ) {
            QuickActionButton(
                icon = Icons.Filled.AutoFixHigh,
                label = "Formatear",
                onClick = { onAction(EditorQuickAction.Format) }
            )
            QuickActionButton(
                icon = Icons.Filled.Comment,
                label = "Comentar",
                onClick = { onAction(EditorQuickAction.Comment) }
            )
            QuickActionButton(
                icon = Icons.Filled.Search,
                label = "Buscar",
                onClick = { onAction(EditorQuickAction.Find) }
            )
            QuickActionButton(
                icon = Icons.Filled.FindReplace,
                label = "Reemplazar",
                onClick = { onAction(EditorQuickAction.Replace) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = ColorTokens.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}