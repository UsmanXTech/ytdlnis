pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.3.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
        id("org.jetbrains.compose") version "1.9.2"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "YTDLnis-Windows"
