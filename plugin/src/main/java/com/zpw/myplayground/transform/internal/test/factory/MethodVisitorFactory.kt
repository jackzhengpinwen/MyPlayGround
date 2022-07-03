package com.zpw.myplayground.transform.internal.test.factory

import org.objectweb.asm.MethodVisitor

abstract class MethodVisitorFactory {
    abstract fun <T: MethodVisitor> createMethodVisitor(clazz: Class<T>): T
}