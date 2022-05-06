package com.zpw.myplayground.dependencygraph

import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.Project

class DependencyGraphGeneratorPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        logger.log("DependencyGraphGeneratorPlugin apply")
        // 给所有子项目注入插件
        if (project == project.rootProject) {
            RootPlugin(project).apply()
        } else {
            ProjectPlugin(project).apply()
        }
    }
}