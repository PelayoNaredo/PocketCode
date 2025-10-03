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

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.pocketcode.core.storage"
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
    implementation(project(":core:api"))
    
    // Room for local database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
