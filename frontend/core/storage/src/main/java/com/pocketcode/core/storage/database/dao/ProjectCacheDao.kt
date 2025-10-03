package com.pocketcode.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pocketcode.core.storage.database.entity.ProjectCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for project cache operations
 */
@Dao
interface ProjectCacheDao {
    
    @Query("SELECT * FROM project_cache ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectCacheEntity>>
    
    @Query("SELECT * FROM project_cache WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectCacheEntity?
    
    @Query("SELECT * FROM project_cache WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProject(): ProjectCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectCacheEntity)
    
    @Update
    suspend fun updateProject(project: ProjectCacheEntity)
    
    @Delete
    suspend fun deleteProject(project: ProjectCacheEntity)
    
    @Query("DELETE FROM project_cache WHERE id = :id")
    suspend fun deleteProjectById(id: String)
    
    @Query("UPDATE project_cache SET isActive = 0")
    suspend fun deactivateAllProjects()
    
    @Query("UPDATE project_cache SET isActive = 1 WHERE id = :id")
    suspend fun setActiveProject(id: String)
}