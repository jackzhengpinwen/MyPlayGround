package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

class MethodTimeMethodVisitor(api: Int, methodVisitor: MethodVisitor, private val owner: String): MethodVisitor(api, methodVisitor) {
    override fun visitCode() {
        visitFieldInsn(
            GETSTATIC,
            owner,
            "timer",
            "J"
        )
        visitMethodInsn(
            INVOKESTATIC,
            "java/lang/System",
            "currentTimeMillis",
            "()J",
            false
        )
        visitInsn(LSUB)
        visitFieldInsn(
            PUTSTATIC,
            owner,
            "timer",
            "J"
        )
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
            visitFieldInsn(
                GETSTATIC,
                owner,
                "timer",
                "J"
            )
            visitMethodInsn(
                INVOKESTATIC,
                "java/lang/System",
                "currentTimeMillis",
                "()J",
                false
            )
            visitInsn(LADD)
            visitFieldInsn(
                PUTSTATIC,
                owner,
                "timer",
                "J"
            )
        }
        super.visitInsn(opcode)
    }
}