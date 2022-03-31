import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `java-library`
    kotlin("jvm") version "1.6.10"
    groovy
    id("com.gradle.plugin-publish") version "0.16.0"
    `maven-publish`
    `kotlin-dsl`
}

group = "com.zpw.myplugin"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
    google()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:7.0.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("com.squareup:kotlinpoet:1.11.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:5.13.0.202109080827-r")
}

gradlePlugin {
//    val includeBuildPlugin by plugins.creating {
//        id = "com.zpw.includegit"
//        implementationClass = "com.zpw.myplayground.MyIncludeGitPlugin"
//    }
    val quadrantPlugin by plugins.creating {
        id = "com.zpw.myplugin"
        implementationClass = "com.zpw.myplayground.QuadrantPlugin"
    }
}

publishing {
    // 配置maven 仓库
    repositories {
        maven {
            //当前项目根目录
            mavenLocal()
        }
    }
    // 配置发布产物
    publications {
        // 名称可以随便定义，这里定义成 maven
        create<MavenPublication>("maven") {// 容器可配置的信息 MavenPublication
            groupId = "com.zpw.myplugin"
            artifactId = "my-plugin"
            version = "1.0.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

afterEvaluate {
    pluginBundle {
        website = "https://melix.github.io/includegit-gradle-plugin/"
        vcsUrl = "https://github.com/melix/includegit-gradle-plugin"
        description = "Adds support for including Git repositories"
        tags = listOf("android")

        plugins {
//            named("includeBuildPlugin") {
//                displayName = "Gradle Include Git repositories plugin"
//            }
            named("quadrantPlugin") {
                displayName = "Gradle quadrant plugin"
            }
        }
        mavenCoordinates {
            groupId = "com.zpw.myplugin"
            artifactId = "my-plugin"
            version = "1.0.0-SNAPSHOT"
        }
    }
}