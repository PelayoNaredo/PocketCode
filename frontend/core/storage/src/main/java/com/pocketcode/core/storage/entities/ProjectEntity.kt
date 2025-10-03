package com.pocketcode.core.storage.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val localPath: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isRemote: Boolean = false,
    val remoteUrl: String? = null
)