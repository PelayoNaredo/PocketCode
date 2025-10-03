package com.pocketcode.core.api.repository

import com.pocketcode.core.api.model.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Base repository interface for common CRUD operations
 */
interface BaseRepository<T, ID> {
    suspend fun getAll(): Flow<DomainResult<List<T>>>
    suspend fun getById(id: ID): DomainResult<T?>
    suspend fun save(item: T): DomainResult<T>
    suspend fun delete(id: ID): DomainResult<Unit>
}