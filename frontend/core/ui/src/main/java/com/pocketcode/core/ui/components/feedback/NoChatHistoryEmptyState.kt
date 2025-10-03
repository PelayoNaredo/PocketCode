package com.pocketcode.core.ui.components.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pocketcode.core.ui.icons.PocketIcons

@Composable
fun NoChatHistoryEmptyState(
    modifier: Modifier = Modifier,
    onStartChat: () -> Unit,
    supportingText: String = "Comienza una conversación para ver aquí tu historial."
) {
    EmptyState(
        title = "Aún no hay mensajes",
        description = supportingText,
        icon = PocketIcons.ChatOutlined,
        actionText = "Iniciar chat",
        onAction = onStartChat,
        modifier = modifier
    )
}
