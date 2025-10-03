package com.pocketcode.core.api.usecase

/**
 * Base use case interface for domain operations
 */
interface BaseUseCase<in P, out R> {
    suspend operator fun invoke(params: P): R
}

/**
 * Use case without parameters
 */
interface BaseUseCaseNoParams<out R> {
    suspend operator fun invoke(): R
}