@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.tasks

import com.zpw.myplayground.dependency.TASK_GROUP_DEP
import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.log
import com.zpw.myplayground.logger
import kotlinx.html.*
import kotlinx.html.dom.create
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

/**
 *  生成未使用的直接依赖项和使用的传递依赖项的报告。
 */
open class DependencyMisuseTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    init {
        group = TASK_GROUP_DEP
        description = "Produces a report of unused direct dependencies and used transitive dependencies"
    }

    @get:Classpath
    lateinit var artifactFiles: FileCollection

    /**
     * This is what the task actually uses as its input.
     */
    @get:Internal
    lateinit var resolvedComponentResult: ResolvedComponentResult

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val declaredDependencies: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val usedClasses: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val usedInlineDependencies: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val usedConstantDependencies: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @get:InputFile
    val usedAndroidResDependencies: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputUnusedDependencies: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputUsedTransitives: RegularFileProperty = objects.fileProperty()

    @get:OutputFile
    val outputHtml: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun action() {
        logger.log("DependencyMisuseTask action")
        // Input 依赖的和引用的文件汇总
        val declaredDependenciesFile = declaredDependencies.get().asFile
        val usedClassesFile = usedClasses.get().asFile
        val usedInlineDependenciesFile = usedInlineDependencies.get().asFile
        val usedConstantDependenciesFile = usedConstantDependencies.get().asFile
        val usedAndroidResourcesFile = usedAndroidResDependencies.orNull?.asFile

        // Output
        val outputUnusedDependenciesFile = outputUnusedDependencies.get().asFile
        val outputUsedTransitivesFile = outputUsedTransitives.get().asFile
        val outputHtmlFile = outputHtml.get().asFile

        // Cleanup prior execution
        outputUnusedDependenciesFile.delete()
        outputUsedTransitivesFile.delete()
        outputHtmlFile.delete()

        val detector = MisusedDependencyDetector(
            declaredComponents = declaredDependenciesFile.readText().fromJsonList(),
            usedClasses = usedClassesFile.readLines(),
            usedInlineDependencies = usedInlineDependenciesFile.readText().fromJsonList(),
            usedConstantDependencies = usedConstantDependenciesFile.readText().fromJsonList(),
            usedAndroidResDependencies = usedAndroidResourcesFile?.readText()?.fromJsonList(),
            root = resolvedComponentResult
        )
        val dependencyReport = detector.detect()

        outputUnusedDependenciesFile.writeText(dependencyReport.unusedDepsWithTransitives.toJson())
        outputUsedTransitivesFile.writeText(dependencyReport.usedTransitives.toJson())
        logger.log(
//            """
//            |===Misused Dependencies===
//            |This report contains directly declared dependencies (in your `dependencies {}` block) which are either:
//            | 1. Completely unused; or
//            | 2. Unused except for transitive dependencies which _are_ used.
//            |    These used-transitives are either declared on the `compile` or `api` configurations (or the Maven equivalent)
//            |    of their respective projects. In some cases, it makes sense to simply use these transitive dependencies. In
//            |    others, it may be best to directly declare these transitive dependencies in your build script.
//            |
            """
            |Unused dependencies report:          ${outputUnusedDependenciesFile.path}
            |Used-transitive dependencies report: ${outputUsedTransitivesFile.path}
            |
            |Completely unused dependencies:
            |${if (dependencyReport.completelyUnusedDeps.isEmpty()) "none" else dependencyReport.completelyUnusedDeps.joinToString(
                separator = "\n- ",
                prefix = "- "
            )}
        """.trimMargin()
        )

//        // 依赖的和引用的类实例
//        val declaredLibraries = declaredDependenciesFile.readText().fromJsonList<Component>()
//        val usedClasses = usedClassesFile.readLines()
//
//        // 使用的类库
//        val unusedLibs = mutableListOf<String>()
//        // 间接引用的类库
//        val usedTransitives = mutableSetOf<TransitiveDependency>()
//        // 直接引用的类库
//        val usedDirectClasses = mutableListOf<String>()
//
//
//        declaredLibraries
//            // 排除具有零类文件的依赖项（例如 androidx.legacy:legacy-support-v4）
//            .filterNot { it.classes.isEmpty() }
//            .forEach { lib ->
//                var count = 0
//                val classes = sortedSetOf<String>()
//
//                lib.classes.forEach { declClass ->
//                    // 寻找未使用的直接依赖项
//                    if (!lib.isTransitive) { // 是直接引用的类库
//                        if (!usedClasses.contains(declClass)) { // 但是不是被引用的类
//                            // 未使用的类
//                            count++
//                        } else {
//                            // 使用的类
//                            usedDirectClasses.add(declClass)
//                        }
//                    }
//
//                    // 寻找使用过的传递依赖
//                    if (lib.isTransitive // 不是直接引用的类库
//                        // 将此列入黑名单
//                        && lib.identifier != "org.jetbrains.kotlin:kotlin-stdlib"
//                        // 假设所有这些都来自 android.jar
//                        && !declClass.startsWith("android.")
//                        && usedClasses.contains(declClass)
//                        // 不在使用的直接依赖项列表中
//                        && !usedDirectClasses.contains(declClass)
//                    ) {
//                        classes.add(declClass)
//                    }
//                }
//                // 如果没被使用的间接引用的类的数量等于类库中的数量，那么这个类库没被使用
//                if (count == lib.classes.size) {
//                    unusedLibs.add(lib.identifier)
//                }
//                // 这个直接引用的类库没被使用
//                if (classes.isNotEmpty()) {
//                    usedTransitives.add(TransitiveDependency(lib.identifier, classes))
//                }
//            }
//
//        val unusedDepsWithTransitives = unusedLibs.mapNotNull { unusedLib ->
//            root.dependencies.filterIsInstance<ResolvedDependencyResult>().find {
//                unusedLib == it.selected.id.asString()
//            }?.let {
//                relate(it, UnusedDirectDependency(unusedLib, mutableSetOf()), usedTransitives.toSet())
//            }
//        }.toSet()
//
//        val completelyUnusedDeps = unusedDepsWithTransitives
//            .filter { it.usedTransitiveDependencies.isEmpty() }
//            .map { it.identifier }
//            .toSortedSet()
//
//        outputUnusedDependenciesFile.writeText(unusedDepsWithTransitives.toJson())
//        outputUsedTransitivesFile.writeText(usedTransitives.toJson())
//        logger.quiet(
//            """===Misused Dependencies===
//            |This report contains directly declared dependencies (in your `dependencies {}` block) which are either:
//            | 1. Completely unused; or
//            | 2. Unused except for transitive dependencies which _are_ used.
//            |    These used-transitives are either declared on the `compile` or `api` configurations (or the Maven equivalent)
//            |    of their respective projects. In some cases, it makes sense to simply use these transitive dependencies. In
//            |    others, it may be best to directly declare these transitive dependencies in your build script.
//            |
//            |Unused dependencies report:          ${outputUnusedDependenciesFile.path}
//            |Used-transitive dependencies report: ${outputUsedTransitivesFile.path}
//            |
//            |Completely unused dependencies:
//            |${if (completelyUnusedDeps.isEmpty()) "none" else completelyUnusedDeps.joinToString(
//                separator = "\n- ",
//                prefix = "- "
//            )}
//        """.trimMargin()
//        )
//
//        writeHtmlReport(completelyUnusedDeps, unusedDepsWithTransitives, usedTransitives, outputHtmlFile)
    }
}

