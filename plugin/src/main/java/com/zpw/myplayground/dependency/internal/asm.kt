package com.zpw.myplayground.dependency.internal

import com.zpw.myplayground.logger
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM6
import org.slf4j.Logger

/**
 * 这将只收集类名。
 */
class ClassNameCollector() : ClassVisitor(ASM6) {

    var className: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
    }
}

/**
 * 这将收集类名和该类使用的所有类的名称以及该类的方法。
 */
class ClassAnalyzer : ClassVisitor(Opcodes.ASM6) {

    private val classes = mutableSetOf<String>()
    private val methodAnalyzer = MethodAnalyzer(classes)
    private val fieldAnalyzer = FieldAnalyzer(classes)
    private val annotationAnalyzer = AnnotationAnalyzer(classes)

    fun classes(): Set<String> = classes

    private fun addClass(className: String?) {
        classes.addClass(className)
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        logger.log("ClassAnalyzer#visit: $name extends $superName {")
        addClass("L$superName;")
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        logger.log("ClassAnalyzer#visitField: $descriptor $name")
        addClass(descriptor)
        return fieldAnalyzer
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        logger.log("ClassAnalyzer#visitMethod: $name $descriptor")

        descriptor?.let {
            METHOD_DESCRIPTOR_REGEX.findAll(it).forEach {
                addClass(it.value)
            }
        }

        return methodAnalyzer
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        logger.log("ClassAnalyzer#visitAnnotation: descriptor=$descriptor visible=$visible")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor? {
        logger.log("ClassAnalyzer#visitTypeAnnotation: typeRef=$typeRef typePath=$typePath descriptor=$descriptor visible=$visible")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitEnd() {
        logger.log("}")
    }
}

class MethodAnalyzer(
    private val classes: MutableSet<String>
) : MethodVisitor(Opcodes.ASM6) {

    private val annotationAnalyzer = AnnotationAnalyzer(classes)

    private fun addClass(className: String?) {
        classes.addClass(className)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        logger.log("- MethodAnalyzer#visitTypeInsn: $type")
        addClass("L$type;")
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        logger.log("- MethodAnalyzer#visitFieldInsn: $owner.$name $descriptor")
        addClass("L$owner;")
        addClass(descriptor)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        logger.log("- MethodAnalyzer#visitMethodInsn: $owner.$name $descriptor")
        addClass("L$owner;")
        descriptor?.let {
            METHOD_DESCRIPTOR_REGEX.findAll(it).forEach {
                addClass(it.value)
            }
        }
    }

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?
    ) {
        logger.log("- MethodAnalyzer#visitInvokeDynamicInsn: $name $descriptor")
        addClass(descriptor)
    }

    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {
        logger.log("- MethodAnalyzer#visitLocalVariable: $name $descriptor")
        addClass(descriptor)
    }

    override fun visitLocalVariableAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        start: Array<out Label>?,
        end: Array<out Label>?,
        index: IntArray?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
        logger.log("- MethodAnalyzer#visitLocalVariableAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        logger.log("- MethodAnalyzer#visitAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitInsnAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
        logger.log("- MethodAnalyzer#visitInsnAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor? {
        logger.log("- MethodAnalyzer#visitParameterAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        logger.log("- MethodAnalyzer#visitTryCatchBlock: $type")
        addClass("L$type;")
    }

    override fun visitTryCatchAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
        logger.log("- MethodAnalyzer#visitTryCatchAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }
}

private class AnnotationAnalyzer(
    private val classes: MutableSet<String>,
    private val level: Int = 0
) : AnnotationVisitor(Opcodes.ASM6) {

    private fun addClass(className: String?) {
        classes.addClass(className)
    }

    private fun indent() = "  ".repeat(level)

    override fun visit(name: String?, value: Any?) {
        fun getValue(value: Any?): String? {
            return if (value is String && value.contains("\n")) {
                ""
            } else {
                value.toString()
            }
        }

        logger.log("${indent()}- AnnotationAnalyzer#visit: name=$name, value=(${value?.javaClass?.simpleName}, ${getValue(value)})")
        if (value is String) {
            addClass(value)
        }
    }

    override fun visitEnum(name: String?, descriptor: String?, value: String?) {
        logger.log("${indent()}- AnnotationAnalyzer#visitEnum: name=$name, descriptor=$descriptor, value=$value")
        addClass(descriptor)
    }

    override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor? {
        logger.log("${indent()}- AnnotationAnalyzer#visitAnnotation: name=$name, descriptor=$descriptor")
        addClass(descriptor)
        return AnnotationAnalyzer(classes, level + 1)
    }

    override fun visitArray(name: String?): AnnotationVisitor? {
        logger.log("${indent()}- AnnotationAnalyzer#visitArray: name=$name")
        return AnnotationAnalyzer(classes, level + 1)
    }
}

private class FieldAnalyzer(
    private val classes: MutableSet<String>
) : FieldVisitor(Opcodes.ASM6) {

    private val annotationAnalyzer = AnnotationAnalyzer(classes)

    private fun addClass(className: String?) {
        classes.addClass(className)
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        addClass(descriptor)
        return annotationAnalyzer
    }
}

// Begins with an 'L'
// followed by at least one word character
// followed by one or more word char, /, or $, in any combination
// ends with a ';'
// Not perfect, but probably close enough
private val METHOD_DESCRIPTOR_REGEX = """L\w[\w/$]+;""".toRegex()

private fun MutableSet<String>.addClass(className: String?) {
    className?.let {
        it.replace("[", "")
        if(it.startsWith("L")) {
            add(it.substring(1, it.length - 1))
        }
    }
}