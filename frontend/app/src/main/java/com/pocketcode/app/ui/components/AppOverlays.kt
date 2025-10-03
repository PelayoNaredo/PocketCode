package com.pocketcode.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcode.features.settings.ui.ModernSettingsScreen
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.feedback.PocketDialog
import com.pocketcode.core.ui.tokens.ComponentTokens.CardVariant
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.TypographyTokens
import com.pocketcode.core.ui.tokens.SpacingTokens

/**
 * Overlay container for app-level overlays (Settings, User Profile, etc.)
 */
@Composable
fun AppOverlays(
    showSettings: Boolean,
    showUserProfile: Boolean,
    onDismissSettings: () -> Unit,
    onDismissUserProfile: () -> Unit
) {
    // Settings overlay
    if (showSettings) {
        ModernSettingsScreen(
            onDismiss = onDismissSettings
        )
    }
    
    // User profile overlay
    if (showUserProfile) {
        UserProfileOverlay(
            onDismiss = onDismissUserProfile
        )
    }
}

/**
 * User profile overlay
 */
@Composable
private fun UserProfileOverlay(
    onDismiss: () -> Unit
) {
    PocketDialog(
        title = "Perfil de usuario",
        message = "Gestiona tu cuenta y preferencias de PocketCode.",
        onDismissRequest = onDismiss,
        confirmText = "Cerrar",
        onConfirm = onDismiss,
        content = {
            PocketCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Elevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.large),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.medium),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Pocket ID",
                        style = TypographyTokens.Title.small,
                        color = ColorTokens.onSurface
                    )
                    Text(
                        text = "En breve podrás personalizar tu perfil, vincular cuentas externas y gestionar accesos.",
                        style = TypographyTokens.Body.medium,
                        color = ColorTokens.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Próximamente",
                            style = TypographyTokens.Display.small,
                            color = ColorTokens.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    )
}