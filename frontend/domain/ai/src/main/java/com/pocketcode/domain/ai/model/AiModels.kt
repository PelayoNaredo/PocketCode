package com.pocketcode.domain.ai.model

/**
 * AI assistant message types
 */
data class AiMessage(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * AI code generation request
 */
data class CodeGenerationRequest(
    val prompt: String,
    val language: String = "kotlin",
    val context: String? = null,
    val maxTokens: Int = 1000
)

/**
 * AI code generation response
 */
data class CodeGenerationResponse(
    val generatedCode: String,
    val explanation: String? = null,
    val suggestions: List<String> = emptyList()
)