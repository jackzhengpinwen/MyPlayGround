//import com.zpw.myplayground.myGitRepositories
//import me.champeau.gradle.igp.gitRepositories


pluginManagement {
    repositories {
        maven {
            url = uri("$rootDir/repo")
        }
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
//    includeBuild("plugin")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("$rootDir/repo")
        }
        mavenLocal()
        google()
        mavenCentral()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "MyPlayGround"
include(":app")
//include(":plugin")

plugins {
//    id("me.champeau.includegit") version "0.1.5"
//    id("com.zpw.includegit") version "1.0.0-SNAPSHOT"
}

//gitRepositories {
//    include("circleimageview") {
//        uri.set("https://github.com/hdodenhof/CircleImageView.git")
//        branch.set("master")
//    }
//}

//myGitRepositories {
//    include("Circleimageview") {
//        uri.set("https://github.com/hdodenhof/CircleImageView.git")
//        branch.set("master")
//    }
//}