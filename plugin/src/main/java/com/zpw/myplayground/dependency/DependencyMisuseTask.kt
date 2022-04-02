@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency

import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.log
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 *  生成未使用的直接依赖项和使用的传递依赖项的报告。
 */
open class DependencyMisuseTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    init {
        group = "verification"
        description = "Produces a report of unused direct dependencies and used transitive dependencies"
    }

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

    @TaskAction
    fun action() {
        logger.log("DependencyMisuseTask action")
        // Input 依赖的和引用的文件汇总
        val declaredDependenciesFile = declaredDependencies.get().asFile
        val usedClassesFile = usedClasses.get().asFile

        // Output
        val outputUnusedDependenciesFile = outputUnusedDependencies.get().asFile
        val outputUsedTransitivesFile = outputUsedTransitives.get().asFile

        // Cleanup prior execution
        outputUnusedDependenciesFile.delete()
        outputUsedTransitivesFile.delete()

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

        outputUnusedDependenciesFile.writeText(unusedLibs.joinToString("\n"))
        logger.quiet("Unused dependencies report: ${outputUnusedDependenciesFile.path}")
        logger.quiet("Unused dependencies:\n${unusedLibs.joinToString(separator = "\n- ", prefix = "- ")}\n")

        // TODO known issues:
        // 1. 应该排除 org.jetbrains.kotlin:kotlin-stdlib 吗？
        // 2. 生成的代码可能使用及物（例如使用 vanilla dagger 和 org.jetbrains:annotations 的 dagger.android）。
        // 3. 未使用的指示布局 XML 文件中引用的错误报告类（例如，androidx.constraintlayout:constraintlayout && androidx.constraintlayout.widget.ConstraintLayout）
        outputUsedTransitivesFile.writeText(usedTransitives.toJson())
        logger.quiet("Used transitive dependencies report: ${outputUsedTransitivesFile.path}")
        logger.quiet("Used transitive dependencies:\n${usedTransitives.toPrettyString()}")
    }
}