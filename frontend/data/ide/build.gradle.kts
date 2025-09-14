// Build file for the `:data:ide` module.
// This is a Kotlin library module.
// Responsibilities:
// - Apply the `kotlin("jvm")` plugin.
// - Declare a dependency on the corresponding domain module, `":domain:ide"`, to get access
//   to the repository interfaces it needs to implement.
// - Declare a dependency on `":core:network"` to make API calls to the backend (e.g., for AI services).
// - Declare a dependency on `":core:api"` for the DTOs.
// - Declare a dependency on Hilt for dependency injection.
//
// This module provides the concrete implementation of the IDE-related data sources.
