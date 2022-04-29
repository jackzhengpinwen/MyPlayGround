package com.zpw.myplayground.dependencygraph

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.zpw.myplayground.dependency.internal.capitalize
import com.zpw.myplayground.dependencygraph.dependency.DependencyDetectTask
import com.zpw.myplayground.dependencygraph.manifest.ManifestDetectTask
import com.zpw.myplayground.dependencygraph.manifest.ManifestVisitor
import com.zpw.myplayground.dependencygraph.proguard.ProguardDetectTask
import com.zpw.myplayground.log
import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.kotlin.dsl.*

class DependencyGraphGeneratorPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        logger.log("DependencyGraphGeneratorPlugin apply")

        // 给所有子项目注入插件
        if (project == project.rootProject) {
            logger.log("Adding root project tasks")
            project.subprojects {
                apply(plugin = "com.zpw.myplugin")
                // 过滤不需要的task
                project.tasks.matching {
                    it.name.startsWith("kapt") && it.name.endsWith("TestKotlin")
                }.configureEach {
                    this.enabled = false
                }
            }
        }

        // 给每个子module设置打包pom.xml操作
        project.pluginManager.withPlugin("com.android.library") {
            logger.log("Adding Android tasks to ${project.name}")
            project.afterEvaluate {
                the<LibraryExtension>().libraryVariants.all {
                    project.tasks.withType<GenerateMavenPom>().configureEach {
                        destination =
                            project.layout.buildDirectory.file("poms/pom.xml").get().asFile
                    }

                    project.tasks.named("bundle${this.name.capitalize()}Aar").configure {
                        val t = this as com.android.build.gradle.tasks.BundleAar
                        t.from(layout.buildDirectory.file("poms/pom.xml").get().asFile)
                    }
                }
            }
        }

        // 分析app module
        project.pluginManager.withPlugin("com.android.application") {
            logger.log("Adding Android tasks to ${project.name}")
            project.afterEvaluate {
                the<AppExtension>().applicationVariants.all {
                    if (this.name == "debug") {
                        configurations.forEach {
                            logger.log("configuration is ${it.name}")
                        }
                        // 解析系统产生的最终proguard配置
                        val minifyTask = project.tasks.getByName("minify${this.name.capitalize()}WithR8")
                        val proguardDetectTask = project.tasks.register<ProguardDetectTask>("proguardDetectTask") {
                                minifyInputFiles = minifyTask.inputs.files
                                minifyOutputFiles = minifyTask.outputs.files
                            }
                        // 解析系统产生的最终manifest配置
                        val manifestTask = project.tasks.getByName("process${this.name.capitalize()}Manifest")
                        val manifestDetectTask = project.tasks.register<ManifestDetectTask>("manifestDetectTask") {
                            manifestInputFiles = manifestTask.inputs.files
                            manifestOutputFiles = manifestTask.outputs.files
                        }
                        ManifestVisitor.generateReport(project, "debugCompileClasspath")
                    }
                }
            }
        }
    }
}