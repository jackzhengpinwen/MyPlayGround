@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.tasks

import com.zpw.myplayground.dependency.TASK_GROUP_DEP
import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.log
import com.zpw.myplayground.logger
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.result.ResolutionResult
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.get
import javax.inject.Inject

/**
 *  此任务生成所有依赖项的报告，无论它们是否可传递，以及它们包含的类。
 */
@CacheableTask
open class DependencyReportTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        group = TASK_GROUP_DEP
        description = "Produces a report of all direct and transitive dependencies"
    }

    @get:Classpath
    val artifactFiles = objects.fileCollection()

    /**
     * This is what the task actually uses as its input. I really only care about the [ResolutionResult].
     */
    @get:Internal
    lateinit var configuration: Configuration

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val allArtifacts: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val output: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputPretty: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {
        logger.log("DependencyReportTask action")
        // Inputs
        val allArtifacts = allArtifacts.get().asFile.readText().fromJsonList<Artifact>()

        // Outputs
        val outputFile = output.get().asFile
        val outputPrettyFile = outputPretty.get().asFile

        // Cleanup prior execution
        outputFile.delete()
        outputPrettyFile.delete()

        // Actual work
        val components = ArtifactToComponentTransformer(
            // TODO I suspect I don't need to use the runtimeClasspath for getting this set of "direct artifacts"
            configuration,
            allArtifacts
        ).components()

//        components.forEach {
//            logger.log("components is ${it}")
//        }

//        // Step 1. 更新所有工件列表：传递与否？
//        // 运行时类路径只会给我直接依赖项
//        val dependencies: Set<DependencyResult> =
//            // 获取到RuntimeClasspath的配置 ConfigurationContainer
//            project.configurations.getByName(configurationName.get())
//            // 此配置的传入依赖项 ResolvableDependencies
//            .incoming
//            // 返回解析的依赖图，如果需要，执行解析。这将解析依赖关系图，但不会解析或下载文件。
//            .resolutionResult
//            .root
//            .dependencies
//
//        // 判断模块中直接引用的库列表
//        val directArtifacts = traverseDependencies(dependencies)
//
//        // 将模块中涉及间接引用的库列表进行标记
//        allArtifacts.forEach { dep ->
//            dep.isTransitive = !directArtifacts.any { it.identifier == dep.identifier }
//        }
//
//        // Step 2. 从每个 jar 中提取声明的类
//        val libraries = allArtifacts.filter {
//            if (!it.file!!.exists()) {
//                logger.error("File doesn't exist for dep $it")
//            }
//            it.file!!.exists()
//        }.map { dep ->
//            val z = ZipFile(dep.file)
//
//            val classes = z.entries().toList()
//                .filterNot { it.isDirectory }
//                .filter { it.name.endsWith(".class") }
//                .map { classEntry ->
//                    val reader = ClassReader(z.getInputStream(classEntry).readBytes())
//
//                    val classNameCollector = ClassNameCollector()
//                    reader.accept(classNameCollector, 0)
//                    classNameCollector
//                }
//                .mapNotNull { it.className }
//                .filterNot {
//                    // Filter out `java` packages, but not `javax`
//                    it.startsWith("java/")
//                }
//                .map { it.replace("/", ".") }
//                .toSortedSet()
//
//            Component(dep.identifier, dep.isTransitive!!, classes)
//        }.sorted()

        outputFile.writeText(components.toJson())
        outputPrettyFile.writeText(components.toPrettyString())
    }
}

//private fun traverseDependencies(results: Set<DependencyResult>): Set<Artifact> = results
//    // 返回一个列表，其中包含作为指定类型参数 R 的实例的所有元素。
//    .filterIsInstance<ResolvedDependencyResult>()
//    .map { result ->
//        val componentResult = result.selected
//
//        when (val componentIdentifier = componentResult.id) {
//            // 作为当前构建的一部分构建的组件实例的标识符。
//            is ProjectComponentIdentifier -> {
//                Artifact(componentIdentifier)
//            }
//            // 组件实例的标识符，可作为模块版本使用。
//            is ModuleComponentIdentifier -> {
//                Artifact(componentIdentifier)
//            }
//            else -> throw GradleException("Unexpected ComponentIdentifier type: ${componentIdentifier.javaClass.simpleName}")
//        }
//    }.toSet()

