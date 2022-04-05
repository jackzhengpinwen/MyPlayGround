package com.zpw.myplayground.dependency

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.tasks.BundleLibraryClassesJar
import com.zpw.myplayground.dependency.collectAllTaskInfo.TestAssembleDebugTask
import com.zpw.myplayground.log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private const val ANDROID_APP_PLUGIN = "com.android.application"
private const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
private const val JAVA_LIBRARY_PLUGIN = "org.jetbrains.kotlin.jvm"

class DependencyAnalysisPlugin: Plugin<Project> {
    override fun apply(project: Project) = project.run {
        logger.log("DependencyAnalysisPluginChange apply ${project.name}")
        // pluginManager:此插件感知对象的插件管理器
        pluginManager
            // 应用指定插件时执行给定的操作
            .withPlugin(ANDROID_APP_PLUGIN)
            {
                logger.log("Adding Android tasks to ${project.name}")
                project.analyzeAndroidApplicationDependencies()
            }
        pluginManager.withPlugin(ANDROID_LIBRARY_PLUGIN) {
            logger.log("Adding Android tasks to ${project.name}")
            project.analyzeAndroidLibraryDependencies()
        }
        pluginManager.withPlugin(JAVA_LIBRARY_PLUGIN) {
            logger.log("Adding JVM tasks to ${project.path}")
            project.analyzeJavaLibraryDependencies()
        }
    }

    private fun Project.analyzeAndroidApplicationDependencies() {
        logger.log("analyzeAndroidApplicationDependencies")
        // afterEvaluate 在应用插件的模块配置结束后执行
        // 我们需要 afterEvaluate，这样我们才能获得对 `KotlinCompile` 任务的引用。
        afterEvaluate {
            logger.log("Application afterEvaluate is call")
//            project.analyzeAssembleDebug()
            // 返回指定类型的插件约定或扩展
            the<AppExtension>()
                // 返回应用项目包含的构建变体的集合
                .applicationVariants
                // 要处理此集合中的元素，应该使用 [all]迭代器。这是因为插件仅在评估项目后才会填充此集合。
                // 对该集合中的所有对象以及随后添加到该集合的任何对象执行给定的操作
                .all {
                    val androidClassAnalyzer = AppClassAnalyzer(project, this)
                    project.analyzeAndroidDependencies(androidClassAnalyzer)
                }
        }
    }

    private fun Project.analyzeAndroidLibraryDependencies() {
        logger.log("analyzeAndroidLibraryDependencies")
        afterEvaluate {
            logger.log("AndroidLibrary afterEvaluate is call")
            the<LibraryExtension>().libraryVariants.all {
                val androidClassAnalyzer = LibClassAnalyzer(project, this)
                project.analyzeAndroidDependencies(androidClassAnalyzer)
            }
        }
    }

    private fun Project.analyzeJavaLibraryDependencies() {
        logger.log("analyzeJavaLibraryDependencies")
        the<JavaPluginConvention>().sourceSets.forEach { sourceSet ->
            try {
                val javaModuleClassAnalyzer = JavaModuleClassAnalyzer(this, sourceSet)
                analyzeAndroidDependencies(javaModuleClassAnalyzer)
            } catch (e: UnknownTaskException) {
                logger.warn("Skipping tasks creation for sourceSet `${sourceSet.name}`")
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
                configurations[androidClassAnalyzer.compileConfigurationName]
                // 此配置的传入依赖项 ResolvableDependencies
                .incoming
                // 为这组依赖项解析的工件的视图 ArtifactView
                .artifactView {
                    // Attribute 是具有类型的命名实体
                    attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, androidClassAnalyzer.attributeValue)
                    // 将配置的依赖项转换成 Attribute 返回
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

                artifactFiles = configurations.getByName(androidClassAnalyzer.runtimeConfigurationName).incoming.artifactView {
                    attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, androidClassAnalyzer.attributeValue)
                }.artifacts.artifactFiles

                // 将 ArtifactsAnalysisTask 的输出作为 DependencyReportTask 的输入
                configurationName.set(androidClassAnalyzer.runtimeConfigurationName)
                allArtifacts.set(artifactsReportTask.flatMap { it.output })

                output.set(layout.buildDirectory.file(getAllDeclaredDepsPath(variantName)))
                outputPretty.set(layout.buildDirectory.file(getAllDeclaredDepsPrettyPath(variantName)))
            }

        // 4.生成一份报告，将真正引用的类和依赖的类做一个交集
        tasks.register("misusedDependencies$variantTaskName", DependencyMisuseTask::class.java) {
            artifactFiles = configurations.getByName(androidClassAnalyzer.runtimeConfigurationName).incoming.artifactView {
                attributes.attribute(AndroidArtifacts.ARTIFACT_TYPE, androidClassAnalyzer.attributeValue)
            }.artifacts.artifactFiles

            // 将依赖的类库和真正引用到的类库保存起来作为输入文件
            configurationName.set(androidClassAnalyzer.runtimeConfigurationName)
            declaredDependencies.set(dependencyReportTask.flatMap { it.output })
            usedClasses.set(analyzeClassesTask.flatMap { it.output })

//            logger.log("DependencyMisuseTask input path is ${dependencyReportTask.flatMap { it.output }.get().asFile.absolutePath}")
//            logger.log("DependencyMisuseTask input path is ${analyzeClassesTask.flatMap { it.output }.get().asFile.absolutePath}")

            outputUnusedDependencies.set(
                layout.buildDirectory.file(getUnusedDirectDependenciesPath(variantName))
            )
            outputUsedTransitives.set(
                layout.buildDirectory.file(getUsedTransitiveDependenciesPath(variantName))
            )
            outputHtml.set(
                layout.buildDirectory.file(getMisusedDependenciesHtmlPath(variantName))
            )
//            logger.log("UnusedDirectDependenciesPath outputPath is ${getUnusedDirectDependenciesPath(variantName)} " +
//                    "UsedTransitiveDependenciesPath outputPath is ${getUsedTransitiveDependenciesPath(variantName)}")
        }

        androidClassAnalyzer.registerAbiAnalysisTask(dependencyReportTask)
    }

