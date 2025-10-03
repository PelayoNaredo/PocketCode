plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.pocketcode"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pocketcode.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    
    // KAPT configuration to fix warnings
    kapt {
        correctErrorTypes = true
        useBuildCache = true
        mapDiagnosticLocations = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android & Jetpack
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Pager for slides navigation (Foundation includes HorizontalPager)
    implementation("androidx.compose.foundation:foundation")
    
    // Extended Material Icons (más iconos disponibles)
    implementation("androidx.compose.material:material-icons-extended")

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // Background work
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // RevenueCat for Subscriptions
    implementation("com.revenuecat.purchases:purchases:6.0.0")

    // DataStore & Serialization
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Firebase (analytics & crash reporting)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Project Modules
    implementation(project(":core:ui"))
    implementation(project(":core:api"))
    implementation(project(":core:utils"))
    implementation(project(":core:storage"))
    implementation(project(":core:network"))
    
    // Features
    implementation(project(":features:project"))
    implementation(project(":features:settings"))
    implementation(project(":features:marketplace"))
    implementation(project(":features:ai"))
    implementation(project(":features:auth"))
    implementation(project(":features:onboarding"))
    implementation(project(":features:editor"))
    implementation(project(":features:preview"))
    
    // Data layer
    implementation(project(":data:project"))
    implementation(project(":data:marketplace"))
    implementation(project(":data:ai"))
    implementation(project(":data:auth"))
    
    // Domain layer
    implementation(project(":domain:project"))
    implementation(project(":domain:marketplace"))
    implementation(project(":domain:ai"))
    implementation(project(":domain:auth"))

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
