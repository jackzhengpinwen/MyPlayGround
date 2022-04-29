package com.zpw.myplayground.dependency

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.zpw.myplayground.dependency.collectAllTaskInfo.TestAssembleDebugTask
import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.dependency.internal.AndroidAppAnalyzer
import com.zpw.myplayground.dependency.internal.AndroidLibAnalyzer
import com.zpw.myplayground.dependency.internal.ConfigurationsToDependenciesTransformer
import com.zpw.myplayground.dependency.internal.DependencyAnalyzer
import com.zpw.myplayground.dependency.internal.JavaLibAnalyzer
import com.zpw.myplayground.dependency.tasks.*
import com.zpw.myplayground.log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private const val ANDROID_APP_PLUGIN = "com.android.application"
private const val ANDROID_LIBRARY_PLUGIN = "com.android.library"
private const val JAVA_LIBRARY_PLUGIN = "org.jetbrains.kotlin.jvm"

private const val EXTENSION_NAME = "dependencyAnalysis"

private const val CONF_DEPENDENCY_REPORT = "dependencyReport"
private const val CONF_ABI_REPORT = "abiReport"
private const val CONF_ADVICE_REPORT = "adviceReport"

internal const val TASK_GROUP_DEP = "dependency-analysis"

class DependencyAnalysisPlugin: Plugin<Project> {
    private fun Project.getExtension(): DependencyAnalysisExtension? =
        rootProject.extensions.findByType()!!

    private val artifactAdded = AtomicBoolean(false)
    private lateinit var ANDROID_GRADLE_PLUGIN_VERSION: String

    override fun apply(project: Project) = project.run {
        logger.log("DependencyAnalysisPluginChange apply ${project.name}")
        ANDROID_GRADLE_PLUGIN_VERSION = version.toString()
        // pluginManager:此插件感知对象的插件管理器
        pluginManager
            // 应用指定插件时执行给定的操作
            .withPlugin(ANDROID_APP_PLUGIN)
            {
                logger.log("Adding Android tasks to ${project.name}")
                project.configureAndroidAppProject()
            }
        pluginManager.withPlugin(ANDROID_LIBRARY_PLUGIN) {
            logger.log("Adding Android tasks to ${project.name}")
            project.configureAndroidLibProject()
        }
        pluginManager.withPlugin(JAVA_LIBRARY_PLUGIN) {
            logger.log("Adding JVM tasks to ${project.path}")
            getExtension()!!.theVariants.convention(listOf(JAVA_LIB_SOURCE_SET_DEFAULT))
            project.analyzeJavaLibraryDependencies()
        }
        if (this == rootProject) {
            logger.debug("Adding root project tasks")
            extensions.create<DependencyAnalysisExtension>(EXTENSION_NAME, objects)
            configureRootProject()
            subprojects {
                apply(plugin = "com.zpw.myplugin")
            }
        }
    }

