pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "7.1.2"
        id("com.gradle.enterprise") version "3.8.1"
        id("org.jetbrains.kotlin.jvm") version "1.5.31"
        id("org.jetbrains.dokka") version "1.5.31"
    }
}

plugins {
    id("com.gradle.enterprise")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "my-plugin"

include(":antlr")