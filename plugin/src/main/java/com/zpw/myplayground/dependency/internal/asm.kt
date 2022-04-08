package com.zpw.myplayground.dependency.internal

import com.autonomousapps.internal.asm.tree.*
import com.zpw.myplayground.dependency.internal.kotlin.*
import com.zpw.myplayground.logger
import kotlinx.metadata.jvm.*
import com.autonomousapps.internal.asm.*
import com.autonomousapps.internal.asm.Opcodes.ASM7

val ACCESS_NAMES = mapOf(
    Opcodes.ACC_PUBLIC to "public",
    Opcodes.ACC_PROTECTED to "protected",
    Opcodes.ACC_PRIVATE to "private",
    Opcodes.ACC_STATIC to "static",
    Opcodes.ACC_FINAL to "final",
    Opcodes.ACC_ABSTRACT to "abstract",
    Opcodes.ACC_SYNTHETIC to "synthetic",
    Opcodes.ACC_INTERFACE to "interface",
    Opcodes.ACC_ANNOTATION to "annotation"
)

data class ClassBinarySignature(
    val name: String,
    val superName: String,
    val outerName: String?,
    val supertypes: List<String>,
    val memberSignatures: List<MemberBinarySignature>,
    val access: AccessFlags,
    val isEffectivelyPublic: Boolean,
    val isNotUsedWhenEmpty: Boolean
) {
    val signature: String
        get() = "${access.getModifierString()} class $name" + if (supertypes.isEmpty()) "" else " : ${supertypes.joinToString()}"
}

interface MemberBinarySignature {
    val jvmMember: JvmMemberSignature
    val name: String get() = jvmMember.name
    val desc: String get() = jvmMember.desc
    val access: AccessFlags
    val isPublishedApi: Boolean

    fun isEffectivelyPublic(classAccess: AccessFlags, classVisibility: ClassVisibility?) =
        access.isPublic && !(access.isProtected && classAccess.isFinal)
                && (findMemberVisibility(classVisibility)?.isPublic(isPublishedApi) ?: true)

    fun findMemberVisibility(classVisibility: ClassVisibility?): MemberVisibility? {
        return classVisibility?.findMember(jvmMember)
    }

    val signature: String
}

data class MethodBinarySignature(
    override val jvmMember: JvmMethodSignature,
    override val isPublishedApi: Boolean,
    override val access: AccessFlags
) : MemberBinarySignature {
    override val signature: String
        get() = "${access.getModifierString()} fun $name $desc"

    override fun isEffectivelyPublic(classAccess: AccessFlags, classVisibility: ClassVisibility?) =
        super.isEffectivelyPublic(classAccess, classVisibility)
                && !isAccessOrAnnotationsMethod()
                && !isDummyDefaultConstructor()

    override fun findMemberVisibility(classVisibility: ClassVisibility?): MemberVisibility? {
        return super.findMemberVisibility(classVisibility) ?: classVisibility?.let { alternateDefaultSignature(it.name)?.let(it::findMember) }
    }

    private fun isAccessOrAnnotationsMethod() = access.isSynthetic && (name.startsWith("access\$") || name.endsWith("\$annotations"))

    private fun isDummyDefaultConstructor() = access.isSynthetic && name == "<init>" && desc == "(Lkotlin/jvm/internal/DefaultConstructorMarker;)V"

    /**
     * Calculates the signature of this method without default parameters
     *
     * Returns `null` if this method isn't an entry point of a function
     * or a constructor with default parameters.
     * Returns an incorrect result, if there are more than 31 default parameters.
     */
    private fun alternateDefaultSignature(className: String): JvmMethodSignature? {
        return when {
            !access.isSynthetic -> null
            name == "<init>" && "ILkotlin/jvm/internal/DefaultConstructorMarker;" in desc ->
                JvmMethodSignature(name, desc.replace("ILkotlin/jvm/internal/DefaultConstructorMarker;", ""))
            name.endsWith("\$default") && "ILjava/lang/Object;)" in desc ->
                JvmMethodSignature(
                    name.removeSuffix("\$default"),
                    desc.replace("ILjava/lang/Object;)", ")").replace("(L$className;", "(")
                )
            else -> null
        }
    }
}

data class FieldBinarySignature(
    override val jvmMember: JvmFieldSignature,
    override val isPublishedApi: Boolean,
    override val access: AccessFlags
) : MemberBinarySignature {
    override val signature: String
        get() = "${access.getModifierString()} field $name $desc"

    override fun findMemberVisibility(classVisibility: ClassVisibility?): MemberVisibility? {
        return super.findMemberVisibility(classVisibility)
            ?: takeIf { access.isStatic }?.let { super.findMemberVisibility(classVisibility?.companionVisibilities) }
    }
}

data class AccessFlags(val access: Int) {
    val isPublic: Boolean get() = isPublic(access)
    val isProtected: Boolean get() = isProtected(access)
    val isStatic: Boolean get() = isStatic(access)
    val isFinal: Boolean get() = isFinal(access)
    val isSynthetic: Boolean get() = isSynthetic(access)

