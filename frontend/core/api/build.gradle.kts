// Build file for the `:core:api` module.
// This is a Kotlin-only library module.
// Responsibilities:
// - Apply the `kotlin("android")` plugin.
// - It should have minimal to no dependencies, as it defines the contracts
//   and data models used throughout the app.
// - It might depend on a serialization library (like kotlinx.serialization).
//
// This module is a dependency for both the `domain` and `data` layers.

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.pocketcode.core.api"
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}