package com.pocketcode.core.storage.database

// import androidx.room.Database
// import androidx.room.RoomDatabase

/**
 * This file would define the application's Room database.
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
// @Database(entities = [/* ProjectEntity::class */], version = 1)
abstract class AppDatabase /*: RoomDatabase()*/ {
    // abstract fun projectDao(): ProjectDao
}
