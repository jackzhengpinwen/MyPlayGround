package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.util.Printer

class InfoMethodVisitor(api: Int, visitMethod: MethodVisitor): MethodVisitor(api, visitMethod) {
    override fun visitCode() {
//        println("MethodVisitor.visitCode()")
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
//        println(String.format("MethodVisitor.visitInsn(%s)", Printer.OPCODES[opcode]))
        super.visitInsn(opcode)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
//        println(String.format("MethodVisitor.visitIntInsn(%s, %s)", Printer.OPCODES[opcode], operand))
        super.visitIntInsn(opcode, operand)
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {
//        println(String.format("MethodVisitor.visitVarInsn(%s, %s)", Printer.OPCODES[opcode], `var`))
        super.visitVarInsn(opcode, `var`)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
//        println(String.format("MethodVisitor.visitTypeInsn(%s, %s)", Printer.OPCODES[opcode], type))
        super.visitTypeInsn(opcode, type)
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
//        println(String.format("MethodVisitor.visitFieldInsn(%s, %s, %s, %s)", Printer.OPCODES[opcode], owner, name, descriptor))
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
//        println(String.format("MethodVisitor.visitMethodInsn(%s, %s, %s, %s, %s)", Printer.OPCODES[opcode], owner, name, descriptor, isInterface))
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
//        println(String.format("MethodVisitor.visitJumpInsn(%s, %s)", Printer.OPCODES[opcode], label))
        super.visitJumpInsn(opcode, label)
    }

    override fun visitLabel(label: Label?) {
//        println(String.format("MethodVisitor.visitLabel(%s)", label))
        super.visitLabel(label)
    }

    override fun visitLdcInsn(value: Any?) {
//        println(String.format("MethodVisitor.visitLdcInsn(%s)", value))
        super.visitLdcInsn(value)
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
//        println(String.format("MethodVisitor.visitIincInsn(%s, %s)", `var`, increment))
        super.visitIincInsn(`var`, increment)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
//        println(String.format("MethodVisitor.visitMaxs(%s, %s)", maxStack, maxLocals))
        super.visitMaxs(maxStack, maxLocals)
    }

    override fun visitEnd() {
//        println(String.format("MethodVisitor.visitEnd()"))
        super.visitEnd()
    }
}