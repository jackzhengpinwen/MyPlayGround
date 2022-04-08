@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.tasks

import com.zpw.myplayground.dependency.TASK_GROUP_DEP
import com.zpw.myplayground.dependency.internal.toJson
import com.zpw.myplayground.dependency.internal.toPrettyString
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import javax.inject.Inject

open class AbiAnalysisAggregateReportTask @Inject constructor(
    objects: ObjectFactory
): DefaultTask() {

    init {
        group = TASK_GROUP_DEP
        description = "Aggregates ABI analysis reports across all subprojects"
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    lateinit var abiReports: Configuration

    @get:OutputFile
    val projectReport: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val projectReportPretty: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {
        // Outputs
        val projectReportFile = projectReport.get().asFile
        val projectReportPrettyFile = projectReportPretty.get().asFile
        // Cleanup prior execution
        projectReportFile.delete()
        projectReportPrettyFile.delete()

        val abiAnalysisReports = abiReports.dependencies.map { dependency ->
            val path = (dependency as ProjectDependency).dependencyProject.path

            val abiList = abiReports.fileCollection(dependency).files
                // There will only be one. This just makes it explicit.
                .first()
                .readLines()

            path to abiList
        }.toMap()

        projectReportFile.writeText(abiAnalysisReports.toJson())
        projectReportPrettyFile.writeText(abiAnalysisReports.toPrettyString())

        logger.quiet("Unused dependencies report: ${projectReportFile.path}")
        logger.quiet("Unused dependencies report, pretty-printed: ${projectReportPrettyFile.path}")

        // TODO write an HTML report
    }
}
