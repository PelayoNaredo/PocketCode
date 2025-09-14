// This is the build file for the main `:app` module.
// The `:app` module is the entry point of the Android application.
// Responsibilities:
// - Apply the `com.android.application` plugin.
// - Define the `applicationId`, `minSdk`, `targetSdk`, and `versionCode`/`versionName`.
// - Declare dependencies on all the `feature` modules (e.g., `:features:editor`, `:features:project`).
// - Declare dependencies on core libraries like Hilt for application-level setup.
// - Configure build types (debug, release) and signing configurations.
//
// This file ties all the different parts of the application together into a final, runnable APK/AAB.
//
// Interacts with (dependencies):
// - All `:features:*` modules.
// - `:core:ui`, `:core:utils`, etc., indirectly through features.
