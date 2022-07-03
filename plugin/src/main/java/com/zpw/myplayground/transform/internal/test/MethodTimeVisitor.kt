package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import kotlin.properties.Delegates

class MethodTimeVisitor(classVisitor: ClassVisitor): ClassVisitor(ASM9, classVisitor) {
    lateinit var owner: String
    var isInterface by Delegates.notNull<Boolean>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        owner = name
        isInterface = (access.and(ACC_INTERFACE)) != 0
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (!isInterface && methodVisitor != null && "<init>" != name && "<clinit>" != name) {
            val isAbstractMethod = (access.and(ACC_ABSTRACT)) != 0
            val isNativeMethod = (access.and(ACC_NATIVE)) != 0
            if (!isAbstractMethod && !isNativeMethod) {
                methodVisitor = MethodTimeMethodVisitor(ASM9, methodVisitor, owner)
            }
        }
        return methodVisitor
    }

    override fun visitEnd() {
        if (!isInterface) {
            visitField(
                ACC_PUBLIC + ACC_STATIC,
                "timer",
                "J",
                null,
                null
            )?.visitEnd()
        }
        super.visitEnd()
    }
}