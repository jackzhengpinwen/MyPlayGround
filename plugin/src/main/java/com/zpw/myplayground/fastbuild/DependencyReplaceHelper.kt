package org.modulearchive.dependency

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import com.zpw.myplayground.fastbuild.IInfoCenter
import com.zpw.myplayground.fastbuild.ProjectManageHelper
import com.zpw.myplayground.fastbuild.ModuleArchiveLogger

class DependencyReplaceHelper constructor(
    private val infoCenter: IInfoCenter
) {

    fun replaceDependency() {

        //替换依赖
        replaceDependency(infoCenter.getTargetProject())
        val managerList = infoCenter.getManagerList()
        for (replaceProjectManager in managerList) {
            if (replaceProjectManager.cacheValid) {
                val parent = infoCenter.getTargetProject()
                val replaceProject = replaceProjectManager.obtainProject()
                //原始类型
                copyDependencyWithePrefix(replaceProject, parent, "", configList)
                //Debug 前缀类型
                copyDependencyWithePrefix(replaceProject, parent, "debug", configList)
                //release前缀类型
                copyDependencyWithePrefix(replaceProject, parent, "release", configList)
                //变体前缀
                val flavorName = replaceProjectManager.originData.flavorName
                if (flavorName.isNotBlank()) {
                    //api debugApi tiyaDebugApi
                    copyDependencyWithePrefix(replaceProject, parent, flavorName, configList)
                    copyDependencyWithePrefix(replaceProject, parent, flavorName + "Debug", configList)
                    copyDependencyWithePrefix(replaceProject, parent, flavorName + "Release", configList)
                }
            }
        }
    }

    private val configList = mutableSetOf<String>("api", "runtimeOnly", "implementation")
    private val apiConfigList = mutableSetOf<String>("api", "runtimeOnly", "implementation")
    // private val apiConfigList = mutableSetOf<String>("api")
    // private val apiConfigList = mutableSetOf<String>("api")

    private fun replaceDependency(replaceProject: Project, parent: Project? = null) {

        val managerList = infoCenter.getManagerList()
        val replaceProjectManager = managerList.firstOrNull { it.originData.name == replaceProject.path }

        //子工程依赖替换完成
        for (configuration in replaceProject.configurations) {
            val mutableSet = mutableSetOf<Dependency>()
            mutableSet.addAll(configuration.dependencies)
            for (dependency in mutableSet) {
                handleReplaceDependency(configuration, dependency, replaceProject)
            }
        }

        //把下层的依赖投递到上层
        if (parent != null && replaceProjectManager != null && replaceProjectManager.cacheValid) {
            //原始类型
            copyDependencyWithePrefix(replaceProject, parent, "")
            //Debug 前缀类型
            copyDependencyWithePrefix(replaceProject, parent, "debug")
            //release前缀类型
            copyDependencyWithePrefix(replaceProject, parent, "release")
            //变体前缀
            val flavorName = replaceProjectManager.originData.flavorName
            if (flavorName.isNotBlank()) {
                //api debugApi tiyaDebugApi
                copyDependencyWithePrefix(replaceProject, parent, flavorName)
                copyDependencyWithePrefix(replaceProject, parent, flavorName + "Debug")
                copyDependencyWithePrefix(replaceProject, parent, flavorName + "Release")
            }
        }
    }

    private fun copyDependency(src: Configuration, dest: Configuration) {
        for (dependency in src.dependencies) {
            if (dependency is ModuleDependency) {
                val srcExclude = configMatchExclude(src, dependency)
                val destExclude = configMatchExclude(dest, dependency)
                //被排除了
                if (srcExclude || destExclude) {
                    continue
                } else {
                    src.excludeRules.forEach {
                        dependency.exclude(mapOf("group" to it.group,"module" to it.module))
                    }
                    dest.excludeRules.forEach {
                        dependency.exclude(mapOf("group" to it.group,"module" to it.module))
                    }
                    // dependency.excludeRules.addAll(src.excludeRules)
                    // dependency.excludeRules.addAll(dest.excludeRules)
                }
            }
            dest.dependencies.add(dependency)
        }
    }

    private fun copyDependency(replaceProject: Project, parent: Project, configName: String) {
        val config = replaceProject.configurations.getByName(configName)
        val parentContain = parent.configurations.names.contains(configName)
        if (parentContain) {
            copyDependency(config, parent.configurations.getByName(configName))
        }
    }

    private fun copyDependencyWithePrefix(
        replaceProject: Project,
        parent: Project,
        prefix: String,
        list: Set<String> = apiConfigList
    ) {
        for (configName in list) {

            val newConfigName = if (prefix.isNullOrBlank()) {
                configName
            } else {
                prefix + configName.capitalize()
            }
            // ModuleArchiveLogger.logLifecycle("赋值依赖: ${newConfigName}")
            copyDependency(replaceProject, parent, newConfigName)
        }
    }

    private fun handleReplaceDependency(
        configuration: Configuration,
        dependency: Dependency,
        replaceProject: Project
    ) {
        val moduleArchiveExtension = infoCenter.getModuleArchiveExtension()

        val managerList = infoCenter.getManagerList()

        if (dependency !is ProjectDependency) {
            return
        }

        //依赖对应的project
        val dependencyProject = dependency.dependencyProject

        //防止自己引用自己
        if (dependencyProject === replaceProject) {
            return
        }

        val manager = managerList.firstOrNull { it.originData.name == dependencyProject.path }


        if (manager != null && manager.originData.enable) {
            //标记这个对象被引用了
            manager.flagHasOut = true

//            if (replaceProject != infoCenter.getTargetProject()) {
//                addDependencyToTarget(manager)
//            }

            ModuleArchiveLogger.logLifecycle("Handle dependency：${replaceProject.name}:${dependency.name}  ")

            if (manager.cacheValid) {
                //缓存命中

                ModuleArchiveLogger.logLifecycle("${replaceProject.name} 依赖 ${manager.obtainName()} 缓存命中 ${configuration.state}")
                //添加依赖路径
                replaceProject.repositories.flatDir {
                    this.dir(moduleArchiveExtension.storeLibsDir)
                }
                //https://issuetracker.google.com/issues/165821826
                //移除原始的project依赖
                configuration.dependencies.remove(dependency)
                //添加aar依赖
                configuration.dependencies.add(manager.obtainAARDependency())
            } else {
                ModuleArchiveLogger.logLifecycle("${replaceProject.name} 依赖 ${manager.obtainName()} 没有命中缓存")
                //不存在文件进行构建
                ProjectManageHelper.buildAARGraph(infoCenter, manager)
            }
        }

        //当前进行替换replaceProject是否在管理的范围
        val replaceProInManager = managerList.firstOrNull { it.obtainProject() == replaceProject }

        //记录依赖关系
        if (manager != null && replaceProInManager != null) {
            replaceProInManager.dependencyManagerList.add(manager)
        }

        //替换自工程的依赖
        replaceDependency(dependencyProject, replaceProject)
    }

    private fun configMatchExclude(configuration: Configuration, dependency: Dependency): Boolean {
        for (excludeRule in configuration.excludeRules) {

            return if (excludeRule.module.isNullOrBlank()) {
                dependency.group == excludeRule.group
            } else {
                dependency.group == excludeRule.group && dependency.name == excludeRule.module
            }
        }
        return false
    }
}