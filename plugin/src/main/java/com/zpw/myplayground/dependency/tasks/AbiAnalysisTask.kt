package com.zpw.myplayground.dependency.tasks

import com.zpw.myplayground.dependency.TASK_GROUP_DEP
import com.zpw.myplayground.dependency.internal.Component
import com.zpw.myplayground.dependency.internal.fromJsonList
import com.zpw.myplayground.dependency.internal.kotlin.abiDependencies
import com.zpw.myplayground.dependency.internal.toJson
import com.zpw.myplayground.log
import com.zpw.myplayground.logger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

open class AbiAnalysisTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
): DefaultTask() {

    init {
        group = TASK_GROUP_DEP
        description = "Produces a report of the ABI of this project"
    }

    @get:Classpath
    val jar: RegularFileProperty = objects.fileProperty()

    @get:InputFile
    val dependencies: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val output: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val abiDump: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {
        logger.log("AbiAnalysisTask action")

        logger.log("jar is ${jar.get().asFile.absolutePath}")
        logger.log("dependencies is ${dependencies.get().asFile.absolutePath}")

        workerExecutor.noIsolation()
            .submit(AbiAnalysisWorkAction::class.java) {
                jar.set(this@AbiAnalysisTask.jar)
                dependencies.set(this@AbiAnalysisTask.dependencies)
                output.set(this@AbiAnalysisTask.output)
                abiDump.set(this@AbiAnalysisTask.abiDump)
            }

        workerExecutor.await()

//        logger.log("output is ${output.get().asFile.absolutePath}")
//        logger.log("abiDump is ${abiDump.get().asFile.absolutePath}")
//        logger.log(
//            "These are your API dependencies:\n${output.get().asFile.readLines().joinToString(
//                prefix = "- ",
//                separator = "\n- "
//            )}"
//        )
    }
}

abstract class AbiAnalysisWorkAction: WorkAction<AbiAnalysisParameters> {
    override fun execute() {
        logger.log("AbiAnalysisWorkAction execute")
        val jarFile = parameters.jar.get().asFile
        val dependencies = parameters.dependencies.get().asFile.readText().fromJsonList<Component>()

        val reportFile = parameters.output.get().asFile
        val abiDumpFile = parameters.abiDump.get().asFile

        reportFile.delete()
        abiDumpFile.delete()

        val apiDependencies = abiDependencies(jarFile, dependencies, abiDumpFile)
        reportFile.writeText(apiDependencies.toJson())
//        apiDependencies.forEach {
//            logger.log("class is ${it}")
//        }
    }
}

interface AbiAnalysisParameters: WorkParameters {
    val jar: RegularFileProperty
    val dependencies: RegularFileProperty
    val output: RegularFileProperty
    val abiDump: RegularFileProperty
}

private fun Sequence<MatchResult>.allItems(): List<String> =
    flatMap { matchResult ->
        val groupValues = matchResult.groupValues
        // Ignore the 0th element, as it is the entire match
        if (groupValues.isNotEmpty()) groupValues.subList(1, groupValues.size).asSequence()
        else emptySequence()
    }.toList()