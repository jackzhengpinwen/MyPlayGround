package com.zpw.myplayground.dependency.tasks

import com.autonomousapps.internal.asm.ClassReader
import com.zpw.myplayground.dependency.internal.ClassAnalyzer
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
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

// 此正则表达式匹配 Java FQCN。
private val JAVA_FQCN_REGEX = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*".toRegex()

// ObjectFactory：用于创建各种模型对象的工厂
// 通过使用 javax.inject.Inject 注释公共构造函数或属性 getter 方法，可以将工厂实例注入任务、插件或其他对象。
// 它也可以通过 org.gradle.api.Project#getObjects() 获得。
open class ClassListAnalysisTask @Inject constructor(
    private val objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
): DefaultTask(), ClassAnalysisTask {
    init {
        group = "verification"
        description = "Produces a report of all classes referenced by a given jar"
    }

    /**
     * 通过对象工厂为field创建实例，提供外部配置
     */


    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val kotlinClasses: ConfigurableFileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val javaClasses: ConfigurableFileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val layoutFiles: ConfigurableFileCollection = objects.fileCollection()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val kaptJavaStubs: ConfigurableFileCollection = objects.fileCollection()

    @get:OutputFile
    override val output: RegularFileProperty = objects.fileProperty()

    internal fun layouts(files: List<File>) {
        for(file in files) {
            layoutFiles.from(
                objects.fileTree().from(file)
                    .matching {
                        include { it.path.contains("layout") }// 只过滤layout文件夹下的文件
                    }.files
            )
        }
    }

    @TaskAction
    fun action() {
        logger.log("ClassListAnalysisTask action")
        val reportFile: File = output.get().asFile

        reportFile.delete()

        // 汇总由 Javac 和 kotlinc 产生的所有class文件（有可能包含非.class文件）
        val inputClassFiles = javaClasses.asFileTree.plus(kotlinClasses.asFileTree)
            .filter {
                it.isFile && it.name.endsWith(".class")
            }
            .files

        javaClasses.asFileTree.forEach {
            logger.log("javaClasses --> ${it.absolutePath}")
        }
        kotlinClasses.asFileTree.forEach {
            logger.log("kotlinClasses --> ${it.absolutePath}")
        }
        layoutFiles.files.forEach {
            logger.log("layoutFiles --> ${it.absolutePath}")
        }
        kaptJavaStubs.files.forEach {
            logger.log("kaptJavaStubs --> ${it.absolutePath}")
        }
        logger.log("report output is ${reportFile.absolutePath}")

        // 在类名、字段、方法、注解中用到的类收集起来
        workerExecutor
            // 创建一个 WorkQueue 以提交工作以供异步执行而没有隔离
            .noIsolation()
            // 提交任务，传入参数
            .submit(ClassListAnalysisWorkAction::class.java)
            {
                classes = inputClassFiles
                layouts = layoutFiles.files
                kaptJavaSource = kaptJavaStubs.files
                report = reportFile
            }

        // 等待任务执行结束
        workerExecutor.await()

//        logger.log("Report:\n ${reportFile.readText()}")
    }
}

interface ClassListAnalysisParameters : WorkParameters {
    var classes: Set<File>
    var layouts: Set<File>
    var kaptJavaSource: Set<File>
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

        collectFromSource(parameters.kaptJavaSource, classNames)

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

    @get:Classpath
    val jar: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val layoutFiles: ConfigurableFileCollection = objects.fileCollection()

    /**
     * Java 源文件。由 kotlin-kapt 插件生成的存根。
     */
    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val kaptJavaStubs: ConfigurableFileCollection = objects.fileCollection()

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
            kaptJavaSource = kaptJavaStubs.files
            report = reportFile
        }
        workerExecutor.await()

        logger.debug("Report:\n${reportFile.readText()}")
    }
}

interface JarAnalysisParameters : WorkParameters {
    var jar: File
    var layouts: Set<File>
    var kaptJavaSource: Set<File>
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

        collectFromSource(parameters.kaptJavaSource, classNames)

        parameters.report.writeText(classNames.joinToString(separator = "\n"))
    }
}

private fun collectFromSource(kaptJavaSource: Set<File>, classNames: MutableSet<String>) {
    kaptJavaSource
        .flatMap { it.readLines() }
        // This is grabbing things that aren't class names. E.g., urls, method calls. Maybe it doesn't matter, though.
        // If they can't be associated with a module, then they're ignored later in the analysis. Some FQCN references
        // are only available via import statements; others via FQCN in the body. Should be improved, but it's unclear
        // how best.
        .flatMap { JAVA_FQCN_REGEX.findAll(it).toList() }
        .map { it.value }
        .map { it.removeSuffix(".class") }
        .fold(classNames) { set, item -> set.apply { add(item) } }
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