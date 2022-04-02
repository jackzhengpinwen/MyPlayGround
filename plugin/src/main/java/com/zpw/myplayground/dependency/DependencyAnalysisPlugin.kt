package com.zpw.myplayground.dependency

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.crash.afterEvaluate
import com.android.build.gradle.internal.tasks.BundleLibraryClassesJar
import com.zpw.myplayground.log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.attributes.Attribute
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private const val ANDROID_APP_PLUGIN = "com.android.application"
private const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
private const val KOTLIN_JVM_PLUGIN = "org.jetbrains.kotlin.jvm"

class DependencyAnalysisPlugin: Plugin<Project> {
    override fun apply(project: Project) = project.run {
        logger.log("DependencyAnalysisPluginChange apply ${project.name}")
        pluginManager.withPlugin(ANDROID_APP_PLUGIN) {
            logger.log("Adding Android tasks to ${project.name}")
            project.analyzeAndroidApplicationDependencies()
        }
        pluginManager.withPlugin(ANDROID_LIBRARY_PLUGIN) {
            logger.log("Adding Android tasks to ${project.name}")
            project.analyzeAndroidLibraryDependencies()
        }
    }

    private fun Project.analyzeAndroidApplicationDependencies() {
        logger.log("analyzeAndroidApplicationDependencies")
        // 我们需要 afterEvaluate，这样我们才能获得对 `KotlinCompile` 任务的引用。
        afterEvaluate {
            logger.log("Application afterEvaluate is call")
            the<AppExtension>().applicationVariants.all {
                val androidClassAnalyzer = AppClassAnalyzer(project, name)
                project.analyzeAndroidDependencies(androidClassAnalyzer)
            }
        }
    }

    private fun Project.analyzeAndroidLibraryDependencies() {
        logger.log("analyzeAndroidLibraryDependencies")
        afterEvaluate {
            logger.log("Library afterEvaluate is call")
            the<LibraryExtension>().libraryVariants.all {
                val androidClassAnalyzer = LibClassAnalyzer(project, name)
                project.analyzeAndroidDependencies(androidClassAnalyzer)
            }
        }
    }

    private fun <T: ClassAnalysisTask> Project.analyzeAndroidDependencies(androidClassAnalyzer: AndroidClassAnalyzer<T>) {
        // 将 `flavorDebug` 转换为 `FlavorDebug`
        val variantName = androidClassAnalyzer.variantName
        val variantTaskName = androidClassAnalyzer.variantNameCapitalized
        logger.log("variantName is ${name} variantTaskName is ${variantTaskName}")

        // 1.生成一份报告，列出项目中所有使用的类
        val analyzeClassesTask = androidClassAnalyzer.registerClassAnalysisTask()

        // 2.生成一份报告，列出所有直接和传递依赖关系、它们的工件和组件类型
        val artifactsReportTask = tasks.register("artifactsReport$variantTaskName", ArtifactsAnalysisTask::class.java) {

            val artifactCollection =
                // 获取到CompileClasspath的配置 ConfigurationContainer
                configurations["${variantName}CompileClasspath"]
                // 此配置的传入依赖项 ResolvableDependencies
                .incoming
                // 为这组依赖项解析的工件的视图 ArtifactView
                .artifactView {
                    // Attribute 是具有类型的命名实体
                    val attribute = Attribute.of("artifactType", String::class.java)
                    // 将配置的依赖项转换成 Attribute 返回
                    attributes.attribute(attribute, "android-classes")
                }.artifacts

            // 将获取的数据配置给任务
            artifactFiles = artifactCollection.artifactFiles
            artifacts = artifactCollection

            output.set(layout.buildDirectory.file(getArtifactsPath(variantName)))
            outputPretty.set(layout.buildDirectory.file(getArtifactsPrettyPath(variantName)))
        }

        // 3.生成一份报告，列出模块中依赖的所有库以及库中包含的类，标明了这个库是直接还是间接依赖，内部类也算在里面
        val dependencyReportTask =
            tasks.register("dependenciesReport$variantTaskName", DependencyReportTask::class.java) {
                dependsOn(artifactsReportTask)

                this.variantName.set(variantName)
                // 将 ArtifactsAnalysisTask 的输出作为 DependencyReportTask 的输入
                allArtifacts.set(artifactsReportTask.flatMap { it.output })

                output.set(layout.buildDirectory.file(getAllDeclaredDepsPath(variantName)))
                outputPretty.set(layout.buildDirectory.file(getAllDeclaredDepsPrettyPath(variantName)))
            }

        // 4.生成一份报告，将真正引用的类和依赖的类做一个交集
        tasks.register("misusedDependencies$variantTaskName", DependencyMisuseTask::class.java) {
            // 将依赖的类库和真正引用到的类库保存起来作为输入文件
            declaredDependencies.set(dependencyReportTask.flatMap { it.output })
            usedClasses.set(analyzeClassesTask.flatMap { it.output })

            logger.log("DependencyMisuseTask input path is ${dependencyReportTask.flatMap { it.output }.get().asFile.absolutePath}")
            logger.log("DependencyMisuseTask input path is ${analyzeClassesTask.flatMap { it.output }.get().asFile.absolutePath}")

            outputUnusedDependencies.set(
                layout.buildDirectory.file(getUnusedDirectDependenciesPath(variantName))
            )
            outputUsedTransitives.set(
                layout.buildDirectory.file(getUsedTransitiveDependenciesPath(variantName))
            )
            logger.log("UnusedDirectDependenciesPath outputPath is ${getUnusedDirectDependenciesPath(variantName)} " +
                    "UsedTransitiveDependenciesPath outputPath is ${getUsedTransitiveDependenciesPath(variantName)}")
        }
    }

