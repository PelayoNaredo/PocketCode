package com.pocketcode.core.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    text: String? = null,
    color: Color = ColorTokens.primary,
    useContentColor: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        CircularProgressIndicator(
            color = if (useContentColor) MaterialTheme.colorScheme.onSurface else color,
            modifier = Modifier.size(48.dp)
        )
        text?.let {
            Text(
                text = it,
                style = TypographyTokens.Body.medium,
                color = if (useContentColor) MaterialTheme.colorScheme.onSurface else ColorTokens.onSurface
            )
        }
    }
}

@Composable
fun SmallLoadingIndicator(
    modifier: Modifier = Modifier,
    text: String? = null,
    color: Color = ColorTokens.primary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 2.dp,
            modifier = Modifier.size(20.dp)
        )
        text?.let {
            Text(
                text = it,
                style = TypographyTokens.Label.medium,
                color = ColorTokens.onSurface
            )
        }
    }
}
