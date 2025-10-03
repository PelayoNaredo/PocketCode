// Build file for the `:core:utils` module.
// This is a Kotlin-only library module.
// Responsibilities:
// - Apply the `kotlin("android")` plugin.
// - It should have very few or no dependencies.
// - It contains pure Kotlin helper functions, extension functions, and utility classes
//   that can be used anywhere in the application.
//
// Examples of utils: validators, date formatters, string manipulators.
// This module can be a dependency for any other module in the project.

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pocketcode.core.utils"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