    private class AppClassAnalyzer(
        private val project: Project,
        override val variantName: String
    ): AndroidClassAnalyzer<ClassAnalysisTask> {

        override val variantNameCapitalized: String = variantName.capitalized()

        override fun registerClassAnalysisTask(): TaskProvider<ClassListAnalysisTask> {
            // Known to exist in Kotlin 1.3.50.
            val kotlinCompileTask = project.tasks.named("compile${variantNameCapitalized}Kotlin", KotlinCompile::class.java)
            // Known to exist in AGP 3.5 and 3.6, albeit with different backing classes (AndroidJavaCompile and JavaCompile)
            val javaCompileTask = project.tasks.named("compile${variantNameCapitalized}JavaWithJavac")

            return project.tasks.register("analyzeClassUsage$variantNameCapitalized", ClassListAnalysisTask::class.java) {
                dependsOn(kotlinCompileTask, javaCompileTask)

                // TODO would be nice if these outputs carried task dependencies
                // 将所有产生的.class文件都保存起来作为输入目录
                kotlinClasses.from(kotlinCompileTask.get().outputs.files)
                javaClasses.from(javaCompileTask.get().outputs.files)

                output.set(project.layout.buildDirectory.file(getAllUsedClassesPath(variantName)))
            }
        }

    }

    private class LibClassAnalyzer(
        private val project: Project,
        override val variantName: String
    ) : AndroidClassAnalyzer<JarAnalysisTask> {

        override val variantNameCapitalized: String = variantName.capitalize()

        override fun registerClassAnalysisTask(): TaskProvider<JarAnalysisTask> {
            // Known to exist in AGP 3.5 and 3.6
            val bundleTask = project.tasks.named("bundleLibCompile$variantNameCapitalized", BundleLibraryClassesJar::class.java)

            return project.tasks.register("analyzeClassUsage$variantNameCapitalized", JarAnalysisTask::class.java) {
                jar.set(bundleTask.flatMap { it.output })
                output.set(project.layout.buildDirectory.file(getAllUsedClassesPath(variantName)))
            }
        }
    }

    interface AndroidClassAnalyzer<T: ClassAnalysisTask> {
        val variantName: String
        val variantNameCapitalized: String

        fun registerClassAnalysisTask(): TaskProvider<out T>
    }
}

private fun getVariantDirectory(variantName: String) = "dependency-analysis/$variantName"

private fun getArtifactsPath(variantName: String) = "${getVariantDirectory(variantName)}/artifacts.txt"

private fun getArtifactsPrettyPath(variantName: String) = "${getVariantDirectory(variantName)}/artifacts-pretty.txt"

private fun getAllUsedClassesPath(variantName: String) = "${getVariantDirectory(variantName)}/all-used-classes.txt"

private fun getAllDeclaredDepsPath(variantName: String) =
    "${getVariantDirectory(variantName)}/all-declared-dependencies.txt"

private fun getAllDeclaredDepsPrettyPath(variantName: String) =
    "${getVariantDirectory(variantName)}/all-declared-dependencies-pretty.txt"

private fun getUnusedDirectDependenciesPath(variantName: String) =
    "${getVariantDirectory(variantName)}/unused-direct-dependencies.txt"

private fun getUsedTransitiveDependenciesPath(variantName: String) =
    "${getVariantDirectory(variantName)}/used-transitive-dependencies.txt"