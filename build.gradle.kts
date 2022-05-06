import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.tree

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0-beta04")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("org.jacoco:org.jacoco.core:0.8.7")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.41")
    }
}

plugins {
    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
}

dependencyRoot {

}