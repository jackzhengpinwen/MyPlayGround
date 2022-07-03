package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

class MethodExitMethodVisitor(api: Int, classVisitor: MethodVisitor, private val className: String, private val methodName: String): MethodVisitor(api, classVisitor) {
    override fun visitInsn(opcode: Int) {
        if (opcode == ATHROW || (opcode >= IRETURN && opcode <= RETURN)) {
            visitFieldInsn(
                GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            visitLdcInsn("${className} ${methodName} Method Exit...")
            visitMethodInsn(
                INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false
            )
        }
        super.visitInsn(opcode)
    }
}