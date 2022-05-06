package com.zpw.myplayground.dependencygraph.so

import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import java.io.File
import javax.inject.Inject

abstract class SoDetectTask @Inject constructor(
    objects: ObjectFactory
): DefaultTask() {
    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var soInputFiles: FileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var soOutputFiles: FileCollection = objects.fileCollection()

    @TaskAction
    fun action() {
        soInputFiles.files.forEach {
            logger.log("soInputFiles is ${it}")
        }

        val compileResourceArtifacts = project
            .configurations["debugCompileClasspath"]
            .incoming
            .artifactView {
                attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, "android-jni")
            }.artifacts
        compileResourceArtifacts.artifacts.forEach {
            logger.log("resourceArtifacts is ${it.file.absolutePath?:""}")
            val name = it.file.absolutePath?:""
            if(name.contains("library1")) {
                val dir = File(name)
                val queue = ArrayDeque<File>()
                queue.addAll(dir.listFiles())
                while (queue.isNotEmpty()) {
                    val file = queue.removeFirst()
                    if (file.isDirectory) {
                        logger.log("dir name: ${file.name}")
                        queue.addAll(file.listFiles())
                    } else {
                        logger.log("file name: ${file.name}")
                    }
                }
            }
        }

        soOutputFiles.files.forEach {
            logger.log("soOutputFiles is ${it}")
        }
    }
}