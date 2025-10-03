package com.pocketcode.features.ai.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.core.ui.components.button.PocketButton
import com.pocketcode.core.ui.components.card.PocketCard
import com.pocketcode.core.ui.components.feedback.EmptyState
import com.pocketcode.core.ui.components.feedback.ErrorDisplay
import com.pocketcode.core.ui.components.feedback.SmallLoadingIndicator
import com.pocketcode.core.ui.components.form.FormContainer
import com.pocketcode.core.ui.components.form.rememberFieldState
import com.pocketcode.core.ui.components.input.PocketTextField
import com.pocketcode.core.ui.components.layout.PocketScaffold
import com.pocketcode.core.ui.components.layout.PocketScaffoldConfig
import com.pocketcode.core.ui.components.navigation.ChatTopBar
import com.pocketcode.core.ui.icons.PocketIcons
import com.pocketcode.core.ui.snackbar.GlobalSnackbarEvent
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalSnackbarSeverity
import com.pocketcode.core.ui.snackbar.LocalGlobalSnackbarDispatcher
import com.pocketcode.core.ui.tokens.ColorTokens
import com.pocketcode.core.ui.tokens.ComponentTokens
import com.pocketcode.core.ui.tokens.ComponentTokens.ButtonVariant
import com.pocketcode.core.ui.tokens.SpacingTokens
import com.pocketcode.domain.ai.model.AiMessage
import com.pocketcode.domain.ai.model.CodeGenerationResponse
import com.pocketcode.domain.ai.model.MessageRole
import com.pocketcode.features.ai.viewmodel.AiAssistantUiState
import com.pocketcode.features.ai.viewmodel.AiAssistantViewModel
import java.io.IOException
import kotlin.text.Charsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AiAssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarDispatcher = LocalGlobalSnackbarDispatcher.current
    val context = LocalContext.current
    var pendingCodeToSave by remember { mutableStateOf<CodeGenerationResponse?>(null) }

    val saveCodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val code = pendingCodeToSave
        if (code == null) {
            pendingCodeToSave = null
            return@rememberLauncherForActivityResult
        }

        if (uri == null) {
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "Guardado cancelado",
                    origin = GlobalSnackbarOrigin.AI_ASSISTANT,
                    severity = GlobalSnackbarSeverity.INFO,
                    analyticsId = "ai_snippet_save_cancelled"
                )
            )
        } else {
            val result = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(code.generatedCode.toByteArray(Charsets.UTF_8))
                } ?: throw IOException("No se pudo abrir el archivo de destino")
            }

            snackbarDispatcher.dispatch(
                if (result.isSuccess) {
                    GlobalSnackbarEvent(
                        message = "Snippet guardado correctamente",
                        origin = GlobalSnackbarOrigin.AI_ASSISTANT,
                        severity = GlobalSnackbarSeverity.SUCCESS,
                        analyticsId = "ai_snippet_save_success"
                    )
                } else {
                    GlobalSnackbarEvent(
                        message = "Error al guardar el snippet",
                        supportingText = result.exceptionOrNull()?.localizedMessage,
                        origin = GlobalSnackbarOrigin.AI_ASSISTANT,
                        severity = GlobalSnackbarSeverity.ERROR,
                        analyticsId = "ai_snippet_save_error"
                    )
                }
            )
        }

        pendingCodeToSave = null
    }

    val handleCopy = remember(clipboardManager, snackbarDispatcher) {
        { response: CodeGenerationResponse ->
            clipboardManager.setText(AnnotatedString(response.generatedCode))
            snackbarDispatcher.dispatch(
                GlobalSnackbarEvent(
                    message = "Código copiado al portapapeles",
                    origin = GlobalSnackbarOrigin.AI_ASSISTANT,
                    severity = GlobalSnackbarSeverity.SUCCESS,
                    analyticsId = "ai_snippet_copy_success"
                )
            )
        }
    }

    val handleSave = remember(snackbarDispatcher, saveCodeLauncher) {
        { response: CodeGenerationResponse ->
            pendingCodeToSave = response
            val defaultFileName = "snippet_${System.currentTimeMillis()}.txt"
            saveCodeLauncher.launch(defaultFileName)
        }
    }

    PocketScaffold(
        config = PocketScaffoldConfig(
            paddingValues = PaddingValues(
                horizontal = SpacingTokens.Semantic.screenPaddingHorizontal,
                vertical = SpacingTokens.Semantic.screenPaddingVertical
            ),
            isScrollable = false,
            backgroundColor = ColorTokens.background
        ),
        topBar = {
            ChatTopBar(
                title = "Asistente Pocket",
                isTyping = uiState.isLoading,
                onNavigationClick = onNavigateBack,
                onClearChat = viewModel::clearChat
            )
        }
    ) { paddingValues ->
        AIAssistantPanel(
            uiState = uiState,
            chatHistory = chatHistory,
            onSendPrompt = viewModel::sendMessage,
            onGenerateCode = viewModel::generateCode,
            onClearError = viewModel::clearError,
            onCopyGeneratedCode = handleCopy,
            onSaveGeneratedCode = handleSave,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun AIAssistantPanel(
    uiState: AiAssistantUiState,
    chatHistory: List<AiMessage>,
    onSendPrompt: (String) -> Unit,
    onGenerateCode: (String) -> Unit,
    onClearError: () -> Unit,
    onCopyGeneratedCode: (CodeGenerationResponse) -> Unit,
    onSaveGeneratedCode: (CodeGenerationResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    FormContainer(
        modifier = modifier.fillMaxSize(),
        title = "Conversación con Pocket",
        description = "Haz preguntas, depura código o solicita snippets listos para usar.",
        isLoading = uiState.isLoading && chatHistory.isEmpty(),
        showSubmitButton = false,
        scrollable = false
    ) {
        val promptState = rememberFieldState()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            if (chatHistory.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val promptValue = promptState.value
                    val hasPrompt = promptValue.isNotBlank()
                    EmptyState(
                        title = "Inicia una conversación",
                        description = "Comparte dudas, pega un bloque de código o pide ayuda para generar nuevas funciones.",
                        icon = PocketIcons.Chat,
                        actionText = if (hasPrompt) "Enviar mensaje" else null,
                        onAction = if (hasPrompt) {
                            {
                                onSendPrompt(promptValue)
                                promptState.value = ""
                            }
                        } else null
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
                ) {
                    items(chatHistory, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }

                    if (uiState.isLoading) {
                        item { AssistantTypingIndicator() }
                    }
                }
            }

            uiState.error?.let { error ->
                ErrorDisplay(
                    error = error,
                    onRetry = onClearError
                )
            }

            uiState.lastGeneratedCode?.let { response ->
                GeneratedCodeCard(
                    response = response,
                    onCopy = { onCopyGeneratedCode(response) },
                    onSave = { onSaveGeneratedCode(response) }
                )
            }

            InputArea(
                prompt = promptState.value,
                onPromptChange = { promptState.value = it },
                isSending = uiState.isLoading,
                isGenerating = uiState.isGeneratingCode,
                onSend = {
                    if (promptState.value.isNotBlank()) {
                        onSendPrompt(promptState.value)
                        promptState.value = ""
                    }
                },
                onGenerate = {
                    if (promptState.value.isNotBlank()) {
                        onGenerateCode(promptState.value)
                        promptState.value = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun AssistantTypingIndicator() {
    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = ComponentTokens.CardVariant.Filled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            SmallLoadingIndicator()
            Text(
                text = "El asistente está pensando...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageBubble(message: AiMessage) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        PocketCard(
            modifier = Modifier.widthIn(max = 320.dp),
            variant = if (isUser) ComponentTokens.CardVariant.Elevated else ComponentTokens.CardVariant.Filled
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GeneratedCodeCard(
    response: CodeGenerationResponse,
    onCopy: () -> Unit,
    onSave: () -> Unit
) {
    PocketCard(
        modifier = Modifier.fillMaxWidth(),
        variant = ComponentTokens.CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
        ) {
            Text(
                text = "Código sugerido",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = response.generatedCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            response.explanation?.let { explanation ->
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (response.suggestions.isNotEmpty()) {
                response.suggestions.forEach { suggestion ->
                    Text(
                        text = "• $suggestion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
            ) {
                PocketButton(
                    text = "Copiar",
                    onClick = onCopy,
                    variant = ButtonVariant.Secondary,
                    leadingIcon = {
                        Icon(
                            imageVector = PocketIcons.ContentCopy,
                            contentDescription = null
                        )
                    }
                )

                PocketButton(
                    text = "Guardar",
                    onClick = onSave,
                    variant = ButtonVariant.Primary,
                    leadingIcon = {
                        Icon(
                            imageVector = PocketIcons.Save,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun InputArea(
    prompt: String,
    onPromptChange: (String) -> Unit,
    isSending: Boolean,
    isGenerating: Boolean,
    onSend: () -> Unit,
    onGenerate: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal)
    ) {
        PocketTextField(
            value = prompt,
            onValueChange = onPromptChange,
            label = "Escribe tu solicitud",
            placeholder = "Pregunta, describe un bug o pide un snippet...",
            enabled = !isSending && !isGenerating,
            helperText = when {
                isGenerating -> "Generando código..."
                isSending -> "Enviando mensaje..."
                else -> null
            },
            singleLine = false,
            maxLines = 4
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Semantic.contentSpacingNormal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PocketButton(
                text = "Enviar",
                onClick = onSend,
                modifier = Modifier.weight(1f),
                enabled = prompt.isNotBlank() && !isSending,
                loading = isSending,
                leadingIcon = {
                    Icon(
                        imageVector = PocketIcons.ArrowForward,
                        contentDescription = null
                    )
                }
            )

            PocketButton(
                text = "Generar código",
                onClick = onGenerate,
                modifier = Modifier.weight(1f),
                variant = ButtonVariant.Secondary,
                enabled = prompt.isNotBlank() && !isGenerating,
                loading = isGenerating,
                leadingIcon = {
                    Icon(
                        imageVector = PocketIcons.Code,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AIAssistantPanelPreview() {
    val sampleMessages = listOf(
        AiMessage(id = "1", content = "¿Cómo puedo crear una función recursiva?", role = MessageRole.USER),
        AiMessage(id = "2", content = "Puedes definir la función y llamar a sí misma con un caso base claro.", role = MessageRole.ASSISTANT)
    )
    val sampleState = AiAssistantUiState()

    AIAssistantPanel(
        uiState = sampleState,
        chatHistory = sampleMessages,
        onSendPrompt = {},
        onGenerateCode = {},
        onClearError = {},
        onCopyGeneratedCode = {},
        onSaveGeneratedCode = {}
    )
}
