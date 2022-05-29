package com.zpw.myplayground.fastbuild

import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

object CacheGraphCalcHelper {

    /**
     * 计算出缓存有效
     */
    fun calcCacheValid(info: IInfoCenter): List<ProjectManageWrapper> {

        val propertyInfoHelper = info.getPropertyInfoHelper()
        val projectManageList =
            info.getModuleArchiveExtension().projectConfig.toList().map { it.convertWrapper(info) }
        val countDownLatch = CountDownLatch(projectManageList.size)

        for (projectManage in projectManageList) {
            thread {
                projectManage.cacheValid = propertyInfoHelper.currentAarHit(projectManage)
                countDownLatch.countDown()
            }
        }
        countDownLatch.await()
        return projectManageList
    }

    /**
     * 计算出依赖图
     */
//    fun calcDependenceCacheValid(info: IInfoCenter) {
//        val pro = info.getTargetProject()
//        val propertyInfoHelper = info.getPropertyInfoHelper()
//        val moduleList = info.getModuleArchiveExtension().getProjectConfig().toList()
//        for (projectManage in moduleList) {
//            projectManage.obtainProject(pro).configurations.all { configuration ->
//                configuration.dependencies.all {dependency ->
//                    if (dependency !is ProjectDependency) {
//                        return
//                    }
//                }
//            }
//        }
//    }


}