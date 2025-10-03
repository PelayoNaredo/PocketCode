package com.pocketcode.core.storage.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pocketcode.core.storage.entities.ProjectEntity
import com.pocketcode.core.storage.dao.ProjectDao

/**
 * Main database for the application
 * Version 1 - Initial database setup
 * 
 * This file defines the application's Room database.
 * Room is an abstraction layer over SQLite that makes it easier to work with a local database.
 *
 * Responsibilities:
 * - Annotated with `@Database`, it lists all the entity classes (tables) and the database version.
 * - It defines abstract methods for accessing the Data Access Objects (DAOs).
 * - A Hilt module would typically be created in this module to provide a singleton
 *   instance of this AppDatabase to the rest of the application.
 *
 * Interacts with:
 * - Entity classes (e.g., a `ProjectEntity` for caching project data locally).
 * - DAO interfaces (e.g., `ProjectDao`).
 * - `:data` modules: Repositories in the data layer will use the DAOs provided by this
 *   database to access local data.
 */
@Database(
    entities = [
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun projectDao(): ProjectDao
}
