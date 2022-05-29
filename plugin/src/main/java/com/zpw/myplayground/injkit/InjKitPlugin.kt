package com.zpw.myplayground.injkit

import com.zpw.myplayground.log
import com.zpw.myplayground.logger
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.compile.JavaCompile

class InjKitPlugin: Plugin<Project> {
    override fun apply(target: Project) = target.run {
        logger.log("InjKitPlugin apply")
    }
}