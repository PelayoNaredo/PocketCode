package com.pocketcode.core.ui.components.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

enum class PocketSnackbarStyle {
    Info,
    Success,
    Warning,
    Error
}

@Composable
private fun PocketSnackbarStyle.accentColor(): Color = when (this) {
    PocketSnackbarStyle.Info -> ColorTokens.info
    PocketSnackbarStyle.Success -> ColorTokens.Semantic.success500
    PocketSnackbarStyle.Warning -> ColorTokens.Semantic.warning500
    PocketSnackbarStyle.Error -> MaterialTheme.colorScheme.error
}

@Composable
private fun PocketSnackbarStyle.icon(): ImageVector = when (this) {
    PocketSnackbarStyle.Info -> Icons.Outlined.Info
    PocketSnackbarStyle.Success -> Icons.Outlined.CheckCircle
    PocketSnackbarStyle.Warning -> Icons.Outlined.WarningAmber
    PocketSnackbarStyle.Error -> Icons.Outlined.ErrorOutline
}

@Composable
fun PocketSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    style: (SnackbarData) -> PocketSnackbarStyle = { PocketSnackbarStyle.Info },
    supportingText: (SnackbarData) -> String? = { null }
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { data ->
        PocketSnackbar(
            data = data,
            style = style(data),
            supportingText = supportingText(data)
        )
    }
}

@Composable
fun PocketSnackbar(
    data: SnackbarData,
    style: PocketSnackbarStyle,
    supportingText: String? = null,
    modifier: Modifier = Modifier
) {
    val accentColor = style.accentColor()
    Surface(
        modifier = modifier
            .padding(horizontal = SpacingTokens.Semantic.screenPaddingHorizontal)
            .clip(RoundedCornerShape(16.dp)),
        tonalElevation = 6.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(SpacingTokens.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.medium)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor)
            )
            androidx.compose.material3.Icon(
                imageVector = style.icon(),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xsmall)
            ) {
                Text(
                    text = data.visuals.message,
                    style = TypographyTokens.Body.medium,
                    fontWeight = FontWeight.Medium,
                    color = ColorTokens.onSurface
                )
                supportingText?.takeIf { it.isNotBlank() }?.let { message ->
                    Text(
                        text = message,
                        style = TypographyTokens.Body.small,
                        color = ColorTokens.onSurfaceVariant
                    )
                }
            }
            data.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = data::performAction,
                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

