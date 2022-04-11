package com.zpw.myplayground.condependencies

import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import java.lang.IllegalArgumentException

class MyConDependencyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        logger.log("MyConDependencyPlugin apply")
        val myConDependency =
            project.extensions.create("myConDependency", MyConDependencyExtension::class.java)
        val defaultDep = mutableMapOf<String, String>()
        defaultDep["glide"] = "4.11.0"// just for test!!
        myConDependency.conDependencies.convention(defaultDep)
        project.gradle.projectsEvaluated {
            project.dependencies {
                myConDependency.conDependencies.get().map {
                    when(it.key) {
                        "glide" -> {
                            add("implementation", create("com.github.bumptech.glide:glide:${it.value}"))
                        }
                        else -> {
                            throw IllegalArgumentException("${it.key} is not support in conDependencies!!")
                        }
                    }!!
                }
            }
        }
    }
}