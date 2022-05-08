package com.zpw.myplayground.bigimage

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.zpw.myplayground.log
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*
import java.io.File

class BigImageCheckPlugin: Plugin<Project> {
    override fun apply(project: Project) = project.run {
        logger.log("BigImageCheckPlugin apply")
        afterEvaluate {
            the<AppExtension>().applicationVariants.all {
                val mainSourceSet = sourceSets.get(0)
                mainSourceSet.resDirectories.forEach {
                    val queue = ArrayDeque<File>()
                    queue.addFirst(it)
                    while(queue.isNotEmpty()) {
                        val file = queue.removeFirst()
                        if (file.isDirectory) {
                            queue.addAll(file.listFiles())
                            logger.log("res dir is ${file.name}")
                        } else {
                            logger.log("res file is ${file.name} size is ${file.length()}")
                        }
                    }
                }
            }
        }
    }
}