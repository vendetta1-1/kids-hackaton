pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rofles"
includeBuild("conventions")
include(":app")
include(":core")
include(":domain")
include(":data")
include(":feature:solo-effect")
include(":feature:high-five")
include(":feature:element-picker")
include(":feature:album")
include(":feature:settings")
include(":feature:demo")
 