@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.tasks

import com.autonomousapps.internal.*
import com.zpw.myplayground.dependency.Behavior
import com.zpw.myplayground.dependency.TASK_GROUP_DEP
import com.zpw.myplayground.dependency.internal.*
import com.zpw.myplayground.dependency.internal.fromJsonList
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Produces human- and machine-readable advice on how to modify a project's dependencies in order to have a healthy
 * build.
 */
@CacheableTask
abstract class AdviceTask : DefaultTask() {

    init {
        group = TASK_GROUP_DEP
        description = "Provides advice on how best to declare the project's dependencies"
    }

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val unusedDependenciesReport: RegularFileProperty

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val usedTransitiveDependenciesReport: RegularFileProperty

    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val abiDependenciesReport: RegularFileProperty

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val allDeclaredDependenciesReport: RegularFileProperty

    @get:Input
    abstract val failOnAny: Property<Behavior>

    @get:Input
    abstract val failOnUnusedDependencies: Property<Behavior>

    @get:Input
    abstract val failOnUsedTransitiveDependencies: Property<Behavior>

    @get:Input
    abstract val failOnIncorrectConfiguration: Property<Behavior>

    @get:OutputFile
    abstract val adviceReport: RegularFileProperty

    @TaskAction
    fun action() {
        // Output
        val adviceFile = adviceReport.get().asFile
        adviceFile.delete()

        // Inputs
        val unusedDirectComponents = unusedDependenciesReport.get().asFile.readText().fromJsonList<UnusedDirectComponent>()
        val usedTransitiveComponents = usedTransitiveDependenciesReport.get().asFile.readText().fromJsonList<TransitiveComponent>()
        val abiDeps = abiDependenciesReport.orNull?.asFile?.readText()?.fromJsonList<Dependency>() ?: emptyList()
        val allDeclaredDeps = allDeclaredDependenciesReport.get().asFile.readText().fromJsonList<Artifact>()
            .map { it.dependency }
            .filter { it.configurationName != null }

        // Print to the console three lists:
        // 1. Dependencies that should be removed
        // 2. Dependencies that are already declared and whose configurations should be modified
        // 3. Dependencies that should be added and the configurations on which to add them

        val advisor = Advisor(
            unusedDirectComponents = unusedDirectComponents,
            usedTransitiveComponents = usedTransitiveComponents,
            abiDeps = abiDeps,
            allDeclaredDeps = allDeclaredDeps,
            ignoreSpec = Advisor.IgnoreSpec(
                anyBehavior = failOnAny.get(),
                unusedDependenciesBehavior = failOnUnusedDependencies.get(),
                usedTransitivesBehavior = failOnUsedTransitiveDependencies.get(),
                incorrectConfigurationsBehavior = failOnIncorrectConfiguration.get()
            )
        )

        var didGiveAdvice = false

        if (!advisor.filterRemove) {
            advisor.getRemoveAdvice()?.let {
                logger.quiet("Unused dependencies which should be removed:\n$it\n")
                didGiveAdvice = true
            }
        }

        if (!advisor.filterAdd) {
            advisor.getAddAdvice()?.let {
                logger.quiet("Transitively used dependencies that should be declared directly as indicated:\n$it\n")
                didGiveAdvice = true
            }
        }

        if (!advisor.filterChange) {
            advisor.getChangeAdvice()?.let {
                logger.quiet("Existing dependencies which should be modified to be as indicated:\n$it\n")
                didGiveAdvice = true
            }
        }

        if (didGiveAdvice) {
            logger.quiet("See machine-readable report at ${adviceFile.path}")
        } else {
            logger.quiet("Looking good! No changes needed")
        }

        val advices = advisor.getAdvices().filterNot {
            when {
                advisor.filterAdd -> it.isAdd()
                advisor.filterRemove -> it.isRemove()
                advisor.filterChange -> it.isChange()
                else -> false
            }
        }

        adviceFile.writeText(advices.toJson())
    }
}
