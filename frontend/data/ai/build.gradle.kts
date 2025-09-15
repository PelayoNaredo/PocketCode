plugins {
    id("org.jetbrains.kotlin.jvm")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

dependencies {
    implementation(project(":domain:ai"))
    implementation(project(":core:network"))

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
}
