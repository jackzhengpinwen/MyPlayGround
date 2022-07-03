package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodAroundMethodVIsitor(api: Int, classVisitor: MethodVisitor, private val className: String, private val methodName: String): MethodVisitor(api, classVisitor) {
    override fun visitCode() {
        visitFieldInsn(
            Opcodes.GETSTATIC,
            "java/lang/System",
            "out",
            "Ljava/io/PrintStream;"
        )
        visitLdcInsn("${className} ${methodName} Method Enter...")
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        )

        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        if (opcode == Opcodes.ATHROW || (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
            visitFieldInsn(
                Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            visitLdcInsn("${className} ${methodName} Method Exit...")
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V",
                false
            )
        }
        super.visitInsn(opcode)
    }
}