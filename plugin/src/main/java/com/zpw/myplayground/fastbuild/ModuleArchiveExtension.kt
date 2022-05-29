package com.zpw.myplayground.fastbuild

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.io.File

open class ModuleArchiveExtension {
    /**
     * 是否启用插件
     */
    var pluginEnable: Boolean = false

    /**
     * 是否打印日志
     */
    var logEnable: Boolean = false

    /***
     * 如果当前的task任务名称满足就启动依赖替换
     */
    var detectLauncherRegex: String = ""


    /**
     * 得到存储lib的目录
     */
    var storeLibsDir: File = File("")

    /**
     * 管理子module
     */

    var projectConfig: NamedDomainObjectContainer<ProjectManage>

    constructor(project: Project) {
        projectConfig = project.container(ProjectManage::class.java)
    }


    fun subModuleConfig(action: Action<NamedDomainObjectContainer<ProjectManage>>) {
        action.execute(projectConfig)
    }

}