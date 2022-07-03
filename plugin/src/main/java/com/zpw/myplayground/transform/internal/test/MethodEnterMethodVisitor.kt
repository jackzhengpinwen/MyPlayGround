package com.zpw.myplayground.transform.internal.test


import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL

class MethodEnterMethodVisitor(api: Int, classVisitor: MethodVisitor, private val className: String, private val methodName: String): MethodVisitor(api, classVisitor) {
    override fun visitCode() {
        visitFieldInsn(
            GETSTATIC,
            "java/lang/System",
            "out",
            "Ljava/io/PrintStream;"
        )
        visitLdcInsn("${className} ${methodName} Method Enter...")
        visitMethodInsn(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        )

        super.visitCode()
    }
}