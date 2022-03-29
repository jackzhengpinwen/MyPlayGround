buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("org.jacoco:org.jacoco.core:0.8.7")
    }
}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}