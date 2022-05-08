package com.zpw.myplayground.dependencygraph.resource

import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import java.io.File
import javax.inject.Inject

abstract class ResDetectTask @Inject constructor(
    objects: ObjectFactory
): DefaultTask() {
    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var resInputFiles: FileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var resOutputFiles: FileCollection = objects.fileCollection()

    @TaskAction
    fun action() {
        resInputFiles.files.forEach {
            logger.log("resInputFiles is ${it}")
        }

        val compileResourceArtifacts = project.configurations["debugCompileClasspath"].incoming.artifactView {
            attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, "android-res")
        }.artifacts
        compileResourceArtifacts.artifacts.forEach {
            logger.log("resourceArtifacts is ${it.file.absolutePath?:""}")
            val name = it.file.absolutePath?:""

        }

        resOutputFiles.files.forEach {
            logger.log("resOutputFiles is ${it}")
        }
    }
}