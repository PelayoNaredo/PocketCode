plugins {
	id("com.android.library")
	id("org.jetbrains.kotlin.android")
}

android {
	namespace = "com.pocketcode.core.ui"
	compileSdk = 34

	defaultConfig {
		minSdk = 24
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables.useSupportLibrary = true
	}

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.8"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
		freeCompilerArgs += listOf(
			"-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
			"-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
			"-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
		)
	}

	packaging {
		resources.excludes += setOf(
			"/META-INF/{AL2.0,LGPL2.1}",
			"META-INF/LICENSE*"
		)
	}
}

dependencies {
	val composeBom = platform("androidx.compose:compose-bom:2024.05.00")

	implementation(composeBom)
	androidTestImplementation(composeBom)

	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-util")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.material:material-icons-extended")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.animation:animation")
	implementation("androidx.compose.runtime:runtime-livedata")
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.foundation:foundation-layout")
	implementation("androidx.compose.material3:material3-window-size-class")

	implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
	implementation("androidx.navigation:navigation-compose:2.7.7")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

	debugImplementation("androidx.compose.ui:ui-tooling")
	debugImplementation("androidx.compose.ui:ui-test-manifest")

	androidTestImplementation("androidx.compose.ui:ui-test-junit4")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
