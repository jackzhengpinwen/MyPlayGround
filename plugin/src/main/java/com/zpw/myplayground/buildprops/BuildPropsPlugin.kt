package com.zpw.myplayground.buildprops

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.crash.afterEvaluate
import com.zpw.myplayground.log
import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

class BuildPropsPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        logger.log("BuildPropsPlugin apply")
        if (!target.plugins.hasPlugin("idea")) {
            target.plugins.apply("idea")
        }
        target.afterEvaluate {
            val sourceSets = target.the<JavaPluginExtension>().sourceSets
//            target.configureIdeaModule(sourceSets)
            val buildProps: TaskProvider<BuildGenerator> = target.tasks.register("generateBuildJavaFile", BuildGenerator::class.java)
            sourceSets.filter {
                logger.log("sourceSets is ${it.name}")
                it.name == SourceSet.MAIN_SOURCE_SET_NAME
            }.map { sourceSet ->
                listOf("java", "kotlin", "groovy").mapNotNull { lang ->
                    target.tasks.findByName(sourceSet.getCompileTaskName(lang))
                }
            }.flatten().forEach {
//                (it.dependsOn(buildProps) as? SourceTask)?.source(buildProps.get().output)
            }
        }
    }
}