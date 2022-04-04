@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.internal

import org.gradle.api.GradleException
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.io.File

internal data class Artifact(
    /**
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val identifier: String,
    /**
     * Library (e.g., downloaded from jcenter) or a project ("module" in a multi-module project).
     */
    val componentType: ComponentType,
    /**
     * If false, a direct dependency (declared in the `dependencies {}` block). If true, a transitive dependency.
     */
    var isTransitive: Boolean? = null,
    /**
     * Physical artifact on disk; a jar file.
     */
    var file: File? = null
) {

    constructor(componentIdentifier: ComponentIdentifier, file: File? = null) : this(
        identifier = componentIdentifier.asString(),
        componentType = ComponentType.of(componentIdentifier),
        file = file
    )
}

internal enum class ComponentType {
    /**
     * A 3rd-party dependency.
     */
    LIBRARY,
    /**
     * A project dependency, aka a "module" in a multi-module or multi-project build.
     */
    PROJECT;

    companion object {
        fun of(componentIdentifier: ComponentIdentifier) = when (componentIdentifier) {
            is ModuleComponentIdentifier -> LIBRARY
            is ProjectComponentIdentifier -> PROJECT
            else -> throw GradleException("'This shouldn't happen'")
        }
    }
}

/**
 * 库或项目。
 */
internal data class Component(
    /**
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val identifier: String,
    /**
     * 如果为 false，则为直接依赖项（在 `dependencies {}` 块中声明）。如果为true，则为传递依赖。
     */
    val isTransitive: Boolean,
    /**
     * 此库声明的类。
     */
    val classes: Set<String>
) : Comparable<Component> {

    override fun compareTo(other: Component): Int {
        return identifier.compareTo(other.identifier)
    }
}

/**
 * 表示“误用”的传递依赖。 [identifier] 是唯一名称，[usedTransitiveClasses] 是直接使用的依赖项的类成员（“不应该”）。
 */
internal data class TransitiveDependency(
    /**
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val identifier: String,
    /**
     * 这些是相关项目直接使用的此依赖项的类成员。他们已经泄漏到类路径中。
     */
    val usedTransitiveClasses: Set<String>
)

/**
 * Represents a dependency ([identifier]) that is declared in the `dependencies {}` block of a build script. This
 * dependency is unused and has zero or more transitive dependencies that _are_ used ([usedTransitiveDependencies]).
 */
internal data class UnusedDirectDependency(
    /**
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val identifier: String,
    /**
     * If this direct dependency has any transitive dependencies that are used, they will be in this set.
     *
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val usedTransitiveDependencies: MutableSet<String>
)