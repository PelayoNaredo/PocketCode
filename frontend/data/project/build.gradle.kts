// Build file for the `:data:project` module.
// This is a Kotlin library module.
// Responsibilities:
// - Apply the `kotlin("jvm")` plugin.
// - Depend on the `":domain:project"` module for repository interfaces.
// - Depend on `":core:storage"` for access to the local database (Room) and filesystem.
// - Depend on `":core:api"` for DTOs if it needs to sync with a remote.
// - Depend on Hilt for dependency injection.
//
// This module provides the concrete implementation for project and file management data sources.
