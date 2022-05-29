package com.zpw.myplayground.fastbuild

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import java.io.File

fun ProjectManage.convertWrapper(info: IInfoCenter): ProjectManageWrapper {
    return ProjectManageWrapper(this, info)
}


open class ProjectManageWrapper(val originData: ProjectManage, private val info: IInfoCenter) {


    /**
     * 缓存是否有效
     */
    var cacheValid = false


    /**
     * 对应的project
     */
    private var project: Project? = null

    /**
     * 缓存的aar文件的地址
     */
    private var aarFile: File? = null

    /**
     * 最後修改文件夾的時間
     */
    private var lastModified: Long = 0

    /**
     * 对应替换的后的依赖声明
     */
    private var dependency: Dependency? = null


    /**
     * 这个工程依赖的其他module
     */
    var dependencyManagerList: MutableSet<ProjectManageWrapper> = mutableSetOf()

    /**
     * 获取关联的project对象
     */
    fun obtainProject(): Project {
        if (this.project == null) {
            this.project = info.getTargetProject().project(obtainName())

        }
        return this.project!!
    }

    /**
     * 获取缓存的aar文件地址
     */
    fun obtainCacheAARFile(): File {
        if (this.aarFile == null) {
            val moduleArchiveExtension = info.getModuleArchiveExtension()
            aarFile = File(
                moduleArchiveExtension.storeLibsDir,
                originData.aarName
            )
        }

        return aarFile!!
    }


    /**
     * 这个project文件下的最后修改时间
     */
    fun obtainLastModified(): Long {

        if (lastModified <= 0) {
            val aarProject = obtainProject()
            val file = aarProject.fileTree(".").matching {
                this.exclude("build", ".gradle",".cxx")
            }.toList().maxBy {
                it.lastModified()
            }
            lastModified = file?.lastModified() ?: 0
        }
        return lastModified
    }

    /**
     * 这个对象的名称
     */
    fun obtainName(): String = originData.name


    /**
     * 获取替换的依赖声明
     */
    fun obtainAARDependency(): Dependency {
        if (this.dependency == null) {
            val obtainProject = obtainProject()
            this.dependency = obtainProject.dependencies.create(
                mapOf(
                    "name" to obtainCacheAARFile().name.replace(
                        ".aar",
                        ""
                    ), "ext" to "aar"
                )
            )
        }
        return dependency as Dependency
    }

    /**
     * 当前依赖被引用
     */
    var flagHasOut: Boolean = false


}