package com.zpw.myplayground.injkit

import com.zpw.myplayground.log
import org.gradle.api.Plugin
import org.gradle.api.Project

class InjKitPlugin: Plugin<Project> {
    val TASK_NAME = "extractStringsFromLayouts"

    override fun apply(target: Project): Unit = target.run {
        logger.log("InjKitPlugin apply")
        tasks.register(
            TASK_NAME,
            AndroidStringExtractorTask::class.java
        )
    }
}