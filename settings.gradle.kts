//import com.zpw.myplayground.myGitRepositories
//import me.champeau.gradle.igp.gitRepositories

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
    includeBuild("plugin")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        // Add these two maven entries.
        maven { setUrl("./flutter") }
        maven { setUrl("https://storage.googleapis.com/download.flutter.io") }
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "MyPlayGround"
include(":app")
include(":library1")
include(":library2")
include(":library3")
include(":annotation")
//include(":plugin")
//include(":plugintest")

plugins {
    id("com.dropbox.focus") version "0.4.0"
}

//focus {
//    allSettingsFileName = "settings-all.gradle"
//    focusFileName = "settings-focus.gradle"
//}

//plugins {
//    id("me.champeau.includegit") version "0.1.5"
//    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
//}

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
