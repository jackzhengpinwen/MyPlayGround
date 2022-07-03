package com.zpw.myplayground.asm.classreader

import com.zpw.myplayground.asm.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ASM7
import java.util.*

class HelloWorldClassReader {
    companion object {
        private val testClass = "com.zpw.myplayground.asm.classreader.Test.class".replace(".", "/")

        fun readTest() {
            val bytes = FileUtils.readBytes(testClass)
            val cr = ClassReader(bytes)
            println("access is ${cr.access}")
            println("className is ${cr.className}")
            println("superName is ${cr.superName}")
            println("interfaces is ${Arrays.toString(cr.interfaces)}")
        }

        fun readToTransform() {
            val filePath = ""
            val bytes = FileUtils.readBytes(testClass)
            val cr = ClassReader(bytes)
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val cv = object : ClassVisitor(ASM7, cw) {}
            val parsingOptions = ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES
            cr.accept(cv, parsingOptions)
            val bytes1 = cw.toByteArray()
            FileUtils.writeBytes(filePath, bytes1)
        }
    }
}