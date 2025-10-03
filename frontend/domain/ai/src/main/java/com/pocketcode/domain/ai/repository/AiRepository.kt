package com.pocketcode.domain.ai.repository

import com.pocketcode.core.api.model.DomainResult
import com.pocketcode.domain.ai.model.AiMessage
import com.pocketcode.domain.ai.model.CodeGenerationRequest
import com.pocketcode.domain.ai.model.CodeGenerationResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI operations
 */
interface AiRepository {
    
    /**
     * Send a message to the AI assistant and get a response
     */
    suspend fun sendMessage(message: String): DomainResult<AiMessage>
    
    /**
     * Generate code based on a prompt
     */
    suspend fun generateCode(request: CodeGenerationRequest): DomainResult<CodeGenerationResponse>
    
    /**
     * Get chat history
     */
    fun getChatHistory(): Flow<List<AiMessage>>
    
    /**
     * Clear chat history
     */
    suspend fun clearChatHistory(): DomainResult<Unit>
    
    /**
     * Save a message to chat history
     */
    suspend fun saveMessage(message: AiMessage): DomainResult<Unit>
}