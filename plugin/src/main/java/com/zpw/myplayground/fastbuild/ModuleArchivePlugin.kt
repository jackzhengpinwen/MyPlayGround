package com.zpw.myplayground.fastbuild

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.modulearchive.dependency.DependencyReplaceHelper
import java.io.FileReader
import java.util.*


class ModuleArchivePlugin : Plugin<Project>, IInfoCenter {
    private lateinit var plugin: ModuleArchivePlugin
    private lateinit var project: Project
    private lateinit var moduleArchiveTask: ModuleArchiveTask
    private lateinit var moduleArchiveExtension: ModuleArchiveExtension
    private lateinit var dependencyReplaceHelper: DependencyReplaceHelper
    private lateinit var propertyInfoHelper: PropertyInfoHelper

    private var projectManageWrapperList: List<ProjectManageWrapper> = emptyList()

    override fun apply(project: Project) {
        ModuleArchiveLogger.logLifecycle("ModuleArchivePlugin apply")
        this.plugin = this
        this.project = project

        //构造配置
        this.moduleArchiveExtension = project.extensions.create<ModuleArchiveExtension>(
            "moduleArchive",
            ModuleArchiveExtension::class.java,
            project
        )

        val moduleArchiveTask: TaskProvider<ModuleArchiveTask> =
            project.tasks.register(
                "moduleArchiveTask",
                ModuleArchiveTask::class.java
            )

        this.moduleArchiveTask = moduleArchiveTask.get()


        dependencyReplaceHelper = DependencyReplaceHelper(this)
        propertyInfoHelper = PropertyInfoHelper(this)
        moduleArchiveTask.get().infoCenter = this
        moduleArchiveTask.get().doLast {
            propertyInfoHelper.writeFile()
        }
        //是否开启日志
        project.gradle.projectsEvaluated {
            //沒有啓用直接返回
            if (!moduleArchiveExtension.pluginEnable) {
                return@projectsEvaluated
            }

            val androidExtension = project.extensions.getByName("android") as BaseAppModuleExtension

            androidExtension.applicationVariants.forEach { variant ->
                ModuleArchiveLogger.logLifecycle("variant is ${variant.name}")
                variant.assembleProvider.get().finalizedBy(moduleArchiveTask)
            }

            var starTime = System.currentTimeMillis();
            //赋值日志是否启用
            ModuleArchiveLogger.enableLogging = moduleArchiveExtension.logEnable

            for (childProject in project.rootProject.childProjects) {
                ModuleArchiveLogger.logLifecycle("childProject is ${childProject.key}")
                childProject.value.repositories.flatDir {
                    this.dir(moduleArchiveExtension.storeLibsDir)
                }
            }

            val launcher = project.gradle.startParameter.taskNames.firstOrNull { taskName ->
                if (moduleArchiveExtension.detectLauncherRegex.isNullOrBlank()) {
                    taskName.contains(project.name)
                } else {
                    taskName.contains(moduleArchiveExtension.detectLauncherRegex)
                }

            }
            if (launcher.isNullOrBlank()) {
                ModuleArchiveLogger.logLifecycle("检测任务不相关不启用替换逻辑")
                return@projectsEvaluated
            }


            //转化对象并计算出缓存是否有效
            projectManageWrapperList = CacheGraphCalcHelper.calcCacheValid(plugin).toMutableList()


            //设置task输出目录
            moduleArchiveTask.get().aarOutDir(moduleArchiveExtension.storeLibsDir)

            dependencyReplaceHelper.replaceDependency()

            val endTime = System.currentTimeMillis();
            ModuleArchiveLogger.logLifecycle("插件花費的配置時間${endTime - starTime}")
        }
    }


    fun readConfig() {
        val properties = Properties()
        val file = project.rootProject.file("moduleArchiveConfig")
        if (file.exists()) {
            val reader = FileReader(file)
            properties.load(reader)
        }
    }

    override fun getModuleArchivePlugin(): ModuleArchivePlugin {
        return this
    }

    override fun getModuleArchiveExtension(): ModuleArchiveExtension {
        return moduleArchiveExtension
    }

    override fun getDependencyReplaceHelper(): DependencyReplaceHelper {
        return dependencyReplaceHelper
    }

    override fun getModuleArchiveTask(): ModuleArchiveTask {
        return moduleArchiveTask
    }

    override fun getTargetProject(): Project {
        return project
    }

    override fun getPropertyInfoHelper(): PropertyInfoHelper {
        return propertyInfoHelper

    }

    override fun getManagerList(): List<ProjectManageWrapper> {
        return projectManageWrapperList
    }
}