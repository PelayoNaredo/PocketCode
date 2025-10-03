package com.pocketcode.core.ui.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonSize
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant

@Composable
fun PocketDialog(
    title: String,
    message: String? = null,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector? = null,
    content: (@Composable () -> Unit)? = null,
    confirmVariant: ButtonVariant = ButtonVariant.Primary,
    dismissVariant: ButtonVariant = ButtonVariant.Text,
    confirmSize: ButtonSize = ButtonSize.Medium,
    dismissSize: ButtonSize = ButtonSize.Medium
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = icon?.let {
            {
                Icon(imageVector = it, contentDescription = null)
            }
        },
        title = {
            Text(
                text = title,
                style = TypographyTokens.Title.large,
                fontWeight = FontWeight.SemiBold,
                color = ColorTokens.onSurface
            )
        },
        text = if (message != null || content != null) {
            {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.small)
                ) {
                    message?.let {
                        Text(
                            text = it,
                            style = TypographyTokens.Body.medium,
                            color = ColorTokens.onSurfaceVariant
                        )
                    }
                    content?.invoke()
                }
            }
        } else {
            null
        },
        confirmButton = {
            PocketButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = confirmEnabled,
                loading = confirmLoading,
                variant = confirmVariant,
                size = confirmSize
            )
        },
        dismissButton = dismissText?.let { label ->
            {
                PocketButton(
                    text = label,
                    onClick = {
                        onDismiss?.invoke()
                        onDismissRequest()
                    },
                    variant = dismissVariant,
                    size = dismissSize
                )
            }
        }
    )
}
