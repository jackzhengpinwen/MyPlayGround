package com.zpw.myplayground.dependency.collectAllTaskInfo

import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class TestAssembleDebugTask @Inject constructor(
    private val objects: ObjectFactory
): DefaultTask() {
    init {
        group = "verification"
        description = "Analyze Module Task"
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val inFiles: ConfigurableFileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val outFiles: ConfigurableFileCollection = objects.fileCollection()

    @TaskAction
    fun action() {
        logger.log("MyTestTask action")
    }
}