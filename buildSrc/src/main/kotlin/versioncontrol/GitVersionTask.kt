package versioncontrol

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GitVersionTask: DefaultTask() {
    @get:OutputFile
    abstract val gitVersionOutputFile: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val process = ProcessBuilder(
            "git",
            "rev-parse --short HEAD"
        ).start()
        val error = process.errorStream.readBytes().toString()
        if (error.isNotBlank()) {
            System.err.println("Git error : $error")
        }
        val gitVersion = process.inputStream.readBytes().toString()
        System.err.println("Git version : $gitVersion")

        gitVersionOutputFile.get().asFile.writeText(gitVersion)


    }
}