package com.pocketcode.core.api.model

/**
 * Base response wrapper for API calls
 */
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val exception: Throwable) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

/**
 * Result type for domain operations
 */
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val exception: Throwable) : DomainResult<Nothing>()
}

/**
 * Extension function to convert ApiResponse to DomainResult
 */
fun <T> ApiResponse<T>.toDomainResult(): DomainResult<T> = when (this) {
    is ApiResponse.Success -> DomainResult.Success(data)
    is ApiResponse.Error -> DomainResult.Error(exception)
    is ApiResponse.Loading -> DomainResult.Error(IllegalStateException("Loading state cannot be converted to DomainResult"))
}