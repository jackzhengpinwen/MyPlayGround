package com.zpw.myplayground.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter


fun <T> Class<T>.ASMPrint() {
    println("class name is " + this.canonicalName)
    val parsingOptions = ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_DEBUG)
    val asmCode = true

    val printer = if (asmCode) {
        ASMifier()
    } else {
        Textifier()
    }

    val printWriter = PrintWriter(System.out, true)
    val traceClassVisitor = TraceClassVisitor(null, printer, printWriter)
    try {
        ClassReader(this.canonicalName).accept(traceClassVisitor, parsingOptions)
    } catch (e: Exception) {
        println("e is " + e.printStackTrace())
    }
}