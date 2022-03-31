plugins {
    `java-gradle-plugin`
    `java-library`
    groovy
    id("com.gradle.plugin-publish") version "0.16.0"
    `maven-publish`
    `kotlin-dsl`
}

group = "com.zpw.includegit"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:5.13.0.202109080827-r")
}

gradlePlugin {
    val includeBuildPlugin by plugins.creating {
        id = "com.zpw.includegit"
        implementationClass = "com.zpw.myplayground.MyIncludeGitPlugin"
    }
}

publishing {
    // 配置maven 仓库
    repositories {
        maven {
            //当前项目根目录
            url = uri("$rootDir/repo")
        }
    }
    // 配置发布产物
    publications {
        // 名称可以随便定义，这里定义成 maven
        create<MavenPublication>("maven") {// 容器可配置的信息 MavenPublication
            groupId = "com.zpw.includegit"
            artifactId = "my-include-git-plugin"
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
        tags = listOf("git", "included builds")

        plugins {
            named("includeBuildPlugin") {
                displayName = "Gradle Include Git repositories plugin"
            }
        }
        mavenCoordinates {
            groupId = "com.zpw.includegit"
            artifactId = "my-include-git-plugin"
            version = "1.0.0-SNAPSHOT"
        }
    }
}