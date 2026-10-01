// Configuración de compilación Gradle en sintaxis Kotlin. Conservar las versiones del proyecto; la guía explica SDK, plugins, repositorios y dependencias.
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
    }
}

rootProject.name = "FakeStoreRolesKotlin"
include(":app")
