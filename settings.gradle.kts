pluginManagement {
    repositories {
        google()                         // ✅ must be here
        mavenCentral()                    // ✅ must be here
        gradlePluginPortal()              // ✅ must be here
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "MECCA"
include(":app")
 