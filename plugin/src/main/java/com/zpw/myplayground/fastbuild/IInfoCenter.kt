package com.zpw.myplayground.fastbuild

import org.gradle.api.Project
import org.modulearchive.dependency.DependencyReplaceHelper

interface IInfoCenter {

    fun getModuleArchivePlugin(): ModuleArchivePlugin
    fun getModuleArchiveExtension(): ModuleArchiveExtension
    fun getDependencyReplaceHelper(): DependencyReplaceHelper
    fun getModuleArchiveTask(): ModuleArchiveTask
    fun getTargetProject(): Project
    fun getPropertyInfoHelper(): PropertyInfoHelper
    fun getManagerList(): List<ProjectManageWrapper>
}