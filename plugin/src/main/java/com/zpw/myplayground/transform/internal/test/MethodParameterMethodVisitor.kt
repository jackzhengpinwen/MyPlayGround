package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type


class MethodParameterMethodVisitor(api: Int, mv: MethodVisitor, private val methodAccess: Int, private val methodName: String, private val methodDesc: String): MethodVisitor(api, mv) {
    override fun visitCode() {
        val isStatic = ((methodAccess.and(ACC_STATIC)) != 0)
        var slotIndex = if (isStatic) 0 else 1
        printMessage("Method Enter: $methodName$methodDesc")

        val methodType = Type.getMethodType(methodDesc)
        val argumentTypes = methodType.argumentTypes
        argumentTypes.forEach { t ->
            val sort = t.sort
            val size = t.size
            val descriptor = t.descriptor
            val opcode = t.getOpcode(ILOAD)
            visitVarInsn(opcode, slotIndex)
            if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                val methodDesc = String.format("(%s)V", descriptor)
                printValueOnStackByUitl(methodDesc)
            } else {
                printValueOnStackByUitl("(Ljava/lang/Object;)V")
            }
//            if (sort == Type.BOOLEAN) {
//                printBoolean();
//            }
//            else if (sort == Type.CHAR) {
//                printChar();
//            }
//            else if (sort == Type.BYTE || sort == Type.SHORT || sort == Type.INT) {
//                printInt();
//            }
//            else if (sort == Type.FLOAT) {
//                printFloat();
//            }
//            else if (sort == Type.LONG) {
//                printLong();
//            }
//            else if (sort == Type.DOUBLE) {
//                printDouble();
//            }
//            else if (sort == Type.OBJECT && "Ljava/lang/String;".equals(descriptor)) {
//                printString();
//            }
//            else if (sort == Type.OBJECT) {
//                printObject();
//            }
//            else {
//                printMessage("No Support");
//            }
            slotIndex += size;
        }
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
            printMessage("Method Exit: $methodName$methodDesc")
            if (opcode >= IRETURN && opcode <= DRETURN) {
                val methodType = Type.getMethodType(methodDesc)
                val returnType = methodType.returnType
                val size = returnType.size
                val descriptor = returnType.descriptor
                if (size == 1) {
                    super.visitInsn(DUP)
                } else {
                    super.visitInsn(DUP2)
                }
                val methodDesc = String.format("(%s)V", descriptor)
                printValueOnStackByUitl(methodDesc)
            } else if (opcode === ARETURN) {
                super.visitInsn(DUP)
                printValueOnStackByUitl("(Ljava/lang/Object;)V")
            } else if (opcode === RETURN) {
                printMessage("    return void")
            } else {
                printMessage("    abnormal return")
            }
//            if (opcode == IRETURN) {
//                super.visitInsn(DUP);
//                printInt();
//            }
//            else if (opcode == FRETURN) {
//                super.visitInsn(DUP);
//                printFloat();
//            }
//            else if (opcode == LRETURN) {
//                super.visitInsn(DUP2);
//                printLong();
//            }
//            else if (opcode == DRETURN) {
//                super.visitInsn(DUP2);
//                printDouble();
//            }
//            else if (opcode == ARETURN) {
//                super.visitInsn(DUP);
//                printObject();
//            }
//            else if (opcode == RETURN) {
//                printMessage("    return void");
//            }
//            else {
//                printMessage("    abnormal return");
//            }
        }
        super.visitInsn(opcode)
    }

    private fun printMessageByUitl(str: String) {
        visitLdcInsn(str)
        visitMethodInsn(
            INVOKESTATIC,
            "com.zpw.myplayground.transform.internal.test.ParameterUtils".replace(".", "/"),
            "printText",
            "(Ljava/lang/String;)V",
            false
        )
    }

    private fun printValueOnStackByUitl(descriptor: String) {
        visitMethodInsn(
            INVOKESTATIC,
            "com.zpw.myplayground.transform.internal.test.ParameterUtils".replace(".", "/"),
            "printValueOnStack",
            descriptor,
            false
        )
    }

    private fun printBoolean() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(SWAP)
        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false)
    }

    private fun printChar() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(SWAP)
        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(C)V", false)
    }

    private fun printInt() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(SWAP)
        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
    }

    private fun printFloat() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(SWAP)
        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false)
    }

    private fun printLong() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(DUP_X2)
        visitInsn(POP)
        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false)
    }

    private fun printDouble() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(DUP_X2)
        visitInsn(POP)
        visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(D)V", false)
    }

    private fun printString() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(SWAP)
        visitMethodInsn(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        )
    }

    private fun printObject() {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitInsn(SWAP)
        visitMethodInsn(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/Object;)V",
            false
        )
    }

    private fun printMessage(str: String) {
        visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        visitLdcInsn(str)
        visitMethodInsn(
            INVOKEVIRTUAL,
            "java/io/PrintStream",
            "println",
            "(Ljava/lang/String;)V",
            false
        )
    }
}