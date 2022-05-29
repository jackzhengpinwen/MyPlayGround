package com.zpw.myplayground.fastbuild

import com.android.build.gradle.LibraryExtension

object ProjectManageHelper {


    /**
     * 让当前aar工程参与构建
     */
    fun buildAARGraph(infoCenter: IInfoCenter, projectManage: ProjectManageWrapper) {

        val aarProject = projectManage.obtainProject()
//        aarProject.afterEvaluate {
//            println()
//        }
        aarProject.plugins.all {
            //是AndroidLib 插件
            if (this is com.android.build.gradle.LibraryPlugin) {
                val extension: LibraryExtension =
                    aarProject.extensions.getByName("android") as LibraryExtension
                extension.libraryVariants.forEach { variant ->

                    //构建体必须相同
                    if (projectManage.originData.useDebug == variant.buildType.isDebuggable && variant.flavorName == projectManage.originData.flavorName) {
                        val packageLibraryProvider = variant.packageLibraryProvider

                        //链接构建无环图
                        infoCenter.getModuleArchiveTask()
                            .aarInput(packageLibraryProvider, projectManage,variant.assembleProvider)

                        ModuleArchiveLogger.logLifecycle("${projectManage.obtainName()}:  aar join build")
                        return@all
                    }
                }
            }
        }

    }

    /**
     *

    /**
     * 让当前aar工程参与构建
     */
    fun obtainAARDirInspection(
        infoCenter: IInfoCenter,
        projectManage: ProjectManage,
    ) {
        val targetProject = infoCenter.getTargetProject()
        val aarProject = projectManage.obtainProject(infoCenter.getTargetProject())
        aarProject.state

        aarProject.plugins.all { plugin ->
            //是AndroidLib 插件
            if (plugin is com.android.build.gradle.LibraryPlugin) {
                val extension: LibraryExtension =
                    aarProject.extensions.getByName("android") as LibraryExtension
                extension.libraryVariants.all { variant ->

                    //构建体必须相同
                    if (projectManage.useDebug == variant.buildType.isDebuggable && variant.flavorName == projectManage.flavorName) {
                        val packageLibraryProvider = variant.packageLibraryProvider
                        val aar = packageLibraryProvider.get()
                        var smf: String = ""
                        variant.sourceSets.forEach { provider ->
                            smf += provider.getManifestFile().lastModified()

                            provider.javaDirectories.forEach { directory ->
                                println("javaDirectories ${directory.lastModified()}  ${directory.absolutePath}")
                                smf += directory.lastModified()
                                targetProject.fileTree(directory).forEach { file ->
                                    println("ProjectManageHelper.obtainAARDirInspection")
                                }
                            }

                            provider.kotlinDirectories.forEach { directory ->
                                println("kotlinDirectories ${directory.lastModified()}  ${directory.absolutePath}")

                                smf += directory.lastModified()
                            }

//                            provider.resourcesDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//
//                            provider.aidlDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//
//                            provider.renderscriptDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//
//                            provider.cDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//                            provider.cppDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//                            provider.resDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//                            provider.assetsDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//                            provider.jniLibsDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//
//                            provider.shadersDirectories.forEach { directory ->
//                                smf += directory.lastModified()
//                            }
//
//                            provider.customDirectories.forEach { directory ->
//                                smf +=  directory.directory.lastModified()
//                            }

                        }

//                        println("测试代码:::${smf.hashCode()}")
//                        println("测试代码22222:::${smf}")
//                        //链接构建无环图
//                        infoCenter.getModuleArchiveTask()
//                            .aarInput(packageLibraryProvider, projectManage)
//                        ModuleArchiveLogger.logLifecycle("${projectManage.name}: ${projectManage.flavorName} aar join build")
                        return@all
                    }
                }
            }
        }
    }
     */

}