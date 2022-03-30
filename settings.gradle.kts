import me.champeau.gradle.igp.gitRepositories

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

include(":app")
rootProject.name = "MyPlayGround"

plugins {
    id("me.champeau.includegit") version "0.1.5"
}

gitRepositories {
    include("circleimageview") {
        uri.set("https://github.com/hdodenhof/CircleImageView.git")
        branch.set("master")
    }
}