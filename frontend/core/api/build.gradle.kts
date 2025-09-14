// Build file for the `:core:api` module.
// This is a Kotlin-only library module.
// Responsibilities:
// - Apply the `kotlin("jvm")` or a similar library plugin.
// - It should have minimal to no dependencies, as it defines the contracts
//   and data models used throughout the app.
// - It might depend on a serialization library (like kotlinx.serialization).
//
// This module is a dependency for both the `domain` and `data` layers.
