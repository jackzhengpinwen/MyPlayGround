package com.zpw.myplayground.fastbuild

open class ProjectManage constructor(val name: String) {

    /**
     * 使用debug包
     */
    var useDebug: Boolean = true

    /**
     * 是否启用
     */
    var enable: Boolean = true

    /**
     * 配置aar
     */
    var aarName: String = "_${name.replace(":","")}.aar"

    /**
     * 风味组合名 如AAXX
     */
    var flavorName: String = ""


    init {

    }


}