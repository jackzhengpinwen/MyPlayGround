@file:Suppress("UnstableApiUsage")

package com.zpw.myplayground.dependency.internal

import org.gradle.api.GradleException
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import java.util.*

internal fun String.capitalize() = substring(0, 1).toUpperCase(Locale.ROOT) + substring(1)

internal fun ComponentIdentifier.asString(): String {
    return when (this) {
        is ProjectComponentIdentifier -> projectPath
        is ModuleComponentIdentifier -> moduleIdentifier.toString()
        // OpaqueComponentArtifactIdentifier implements ComponentArtifactIdentifier, ComponentIdentifier
//        is ComponentArtifactIdentifier -> toString()
        else -> throw GradleException("Cannot identify ComponentIdentifier subtype. Was ${javaClass.simpleName}, named $this")
    }
}

fun ComponentIdentifier.resolvedVersion(): String? {
    return when (this) {
        is ProjectComponentIdentifier -> null
        is ModuleComponentIdentifier -> version
        else -> throw GradleException("Cannot identify ComponentIdentifier subtype. Was ${javaClass.simpleName}, named $this")
    }
}

fun DependencySet.toIdentifiers(): Set<String> = mapNotNull {
    when (it) {
        is ProjectDependency -> it.dependencyProject.path
        is ModuleDependency -> "${it.group}:${it.name}:${it.version}"
        // Don't have enough information, so ignore it
        is SelfResolvingDependency -> null
        else -> throw GradleException("Unknown Dependency subtype: \n$it\n${it.javaClass.name}")
    }
}.toSet()

fun Sequence<MatchResult>.allItems(): List<String> =
    flatMap { matchResult ->
        val groupValues = matchResult.groupValues
        // Ignore the 0th element, as it is the entire match
        if (groupValues.isNotEmpty()) groupValues.subList(1, groupValues.size).asSequence()
        else emptySequence()
    }.toList()

internal val DESC_REGEX = """L(\w[\w/$]+);""".toRegex()

// This regex matches a Java FQCN.
// https://stackoverflow.com/questions/5205339/regular-expression-matching-fully-qualified-class-names#comment5855158_5205467
val JAVA_FQCN_REGEX =
    "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*".toRegex()
val JAVA_FQCN_REGEX_SLASHY =
    "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*/)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*".toRegex()
