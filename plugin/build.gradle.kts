import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    kotlin("kapt") version embeddedKotlinVersion
    `java-gradle-plugin`
    `java-library`
    id("com.gradle.plugin-publish") version "0.16.0"
    `maven-publish`
    `kotlin-dsl`
    antlr
    id("com.bnorm.power.kotlin-power-assert") version "0.1.0"
}

group = "com.zpw.myplugin"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}


val asmVersion = "9.2.0.1"
val antlrVersion by extra("4.9.2")
val internalAntlrVersion by extra("4.8.2")

dependencies {
    implementation(gradleApi())
    implementation(files("libs/asm-$asmVersion.jar"))
    implementation(files("libs/antlr-$internalAntlrVersion.jar"))
    implementation("com.squareup:kotlinpoet:1.11.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10") {
        because("For Kotlin ABI analysis")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.10") {
        because("For writing HTML reports")
    }
    implementation("com.squareup.moshi:moshi:1.12.0") {
        because("For writing reports in JSON format")
    }
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0") {
        because("For writing reports based on Kotlin classes")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.3.0") {
        because("For Kotlin ABI analysis")
    }
    antlr("org.antlr:antlr4:4.9.1") {
        because("For source parsing")
    }
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10") {
        because("Auto-wiring into Kotlin projects")
    }
    compileOnly("com.android.tools.build:gradle:7.2.0-beta04") {
        because("Auto-wiring into Android projects")
    }
    compileOnly("org.apache.commons:commons-lang3:3.4") {
        because("File create")
    }
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:5.13.0.202109080827-r")
//    implementation("org.ow2.asm:asm:9.2")
//    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-util:9.1")

    implementation("com.google.code.gson:gson:2.9.0")

    implementation("com.google.guava:guava:31.0.1-jre") {
        because("Graphs")
    }

    implementation("io.github.irgaly.xml:original-characters-stax:1.0.1") {
        because("Unused res delete")
    }
    implementation("org.jooq:joor-java-8:0.9.13")
    implementation("org.javassist:javassist:3.28.0-GA")
}

tasks.jar {
    // Bundle shaded ASM jar into final artifact
    from(zipTree("libs/asm-$asmVersion.jar"))
    from(zipTree("libs/antlr-$internalAntlrVersion.jar"))
}

// https://docs.gradle.org/current/userguide/antlr_plugin.html
// https://discuss.gradle.org/t/using-gradle-2-10s-antlr-plugin-to-import-an-antlr-4-lexer-grammar-into-another-grammar/14970/6
//val antlr = tasks.generateGrammarSource
//antlr {
//    /*
//     * Ignore implied package structure for .g4 files and instead use this for all generated source.
//     */
//    val pkg = "com.zpw.myplayground.dependency.internal.grammar"
//    val dir = pkg.replace(".", "/")
//    outputDirectory = file("$buildDir/generated-src/antlr/main/$dir")
//    arguments = arguments + listOf(
//        // Specify the package declaration for generated Java source
//        "-package", pkg,
//        // Specify that generated Java source should go into the outputDirectory, regardless of package structure
//        "-Xexact-output-dir",
//        // Specify the location of "libs"; i.e., for grammars composed of multiple files
//        "-lib", "src/main/antlr/$dir"
//    )
//}

//tasks.compileKotlin {
//    dependsOn(antlr)
//}

gradlePlugin {
//    val includeBuildPlugin by plugins.creating {
//        id = "com.zpw.includegit"
//        implementationClass = "com.zpw.myplayground.gitinclude.MyIncludeGitPlugin"
//    }
//    val dependencyPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.dependency.DependencyAnalysisPlugin"
//    }
//    val condependencyPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.condependencies.MyConDependencyPlugin"
//    }
//    val transformPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.transform.MyTransformPlugin"
//    }
//    val dependencyGraphPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.dependencygraph.DependencyGraphGeneratorPlugin"
//    }
//    val doubleClickCheckPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.doubleclickcheck.DoubleClickPlugin"
//    }
//    val bigImageCheckPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.bigimage.BigImageCheckPlugin"
//    }
//    val fastBuildPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.fastbuild.ModuleArchivePlugin"
//    }
//    val focusPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.focus.MyFocusPlugin"
//    }
//    val injKitPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.injkit.InjKitPlugin"
//    }
//    val removeunusedresPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.removeusedres.RemoveUnusedResourcesPlugin"
//    }
//    val buildPropsPlugin by plugins.creating {
//        id = "com.zpw.myplugin"
//        implementationClass = "com.zpw.myplayground.buildprops.BuildPropsPlugin"
//    }
}

publishing {
    // ??????maven ??????
    repositories {
        maven {
            //?????????????????????
            mavenLocal()
        }
    }
    // ??????????????????
    publications {
        // ?????????????????????????????????????????? maven
        create<MavenPublication>("maven") {// ???????????????????????? MavenPublication
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
//            named("dependencyPlugin") {
//                displayName = "Gradle dependencyPlugin plugin"
//            }
//            named("condependencyPlugin") {
//                displayName = "Gradle condependencyPlugin plugin"
//            }
//            named("transformPlugin") {
//                displayName = "Gradle condependencyPlugin plugin"
//            }
//            named("dependencyGraphPlugin") {
//                displayName = "Gradle dependencyGraphPlugin plugin"
//            }
//            named("doubleClickCheckPlugin") {
//                displayName = "Gradle doubleClickCheckPlugin plugin"
//            }
//            named("bigImageCheckPlugin") {
//                displayName = "Gradle bigImageCheckPlugin plugin"
//            }
//            named("fastBuildPlugin") {
//                displayName = "Gradle fastBuildPlugin plugin"
//            }
//            named("focusPlugin") {
//                displayName = "Gradle focusPlugin plugin"
//            }
//            named("injKitPlugin") {
//                displayName = "Gradle injKitPlugin plugin"
//            }
//            named("removeunusedresPlugin") {
//                displayName = "Gradle RemoveUnusedResourcesPlugin plugin"
//            }
//            named("buildPropsPlugin") {
//                displayName = "Gradle BuildPropsPlugin plugin"
//            }
        }
        mavenCoordinates {
            groupId = "com.zpw.myplugin"
            artifactId = "my-plugin"
            version = "1.0.0-SNAPSHOT"
        }
    }
}