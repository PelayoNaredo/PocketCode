// Build file for the `:core:storage` module.
// This is an Android library module because it may interact with Android-specific
// storage APIs or use a database like Room which requires the Android framework.
// Responsibilities:
// - Apply the `com.android.library` plugin.
// - Declare dependencies for local storage solutions, such as:
//   - Room for structured, relational data.
//   - Jetpack DataStore for key-value preferences.
// - Declare a dependency on Hilt for providing database DAOs or DataStore instances.
//
// This module will be a dependency for any `:data` module that needs to persist data locally.
