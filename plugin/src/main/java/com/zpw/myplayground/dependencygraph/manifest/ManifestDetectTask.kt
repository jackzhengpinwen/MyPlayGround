package com.zpw.myplayground.dependencygraph.manifest

import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class ManifestDetectTask @Inject constructor(
    objects: ObjectFactory
): DefaultTask() {
    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var manifestInputFiles: FileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var manifestOutputFiles: FileCollection = objects.fileCollection()

    @TaskAction
    fun action() {
        manifestInputFiles.files.forEach {
            logger.log("manifestInputFiles is ${it}")
        }

        manifestOutputFiles.files.forEach {
            logger.log("manifestOutputFiles is ${it}")
        }
    }
}