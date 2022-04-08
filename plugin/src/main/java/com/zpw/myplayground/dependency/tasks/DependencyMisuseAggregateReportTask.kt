@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.tasks

import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.dependency.internal.TransitiveDependency
import com.zpw.myplayground.dependency.internal.UnusedDirectDependency
import com.zpw.myplayground.dependency.internal.fromJsonList
import com.zpw.myplayground.dependency.internal.toJson
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import com.zpw.myplayground.log
import javax.inject.Inject

open class DependencyMisuseAggregateReportTask @Inject constructor(
    objects: ObjectFactory
): DefaultTask() {

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    lateinit var unusedDependencyReports: Configuration

    @get:OutputFile
    val projectReport: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val projectReportPretty: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {
        logger.log("DependencyMisuseAggregateReportTask action")
        // Outputs
        val projectReportFile = projectReport.get().asFile
        val projectReportPrettyFile = projectReportPretty.get().asFile
        // Cleanup prior execution
        projectReportFile.delete()
        projectReportPrettyFile.delete()

        val unusedDirectDependencies = unusedDependencyReports.dependencies.map { dependency ->
            val path = (dependency as ProjectDependency).dependencyProject.path

            val unusedDependencies = unusedDependencyReports.fileCollection(dependency).files
                .first()
                .readText().fromJsonList<UnusedDirectDependency>()

            path to unusedDependencies
        }.toMap()

        projectReportFile.writeText(unusedDirectDependencies.toJson())
        projectReportPrettyFile.writeText(unusedDirectDependencies.toPrettyString())

        logger.quiet("Unused dependencies report: ${projectReportFile.path}")
        logger.quiet("Unused dependencies report, pretty-printed: ${projectReportPrettyFile.path}")

        // TODO write an HTML report
    }
}
