package com.zpw.myplayground.fastbuild

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.*
import java.io.File

public abstract class ModuleArchiveDetectTask : DefaultTask() {

    @InputFiles
    @SkipWhenEmpty
    abstract fun getInputAARBuildDir(): ConfigurableFileCollection

    @OutputDirectory
    var outPutDirFile = File(".")


    @TaskAction
    fun perform() {

    }




}