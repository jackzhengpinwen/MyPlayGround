package com.zpw.myplayground.dependencygraph.proguard

import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.zpw.myplayground.logger
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File
import java.util.zip.ZipFile

object ProguardVisitor {
    fun generateReport(project: Project, configurationName: String) {
        val report = StringBuilder()
        val config = project.configurations.firstOrNull { it.name == configurationName } as Configuration
        config.incoming.artifactView {
            attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, "aar")
        }.artifacts.artifactFiles.asFileTree.files.forEach {
            logger.log("artifact is ${it.absolutePath}")
            if (it.absolutePath.contains("com/zpw")) {
                val zip = ZipFile(it)
                zip.entries().toList()
                    .filterNot { it.isDirectory }
                    .map {
                        if (it.name == "proguard.txt") {
                            val buffer = zip.getInputStream(it).bufferedReader()
                            var line = ""
                            while ((buffer.readLine().also { line = it }).isNotBlank()) {
                                report.append(line).append("\n");
                            }
                        }
                    }
            }
        }
        buildDirOutputFile(project, configurationName).writeText(report.toString())
    }

    fun buildDirOutputFile(
        project: Project,
        configurationName: String
    ): File {
        return project.layout
            .buildDirectory
            .get()
            .dir("tmp/proguard-build")
            .file("$configurationName.txt")
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
}