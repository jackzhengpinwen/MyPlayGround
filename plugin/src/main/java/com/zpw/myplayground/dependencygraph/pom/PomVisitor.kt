package com.zpw.myplayground.dependencygraph.pom

import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.zpw.myplayground.logger
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

object PomVisitor {
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
                        if (it.name == "pom.txt") {
                            val file = File(it.getName())
                            val os = FileOutputStream(file)
                            val zin = zip.getInputStream(it)
                            var line = 0
                            while ((zin.read().also { line = it }) != -1) {
                                os.write(line)
                            }
                            os.close()
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
            .dir("tmp/pom-build")
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