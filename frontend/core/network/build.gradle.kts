// Build file for the `:core:network` module.
// This is a Kotlin library module that provides networking capabilities.
// Responsibilities:
// - Apply the `kotlin("jvm")` or `com.android.library` plugin.
// - Declare dependencies on a networking client like Ktor or Retrofit.
// - Declare a dependency on a JSON serialization library (e.g., kotlinx.serialization.json).
// - Declare a dependency on Hilt for providing the network client via DI.
//
// This module will be a dependency for any `:data` module that needs to make API calls.
