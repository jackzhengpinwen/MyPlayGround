package com.zpw.lib.fixture

import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories

abstract class AbstractProject: AutoCloseable {
    // 创建 project 根目录
    val projectDir = Path.of("build/functionalTest/${slug()}").createDirectories()
    // 创建 build 编译目录
    private val buildDir = projectDir.resolve("build")

    // 在 build 目录下创建指定目录
    fun buildFile(filename: String): Path {
        return buildDir.resolve(filename)
    }

    // 根据子类创建特殊的文件名
    private fun slug(): String {
        val worker = System.getProperty("org.gradle.test.worker")?.let { w ->
            "-$w"
        }.orEmpty()
        return "${javaClass.simpleName}-${UUID.randomUUID().toString().take(16)}$worker"
    }

    override fun close() {
        projectDir.toFile().deleteRecursively()
    }
}