    private fun Project.configureAndroidAppProject() {
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
                    val androidClassAnalyzer = AndroidAppAnalyzer(
                        this@configureAndroidAppProject,
                        this,
                        ANDROID_GRADLE_PLUGIN_VERSION)
                    project.analyzeDependencies(androidClassAnalyzer)
                }
        }
    }

    private fun Project.configureAndroidLibProject() {
        logger.log("analyzeAndroidLibraryDependencies")
        afterEvaluate {
            logger.log("AndroidLibrary afterEvaluate is call")
//            project.analyzeLibDebug()
            the<LibraryExtension>().libraryVariants.all {
                val androidClassAnalyzer = AndroidLibAnalyzer(
                    project,
                    this,
                    ANDROID_GRADLE_PLUGIN_VERSION)
                project.analyzeDependencies(androidClassAnalyzer)
            }
        }
    }

    private fun Project.analyzeJavaLibraryDependencies() {
        logger.log("analyzeJavaLibraryDependencies")
        the<JavaPluginConvention>().sourceSets
            .filterNot { it.name == "test" }
            .forEach { sourceSet ->
            try {
                val javaModuleClassAnalyzer = JavaLibAnalyzer(this, sourceSet)
                project.analyzeDependencies(javaModuleClassAnalyzer)
            } catch (e: UnknownTaskException) {
                logger.warn("Skipping tasks creation for sourceSet `${sourceSet.name}`")
            }
        }
    }

    private fun <T: ClassAnalysisTask> Project.analyzeDependencies(dependencyAnalyzer: DependencyAnalyzer<T>) {
        // 将 `flavorDebug` 转换为 `FlavorDebug`
        val variantName = dependencyAnalyzer.variantName
        val variantTaskName = dependencyAnalyzer.variantNameCapitalized
        logger.log("variantName is ${name} variantTaskName is ${variantTaskName}")

        // 2.生成一份报告，列出所有直接和传递依赖关系、它们的工件和组件类型
        val artifactsReportTask =
            tasks.register<ArtifactsAnalysisTask>("artifactsReport$variantTaskName") {
                logger.log("project is ${project.name}, task is ${name}")
                logger.log("compileConfigurationName is ${dependencyAnalyzer.compileConfigurationName}")

                // 获取到debugCompileClasspath的配置 ConfigurationContainer
                val configurationContainer = configurations[dependencyAnalyzer.compileConfigurationName]
                // 此配置的传入依赖项 ResolvableDependencies
                val resolvableDependencies = configurationContainer.incoming
                // 为这组依赖项解析的工件的视图 Artifacts
                val artifactCollection = resolvableDependencies.artifactView {
                    // Attribute 是具有类型的命名实体
                    attributes.attribute(dependencyAnalyzer.attribute, dependencyAnalyzer.attributeValue)
                    // 将配置的依赖项转换成 Attribute 返回
                }.artifacts

                // 将获取的数据配置给任务
                setArtifacts(artifactCollection)
    //            logger.log("artifactFiles has ${artifactCollection.artifactFiles.asFileTree.files.size}")
    //            artifactCollection.artifactFiles.asFileTree.files.forEach {
    //                /**
    //                 * 就是 configuration 包含的依赖，包括所有jar包，还有将
    //                 * build.gradle dependency 转换为jar包
    //                 */
    //                logger.log("artifact is ${it.absolutePath}")
    //            }

                val dependencyConfs = ConfigurationsToDependenciesTransformer(variantName, project)
                    .dependencyConfigurations()
                dependencyConfigurations.set(dependencyConfs)
//                logger.log("dependencyConfs is ${dependencyConfs}")

                output.set(layout.buildDirectory.file(getArtifactsPath(variantName)))
                outputPretty.set(layout.buildDirectory.file(getArtifactsPrettyPath(variantName)))
        }

        // 3.生成一份报告，列出模块中依赖的所有库以及库中包含的类，标明了这个库是直接还是间接依赖，内部类也算在里面
        val dependencyReportTask =
            tasks.register<DependencyReportTask>("dependenciesReport$variantTaskName") {
                logger.log("project is ${project.name}, task is ${name}")
                logger.log("runtimeConfigurationName is ${dependencyAnalyzer.runtimeConfigurationName}")

                val runtimeClasspath = configurations[dependencyAnalyzer.runtimeConfigurationName]
                configuration = runtimeClasspath

                val artifactCollection = runtimeClasspath
                    .incoming
                    .artifactView {
                        attributes.attribute(dependencyAnalyzer.attribute,
                            dependencyAnalyzer.attributeValue)
                    }.artifacts.artifactFiles

//                artifactCollection.asFileTree.files.forEach {
//                    logger.log("artifact is ${it.absolutePath}")
//                }

                artifactFiles.setFrom(
                    artifactCollection
                )

                // 将 ArtifactsAnalysisTask 的输出作为 DependencyReportTask 的输入
                allArtifacts.set(artifactsReportTask.flatMap { it.output })

                output.set(layout.buildDirectory.file(getAllDeclaredDepsPath(variantName)))
                outputPretty.set(layout.buildDirectory.file(getAllDeclaredDepsPrettyPath(variantName)))
            }

        // 内联函数使用分析
        val inlineTask = tasks.register<InlineMemberExtractionTask>("inlineMemberExtractor$variantTaskName") {
            artifacts.set(artifactsReportTask.flatMap { it.output })
            kotlinSourceFiles.setFrom(dependencyAnalyzer.kotlinSourceFiles)
//            dependencyAnalyzer.kotlinSourceFiles.asFileTree.files.forEach {
//                logger.log("kotlinSourceFiles is ${it.absolutePath}")
//            }
            inlineMembersReport.set(layout.buildDirectory.file(getInlineMembersPath(variantName)))
            inlineUsageReport.set(layout.buildDirectory.file(getInlineUsagePath(variantName)))
        }

        // 常量使用分析
        val constantTask = tasks.register<ConstantUsageDetectionTask>("constantUsageDetector$variantTaskName") {
            artifacts.set(artifactsReportTask.flatMap { it.output })
            javaSourceFiles.setFrom(dependencyAnalyzer.javaSourceFiles)
            kotlinSourceFiles.setFrom(dependencyAnalyzer.kotlinSourceFiles)
//            dependencyAnalyzer.kotlinSourceFiles.asFileTree.files.forEach {
//                logger.log("kotlinSourceFiles is ${it.absolutePath}")
//            }
//            dependencyAnalyzer.javaSourceFiles?.asFileTree?.files?.forEach {
//                logger.log("javaSourceFiles is ${it.absolutePath}")
//            }
            constantUsageReport.set(layout.buildDirectory.file(getConstantUsagePath(variantName)))
        }

        // 资源使用分析
        val androidResUsageTask = dependencyAnalyzer.registerAndroidResAnalysisTask()

        // 1.生成一份报告，列出项目中所有使用的类
        val analyzeClassesTask = dependencyAnalyzer.registerClassAnalysisTask()

        // 4.生成一份报告，将真正引用的类和依赖的类做一个交集
        val misusedDependenciesTask = tasks.register<DependencyMisuseTask>("misusedDependencies$variantTaskName") {
            logger.log("project is ${project.name}, task is ${name}")
            logger.log("runtimeConfigurationName is ${dependencyAnalyzer.runtimeConfigurationName}")

            val runtimeConfiguration = configurations[dependencyAnalyzer.runtimeConfigurationName]

            artifactFiles =
                runtimeConfiguration.incoming.artifactView {
                    attributes.attribute(dependencyAnalyzer.attribute, dependencyAnalyzer.attributeValue)
                }.artifacts.artifactFiles

            /**
             * appcompat-1.4.1-runtime.jar
             */
//            artifactFiles.asFileTree.files.forEach {
//                logger.log("artifact is ${it.absolutePath}")
//            }

            resolvedComponentResult = runtimeConfiguration
                .incoming
                .resolutionResult
                .root

            /**
             * androidx.appcompat:appcompat:1.4.1
             */
//            resolvedComponentResult.dependencies.forEach {
//                logger.log("resolvedComponent is ${it}")
//            }

            // 将依赖的类库和真正引用到的类库保存起来作为输入文件
            declaredDependencies.set(dependencyReportTask.flatMap { it.output })
            usedClasses.set(analyzeClassesTask.flatMap { it.output })
            usedInlineDependencies.set(inlineTask.flatMap { it.inlineUsageReport })
            usedConstantDependencies.set(constantTask.flatMap { it.constantUsageReport })
            androidResUsageTask?.let { task ->
                usedAndroidResDependencies.set(task.flatMap { it.usedAndroidResDependencies })
            }

            outputUnusedDependencies.set(
                layout.buildDirectory.file(getUnusedDirectDependenciesPath(variantName))
            )
            outputUsedTransitives.set(
                layout.buildDirectory.file(getUsedTransitiveDependenciesPath(variantName))
            )
            outputHtml.set(
                layout.buildDirectory.file(getMisusedDependenciesHtmlPath(variantName))
            )
        }

        val abiAnalysisTask = dependencyAnalyzer.registerAbiAnalysisTask(dependencyReportTask)

        val adviceTask = tasks.register<AdviceTask>("advice$variantTaskName") {
            unusedDependenciesReport.set(misusedDependenciesTask.flatMap { it.outputUnusedDependencies })
            usedTransitiveDependenciesReport.set(misusedDependenciesTask.flatMap { it.outputUsedTransitives })
            abiAnalysisTask?.let { task ->
                abiDependenciesReport.set(task.flatMap { it.output })
            }
            allDeclaredDependenciesReport.set(artifactsReportTask.flatMap { it.output })

            // Failure states
            with(getExtension()!!.issueHandler) {
                failOnAny.set(anyIssue.behavior)
                failOnUnusedDependencies.set(unusedDependenciesIssue.behavior)
                failOnUsedTransitiveDependencies.set(usedTransitiveDependenciesIssue.behavior)
                failOnIncorrectConfiguration.set(incorrectConfigurationIssue.behavior)
            }

            adviceReport.set(layout.buildDirectory.file(getAdvicePath(variantName)))
        }

        // Adds terminal artifacts to custom configurations to be consumed by root project for aggregate reports.
        maybeAddArtifact(misusedDependenciesTask, abiAnalysisTask, adviceTask, variantName)
    }

    private fun Project.maybeAddArtifact(
        misusedDependenciesTask: TaskProvider<DependencyMisuseTask>,
        abiAnalysisTask: TaskProvider<AbiAnalysisTask>?,
        adviceTask: TaskProvider<AdviceTask>,
        variantName: String
    ) {
        // We must only do this once per project
        if (!shouldAddArtifact(variantName)) {
            return
        }
        artifactAdded.set(true)

        // Configure misused dependencies aggregate and advice tasks
        val dependencyReportsConf = configurations.create(CONF_DEPENDENCY_REPORT) {
            isCanBeResolved = false
        }
        val adviceReportsConf = configurations.create(CONF_ADVICE_REPORT) {
            isCanBeResolved = false
        }
        artifacts {
            add(dependencyReportsConf.name, layout.buildDirectory.file(getUnusedDirectDependenciesPath(variantName))) {
                builtBy(misusedDependenciesTask)
            }
            add(adviceReportsConf.name, layout.buildDirectory.file(getAdvicePath(variantName))) {
                builtBy(adviceTask)
            }
        }
        // Add project dependency on root project to this project, with our new configurations
        rootProject.dependencies {
            add(dependencyReportsConf.name, project(this@maybeAddArtifact.path, dependencyReportsConf.name))
            add(adviceReportsConf.name, project(this@maybeAddArtifact.path, adviceReportsConf.name))
        }

        // Configure ABI analysis aggregate task
        abiAnalysisTask?.let {
            val abiReportsConf = configurations.create(CONF_ABI_REPORT) {
                isCanBeResolved = false
            }
            artifacts {
                add(abiReportsConf.name, layout.buildDirectory.file(getAbiAnalysisPath(variantName))) {
                    builtBy(abiAnalysisTask)
                }
            }
            // Add project dependency on root project to this project, with our new configuration
            rootProject.dependencies {
                add(abiReportsConf.name, project(this@maybeAddArtifact.path, abiReportsConf.name))
            }
        }
    }

    private fun Project.shouldAddArtifact(variantName: String): Boolean {
        if (artifactAdded.get()) {
            return false
        }

        return getExtension()!!.getFallbacks().contains(variantName)
    }

    private fun Project.configureRootProject() {
        val dependencyReportsConf = configurations.create(CONF_DEPENDENCY_REPORT) {
            isCanBeConsumed = false
        }
        val abiReportsConf = configurations.create(CONF_ABI_REPORT) {
            isCanBeConsumed = false
        }
        val adviceReportsConf = configurations.create(CONF_ADVICE_REPORT) {
            isCanBeConsumed = false
        }

        val misusedDependencies = tasks.register<DependencyMisuseAggregateReportTask>("misusedDependenciesReport") {
            dependsOn(dependencyReportsConf)

            unusedDependencyReports = dependencyReportsConf
            projectReport.set(project.layout.buildDirectory.file(getMisusedDependenciesAggregatePath()))
            projectReportPretty.set(project.layout.buildDirectory.file(getMisusedDependenciesAggregatePrettyPath()))
        }
        val abiReport = tasks.register<AbiAnalysisAggregateReportTask>("abiReport") {
            dependsOn(abiReportsConf)

            abiReports = abiReportsConf
            projectReport.set(project.layout.buildDirectory.file(getAbiAggregatePath()))
            projectReportPretty.set(project.layout.buildDirectory.file(getAbiAggregatePrettyPath()))
        }

        // Configured below
        val failOrWarn = tasks.register<FailOrWarnTask>("failOrWarn")

        val adviceReport = tasks.register<AdviceAggregateReportTask>("adviceReport") {
            dependsOn(adviceReportsConf)

            adviceReports = adviceReportsConf
            projectReport.set(project.layout.buildDirectory.file(getAdviceAggregatePath()))
            projectReportPretty.set(project.layout.buildDirectory.file(getAdviceAggregatePrettyPath()))

            finalizedBy(failOrWarn)
        }

        // This task will always print to console, which is the goal.
        val buildHealth = tasks.register("buildHealth") {
            dependsOn(misusedDependencies, abiReport, adviceReport)

            group = TASK_GROUP_DEP
            description = "Executes ${misusedDependencies.name}, ${abiReport.name}, and ${adviceReport.name} tasks"

            finalizedBy(failOrWarn)

            doLast {
                logger.debug("Mis-used Dependencies report: ${misusedDependencies.get().projectReport.get().asFile.path}")
                logger.debug("            (pretty-printed): ${misusedDependencies.get().projectReportPretty.get().asFile.path}")
                logger.debug("ABI report                  : ${abiReport.get().projectReport.get().asFile.path}")
                logger.debug("            (pretty-printed): ${abiReport.get().projectReportPretty.get().asFile.path}")

                logger.quiet("Advice report (aggregated): ${adviceReport.get().projectReport.get().asFile.path}")
                logger.quiet("(pretty-printed)          : ${adviceReport.get().projectReportPretty.get().asFile.path}")
            }
        }

        failOrWarn.configure {
            shouldRunAfter(buildHealth)

            advice.set(adviceReport.flatMap { it.projectReport })

            with(getExtension()!!.issueHandler) {
                failOnAny.set(anyIssue.behavior)
                failOnUnusedDependencies.set(unusedDependenciesIssue.behavior)
                failOnUsedTransitiveDependencies.set(usedTransitiveDependenciesIssue.behavior)
                failOnIncorrectConfiguration.set(incorrectConfigurationIssue.behavior)
            }
        }
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

    private fun Project.analyzeLibDebug() {
        val libTask = project.tasks.named("kaptGenerateStubsDebugKotlin")
        project.tasks
            // 定义一个新任务，将在需要时创建和配置。
            // 当使用诸如 TaskCollection#getByName(java.lang.String) 之类的查询方法定位任务时，
            // 当任务被添加到任务图中以执行时，或者当 Provider#get () 在此方法的返回值上调用
            .register("analyzeLibDebugTask", TestAssembleDebugTask::class.java)
            // configurationAction 要运行以配置任务的操作。此操作在需要任务时运行
            {
                dependsOn(libTask)

                libTask.get().inputs.files.forEach {
                    logger.log("libTask input is ${it.absolutePath}")
                }
                libTask.get().outputs.files.forEach {
                    logger.log("libTask output is ${it.absolutePath}")
                }
                inFiles.from(libTask.get().inputs.files)
                outFiles.from(libTask.get().outputs.files)
            }
    }
}