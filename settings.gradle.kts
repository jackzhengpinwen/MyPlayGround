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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "MyPlayGround"
include(":app")
//include(":dependency-analysis-android-gradle-plugin")
include(":library1")
include(":library2")
//include(":library3")
//include(":plugin")
//include(":plugintest")

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
include(":annotation")
