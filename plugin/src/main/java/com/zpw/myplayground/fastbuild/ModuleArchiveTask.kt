package com.zpw.myplayground.fastbuild

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.Zip
import java.io.File

public abstract class ModuleArchiveTask : DefaultTask() {

    @InputFiles
    @SkipWhenEmpty
    abstract fun getInputAARList(): ConfigurableFileCollection

    @OutputDirectory
    var outPutDirFile = File(".")

    @Internal
    var reNameMap = HashMap<String, ProjectManageWrapper>()

    @Internal
    var infoCenter: IInfoCenter? = null

    @TaskAction
    fun perform() {
        ModuleArchiveLogger.logLifecycle("ModuleArchiveTask perform")
        project.copy {
            this.from(getInputAARList())
            this.into(outPutDirFile)
            this.rename { name ->
                val projectManage = reNameMap[name]

                if (projectManage != null) {
                    ModuleArchiveLogger.logLifecycle("Copy aar  from $name to ${projectManage.originData.aarName}.")
                    infoCenter?.getPropertyInfoHelper()?.upProjectManager(projectManage)
                    return@rename projectManage.originData.aarName
                }
                name
            }

        }

    }


    fun aarInput(
        taskProvider: TaskProvider<Zip>,
        projectManage: ProjectManageWrapper,
        packageLibraryProvider: TaskProvider<Task>
    ) {
        getInputAARList().from(taskProvider)
        //不知道为什么有时候单纯依靠上面的输入输出关联有时候无法成功触发编译aar
        //因此加入下面的代码
        dependsOn(packageLibraryProvider.get())

        val zip = taskProvider.get()
        val archiveFileName = zip.archiveFileName.get()
        reNameMap[archiveFileName] = projectManage
    }

    fun aarOutDir(dir: File) {
        outPutDirFile = dir
    }


}