package com.zpw.myplayground.dependencygraph

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.tasks.BundleAar
import com.zpw.myplayground.dependency.internal.capitalize
import com.zpw.myplayground.dependencygraph.manifest.ManifestDetectTask
import com.zpw.myplayground.dependencygraph.proguard.ProguardDetectTask
import com.zpw.myplayground.dependencygraph.resource.ResDetectTask
import com.zpw.myplayground.dependencygraph.so.SoDetectTask
import com.zpw.myplayground.log
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.*
import java.io.File

internal class ProjectPlugin(private val project: Project) {

    fun apply() = project.run {
        // 给每个子module设置打包pom.xml操作
        pluginManager.withPlugin("com.android.library") {
            logger.log("Adding Android library tasks to ${name}")
            afterEvaluate {
                the<LibraryExtension>().libraryVariants.all {
                    // 修改 maven-publish 生成 pom.xml 的目录
                    tasks.withType<GenerateMavenPom>().configureEach {
                        destination = layout.buildDirectory.file("poms/pom.xml").get().asFile
                    }
                    // 将生成的 pom.xml 打包进 aar 中
                    tasks.named("bundle${this.name.capitalize()}Aar").configure {
                        val t = this as BundleAar
                        t.from(layout.buildDirectory.file("poms/pom.xml").get().asFile)
                    }
                }
            }
        }

        // 分析app module
        pluginManager.withPlugin("com.android.application") {
            logger.log("Adding Android application tasks to ${name}")
            afterEvaluate {
                the<AppExtension>().applicationVariants.all {
                    logger.log("variant is ${this.name}")
                    if (this.name == "debug") {
                        val source = sourceSets
                        // 解析系统产生的最终proguard配置
                        val minifyTask = tasks.getByName("minify${this.name.capitalize()}WithR8")
                        val proguardDetectTask = tasks.register<ProguardDetectTask>("proguardDetectTask") {
                            minifyInputFiles = minifyTask.inputs.files
                            minifyOutputFiles = minifyTask.outputs.files
                        }
                        // 解析系统产生的最终manifest配置
                        val manifestTask = tasks.getByName("process${this.name.capitalize()}Manifest")
                        val manifestDetectTask = tasks.register<ManifestDetectTask>("manifestDetectTask") {
                            manifestInputFiles = manifestTask.inputs.files
                            manifestOutputFiles = manifestTask.outputs.files
                        }
                        // 资源
                        val resTask = tasks.getByName("merge${this.name.capitalize()}Resources")
                        val resDetectTask = tasks.register<ResDetectTask>("resDetectTask") {
                            resInputFiles = resTask.inputs.files
                            resOutputFiles = resTask.outputs.files
                        }
                        tasks.register<ResDetectTask>("findAndroidResUsage")
                        // so
                        tasks.register<SoDetectTask>("findAndroidSoUsage")
                    }
                }
            }
        }
    }
}