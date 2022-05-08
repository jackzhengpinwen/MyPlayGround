package com.zpw.myplayground.transform.internal.log

import com.zpw.myplayground.logger
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.tree.FieldInsnNode

private const val LOGCAT = "android/util/Log"
private const val INSTRUMENT = "com/zpw/myplayground/utils/"
private const val LOG = "${INSTRUMENT}Logcat"
//private val LOG_METHODS = setOf("v", "d", "i", "w", "e", "wtf", "println")
private val LOG_METHODS = setOf("d")

class LogcatClassVisitor(private val nextVisitor: ClassVisitor) : ClassNode(Opcodes.ASM7) {
    override fun visitEnd() {
        super.visitEnd()
//        logger.log("name is ${name}")
        val iterator: Iterator<MethodNode> = methods.iterator()
        while (iterator.hasNext()) {
            val method = iterator.next()
//            logger.log("method is ${method.name}")
            method.instructions?.iterator()?.asIterable()?.filter {
                when (it.opcode) {
                    INVOKESTATIC -> (it as MethodInsnNode).owner == LOGCAT && LOG_METHODS.contains(it.name)
                    else -> false
                }
            }?.forEach {
                when (it.opcode) {
                    INVOKESTATIC -> {
//                        logger.log(" * ${(it as MethodInsnNode).owner}.${it.name}${it.desc} => $LOG.${it.name}${it.desc}: ${name}.${method.name}${method.desc}")
                        (it as MethodInsnNode).owner = LOG
                    }
                }
            }
        }
        accept(nextVisitor)
    }
}

fun <T> Iterator<T>.asIterable(): Iterable<T> = Iterable { this }