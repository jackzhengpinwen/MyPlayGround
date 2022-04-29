package com.zpw.myplayground.dependencygraph.dependency

import com.google.gson.Gson
import com.zpw.myplayground.dependencygraph.model.BuildArtifactDependency
import com.zpw.myplayground.dependencygraph.model.BuildDependency
import com.zpw.myplayground.dependencygraph.model.BuildModuleDependency
import com.zpw.myplayground.logger
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import java.io.File

object DependencyVisitor {
    fun generateReport(project: Project, configurationName: String) {
        val config = project.configurations.firstOrNull { it.name == configurationName } as Configuration
        val dependencies = traverseDependenciesForConfiguration(config)

        val report = StringBuilder().apply {
            dependencies.toReportString().apply {
                if (this.isNotBlank()) {
                    append(this)
                }
            }
        }.toString()
        buildDirOutputFile(project, configurationName).writeText(report)
    }

    fun buildDirOutputFile(
        project: Project,
        configurationName: String
    ): File {
        return project.layout
            .buildDirectory
            .get()
            .dir("tmp/dependency-build")
            .file("$configurationName.txt")
            .asFile
            .apply {
                parentFile.apply {
                    if (!exists()) {
                        // Create the "dependencies" directory if it does not exist
                        mkdirs()
                    }
                }
            }
    }

    private fun List<BuildDependency>.toReportString(): String {
        val deps = this
        return StringBuilder().apply {
            deps.forEach {
                appendLine(it.name)
            }
        }.toString()
    }

    fun ResolvedDependencyResult.toDep(): BuildDependency? {
        return when(val componentIdentifier = selected.id) {
            is ProjectComponentIdentifier -> BuildModuleDependency (
                path = componentIdentifier.projectPath
                )
            is ModuleComponentIdentifier -> BuildArtifactDependency(
                group = componentIdentifier.group,
                artifact = componentIdentifier.module,
                version = componentIdentifier.version
            )
            else -> {
                null
            }
        }
    }

    fun traverseDependenciesForConfiguration(config: Configuration): List<BuildDependency> {
        val incomingResolvableDependencies = config.incoming
        val resolvedComponentResult = incomingResolvableDependencies.resolutionResult.root
        val firstLevelDependencies = resolvedComponentResult
            .dependencies
            .filterIsInstance<ResolvedDependencyResult>()
        val dependencies = mutableListOf<BuildDependency>()
        visit(dependencies, firstLevelDependencies)
        return dependencies
    }

    private fun visit(reportData: MutableList<BuildDependency>, resolvedDependencyResults: Collection<ResolvedDependencyResult>) {
        for (resolvedDependencyResult: ResolvedDependencyResult in resolvedDependencyResults) {
            resolvedDependencyResult.toDep()?.let { dep: BuildDependency ->
                reportData.add(dep)
                visit(reportData, resolvedDependencyResult.selected.dependencies.filterIsInstance<ResolvedDependencyResult>())
            }
        }
    }

    /**
     * 遍历 configurations
     */
    private fun traveseConfigurations(project: Project, configurationName: String) {
        val config = project.configurations.firstOrNull { it.name == configurationName }
        config?.resolvedConfiguration?.firstLevelModuleDependencies?.forEach {
            logger.log("${it.configuration} : ${it.name}")
            buildTree(it, it.configuration, rootNode)
        }
        buildDirOutputFile(project, configurationName).writeText(Gson().toJson(rootNode))
    }

    val rootNode = TreeNode("root", "", mutableListOf())

    private fun buildTree(node: ResolvedDependency, configuration: String, parentNode: TreeNode) {
        if(node.children.isNullOrEmpty()) {
            parentNode.children.add(TreeNode(node.name, configuration, mutableListOf()))
            return
        }
        val levelNode = TreeNode(node.name, configuration, mutableListOf())
        for (n in node.children) {
            buildTree(n, configuration, levelNode)
        }
        parentNode.children.add(levelNode)
    }

    data class TreeNode(
        val name: String,
        val configuration: String,
        val children: MutableList<TreeNode> = mutableListOf(),
    )
}