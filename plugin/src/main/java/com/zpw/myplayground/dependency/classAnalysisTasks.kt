package com.zpw.myplayground.dependency

import com.zpw.myplayground.dependency.internal.ClassAnalyzer
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.objectweb.asm.ClassReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.inject.Inject
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory


interface ClassAnalysisTask: Task {
    @get:OutputFile
    val output: RegularFileProperty
}

open class ClassListAnalysisTask @Inject constructor(
    private val objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
): DefaultTask(), ClassAnalysisTask {
    init {
        group = "verification"
        description = "Produces a report of all classes referenced by a given jar"
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val kotlinClasses: ConfigurableFileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val javaClasses: ConfigurableFileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val layoutFiles: ConfigurableFileCollection = objects.fileCollection()

    @get:OutputFile
    override val output: RegularFileProperty = objects.fileProperty()

    internal fun layouts(files: List<File>) {
        for(file in files) {
            layoutFiles.from(
                objects.fileTree().from(file)
                    .matching {
                        include { it.path.contains("layout") }
                    }.files
            )
        }
    }

    @TaskAction
    fun action() {
        logger.log("ClassListAnalysisTask action")
        val reportFile: File = output.get().asFile

        reportFile.delete()

        // 汇总由Java和kotlin产生的所有class文件
        val inputClassFiles = javaClasses.asFileTree.plus(kotlinClasses.asFileTree)
            .filter {
                it.isFile && it.name.endsWith(".class")
            }
            .files

        // 在类名、字段、方法、注解中用到的类收集起来
        workerExecutor.noIsolation().submit(ClassListAnalysisWorkAction::class.java) {
            classes = inputClassFiles
            layouts = layoutFiles.files
            report = reportFile
        }

        workerExecutor.await()

//        logger.log("Report:\n ${reportFile.readText()}")
    }
}

interface ClassListAnalysisParameters : WorkParameters {
    var classes: Set<File>
    var layouts: Set<File>
    var report: File
}

abstract class ClassListAnalysisWorkAction: WorkAction<ClassListAnalysisParameters> {
    override fun execute() {
        // 分析类文件集合中的类使用情况
        val classNames = parameters.classes
            .map {
                classFile -> classFile.inputStream().use { ClassReader(it) }
            }
            .collectClassNames()

        // 分析布局文件中的类使用情况
        parameters.layouts.map { layoutFile ->
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(layoutFile)
            document.documentElement.normalize()
            document.getElementsByTagName("*")
        }.flatMap { nodeList ->
            nodeList.map { it.nodeName }.filter { it.contains(".") }
        }.fold(classNames) { set, item -> set.apply { add(item) } }

        parameters.report.writeText(classNames.joinToString(separator = "\n"))
    }
}

/**
 * 生成给定 jar 引用的所有类的报告
 */
@CacheableTask
open class JarAnalysisTask @Inject constructor(
    private val objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask(), ClassAnalysisTask {

    init {
        group = "verification"
        description = "Produces a report of all classes referenced by a given jar"
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val jar: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val layoutFiles: ConfigurableFileCollection = objects.fileCollection()

    @get:OutputFile
    override val output: RegularFileProperty = objects.fileProperty()

    internal fun layouts(files: List<File>) {
        for (file in files) {
            layoutFiles.from(
                objects.fileTree().from(file)
                    .matching {
                        include { it.path.contains("layout") }
                    }.files
            )
        }
    }

    @TaskAction
    fun action() {
        val reportFile = output.get().asFile

        // Cleanup prior execution
        reportFile.delete()

        val jarFile = jar.get().asFile

        workerExecutor.noIsolation().submit(JarAnalysisWorkAction::class.java) {
            jar = jarFile
            layouts = layoutFiles.files
            report = reportFile
        }
        workerExecutor.await()

        logger.debug("Report:\n${reportFile.readText()}")
    }
}

interface JarAnalysisParameters : WorkParameters {
    var jar: File
    var layouts: Set<File>
    var report: File
}

abstract class JarAnalysisWorkAction : WorkAction<JarAnalysisParameters> {

    // TODO some jars only have metadata. What to do about them?
    // 1. e.g. kotlin-stdlib-common-1.3.50.jar
    // 2. e.g. legacy-support-v4-1.0.0/jars/classes.jar
    override fun execute() {
        val z = ZipFile(parameters.jar)

        val classNames = z.entries().toList()
            .filterNot { it.isDirectory }
            .filter { it.name.endsWith(".class") }
            .map { classEntry -> z.getInputStream(classEntry).use { ClassReader(it.readBytes()) } }
            .collectClassNames()

        // Analyze class usage in layout files
        parameters.layouts.map { layoutFile ->
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(layoutFile)
            document.documentElement.normalize()
            document.getElementsByTagName("*")
        }.flatMap { nodeList ->
            nodeList.map { it.nodeName }.filter { it.contains(".") }
        }.fold(classNames) { set, item -> set.apply { add(item) } }

        parameters.report.writeText(classNames.joinToString(separator = "\n"))
    }
}

private fun Iterable<ClassReader>.collectClassNames(): MutableSet<String> {
    return map {
        val classNameCollector = ClassAnalyzer()
        it.accept(classNameCollector, 0)
        classNameCollector
    }
        .flatMap { it.classes() }
        .filterNot {
//            logger.log("inputClassFiles after is ${it}", true)
            // Filter out `java` packages, but not `javax`
            it.startsWith("java/")
        }
        .map { it.replace("/", ".") }
        .toSortedSet()
}

private inline fun <R> NodeList.map(transform: (Node) -> R): List<R> {
    val destination = ArrayList<R>(length)
    for (i in 0 until length) {
        destination.add(transform(item(i)))
    }
    return destination
}