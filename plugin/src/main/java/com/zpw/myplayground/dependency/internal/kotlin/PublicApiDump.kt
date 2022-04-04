package com.zpw.myplayground.dependency.internal.kotlin

import com.autonomousapps.internal.asm.ClassReader
import com.autonomousapps.internal.asm.Opcodes
import com.autonomousapps.internal.asm.tree.ClassNode
import com.zpw.myplayground.dependency.internal.*
import kotlinx.metadata.jvm.*
import java.io.InputStream
import java.util.jar.JarFile

fun getBinaryAPI(jar: JarFile, visibilityFilter: (String) -> Boolean = { true }): List<ClassBinarySignature> =
    getBinaryAPI(jar.classEntries().map { entry -> jar.getInputStream(entry) }, visibilityFilter)

fun getBinaryAPI(classStreams: Sequence<InputStream>, visibilityFilter: (String) -> Boolean = { true }): List<ClassBinarySignature> {
    val classNodes = classStreams.map { it.use { stream ->
        val classNode = ClassNode()
        ClassReader(stream).accept(classNode, ClassReader.SKIP_CODE)
        classNode
    }}

    val visibilityMapNew = classNodes.readKotlinVisibilities().filterKeys(visibilityFilter)

    return classNodes
        .map { with(it) {
            val metadata = kotlinMetadata
            val mVisibility = visibilityMapNew[name]
            val classAccess = AccessFlags(effectiveAccess and Opcodes.ACC_STATIC.inv())

            val supertypes = listOf(superName) - "java/lang/Object" + interfaces.sorted()

            val memberSignatures = (
                    fields.map { with(it) { FieldBinarySignature(JvmFieldSignature(name, desc), isPublishedApi(), AccessFlags(access)) } } +
                            methods.map { with(it) { MethodBinarySignature(JvmMethodSignature(name, desc), isPublishedApi(), AccessFlags(access)) } }
                    ).filter {
                    it.isEffectivelyPublic(classAccess, mVisibility)
                }

            ClassBinarySignature(name, superName, outerClassName, supertypes, memberSignatures, classAccess,
                isEffectivelyPublic(mVisibility), metadata.isFileOrMultipartFacade() || isDefaultImpls(metadata)
            )
        }}
        .asIterable()
        .sortedBy { it.name }
}

fun List<ClassBinarySignature>.filterOutNonPublic(nonPublicPackages: List<String> = emptyList()): List<ClassBinarySignature> {
    val nonPublicPaths = nonPublicPackages.map { it.replace('.', '/') + '/' }
    val classByName = associateBy { it.name }

    fun ClassBinarySignature.isInNonPublicPackage() =
        nonPublicPaths.any { name.startsWith(it) }

    fun ClassBinarySignature.isPublicAndAccessible(): Boolean =
        isEffectivelyPublic &&
                (outerName == null || classByName[outerName]?.let { outerClass ->
                    !(this.access.isProtected && outerClass.access.isFinal)
                            && outerClass.isPublicAndAccessible()
                } ?: true)

    fun supertypes(superName: String) = generateSequence({ classByName[superName] }, { classByName[it.superName] })

    fun ClassBinarySignature.flattenNonPublicBases(): ClassBinarySignature {

        val nonPublicSupertypes = supertypes(superName).takeWhile { !it.isPublicAndAccessible() }.toList()
        if (nonPublicSupertypes.isEmpty())
            return this

        val inheritedStaticSignatures = nonPublicSupertypes.flatMap { it.memberSignatures.filter { it.access.isStatic }}

        // not covered the case when there is public superclass after chain of private superclasses
        return this.copy(memberSignatures = memberSignatures + inheritedStaticSignatures, supertypes = supertypes - superName)
    }

    return filter { !it.isInNonPublicPackage() && it.isPublicAndAccessible() }
        .map { it.flattenNonPublicBases() }
        .filterNot { it.isNotUsedWhenEmpty && it.memberSignatures.isEmpty()}
}

fun JarFile.classEntries() = Sequence { entries().iterator() }.filter {
    !it.isDirectory && it.name.endsWith(".class") && !it.name.startsWith("META-INF/")
}

fun List<ClassBinarySignature>.dump() = dump(to = System.out)

fun <T : Appendable> List<ClassBinarySignature>.dump(to: T): T = to.apply { this@dump.forEach {
    append(it.signature).appendln(" {")
    it.memberSignatures.sortedWith(MEMBER_SORT_ORDER).forEach { append("\t").appendln(it.signature) }
    appendln("}\n")
}}