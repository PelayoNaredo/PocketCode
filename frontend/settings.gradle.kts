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
include(":core:network")
include(":core:storage")
include(":core:api")
include(":core:p2p")
include(":data:ide")
include(":data:project")
include(":data:marketplace")
include(":data:ai")
include(":domain:ide")
include(":domain:project")
include(":domain:marketplace")
include(":domain:ai")
include(":features:editor")
include(":features:project")
include(":features:settings")
include(":features:designer")
include(":features:marketplace")
include(":features:ai")
include(":features:authentication")
include(":features:onboarding")
