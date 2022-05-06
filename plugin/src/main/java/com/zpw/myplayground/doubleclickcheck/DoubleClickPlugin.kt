package com.zpw.myplayground.doubleclickcheck

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class DoubleClickPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.the<AppExtension>().registerTransform()

    }
}