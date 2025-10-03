// Build file for the `:domain:project` module.
// This is a pure Kotlin library module.
// Responsibilities:
// - Apply the `kotlin("jvm")` plugin.
// - Like other domain modules, it must not depend on `:data` or `:features` layers.
// - It can depend on `":core:api"` but it's preferable to define its own models.
//
// This module contains the business logic, models, and repository interfaces
// related to project and file management (e.g., creating a project, reading a file).

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("javax.inject:javax.inject:1")
}
