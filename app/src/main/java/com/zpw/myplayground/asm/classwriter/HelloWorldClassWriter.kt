package com.zpw.myplayground.asm.classwriter

import com.zpw.myplayground.asm.FileUtils
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*

class HelloWorldClassWriter {
    companion object {
        private val relativePath = "/Users/zpw/GithubProjects/MyPlayGround/app/src/main/java/com/zpw/myplayground/asm/classwriter.class"
        private val clasName = "com/zpw/myplayground/asm/classwriter/HelloWorld"
        private val nestedClasName = "com/zpw/myplayground/asm/classwriter/GoodChild"
        private val superClass = "java/lang/Object"

        fun generate() {
            FileUtils.writeBytes(relativePath, dumpInterface())
        }

        fun dumpInterface(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                clasName,
                null,
                superClass,
                null
            )
            cw.visitEnd()
            return cw.toByteArray()
        }

        fun dumpInterfaceFiledFunction(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                clasName,
                null,
                superClass,
                arrayOf("java/lang/Cloneable")
            )
            val fv1 = cw.visitField(
                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
                "LESS",
                "I",
                null,
                -1
            )
            val fv2 = cw.visitField(
                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                "EQUAL",
                "I",
                null,
                0
            )
            val fv3 = cw.visitField(
                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                "GREATER",
                "I",
                null,
                1
            )
            val mv1 = cw.visitMethod(
                ACC_PUBLIC + ACC_ABSTRACT,
                "compareTo",
                "(Ljava/lang/Object;)I",
                null,
                null
            )
            cw.visitEnd()
            return cw.toByteArray()
        }

        fun dumpObject(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_SUPER,
                clasName,
                null,
                superClass,
                null
            )
            val mv = cw.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
            )
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(
                INVOKESPECIAL,
                superClass,
                "<init>",
                "()V",
                false
            )
            mv.visitInsn(RETURN)
            mv.visitMaxs(1, 1)
            mv.visitEnd()

            cw.visitEnd()
            return cw.toByteArray()
        }

        fun dumpFiled(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                clasName,
                null,
                superClass,
                null
            )
            val fv1 = cw.visitField(
                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
                "intValue",
                "I",
                null,
                100
            )
            val fv2 = cw.visitField(
                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
                "strValue",
                "Ljava/lang/String;",
                null,
                "ABC"
            )
            cw.visitEnd()
            return cw.toByteArray()
        }

        fun dumpAnnotation(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                clasName,
                null,
                superClass,
                null
            )
            val fv1 = cw.visitField(
                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
                "intValue",
                "I",
                null,
                100
            )
            val anno = fv1.visitAnnotation(
                "Lsample/MyTag;",
                false
            )
            anno.visit("name", "tomcat")
            anno.visit("age", 10)
            anno.visitEnd()
            fv1.visitEnd()
            cw.visitEnd()
            return cw.toByteArray()
        }

        fun dumpNestedObject(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_SUPER,
                clasName,
                null,
                superClass,
                null
            )
            val mv1 = cw.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
            )
            mv1.visitCode()
            mv1.visitVarInsn(ALOAD, 0)
            mv1.visitMethodInsn(
                INVOKESPECIAL,
                superClass,
                "<init>",
                "()V",
                false
            )
            mv1.visitInsn(RETURN)
            mv1.visitMaxs(1, 1)
            mv1.visitEnd()

            val mv2 = cw.visitMethod(
                ACC_PUBLIC,
                "test",
                "()V",
                null,
                null
            )
            mv2.visitCode()
            mv2.visitTypeInsn(NEW, nestedClasName)
            mv2.visitInsn(DUP)
            mv2.visitLdcInsn("Lucy")
            mv2.visitIntInsn(BIPUSH, 0)
            mv2.visitMethodInsn(
                INVOKESPECIAL,
                nestedClasName,
                "<init>",
                "(Ljava/lang/String;I)V",
                false
            )
            mv2.visitVarInsn(ASTORE, 1)
            mv2.visitInsn(RETURN)
            mv2.visitMaxs(4, 2);
            mv2.visitEnd()

            cw.visitEnd()
            return cw.toByteArray()
        }

        fun dumpNestedMethod(): ByteArray? {
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            cw.visit(
                V1_8,
                ACC_PUBLIC + ACC_SUPER,
                clasName,
                null,
                superClass,
                null
            )
            val mv1 = cw.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
            )
            mv1.visitCode()
            mv1.visitVarInsn(ALOAD, 0)
            mv1.visitMethodInsn(
                INVOKESPECIAL,
                superClass,
                "<init>",
                "()V",
                false
            )
            mv1.visitInsn(RETURN)
            mv1.visitMaxs(1, 1)
            mv1.visitEnd()

            val mv2 = cw.visitMethod(
                ACC_PUBLIC,
                "test",
                "(II)V",
                null,
                null
            )
            mv2.visitCode()
            mv2.visitVarInsn(ILOAD, 1);
            mv2.visitVarInsn(ILOAD, 2)
            mv2.visitMethodInsn(
                INVOKESTATIC,
                "java/lang/Math",
                "max",
                "(II)I",
                false
            )
            mv2.visitVarInsn(ISTORE, 3)
            mv2.visitFieldInsn(
                GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;"
            )
            mv2.visitVarInsn(ILOAD, 3)
            mv2.visitMethodInsn(
                INVOKESPECIAL,
                "java/io/PrintStream",
                "println",
                "(I)V",
                false
            )
            mv2.visitInsn(RETURN);
            mv2.visitMaxs(2, 4);
            mv2.visitEnd();
            cw.visitEnd();
            return cw.toByteArray()
        }
    }
}