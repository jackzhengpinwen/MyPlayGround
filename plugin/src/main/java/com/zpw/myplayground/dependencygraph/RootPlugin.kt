package com.zpw.myplayground.dependencygraph

import com.zpw.myplayground.log
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register

internal class RootPlugin(private val project: Project) {
    companion object {
        val confName = "rootConfig"
    }

    fun apply() = project.run {
        logger.log("Adding root project tasks")

        DependencyAnalysisExtension.create(this)
        val rootConf = configurations.create(confName) {
            isCanBeResolved = true
            isCanBeConsumed = false
        }

        afterEvaluate {
            // Must be inside afterEvaluate to access user configuration
            configureRootProject(rootConf = rootConf)
            conditionallyApplyToSubprojects()
        }
    }

    private fun Project.configureRootProject(rootConf: Configuration) {
        val generateRootTask = tasks.register<GenerateBuildRootTask>("generateBuildRoot") {
            dependsOn(rootConf)
            projectRoot = rootConf
        }
    }

    private fun Project.conditionallyApplyToSubprojects() {
        subprojects {
            apply(plugin = "com.zpw.myplugin")
            // 过滤不需要的task
            project.tasks.matching {
                it.name.startsWith("kapt") && it.name.endsWith("TestKotlin")
            }.configureEach {
                this.enabled = false
            }
        }
    }
}