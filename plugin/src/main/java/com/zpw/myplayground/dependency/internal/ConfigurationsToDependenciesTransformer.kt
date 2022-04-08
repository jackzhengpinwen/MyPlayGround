package com.zpw.myplayground.dependency.internal

import com.zpw.myplayground.logger
import org.gradle.api.Project

internal class ConfigurationsToDependenciesTransformer(
    private val variantName: String,
    private val project: Project
) {

    companion object {
        internal val DEFAULT_CONFS = listOf("api", "implementation", "compile", "compileOnly", "runtimeOnly")
    }

    fun dependencyConfigurations(): Set<DependencyConfiguration> {
        val candidateConfNames = DEFAULT_CONFS + DEFAULT_CONFS.map {
            "${variantName}${it.capitalize()}"
        }
        /**
         * [api, implementation, compile, compileOnly, runtimeOnly,
         * debugApi, debugImplementation, debugCompile, debugCompileOnly, debugRuntimeOnly]
         */
//        logger.log("candidateConfNames is ${candidateConfNames}")
        /**
         * configurations 包含很多东西，重量级
         */
//        project.configurations.forEach {
//            logger.log("configuration Name is ${it.name}")
//        }
        /**
         * configuration 现在只包含这几种构建 [api, compileOnly, implementation, runtimeOnly]
         */
        // Filter all configurations for those we care about
        val interestingConfs = project.configurations.asMap
            .filter { (name, _) -> candidateConfNames.contains(name) }
            .map { (_, conf) -> conf }
//        interestingConfs.forEach {
//            logger.log("configuration Name is ${it.name}")
//        }

        val result = interestingConfs.flatMap { conf ->
            val depSet = conf.dependencies.toIdentifiers()
//            logger.log("depSet is ${depSet}")
            depSet.map { identifier ->
                DependencyConfiguration(identifier = identifier, configurationName = conf.name)
            }
        }.toSet()
//        result.forEach {
//            logger.log("DependencyConfiguration is ${it.identifier}, ${it.configurationName}")
//        }
        return result
    }
}
