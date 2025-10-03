package com.pocketcode.core.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

@Composable
fun ErrorDisplay(
    error: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = ColorTokens.error
        )
        Text(
            text = error,
            style = TypographyTokens.Title.medium,
            fontWeight = FontWeight.SemiBold,
            color = ColorTokens.onSurface
        )
        supportingText?.let {
            Text(
                text = it,
                style = TypographyTokens.Body.medium,
                color = ColorTokens.onSurfaceVariant
            )
        }
        onRetry?.let { retry ->
            Button(onClick = retry) {
                Text(text = "Reintentar")
            }
        }
    }
}
