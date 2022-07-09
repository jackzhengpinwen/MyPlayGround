package com.zpw.myplayground.removeusedres

import com.android.build.gradle.internal.lint.AndroidLintTask
import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.Project

class RemoveUnusedResourcesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        logger.log("RemoveUnusedResourcesPlugin apply")
//        target.extensions.create(
//            "removeUnusedResources",
//            RemoveUnusedResourcesExtension::class.java
//        )
        target.tasks.register(
            "removeUnusedResources",
            RemoveUnusedResourcesTask::class.java
        ) {
            mustRunAfter(target.tasks.withType(AndroidLintTask::class.java))
        }

    }
}