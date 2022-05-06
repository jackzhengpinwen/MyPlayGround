package com.zpw.myplayground.dependencygraph

import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.*

@CacheableTask
abstract class GenerateBuildRootTask: DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    lateinit var projectRoot: Configuration

    @TaskAction
    fun action() {
        logger.log("GenerateBuildRootTask action")
        projectRoot.dependencies.asSequence()
            .filterIsInstance<ProjectDependency>()
            .sortedBy {
                it.dependencyProject.path
            }.map { dependency ->
                logger.log("dependency is ${dependency}")
            }
    }
}