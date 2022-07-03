package com.zpw.myplayground.transform.internal.test

import com.zpw.myplayground.transform.internal.test.factory.MethodVisitorFactoryImpl
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import kotlin.collections.ArrayList

class InfoClassVisitor(nextVisitor: ClassVisitor) : ClassVisitor(ASM9, nextVisitor) {
    lateinit var className: String
    lateinit var methodName: String
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
//        println(String.format("ClassVisitor.visit(%d, %s, %s, %s, %s, %s);",
//            version, getAccess(access), name, signature, superName, Arrays.toString(interfaces)))
        className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
//        println(String.format("ClassVisitor.visitField(%s, %s, %s, %s, %s);",
//            getAccess(access), name, descriptor, signature, value))

        val visitField = super.visitField(access, name, descriptor, signature, value)
        return InfoFieldVisitor(ASM9, visitField)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
//        println(String.format("ClassVisitor.visitMethod(%s, %s, %s, %s, %s);",
//            getAccess(access), name, descriptor, signature, exceptions))
        println("classname is ${className}")
        if (!className.startsWith("com/zpw")) return super.visitMethod(access, name, descriptor, signature, exceptions)
        methodName = name
        var visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        if(visitMethod != null && "<init>" == name) {
            val isAbstractMethod = (access.and(ACC_ABSTRACT) == ACC_ABSTRACT)
            val isNativeMethod = (access.and(ACC_NATIVE) == ACC_NATIVE)
            if(!isAbstractMethod && !isNativeMethod) {
//                visitMethod = MethodEnterVisitor(ASM9, visitMethod, className, methodName)
//                visitMethod = MethodExitVisitor(ASM9, visitMethod, className, methodName)
//                visitMethod = MethodParameterVisitor(ASM9, visitMethod, access, name, descriptor)

            }
        }
        return visitMethod
    }

    private fun getAccess(access: Int): String {
        val list = ArrayList<String>()
        if((access.and(ACC_PUBLIC)) != 0) list.add("ACC_PUBLIC")
        if((access.and(ACC_PROTECTED)) != 0) list.add("ACC_PROTECTED")
        if((access.and(ACC_PRIVATE)) != 0) list.add("ACC_PRIVATE")
        return list.toString()
    }
}