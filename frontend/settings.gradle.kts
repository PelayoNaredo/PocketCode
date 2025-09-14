// This file defines the project structure and tells Gradle which modules to include in the build.
// Responsibilities:
// - Include all the Gradle modules that are part of the project using `include()`.
//   (e.g., include(":app"), include(":core:ui"), include(":features:editor")).
// - Configure plugin management repositories (e.g., `google()`, `mavenCentral()`).
// - Can set the project name.
//
// This file is crucial for the multi-module architecture to work correctly.
// It will list every module created in the `core`, `data`, `domain`, and `features` directories.
