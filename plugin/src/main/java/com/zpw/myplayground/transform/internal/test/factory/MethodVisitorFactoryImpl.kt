package com.zpw.myplayground.transform.internal.test.factory

import org.objectweb.asm.MethodVisitor

class MethodVisitorFactoryImpl: MethodVisitorFactory() {
    override fun <T : MethodVisitor> createMethodVisitor(clazz: Class<T>): T {
        var methodVisitor: MethodVisitor? = null
        try {
            methodVisitor = Class.forName(clazz.name).newInstance() as MethodVisitor
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return methodVisitor as T
    }
}