package com.zpw.myplayground.transform.internal.log

import com.zpw.myplayground.logger
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider

abstract class LogcatTransform: TransformAction<TransformParameters.None> {
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        logger.log("LogcatTransform transform")
        val input = inputArtifact.get().asFile
        val unzipDir = outputs.dir(input.name)
    }
}