// This is the top-level build file for the entire Android application.
// It is used to configure project-wide settings and dependencies.
// Responsibilities:
// - Define versions for plugins used across the project (e.g., Android Gradle Plugin, Kotlin).
// - Register plugin repositories (e.g., google(), mavenCentral()).
// - Apply plugins that are common to all modules, if any.
//
// This file orchestrates the build process for all sub-modules (features, core, etc.).

import com.android.build.gradle.BaseExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}

subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<BaseExtension>("android") {
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
            }
        }
        dependencies {
            add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.4")
        }
    }

    plugins.withId("com.android.library") {
        extensions.configure<BaseExtension>("android") {
            compileOptions {
                isCoreLibraryDesugaringEnabled = true
            }
        }
        dependencies {
            add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:2.0.4")
        }
    }
}
