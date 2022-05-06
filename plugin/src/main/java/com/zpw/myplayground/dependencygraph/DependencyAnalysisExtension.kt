package com.zpw.myplayground.dependencygraph

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject
import org.gradle.kotlin.dsl.create

open class DependencyAnalysisExtension @Inject constructor(
    objects: ObjectFactory
) {
    companion object {
        internal const val NAME = "dependencyRoot"

        internal fun create(project: Project): DependencyAnalysisExtension = project.extensions.create(NAME)
    }
}