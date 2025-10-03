pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PocketCode"
include(":app")
include(":core:ui")
include(":core:utils")
include(":core:api")
include(":core:storage")
include(":core:network")
include(":data:project")
include(":data:marketplace")
include(":data:ai")
include(":data:auth")
include(":domain:project")
include(":domain:marketplace")
include(":domain:ai")
include(":domain:auth")
include(":features:project")
include(":features:editor")
include(":features:settings")
include(":features:marketplace")
include(":features:ai")
include(":features:auth")
include(":features:onboarding")
include(":features:preview")

