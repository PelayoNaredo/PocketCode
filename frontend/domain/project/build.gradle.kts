// Build file for the `:domain:project` module.
// This is a pure Kotlin library module.
// Responsibilities:
// - Apply the `kotlin("jvm")` plugin.
// - Like other domain modules, it must not depend on `:data` or `:features` layers.
// - It can depend on `":core:api"` but it's preferable to define its own models.
//
// This module contains the business logic, models, and repository interfaces
// related to project and file management (e.g., creating a project, reading a file).
