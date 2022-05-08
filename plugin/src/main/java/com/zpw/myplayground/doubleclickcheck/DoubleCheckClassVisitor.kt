package com.zpw.myplayground.doubleclickcheck

import com.zpw.myplayground.doubleclickcheck.utils.findLambda
import com.zpw.myplayground.doubleclickcheck.utils.isStatic
import com.zpw.myplayground.doubleclickcheck.utils.nameWithDesc
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

class DoubleCheckClassVisitor(private val nextVisitor: ClassVisitor) : ClassNode(Opcodes.ASM7) {
    companion object {
        private const val OnClickViewMethodDescriptor = "(Landroid/view/View;)V"

        private const val ViewDescriptor = "Landroid/view/View;"

        private const val doubleCheckClass: String = "com.zpw.myplayground.utils.ViewDoubleClickCheck"

        val formatDoubleCheckClass: String
            get() = doubleCheckClass.replace(".", "/")

        private const val doubleCheckMethodName: String = "canClick"

        private const val doubleCheckMethodDescriptor: String = "(Landroid/view/View;)Z"

        private val MethodNode.onlyOneViewParameter: Boolean
            get() = desc == OnClickViewMethodDescriptor
    }

    override fun visitEnd() {
        super.visitEnd()
        if (methods.isNotEmpty()) {
            val shouldHookMethodList = mutableListOf<String>()
            // 遍历方法
            for (methodNode in methods) {
                // 静态不用处理
                if (methodNode.isStatic) continue
                val methodNameWithDesc = methodNode.nameWithDesc
                if (isHookPoint(methodNode)) {
                    shouldHookMethodList.add(methodNameWithDesc)
                }
                //判断方法内部是否有需要处理的 lambda 表达式
                val invokeDynamicInsnNodes = methodNode.findHookPointLambda()
                invokeDynamicInsnNodes.forEach {
                    val handle = it.bsmArgs[1] as? Handle
                    if (handle != null) {
                        shouldHookMethodList.add(handle.name + handle.desc)
                    }
                }
            }
            // 处理命中的方法
            if (shouldHookMethodList.isNotEmpty()) {
                for (methodNode in methods) {
                    val methodNameWithDesc = methodNode.nameWithDesc
                    if (shouldHookMethodList.contains(methodNameWithDesc)) {
                        val argumentTypes = Type.getArgumentTypes(methodNode.desc)
                        val viewArgumentIndex = argumentTypes?.indexOfFirst {
                            it.descriptor == ViewDescriptor
                        } ?: -1
                        if (viewArgumentIndex >= 0) {
                            val instructions = methodNode.instructions
                            if (instructions != null && instructions.size() > 0) {
                                val list = InsnList()
                                list.add(
                                    VarInsnNode(
                                        Opcodes.ALOAD, getVisitPosition(
                                            argumentTypes,
                                            viewArgumentIndex,
                                            methodNode.isStatic
                                        )
                                    )
                                )
                                list.add(
                                    MethodInsnNode(
                                        Opcodes.INVOKESTATIC,
                                        formatDoubleCheckClass,
                                        doubleCheckMethodName,
                                        doubleCheckMethodDescriptor
                                    )
                                )
                                val labelNode = LabelNode()
                                list.add(JumpInsnNode(Opcodes.IFNE, labelNode))
                                list.add(InsnNode(Opcodes.RETURN))
                                list.add(labelNode)
                                instructions.insert(list)
                            }
                        }
                    }
                }
            }
        }
        accept(nextVisitor)
    }

    private fun ClassNode.isHookPoint(methodNode: MethodNode): Boolean {
        if (interfaces.isEmpty()) return false
        extraHookPoints.forEach {
            if (interfaces.contains(it.interfaceName) && methodNode.nameWithDesc == it.methodSign) return true
        }
        return false
    }

    private fun MethodNode.findHookPointLambda(): List<InvokeDynamicInsnNode> {
        val onClickListenerLambda = findLambda {
            val nodeName = it.name
            val nodeDesc = it.desc
            val find = extraHookPoints.find { point ->
                nodeName == point.methodName && nodeDesc.endsWith(point.interfaceSignSuffix)
            }
            return@findLambda find != null
        }
        return onClickListenerLambda
    }

    protected fun getVisitPosition(
        argumentTypes: Array<Type>,
        parameterIndex: Int,
        isStaticMethod: Boolean
    ): Int {
        if (parameterIndex < 0 || parameterIndex >= argumentTypes.size) {
            throw Error("getVisitPosition error")
        }
        return if (parameterIndex == 0) {
            if (isStaticMethod) {
                0
            } else {
                1
            }
        } else {
            getVisitPosition(
                argumentTypes,
                parameterIndex - 1,
                isStaticMethod
            ) + argumentTypes[parameterIndex - 1].size
        }
    }
}

data class DoubleClickHookPoint(
    val interfaceName: String,
    val methodName: String,
    val methodSign: String,
) {
    val interfaceSignSuffix = "L$interfaceName;"
}

private val extraHookPoints = listOf(
    DoubleClickHookPoint(
        interfaceName = "android/view/View\$OnClickListener",
        methodName = "onClick",
        methodSign = "onClick(Landroid/view/View;)V"
    ),
    DoubleClickHookPoint(
        interfaceName = "com/chad/library/adapter/base/BaseQuickAdapter\$OnItemClickListener",
        methodName = "onItemClick",
        methodSign = "onItemClick(Lcom/chad/library/adapter/base/BaseQuickAdapter;Landroid/view/View;I)V"
    ),
    DoubleClickHookPoint(
        interfaceName = "com/chad/library/adapter/base/BaseQuickAdapter\$OnItemChildClickListener",
        methodName = "onItemChildClick",
        methodSign = "onItemChildClick(Lcom/chad/library/adapter/base/BaseQuickAdapter;Landroid/view/View;I)V",
    )
)