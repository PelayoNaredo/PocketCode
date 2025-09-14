plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.pocketcode.data.project"
    compileSdk = 33

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
    implementation(project(":domain:project"))
    implementation(project(":core:storage"))
    implementation(project(":core:api"))

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    // JGit for local Git operations
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}
