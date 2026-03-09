rootProject.name = "msaitodev-android-core"

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
        mavenLocal()
    }
}

include(":core-common")
include(":core-ads")
include(":core-navigation")
include(":core-notifications")
include(":core-cloud-sync")
include(":feature-billing")
include(":feature-settings")