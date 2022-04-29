package com.zpw.myplayground.dependencygraph.dependency

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.diagnostics.DependencyReportTask

abstract class DependencyDetectTask: DependencyReportTask() {

    init {
        outputFile = project.layout
            .buildDirectory
            .get()
            .dir("tmp/dependency-build")
            .file("build-dependency.txt")
            .asFile
            .apply {
                parentFile.apply {
                    if (!exists()) {
                        // Create the "dependencies" directory if it does not exist
                        mkdirs()
                    }
                }
            }
    }

    @TaskAction
    override fun generate() {
        super.setConfiguration("debugCompileClasspath")
        this.outputFile = outputFile
    }
}