package com.pocketcode.domain.ai.usecase

import com.pocketcode.core.api.model.DomainResult
import com.pocketcode.core.api.usecase.BaseUseCase
import com.pocketcode.domain.ai.model.CodeGenerationRequest
import com.pocketcode.domain.ai.model.CodeGenerationResponse
import com.pocketcode.domain.ai.repository.AiRepository
import javax.inject.Inject

/**
 * Use case for generating code using AI
 */
class GenerateCodeUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : BaseUseCase<CodeGenerationRequest, DomainResult<CodeGenerationResponse>> {

    override suspend fun invoke(params: CodeGenerationRequest): DomainResult<CodeGenerationResponse> {
        if (params.prompt.isBlank()) {
            return DomainResult.Error(IllegalArgumentException("Prompt cannot be empty"))
        }
        
        return aiRepository.generateCode(params)
    }
}