    fun getModifiers(): List<String> = ACCESS_NAMES.entries.mapNotNull { if (access and it.key != 0) it.value else null }
    fun getModifierString(): String = getModifiers().joinToString(" ")
}

fun isPublic(access: Int) = access and Opcodes.ACC_PUBLIC != 0 || access and Opcodes.ACC_PROTECTED != 0
fun isProtected(access: Int) = access and Opcodes.ACC_PROTECTED != 0
fun isStatic(access: Int) = access and Opcodes.ACC_STATIC != 0
fun isFinal(access: Int) = access and Opcodes.ACC_FINAL != 0
fun isSynthetic(access: Int) = access and Opcodes.ACC_SYNTHETIC != 0

fun ClassNode.findAnnotation(annotationName: String, includeInvisible: Boolean = false) = findAnnotation(annotationName, visibleAnnotations, invisibleAnnotations, includeInvisible)
fun MethodNode.findAnnotation(annotationName: String, includeInvisible: Boolean = false) = findAnnotation(annotationName, visibleAnnotations, invisibleAnnotations, includeInvisible)
fun FieldNode.findAnnotation(annotationName: String, includeInvisible: Boolean = false) = findAnnotation(annotationName, visibleAnnotations, invisibleAnnotations, includeInvisible)

fun ClassNode.isEffectivelyPublic(classVisibility: ClassVisibility?) =
    isPublic(access)
            && !isLocal()
            && !isWhenMappings()
            && (classVisibility?.isPublic(isPublishedApi()) ?: true)

val ClassNode.effectiveAccess: Int get() = innerClassNode?.access ?: access
val ClassNode.outerClassName: String? get() = innerClassNode?.outerName

val ClassNode.innerClassNode: InnerClassNode? get() = innerClasses.singleOrNull { it.name == name }
fun ClassNode.isLocal() = innerClassNode?.run { innerName == null && outerName == null} ?: false
fun ClassNode.isInner() = innerClassNode != null
fun ClassNode.isWhenMappings() = isSynthetic(access) && name.endsWith("\$WhenMappings")

const val publishedApiAnnotationName = "kotlin/PublishedApi"
fun ClassNode.isPublishedApi() = findAnnotation(publishedApiAnnotationName, includeInvisible = true) != null
fun MethodNode.isPublishedApi() = findAnnotation(publishedApiAnnotationName, includeInvisible = true) != null
fun FieldNode.isPublishedApi() = findAnnotation(publishedApiAnnotationName, includeInvisible = true) != null

private fun findAnnotation(annotationName: String, visibleAnnotations: List<AnnotationNode>?, invisibleAnnotations: List<AnnotationNode>?, includeInvisible: Boolean): AnnotationNode? =
    visibleAnnotations?.firstOrNull { it.refersToName(annotationName) }
        ?: if (includeInvisible) invisibleAnnotations?.firstOrNull { it.refersToName(annotationName) } else null

fun AnnotationNode.refersToName(name: String) = desc.startsWith('L') && desc.endsWith(';') && desc.regionMatches(1, name, 0, name.length)

fun ClassNode.isDefaultImpls(metadata: KotlinClassMetadata?) = isInner() && name.endsWith("\$DefaultImpls") && metadata.isSyntheticClass()

private val MemberBinarySignature.kind: Int
    get() = when (this) {
        is FieldBinarySignature -> 1
        is MethodBinarySignature -> 2
        else -> error("Unsupported $this")
    }

val MEMBER_SORT_ORDER = compareBy<MemberBinarySignature>(
    { it.kind },
    { it.name },
    { it.desc }
)

operator fun AnnotationNode.get(key: String): Any? = values.annotationValue(key)

private fun List<Any>.annotationValue(key: String): Any? {
    for (index in (0 until size / 2)) {
        if (this[index * 2] == key)
            return this[index * 2 + 1]
    }
    return null
}

/**
 * 这将只收集类名。
 */
class ClassNameCollector : ClassVisitor(ASM7) {

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
open class ClassAnalyzer : ClassVisitor(ASM7) {

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
//        logger.log("ClassAnalyzer#visit: $name extends $superName {")
        addClass("L$superName;")
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
//        logger.log("ClassAnalyzer#visitField: $descriptor $name")
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
//        logger.log("ClassAnalyzer#visitMethod: $name $descriptor")

        descriptor?.let {
            METHOD_DESCRIPTOR_REGEX.findAll(it).forEach {
                addClass(it.value)
            }
        }

