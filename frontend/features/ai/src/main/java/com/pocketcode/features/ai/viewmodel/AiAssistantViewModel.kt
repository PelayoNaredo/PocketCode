package com.pocketcode.features.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.core.api.model.DomainResult
import com.pocketcode.domain.ai.model.AiMessage
import com.pocketcode.domain.ai.model.CodeGenerationRequest
import com.pocketcode.domain.ai.model.CodeGenerationResponse
import com.pocketcode.domain.ai.repository.AiRepository
import com.pocketcode.domain.ai.usecase.GenerateCodeUseCase
import com.pocketcode.domain.ai.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for AI Assistant feature
 */
@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val generateCodeUseCase: GenerateCodeUseCase,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<AiMessage>>(emptyList())
    val chatHistory: StateFlow<List<AiMessage>> = _chatHistory.asStateFlow()

    init {
        // Observe chat history
        viewModelScope.launch {
            aiRepository.getChatHistory().collect { messages ->
                _chatHistory.value = messages
            }
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = sendMessageUseCase(message)) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastResponse = result.data.content
                    )
                }
                is DomainResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun generateCode(prompt: String, language: String = "kotlin") {
        if (prompt.isBlank()) return

        _uiState.value = _uiState.value.copy(isGeneratingCode = true, error = null)

        val request = CodeGenerationRequest(
            prompt = prompt,
            language = language
        )

        viewModelScope.launch {
            when (val result = generateCodeUseCase(request)) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingCode = false,
                        lastGeneratedCode = result.data
                    )
                }
                is DomainResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingCode = false,
                        error = result.exception.message ?: "Code generation failed"
                    )
                }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            aiRepository.clearChatHistory()
            _uiState.value = _uiState.value.copy(
                lastResponse = null,
                lastGeneratedCode = null,
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI State for AI Assistant
 */
data class AiAssistantUiState(
    val isLoading: Boolean = false,
    val isGeneratingCode: Boolean = false,
    val lastResponse: String? = null,
    val lastGeneratedCode: CodeGenerationResponse? = null,
    val error: String? = null
)