    private class AppClassAnalyzer(
        private val project: Project,
        private val variant: BaseVariant
    ): AndroidClassAnalyzer<ClassAnalysisTask> {

        override val variantName: String = variant.name
        override val variantNameCapitalized: String = variantName.capitalize()
        override val compileConfigurationName = "${variantName}CompileClasspath"
        override val runtimeConfigurationName = "${variantName}RuntimeClasspath"
        override val attributeValue = "android-classes"

        override fun registerClassAnalysisTask(): TaskProvider<ClassListAnalysisTask> {
            // 按名称和类型定位对象，而不触发其创建或配置，如果没有这样的对象则失败
            val kotlinCompileTask = project.tasks.named("compile${variantNameCapitalized}Kotlin", KotlinCompile::class.java)
            // 尽管有不同的支持类（AndroidJavaCompile 和 JavaCompile）
            val javaCompileTask = project.tasks.named("compile${variantNameCapitalized}JavaWithJavac")

            val kaptStubs = project
                // 提供对该 project 的各种重要目录的访问
                .layout
                // 返回 project 的 build 目录
                .buildDirectory
                // 返回 FileTree，它允许查询包含在此目录中的文件和目录
                .asFileTree
                // 将此树的内容限制为与给定过滤器匹配的那些文件。过滤树是活动的，因此对该树的任何更改都会反映在过滤树中
                .matching {
                    include("**/kapt*/**/${variantName}/**/*.java")
                }

            return project.tasks
                // 定义一个新任务，将在需要时创建和配置。
                // 当使用诸如 TaskCollection#getByName(java.lang.String) 之类的查询方法定位任务时，
                // 当任务被添加到任务图中以执行时，或者当 Provider#get () 在此方法的返回值上调用
                .register("analyzeClassUsage$variantNameCapitalized", ClassListAnalysisTask::class.java)
                // configurationAction 要运行以配置任务的操作。此操作在需要任务时运行
                {
                    dependsOn(kotlinCompileTask, javaCompileTask)

                    // 将所有经过 kotlinc 和 javac 产生的.class文件都保存起来作为输入目录
                    // 向此集合添加一组源路径。给定的路径根据 {@link org.gradle.api.Project#files(Object...)} 进行评估
                    kotlinClasses.from(kotlinCompileTask.get().outputs.files)
                    javaClasses.from(javaCompileTask.get().outputs.files)
                    // 将经过 kapt 产生的.class文件都保存起来作为输入目录
                    kaptJavaStubs.from(kaptStubs)
                    // 将 res 文件夹中可能是用到的类的文件进行检查
                    layouts(variant.sourceSets.flatMap { it.resDirectories })

                    output.set(project.layout.buildDirectory.file(getAllUsedClassesPath(variantName)))
            }
        }
    }

