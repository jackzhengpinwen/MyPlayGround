package com.zpw.myplayground.doubleclickcheck.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode

val MethodNode.isStatic: Boolean
    get() = access and Opcodes.ACC_STATIC != 0

val MethodNode.nameWithDesc: String
    get() = name + desc

fun MethodNode.findLambda(
    filter: (InvokeDynamicInsnNode) -> Boolean
): List<InvokeDynamicInsnNode> {
    val handleList = mutableListOf<InvokeDynamicInsnNode>()
    val instructions = instructions?.iterator() ?: return handleList
    while (instructions.hasNext()) {
        val nextInstruction = instructions.next()
        if (nextInstruction is InvokeDynamicInsnNode) {
            if (filter(nextInstruction)) {
                handleList.add(nextInstruction)
            }
        }
    }
    return handleList
}