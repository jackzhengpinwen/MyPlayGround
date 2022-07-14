package com.zpw.myplayground.buildprops

import com.zpw.myplayground.log
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.ide.idea.model.IdeaModule
import java.io.File

internal fun Project.getGeneratedSourceDir(
    sourceSet: SourceSet
) = File(buildDir, GENERATED_SOURCE_ROOT + File.separator + sourceSet.name + File.separator + "java")

internal fun Project.configureIdeaModule(sourceSets: SourceSetContainer) {
    val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    logger.log("mainSourceSet is $mainSourceSet")
    val testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
    logger.log("testSourceSet is $testSourceSet")
    val mainGeneratedSourcesDir = getGeneratedSourceDir(mainSourceSet)
    logger.log("mainGeneratedSourcesDir is $mainGeneratedSourcesDir")
    val testGeneratedSourcesDir = getGeneratedSourceDir(testSourceSet)
    logger.log("testGeneratedSourcesDir is $testGeneratedSourcesDir")
    val ideaModule = extensions.getByType(IdeaModel::class.java).module
    ideaModule.excludeDirs = getIdeaExcludeDirs(getGeneratedSourceDirs(sourceSets), ideaModule)
    logger.log("ideaModule.excludeDirs is ${ideaModule.excludeDirs}")
    ideaModule.sourceDirs = files(ideaModule.sourceDirs, mainGeneratedSourcesDir).files
    logger.log("ideaModule.sourceDirs is ${ideaModule.sourceDirs}")
    ideaModule.testSourceDirs = files(ideaModule.testSourceDirs, testGeneratedSourcesDir).files
    logger.log("ideaModule.testSourceDirs is ${ideaModule.testSourceDirs}")
    ideaModule.generatedSourceDirs = files(ideaModule.generatedSourceDirs, mainGeneratedSourcesDir, testGeneratedSourcesDir).files
    logger.log("ideaModule.generatedSourceDirs is ${ideaModule.generatedSourceDirs}")
}

internal fun Project.getGeneratedSourceDirs(
    sourceSets: SourceSetContainer
): Set<File> = LinkedHashSet<File>().also { excludes ->
    sourceSets.forEach { sourceSet ->
        var f = getGeneratedSourceDir(sourceSet)

        while (f != this.projectDir) {
            excludes.add(f)
            f = f.parentFile
        }
    }
}

internal fun Project.getIdeaExcludeDirs(
    excludes: Set<File>, ideaModule: IdeaModule
): Set<File> = LinkedHashSet(ideaModule.excludeDirs).also { excludeDirs ->
    if (excludes.contains(buildDir) && excludeDirs.contains(buildDir)) {
        excludeDirs.remove(buildDir)
        buildDir.listFiles()?.filter {
            it.isDirectory
        }?.forEach {
            excludeDirs.add(it)
        }
    }

    excludeDirs.removeAll(excludes)
}

internal const val PLUGIN_ID = "buildprops"

internal val GENERATED_SOURCE_ROOT = "generated${File.separator}source${File.separator}$PLUGIN_ID"