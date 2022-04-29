package com.zpw.myplayground.dependencygraph.proguard

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

abstract class ProguardDetectTask @Inject constructor(
    objects: ObjectFactory
): DefaultTask() {
    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var minifyInputFiles: FileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    var minifyOutputFiles: FileCollection = objects.fileCollection()

    @TaskAction
    fun action() {
        /**
         * app/proguard-rules.pro
         * library1-1.0/proguard.txt
         * library2-4.0/proguard.txt
         */
        minifyInputFiles.files.forEach {
            logger.log("minifyInputFiles is ${it}")
        }

        /**
         * app/build/outputs/mapping/debug/mapping.txt
         * app/build/outputs/mapping/debug/missing_rules.txt
         * app/build/intermediates/dex/debug/minifyDebugWithR8
         * app/build/intermediates/shrunk_java_res/debug/shrunkJavaRes.jar
         * app/build/outputs/mapping/debug/configuration.txt
         * app/build/outputs/mapping/debug/seeds.txt
         * app/build/outputs/mapping/debug/usage.txt
         */
        minifyOutputFiles.files.forEach {
            logger.log("minifyOutputFiles is ${it}")
        }
    }
}