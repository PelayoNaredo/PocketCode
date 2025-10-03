package com.pocketcode.core.ui.components.feedback

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.core.ui.tokens.TypographyTokens

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(horizontal = SpacingTokens.Semantic.screenPaddingHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            SpacingTokens.Semantic.contentSpacingNormal,
            Alignment.CenterVertically
        )
    ) {
        icon?.let {
            Image(
                imageVector = it,
                contentDescription = null
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = TypographyTokens.Title.large,
                color = ColorTokens.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(SpacingTokens.small))
            Text(
                text = description,
                style = TypographyTokens.Body.medium,
                color = ColorTokens.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        actionText?.let { label ->
            Button(
                onClick = { onAction?.invoke() },
                enabled = onAction != null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = label)
            }
        }

        secondaryActionText?.let { label ->
            OutlinedButton(
                onClick = { onSecondaryAction?.invoke() },
                enabled = onSecondaryAction != null,
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
fun NoProjectsEmptyState(
    modifier: Modifier = Modifier,
    onCreateProject: () -> Unit,
    onImportProject: (() -> Unit)? = null
) {
    EmptyState(
        title = "Crea tu primer proyecto",
        description = "Aún no tienes proyectos en PocketCode. Empieza uno desde cero o importa un archivo existente.",
        icon = null,
        actionText = "Crear proyecto",
        onAction = onCreateProject,
        secondaryActionText = onImportProject?.let { "Importar proyecto" },
        onSecondaryAction = onImportProject,
        modifier = modifier.fillMaxWidth()
    )
}
