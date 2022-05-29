buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
        // Add these two maven entries.
        maven { setUrl("./flutter") }
        maven { setUrl("https://storage.googleapis.com/download.flutter.io") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("org.jacoco:org.jacoco.core:0.8.7")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
        val kotlinVersion = "1.6.10"
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

//plugins {
//    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
//}
//
//dependencyRoot {
//
//}