package com.zpw.myplayground

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.gaelmarhic.quadrant.tasks.GenerateActivityClassNameConstants
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import kotlin.reflect.KClass

import com.zpw.myplayground.constants.GeneralConstants.PLUGIN_CONFIG
import com.zpw.myplayground.constants.GeneralConstants.TARGET_DIRECTORY
import com.zpw.myplayground.extensions.QuadrantConfigurationExtension
import com.zpw.myplayground.model.module.RawModule
import java.io.File

abstract class QuadrantPlugin: Plugin<Project> {

    companion object {
        private const val MAIN_SOURCE_SET = "main"
        private const val MANIFEST_FILE_DEPTH = 3
        private const val MANIFEST_FILE_NAME = "AndroidManifest.xml"
    }

    override fun apply(project: Project) {
        with(project) {
            plugins.forEach { plugin ->
                when (plugin) {
                    is AppPlugin -> {
                        System.out.println("ApplicationPlugin apply")
                        applyPlugin(AppExtension::class) { it.applicationVariants }
                    }
                    is LibraryPlugin -> {
                        System.out.println("JavaLibraryPlugin apply")
                    }
                    else -> {
//                        System.out.println("plugin is $plugin")
                    }
                }
            }
        }
    }

    private fun <E : BaseExtension, V : BaseVariant> Project.applyPlugin(
        extensionType: KClass<E>,
        block: (E) -> DomainObjectCollection<V>
    ) {
        val extension = getExtension(extensionType)
        val variants = block(extension)
        val mainSourceSet = extension.sourceSet(MAIN_SOURCE_SET)

        registerTask(createGenerateActivityClassNameConstantsTask(), variants)
//        addTargetDirectoryToSourceSet(mainSourceSet)
    }

    private fun BaseExtension.sourceSet(name: String) = sourceSets.getByName(name)

    private fun Project.createGenerateActivityClassNameConstantsTask(): Task {
        System.out.println("createGenerateActivityClassNameConstantsTask")
        val taskType = GenerateActivityClassNameConstants::class.java
        val taskName = taskType.simpleName.decapitalize()
        val extension = registerConfigurationExtension()
        val createTask: GenerateActivityClassNameConstants = tasks.create(taskName, taskType)
        val rawModuleList = retrieveRawModuleList(this)
        project.rootProject.allprojects.forEach {
            System.out.println("allprojects - ${it.name}, ${it.buildFile}, ${it.manifestFiles}, ${it.dependencies.modules}")
        }
        createTask.apply {
            configurationExtension.set(extension)
            buildScript.set(buildFile)
            manifestFiles.set(rawModuleList.flatMap { it.manifestFiles })
            targetDirectory.set(buildDir.resolve(TARGET_DIRECTORY))
            rawModules.set(rawModuleList)
        }
        return createTask
    }

    private fun retrieveRawModuleList(project: Project) =
        project // This project is the project of the module where the plugin is applied.
            .rootProject
            .allprojects
            .map { it.toRawModule() }

    private fun Project.toRawModule() = RawModule(
        name = name,
        manifestFiles = manifestFiles
    )

    private val Project.manifestFiles: List<File>
        get() = projectDir
            .walk()
            .maxDepth(MANIFEST_FILE_DEPTH)
            .filter { it.name == MANIFEST_FILE_NAME }
            .toList()

    private fun <V : BaseVariant> Project.registerTask(
        taskToBeRegistered: Task,
        variants: DomainObjectCollection<V>
    ) {
        afterEvaluate {
            variants.forEach { variant ->
                tasks.forEach { task ->
                    if (task.isCompileKotlinTask(variant)) {
                        System.out.println("task: ${task.name.capitalize()}" +
                                " is dependsOn ${taskToBeRegistered.name.capitalize()}")
                        task.dependsOn(taskToBeRegistered)
                    }
                }
            }
        }
    }

    private fun <E : BaseExtension> Project.getExtension(type: KClass<E>) = extensions[type]

    private operator fun <T : BaseExtension> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)
    }

    private fun <T : BaseVariant> Task.isCompileKotlinTask(variant: T) =
        name == "compile${variant.name.capitalize()}Kotlin"

    private fun Project.registerConfigurationExtension() =
        extensions.create(PLUGIN_CONFIG, QuadrantConfigurationExtension::class.java)
}