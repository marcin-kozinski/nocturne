pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins { id("nocturne.pre-commit-git-hooks") }

rootProject.name = "nocturne"

include(":app")
