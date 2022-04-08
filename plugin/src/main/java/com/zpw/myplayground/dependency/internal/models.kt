@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.internal

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.gradle.api.GradleException
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.io.File
import java.io.Serializable

/**
 * A tuple of an identifier (project or external module) and the name of the configuration on which it is declared.
 *
 * TODO: this might be temporary. The intent is that this information make its way into a `Dependency` or something.
 */
data class DependencyConfiguration(
    val identifier: String,
    val configurationName: String
) : Serializable

data class ComponentWithInlineMembers(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * A set of imports that indicates a possible use of an inline member from this component ([dependency]).
     */
    val imports: Set<String>
) : Comparable<ComponentWithInlineMembers> {
    override fun compareTo(other: ComponentWithInlineMembers): Int = dependency.compareTo(other.dependency)
}

data class ComponentWithConstantMembers(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * A set of imports that indicates a possible use of a constant member from this component ([dependency]).
     */
    val imports: Set<String>
) : Comparable<ComponentWithConstantMembers> {
    override fun compareTo(other: ComponentWithConstantMembers): Int = dependency.compareTo(other.dependency)
}

/**
 * Primarily used as a pointer to a [file] on disk; a physical artifact.
 */
data class Artifact(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * If false, a direct dependency (declared in the `dependencies {}` block). If true, a transitive dependency.
     */
    var isTransitive: Boolean? = null,
    /**
     * Physical artifact on disk; a jar file.
     */
    var file: File
) {
    constructor(
        componentIdentifier: ComponentIdentifier,
        file: File,
        candidates: Set<DependencyConfiguration>
    ) : this(
        dependency = Dependency(
            identifier = componentIdentifier.asString(),
            resolvedVersion = componentIdentifier.resolvedVersion(),
            configurationName = candidates.find { it.identifier == componentIdentifier.asString() }?.configurationName
        ),
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
data class Component(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * 如果为 false，则为直接依赖项（在 `dependencies {}` 块中声明）。如果为true，则为传递依赖。
     */
    val isTransitive: Boolean,
    /**
     * 此库声明的类。
     */
    val classes: Set<String>
) : Comparable<Component> {

    override fun compareTo(other: Component): Int = dependency.compareTo(other.dependency)
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

/**
 * Represents a dependency ([Dependency.identifier]) that is declared in the `dependencies {}` block of a build script.
 * This dependency is unused and has zero or more transitive dependencies that _are_ used ([usedTransitiveDependencies])
 */
data class UnusedDirectComponent(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * If this direct dependency has any transitive dependencies that are used, they will be in this set.
     *
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val usedTransitiveDependencies: MutableSet<Dependency>
)

data class Res(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * An import that indicates a possible use of an Android resource from this component ([dependency]).
     */
    val import: String
) {
    constructor(componentIdentifier: ComponentIdentifier, import: String) : this(
        dependency = Dependency(
            identifier = componentIdentifier.asString(),
            resolvedVersion = componentIdentifier.resolvedVersion()
        ),
        import = import
    )
}

/**
 * Represents a "mis-used" transitive dependency. The [dependency] represents the unique name
 * (see [Dependency.identifier]), and the [usedTransitiveClasses] are the class members of the dependency that are used
 * directly (which "shouldn't" be).
 */
data class TransitiveComponent(
    /**
     * A tuple of an `identifier` and a resolved version. See [Dependency].
     */
    val dependency: Dependency,
    /**
     * These are class members of this dependency that are used directly by the project in question. They have leaked
     * onto the classpath (either unintentionally or by design). Unintentional leakage is usually the result of use of
     * the `compile` configuration (or Maven scope); cf the `api` configuration, which "leaks" by design.
     */
    val usedTransitiveClasses: Set<String>
)

/**
 * Basically a tuple of [identifier] and [resolvedVersion] (and optionally the [configurationName] on which this
 * dependency is declared). `resolvedVersion` will be null for project dependencies, and `configurationName` will be
 * null for (at least) transitive dependencies.
 *
 * For equality purposes, this class only cares about its `identifier`. No other property matters.
 */
data class Dependency(
    /**
     * In group:artifact form. E.g.,
     * 1. "javax.inject:javax.inject"
     * 2. ":my-project"
     */
    val identifier: String,
    /**
     * Resolved version. Will be null for project dependencies.
     */
    val resolvedVersion: String? = null,
    /**
     * The configuration on which this dependency was declared, or null if none found.
     */
    val configurationName: String? = null
) : Comparable<Dependency> {

    constructor(componentIdentifier: ComponentIdentifier) : this(
        identifier = componentIdentifier.asString(),
        resolvedVersion = componentIdentifier.resolvedVersion()
    )

    /*
     * These overrides all basically say that we don't care about the resolved version for our algorithms. End-users
     * might care, which is why we include it anyway.
     */

    override fun compareTo(other: Dependency): Int = identifier.compareTo(other.identifier)

    override fun toString(): String {
        return if (resolvedVersion != null) {
            "$identifier:$resolvedVersion"
        } else {
            identifier
        }
    }

    /**
     * We only care about the [identifier] for equality comparisons.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dependency

        if (identifier != other.identifier) return false

        return true
    }

    override fun hashCode(): Int = identifier.hashCode()
}

data class Advice(
    /**
     * The dependency that ought to be modified in some way.
     */
    val dependency: Dependency,
    /**
     * The current configuration on which the dependency has been declared. Will be null for transitive dependencies.
     */
    val fromConfiguration: String? = null,
    /**
     * The configuration on which the dependency _should_ be declared. Will be null if the dependency is unused and
     * therefore ought to be removed.
     */
    val toConfiguration: String? = null
) : Comparable<Advice> {

    companion object {
        fun add(dependency: Dependency, toConfiguration: String) =
            Advice(dependency, fromConfiguration = null, toConfiguration = toConfiguration)

        fun remove(dependency: Dependency) =
            Advice(dependency, fromConfiguration = dependency.configurationName, toConfiguration = null)

        fun change(dependency: Dependency, toConfiguration: String) =
            Advice(dependency, fromConfiguration = dependency.configurationName, toConfiguration = toConfiguration)
    }

    override fun compareTo(other: Advice): Int {
        // TODO I'd like to make this comparison more robust
        return dependency.compareTo(other.dependency)
    }

    fun isAdd() = fromConfiguration == null
    fun isRemove() = toConfiguration == null
    fun isChange() = fromConfiguration != null && toConfiguration != null
}