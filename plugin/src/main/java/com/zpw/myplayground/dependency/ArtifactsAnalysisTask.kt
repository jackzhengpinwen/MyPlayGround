@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency

import com.zpw.myplayground.dependency.internal.Artifact
import com.zpw.myplayground.dependency.internal.toJson
import com.zpw.myplayground.dependency.internal.toPrettyString
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ArtifactCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * 生成给定项目所依赖的所有工件的报告。
 */
open class ArtifactsAnalysisTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    init {
        group = "verification"
        description = "Produces a report of all classes referenced by a given jar"
    }

    /**
     * 这是正确连接任务依赖项的“官方”输入，但未使用。
     */
    @get:InputFiles
    lateinit var artifactFiles: FileCollection

    /**
     * 这是任务实际用作其输入的内容。我们需要文件和工件元数据。
     */
    @get:Internal
    lateinit var artifacts: ArtifactCollection

    @get:OutputFile
    val output: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputPretty: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {
        val reportFile = output.get().asFile
        val reportPrettyFile = outputPretty.get().asFile

        // Cleanup prior execution
        reportFile.delete()
        reportPrettyFile.delete()

        val artifacts = artifacts.map {
            Artifact(
                componentIdentifier = it.id.componentIdentifier,
                file = it.file
            )
        }

        reportFile.writeText(artifacts.toJson())
        reportPrettyFile.writeText(artifacts.toPrettyString())
    }
}
