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
    }
}

//configurations.all {
//    resolutionStrategy {
//        force("org.antlr:antlr4-runtime:4.8")
//        force("org.antlr:antlr4-tool:4.8")
//    }
//}

//plugins {
//    id("com.zpw.myplugin") version "1.0.0-SNAPSHOT"
//}