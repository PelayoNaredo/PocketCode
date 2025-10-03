package com.pocketcode.core.ui.components.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonSize
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.ComponentTokens.ShapeTokens
import com.pocketcode.core.ui.tokens.SpacingTokens

@Composable
fun PocketButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val actualEnabled = enabled && !loading
    val colors = resolveButtonColors(variant)
    val contentPadding = PaddingValues(horizontal = size.horizontalPadding, vertical = 0.dp)
    val textStyle = size.textStyle

    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (leadingIcon != null || trailingIcon != null || loading) {
                Arrangement.spacedBy(SpacingTokens.small)
            } else {
                Arrangement.Center
            }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .semantics { contentDescription = "pocket_button_loading" },
                    color = colors.content,
                    strokeWidth = 2.dp
                )
            } else {
                leadingIcon?.let { icon ->
                    CompositionLocalProvider(LocalContentColor provides colors.content) {
                        icon()
                    }
                }
            }

            Text(
                text = text,
                style = textStyle,
                color = colors.content
            )

            if (!loading) {
                trailingIcon?.let { icon ->
                    CompositionLocalProvider(LocalContentColor provides colors.content) {
                        icon()
                    }
                }
            }
        }
    }

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.heightIn(min = size.height),
                enabled = actualEnabled,
                contentPadding = contentPadding,
                shape = ShapeTokens.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                content()
            }
        }

        ButtonVariant.Secondary -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier.heightIn(min = size.height),
                enabled = actualEnabled,
                contentPadding = contentPadding,
                shape = ShapeTokens.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                content()
            }
        }

        ButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.heightIn(min = size.height),
                enabled = actualEnabled,
                contentPadding = contentPadding,
                shape = ShapeTokens.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContentColor = colors.disabledContent
                ),
                border = BorderStroke(width = 1.dp, color = colors.border ?: colors.content)
            ) {
                content()
            }
        }

        ButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.heightIn(min = size.height),
                enabled = actualEnabled,
                contentPadding = contentPadding,
                shape = ShapeTokens.medium,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.content,
                    disabledContentColor = colors.disabledContent,
                    containerColor = colors.container
                )
            ) {
                content()
            }
        }

        ButtonVariant.Danger -> {
            Button(
                onClick = onClick,
                modifier = modifier.heightIn(min = size.height),
                enabled = actualEnabled,
                contentPadding = contentPadding,
                shape = ShapeTokens.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                content()
            }
        }
    }
}

private data class PocketButtonColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
    val border: Color? = null
)

@Composable
private fun resolveButtonColors(variant: ButtonVariant): PocketButtonColors {
    return when (variant) {
        ButtonVariant.Primary -> PocketButtonColors(
            container = ColorTokens.primary,
            content = ColorTokens.onPrimary,
            disabledContainer = ColorTokens.primary.copy(alpha = 0.38f),
            disabledContent = ColorTokens.onPrimary.copy(alpha = 0.6f)
        )

        ButtonVariant.Secondary -> PocketButtonColors(
            container = ColorTokens.primaryContainer,
            content = ColorTokens.onPrimaryContainer,
            disabledContainer = ColorTokens.primaryContainer.copy(alpha = 0.5f),
            disabledContent = ColorTokens.onPrimaryContainer.copy(alpha = 0.6f)
        )

        ButtonVariant.Outline -> PocketButtonColors(
            container = Color.Transparent,
            content = ColorTokens.primary,
            disabledContainer = Color.Transparent,
            disabledContent = ColorTokens.primary.copy(alpha = 0.4f),
            border = ColorTokens.primary
        )

        ButtonVariant.Text -> PocketButtonColors(
            container = Color.Transparent,
            content = ColorTokens.primary,
            disabledContainer = Color.Transparent,
            disabledContent = ColorTokens.primary.copy(alpha = 0.4f)
        )

        ButtonVariant.Danger -> PocketButtonColors(
            container = ColorTokens.Semantic.danger500,
            content = ColorTokens.onError,
            disabledContainer = ColorTokens.Semantic.danger500.copy(alpha = 0.45f),
            disabledContent = ColorTokens.onError.copy(alpha = 0.7f)
        )
    }
}
