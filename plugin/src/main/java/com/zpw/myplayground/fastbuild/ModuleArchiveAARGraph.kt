package com.zpw.myplayground.fastbuild

object ModuleArchiveAARGraphHelper {

    fun buildGraph(config: IInfoCenter) {
        val toList = config.getModuleArchiveExtension().projectConfig.toList()
        println("你好 ---- ${toList}")
    }

}