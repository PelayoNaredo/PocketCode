// Build file for the `:domain:ide` module.
// This is a pure Kotlin library module.
// Responsibilities:
// - Apply the `kotlin("jvm")` plugin.
// - It should have NO dependencies on the `:data` or `:features` layers.
// - It can depend on `":core:api"` for access to common data models if necessary,
//   but it's better to define its own models to be completely independent.
// - It may depend on other `domain` modules (e.g., `:domain:project`).
//
// This module defines the "what" of the IDE's business logic (e.g., "the app can generate code"),
// but not the "how" (which is the data layer's job).
