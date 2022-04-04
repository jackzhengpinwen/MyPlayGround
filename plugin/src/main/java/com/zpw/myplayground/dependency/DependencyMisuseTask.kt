@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency

import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.log
import kotlinx.html.*
import kotlinx.html.dom.create
import org.gradle.api.DefaultTask
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
        group = "verification"
        description = "Produces a report of unused direct dependencies and used transitive dependencies"
    }

    @get:Classpath
    lateinit var artifactFiles: FileCollection

    @get:Internal
    val configurationName: Property<String> = objects.property(String::class.java)

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val declaredDependencies: RegularFileProperty = objects.fileProperty()

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    val usedClasses: RegularFileProperty = objects.fileProperty()

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
        val root = project.configurations.getByName(configurationName.get()).incoming.resolutionResult.root

        // Output
        val outputUnusedDependenciesFile = outputUnusedDependencies.get().asFile
        val outputUsedTransitivesFile = outputUsedTransitives.get().asFile
        val outputHtmlFile = outputHtml.get().asFile

        // Cleanup prior execution
        outputUnusedDependenciesFile.delete()
        outputUsedTransitivesFile.delete()
        outputHtmlFile.delete()

        // 依赖的和引用的类实例
        val declaredLibraries = declaredDependenciesFile.readText().fromJsonList<Component>()
        val usedClasses = usedClassesFile.readLines()

        // 使用的类库
        val unusedLibs = mutableListOf<String>()
        // 间接引用的类库
        val usedTransitives = mutableListOf<TransitiveDependency>()
        // 直接引用的类库
        val usedDirectClasses = mutableListOf<String>()


        declaredLibraries
            // 排除具有零类文件的依赖项（例如 androidx.legacy:legacy-support-v4）
            .filterNot { it.classes.isEmpty() }
            .forEach { lib ->
                var count = 0
                val classes = sortedSetOf<String>()

                lib.classes.forEach { declClass ->
                    // 寻找未使用的直接依赖项
                    if (!lib.isTransitive) { // 是直接引用的类库
                        if (!usedClasses.contains(declClass)) { // 但是不是被引用的类
                            // 未使用的类
                            count++
                        } else {
                            // 使用的类
                            usedDirectClasses.add(declClass)
                        }
                    }

                    // 寻找使用过的传递依赖
                    if (lib.isTransitive // 不是直接引用的类库
                        // 将此列入黑名单
                        && lib.identifier != "org.jetbrains.kotlin:kotlin-stdlib"
                        // 假设所有这些都来自 android.jar
                        && !declClass.startsWith("android.")
                        && usedClasses.contains(declClass)
                        // 不在使用的直接依赖项列表中
                        && !usedDirectClasses.contains(declClass)
                    ) {
                        classes.add(declClass)
                    }
                }
                // 如果没被使用的间接引用的类的数量等于类库中的数量，那么这个类库没被使用
                if (count == lib.classes.size) {
                    unusedLibs.add(lib.identifier)
                }
                // 这个直接引用的类库没被使用
                if (classes.isNotEmpty()) {
                    usedTransitives.add(TransitiveDependency(lib.identifier, classes))
                }
            }

        val unusedDepsWithTransitives = unusedLibs.mapNotNull { unusedLib ->
            root.dependencies.filterIsInstance<ResolvedDependencyResult>().find {
                unusedLib == it.selected.id.asString()
            }?.let {
                relate(it, UnusedDirectDependency(unusedLib, mutableSetOf()), usedTransitives.toSet())
            }
        }.toSet()

        val completelyUnusedDeps = unusedDepsWithTransitives
            .filter { it.usedTransitiveDependencies.isEmpty() }
            .map { it.identifier }
            .toSortedSet()

        outputUnusedDependenciesFile.writeText(unusedDepsWithTransitives.toJson())
        outputUsedTransitivesFile.writeText(usedTransitives.toJson())
        writeHtmlReport(completelyUnusedDeps, unusedDepsWithTransitives, usedTransitives, outputHtmlFile)
    }
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
    usedTransitives: MutableList<TransitiveDependency>,
    outputHtmlFile: File
) {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    document.create.html {
        head { title("Misused Dependencies Report") }
        body {
            h1 { +"Completely unused direct dependencies" }
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

            h1 { +"Unused direct dependencies" }
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

            h1 { +"Used transitive dependencies" }
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
        }
    }.writeToFile(outputHtmlFile)
}