package com.pocketcode.domain.ai.usecase

import com.pocketcode.core.api.model.DomainResult
import com.pocketcode.core.api.usecase.BaseUseCase
import com.pocketcode.domain.ai.model.AiMessage
import com.pocketcode.domain.ai.model.MessageRole
import com.pocketcode.domain.ai.repository.AiRepository
import java.util.*
import javax.inject.Inject

/**
 * Use case for sending a message to the AI assistant
 */
class SendMessageUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : BaseUseCase<String, DomainResult<AiMessage>> {

    override suspend fun invoke(params: String): DomainResult<AiMessage> {
        if (params.isBlank()) {
            return DomainResult.Error(IllegalArgumentException("Message cannot be empty"))
        }
        
        // Save user message first
        val userMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            content = params,
            role = MessageRole.USER
        )
        
        aiRepository.saveMessage(userMessage)
        
        // Get AI response
        return when (val result = aiRepository.sendMessage(params)) {
            is DomainResult.Success -> {
                // Save AI response
                aiRepository.saveMessage(result.data)
                result
            }
            is DomainResult.Error -> result
        }
    }
}