        return methodAnalyzer
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
//        logger.log("ClassAnalyzer#visitAnnotation: descriptor=$descriptor visible=$visible")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor? {
//        logger.log("ClassAnalyzer#visitTypeAnnotation: typeRef=$typeRef typePath=$typePath descriptor=$descriptor visible=$visible")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitEnd() {
//        logger.log("}")
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
//        logger.log("- MethodAnalyzer#visitTypeInsn: $type")
        addClass("L$type;")
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
//        logger.log("- MethodAnalyzer#visitFieldInsn: $owner.$name $descriptor")
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
//        logger.log("- MethodAnalyzer#visitMethodInsn: $owner.$name $descriptor")
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
//        logger.log("- MethodAnalyzer#visitInvokeDynamicInsn: $name $descriptor")
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
//        logger.log("- MethodAnalyzer#visitLocalVariable: $name $descriptor")
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
//        logger.log("- MethodAnalyzer#visitLocalVariableAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
//        logger.log("- MethodAnalyzer#visitAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitInsnAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
//        logger.log("- MethodAnalyzer#visitInsnAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitParameterAnnotation(parameter: Int, descriptor: String?, visible: Boolean): AnnotationVisitor? {
//        logger.log("- MethodAnalyzer#visitParameterAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
//        logger.log("- MethodAnalyzer#visitTryCatchBlock: $type")
        addClass("L$type;")
    }

    override fun visitTryCatchAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
//        logger.log("- MethodAnalyzer#visitTryCatchAnnotation: $descriptor")
        addClass(descriptor)
        return annotationAnalyzer
    }
}

private class AnnotationAnalyzer(
    private val classes: MutableSet<String>,
    private val level: Int = 0
) : AnnotationVisitor(ASM7) {

    private fun addClass(className: String?) {
        classes.addClass(className)
    }

    private fun indent() = "  ".repeat(level)

    override fun visit(name: String?, value: Any?) {
        fun getValue(value: Any?): String {
            return if (value is String && value.contains("\n")) {
                ""
            } else {
                value.toString()
            }
        }

//        logger.log("${indent()}- AnnotationAnalyzer#visit: name=$name, value=(${value?.javaClass?.simpleName}, ${getValue(value)})")
        if (value is String) {
            addClass(value)
        }
    }

    override fun visitEnum(name: String?, descriptor: String?, value: String?) {
//        logger.log("${indent()}- AnnotationAnalyzer#visitEnum: name=$name, descriptor=$descriptor, value=$value")
        addClass(descriptor)
    }

    override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor? {
//        logger.log("${indent()}- AnnotationAnalyzer#visitAnnotation: name=$name, descriptor=$descriptor")
        addClass(descriptor)
        return AnnotationAnalyzer(classes, level + 1)
    }

    override fun visitArray(name: String?): AnnotationVisitor? {
//        logger.log("${indent()}- AnnotationAnalyzer#visitArray: name=$name")
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

private const val KOTLIN_METADATA = "Lkotlin/Metadata;"

internal class KotlinClassHeaderBuilder {

    var kind: Int = 1
    var metadataVersion: IntArray? = null
    var bytecodeVersion: IntArray? = null
    var data1 = mutableListOf<String>()
    var data2 = mutableListOf<String>()
    var extraString: String? = null
    var packageName: String? = null
    var extraInt: Int = 0

    fun build(): KotlinClassHeader {
        return KotlinClassHeader(
            kind = kind,
            metadataVersion = metadataVersion,
            data1 = data1.toTypedArray(),
            data2 = data2.toTypedArray(),
            extraString = extraString,
            packageName = packageName,
            extraInt = extraInt
        )
    }
}

internal class KotlinMetadataVisitor(
) : ClassVisitor(ASM7) {

    internal lateinit var className: String
    internal var builder: KotlinClassHeaderBuilder? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return if (KOTLIN_METADATA == descriptor) {
            builder = KotlinClassHeaderBuilder()
            KotlinAnnotationVisitor(builder!!)
        } else {
            null
        }
    }

    private class KotlinAnnotationVisitor(
        private val builder: KotlinClassHeaderBuilder,
        private val level: Int = 0,
        private val arrayName: String? = null
    ) : AnnotationVisitor(ASM7) {

        private fun indent() = "  ".repeat(level)

        override fun visit(name: String?, value: Any?) {
            when (name) {
                "k" -> builder.kind = value as Int
                "mv" -> builder.metadataVersion = value as IntArray
                "bv" -> builder.bytecodeVersion = value as IntArray
                "xs" -> builder.extraString = value as String
                "pn" -> builder.packageName = value as String
                "xi" -> builder.extraInt = value as Int
            }

            when (arrayName) {
                "d1" -> builder.data1.add(value as String)
                "d2" -> builder.data2.add(value as String)
            }
        }

        override fun visitArray(name: String?): com.autonomousapps.internal.asm.AnnotationVisitor? {
            return KotlinAnnotationVisitor(builder, level + 1, name)
        }
    }
}

internal class ConstantVisitor: ClassVisitor(ASM7) {

    internal lateinit var className: String private set
    internal val classes = mutableSetOf<String>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
    }

    override fun visitField(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? {
        if (isStaticFinal(access)) {
            classes.add(name)
        }

        return null
    }

    private fun isStaticFinal(access: Int): Boolean =
        access and Opcodes.ACC_STATIC != 0 && access and Opcodes.ACC_FINAL != 0
}