internal class MisusedDependencyDetector(
    private val declaredComponents: List<Component>,
    private val usedClasses: List<String>,
    private val usedInlineDependencies: List<Dependency>,
    private val usedConstantDependencies: List<Dependency>,
    private val usedAndroidResDependencies: List<Dependency>?,
    private val root: ResolvedComponentResult
) {
    /**
     * TODO this is still shit, but it's a first step towards testing and refactoring.
     */
    fun detect(): DependencyReport {
        val unusedLibs = mutableListOf<Dependency>()
        val usedTransitives = mutableSetOf<TransitiveComponent>()
        val usedDirectClasses = mutableSetOf<String>()

        // 遍历声明的依赖项
        declaredComponents
            // 排除不包含 class 文件的依赖项（例如 androidx.legacy:legacy-support-v4）
            .filterNot { it.classes.isEmpty() }
            .forEach { component ->
//                logger.log("declaredComponents is ${component}\n\n\n")
                var count = 0// 这个依赖项中未被使用的class数
                val classes = sortedSetOf<String>()// 间接使用的类
                // 遍历依赖项中的所有class
                component.classes.forEach { declClass ->
                    // 过滤直接依赖项
                    if (!component.isTransitive) {
                        // 使用的类中不包含这个直接依赖项的中的class
                        if (!usedClasses.contains(declClass)) {
                            // 未使用的class
                            count++
                        } else {
                            // 被使用的class
                            usedDirectClasses.add(declClass)
                        }
                    }

                    // 寻找使用过的传递依赖
                    if (component.isTransitive
                        // 假设所有这些都来自 android.jar
                        && !declClass.startsWith("android.")
                        && usedClasses.contains(declClass)
                        // 不在使用的直接依赖项集中
                        && !usedDirectClasses.contains(declClass)
                    ) {
                        // 间接使用的类
                        classes.add(declClass)
                    }
                }

                // 未使用的class数目等于这个依赖中的class数目
                if (count == component.classes.size
                    // 排除具有内联用法的模块
                    && component.hasNoInlineUsages()
                    // 排除使用 Android res 的模块
                    && component.hasNoAndroidResUsages()
                    // 排除使用常量的模块
                    && component.hasNoConstantUsages()
                ) {
                    unusedLibs.add(component.dependency)
                }
                // 间接使用的类
                if (classes.isNotEmpty()) {
                    usedTransitives.add(TransitiveComponent(component.dependency, classes))
                }
            }

        // 将 used-transitives 连接到直接依赖项
        val unusedDepsWithTransitives = unusedLibs.mapNotNull { unusedLib ->
            // 从直接依赖项过滤，看看是否使用了未使用的类库中依赖的类库
            root.dependencies.filterIsInstance<ResolvedDependencyResult>().find {
                unusedLib.identifier == it.selected.id.asString()
            }?.let {
                relate(it, UnusedDirectComponent(unusedLib, mutableSetOf()), usedTransitives.toSet())
            }
        }.toSet()
//        unusedDepsWithTransitives.forEach {
//            logger.log("unusedDepsWithTransitives is ${it}")
//        }

        // 这是为了打印到控制台。简化视图
        val completelyUnusedDeps = unusedDepsWithTransitives
            .filter { it.usedTransitiveDependencies.isEmpty() }
            .map { it.dependency.identifier }
            .toSortedSet()

        return DependencyReport(
            unusedDepsWithTransitives,
            usedTransitives,
            completelyUnusedDeps
        )
    }

    private fun Component.hasNoInlineUsages(): Boolean {
        return usedInlineDependencies.none { it == dependency }
    }

    private fun Component.hasNoAndroidResUsages(): Boolean {
        return usedAndroidResDependencies?.none { it == dependency } ?: true
    }

    private fun Component.hasNoConstantUsages(): Boolean {
        return usedConstantDependencies.none { it == dependency }
    }

    /**
     * 这个递归函数将使用的间接依赖项（未声明的依赖项，尽管如此直接使用）
     * 映射到直接依赖项（那些实际上在构建脚本中“直接”声明的依赖项）。
     */
    private fun relate(
        resolvedDependency: ResolvedDependencyResult,
        unusedDep: UnusedDirectComponent,
        transitives: Set<TransitiveComponent>
    ): UnusedDirectComponent {
        resolvedDependency.selected.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach {
            val identifier = it.selected.id.asString()
            val resolvedVersion = it.selected.id.resolvedVersion()

            if (transitives.map { trans -> trans.dependency.identifier }.contains(identifier)) {
                unusedDep.usedTransitiveDependencies.add(Dependency(identifier, resolvedVersion))
            }
            relate(it, unusedDep, transitives)
        }
        return unusedDep
    }

    internal class DependencyReport(
        val unusedDepsWithTransitives: Set<UnusedDirectComponent>,
        val usedTransitives: Set<TransitiveComponent>,
        val completelyUnusedDeps: Set<String>
    )
}

