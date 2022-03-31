package com.gaelmarhic.quadrant.tasks

import com.zpw.myplayground.constants.GeneralConstants.PLUGIN_NAME
import com.zpw.myplayground.extensions.QuadrantConfigurationExtension
import com.zpw.myplayground.helpers.*
import com.zpw.myplayground.model.module.RawModule
import com.zpw.myplayground.processors.GenerateActivityClassNameConstantProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class GenerateActivityClassNameConstants : DefaultTask() {

    @get:Nested
    abstract val configurationExtension: Property<QuadrantConfigurationExtension>

    @get:InputFile
    abstract val buildScript: RegularFileProperty

    @get:InputFiles
    abstract val manifestFiles: ListProperty<File>

    @get:Input
    abstract val rawModules: ListProperty<RawModule>

    @get:OutputDirectory
    abstract val targetDirectory: DirectoryProperty

    init {
        group = PLUGIN_NAME
        description = DESCRIPTION
    }

    @TaskAction
    fun generateConstants() {
        System.out.println("GenerateActivityClassNameConstants generateConstants")
        System.out.println("${rawModules.get()}")
        initProcessor().process(rawModules.get())
    }

    private fun initProcessor() = GenerateActivityClassNameConstantProcessor(
        manifestParsingHelper = ManifestParsingHelper(),
        manifestVerificationHelper = ManifestVerificationHelper(),
        activityFilteringHelper = ActivityFilteringHelper(
            configurationExtension = configurationExtension.get()
        ),
        constantFileDeterminationHelper = ConstantFileDeterminationHelper(
            configurationExtension = configurationExtension.get()
        ),
        constantGenerationHelper = ConstantGenerationHelper(targetDirectory.get().asFile)
    )

    companion object {
        private const val DESCRIPTION =
            "Generates files of constants that hold the Android Activities' full class name."
    }
}