    private class LibClassAnalyzer(
        private val project: Project,
        private val variant: BaseVariant
    ) : AndroidClassAnalyzer<JarAnalysisTask> {

        override val variantName: String = variant.name
        override val variantNameCapitalized: String = variantName.capitalize()
        override val compileConfigurationName = "${variantName}CompileClasspath"
        override val runtimeConfigurationName = "${variantName}RuntimeClasspath"
        override val attributeValue = "android-classes"

        private fun getBundleTask() = project.tasks.named("bundleLibCompileToJar$variantNameCapitalized", BundleLibraryClassesJar::class.java)

        override fun registerClassAnalysisTask(): TaskProvider<JarAnalysisTask> {
            val bundleTask = project.tasks.named("bundleLibCompileToJar$variantNameCapitalized", BundleLibraryClassesJar::class.java)

            val kaptStubs = project.layout.buildDirectory.asFileTree.matching {
                include("**/kapt*/**/${variantName}/**/*.java")
            }

            return project.tasks.register("analyzeClassUsage$variantNameCapitalized", JarAnalysisTask::class.java) {
                jar.set(bundleTask.flatMap { it.output })
                kaptJavaStubs.from(kaptStubs)
                layouts(variant.sourceSets.flatMap { it.resDirectories })

                output.set(project.layout.buildDirectory.file(getAllUsedClassesPath(variantName)))
            }
        }

        override fun registerAbiAnalysisTask(dependencyReportTask: TaskProvider<DependencyReportTask>) {
            project.tasks.register("abiAnalysis$variantNameCapitalized", AbiAnalysisTask::class.java) {
                jar.set(getBundleTask().flatMap { it.output })
                dependencies.set(dependencyReportTask.flatMap { it.output })

                output.set(project.layout.buildDirectory.file(getAbiAnalysisPath(variantName)))
                abiDump.set(project.layout.buildDirectory.file(getAbiDumpPath(variantName)))
            }
        }
    }

    private class JavaModuleClassAnalyzer(
        private val project: Project,
        private val sourceSet: SourceSet
    ) : AndroidClassAnalyzer<JarAnalysisTask> {

        override val variantName = sourceSet.name
        override val variantNameCapitalized = variantName.capitalize()
        // Yes, these two are the same for this case
        override val compileConfigurationName = "compileClasspath"
        override val runtimeConfigurationName = "compileClasspath"
        override val attributeValue = "jar"

        private fun getJarTask() = project.tasks.named(sourceSet.jarTaskName, Jar::class.java)

        override fun registerClassAnalysisTask(): TaskProvider<JarAnalysisTask> {
            val jarTask = project.tasks.named(sourceSet.jarTaskName, Jar::class.java)
            // Best guess as to path to kapt-generated Java stubs
            val kaptStubs = project.layout.buildDirectory.asFileTree.matching {
                include("**/kapt*/**/${variantName}/**/*.java")
            }

            return project.tasks.register("analyzeClassUsage$variantNameCapitalized", JarAnalysisTask::class.java) {
                jar.set(jarTask.flatMap { it.archiveFile })
                kaptJavaStubs.from(kaptStubs)
                output.set(project.layout.buildDirectory.file(getAllUsedClassesPath(variantName)))
            }
        }

        override fun registerAbiAnalysisTask(dependencyReportTask: TaskProvider<DependencyReportTask>) {
            project.tasks.register("abiAnalysis$variantNameCapitalized", AbiAnalysisTask::class.java) {
                jar.set(getJarTask().flatMap { it.archiveFile })
                dependencies.set(dependencyReportTask.flatMap { it.output })

                output.set(project.layout.buildDirectory.file(getAbiAnalysisPath(variantName)))
                abiDump.set(project.layout.buildDirectory.file(getAbiDumpPath(variantName)))
            }
        }
    }

    interface AndroidClassAnalyzer<T: ClassAnalysisTask> {
        val variantName: String
        val variantNameCapitalized: String
        val compileConfigurationName: String
        val runtimeConfigurationName: String
        val attributeValue: String

        // 这会生成一份报告，列出项目中所有使用的类
        fun registerClassAnalysisTask(): TaskProvider<out T>

        // 这对于 com.android.application 项目是无操作的，因为它们没有有意义的 ABI
        fun registerAbiAnalysisTask(dependencyReportTask: TaskProvider<DependencyReportTask>) = Unit
    }

    private fun Project.analyzeAssembleDebug() {
        val assembleTask = project.tasks.named("assembleDebug")
        project.tasks
            // 定义一个新任务，将在需要时创建和配置。
            // 当使用诸如 TaskCollection#getByName(java.lang.String) 之类的查询方法定位任务时，
            // 当任务被添加到任务图中以执行时，或者当 Provider#get () 在此方法的返回值上调用
            .register("analyzeAssembleDebugTask", TestAssembleDebugTask::class.java)
            // configurationAction 要运行以配置任务的操作。此操作在需要任务时运行
            {
                dependsOn(assembleTask)

                assembleTask.get().inputs.files.forEach {
                    logger.log("assembleTask input is ${it.absolutePath}")
                }
                assembleTask.get().outputs.files.forEach {
                    logger.log("assembleTask output is ${it.absolutePath}")
                }
                inFiles.from(assembleTask.get().inputs.files)
                outFiles.from(assembleTask.get().outputs.files)
            }
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

private fun getMisusedDependenciesHtmlPath(variantName: String) =
    "${getVariantDirectory(variantName)}/misused-dependencies.html"

private fun getAbiAnalysisPath(variantName: String) = "${getVariantDirectory(variantName)}/abi.txt"

private fun getAbiDumpPath(variantName: String) = "${getVariantDirectory(variantName)}/abi-dump.txt"