private fun relate(
    resolvedDependency: ResolvedDependencyResult,
    unusedDep: UnusedDirectDependency,
    transitives: Set<TransitiveDependency>
): UnusedDirectDependency {
    resolvedDependency.selected.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach {
        val identifier = it.selected.id.asString()
        if (transitives.map { it.identifier }.contains(identifier)) {
            unusedDep.usedTransitiveDependencies.add(identifier)
        }
        relate(it, unusedDep, transitives)
    }
    return unusedDep
}

private fun writeHtmlReport(
    completelyUnusedDeps: Set<String>,
    unusedDepsWithTransitives: Set<UnusedDirectDependency>,
    usedTransitives: Set<TransitiveDependency>,
    outputHtmlFile: File
) {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    document.create.html {
        head { title("Misused Dependencies Report") }
        body {
            h1 { +"Completely unused direct dependencies" }
            p {
                em { +"You can remove these" }
            }
            table {
                tr {
                    td {}
                    td { strong { +"Identifier" } }
                }
                completelyUnusedDeps.forEachIndexed { i, unusedDep ->
                    tr {
                        td { +"${i + 1}" }
                        td { +unusedDep }
                    }
                }
            }

            h1 { +"Used transitive dependencies" }
            p {
                em { +"You should consider declaring these as direct dependencies" }
            }
            table {
                tr {
                    td {}
                    td { strong { +"Identifier" } }
                }
                usedTransitives.forEachIndexed { i, trans ->
                    tr {
                        td { +"${i + 1}" }
                        td {
                            p { strong { +trans.identifier } }
                            p {
                                em { +"Used transitives" }
                                ul {
                                    trans.usedTransitiveClasses.forEach {
                                        li { +it }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            h1 { +"Unused direct dependencies" }
            p {
                em { +"You only use the transitive dependencies of these dependencies. In some cases, you can remove use of these and just declare the transitives directly. In other cases, you should continue to declare these. This report is provided for informational purposes." }
            }
            table {
                unusedDepsWithTransitives.forEachIndexed { i, unusedDep ->
                    tr {
                        // TODO is valign="bottom" supported?
                        td { +"${i + 1}" }
                        td {
                            strong { +unusedDep.identifier }
                            if (unusedDep.usedTransitiveDependencies.isNotEmpty()) {
                                p {
                                    em { +"Used transitives" }
                                    ul {
                                        unusedDep.usedTransitiveDependencies.forEach {
                                            li { +it }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }.writeToFile(outputHtmlFile)
}