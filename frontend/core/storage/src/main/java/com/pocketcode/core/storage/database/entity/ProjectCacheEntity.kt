package com.pocketcode.core.storage.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Room entity for caching project metadata
 */
@Entity(tableName = "project_cache")
data class ProjectCacheEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val localPath: String,
    val lastModified: Date,
    val isActive: Boolean = false
)