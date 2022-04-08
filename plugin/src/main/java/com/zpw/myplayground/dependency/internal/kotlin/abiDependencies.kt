package com.zpw.myplayground.dependency.internal.kotlin

import com.zpw.myplayground.dependency.internal.Component
import com.zpw.myplayground.dependency.internal.DESC_REGEX
import com.zpw.myplayground.dependency.internal.Dependency
import com.zpw.myplayground.dependency.internal.allItems
import com.zpw.myplayground.logger
import java.io.File
import java.util.jar.JarFile

/**
 * 给定一个 jar 及其依赖项列表（作为 [Component]s），返回代表此 jar 的 ABI（或公共 API）的一组 [Dependency]s。
 */
fun abiDependencies(jarFile: File, jarDependencies: List<Component>, abiDumpFile: File? = null): Set<Dependency> {
    val apis = getBinaryAPI(JarFile(jarFile))
    /**
     * BuildConfig.java
     * ClassBinarySignature(name=com/zpw/library3/BuildConfig, superName=java/lang/Object,
     * outerName=null, supertypes=[],
     * memberSignatures=[
     * FieldBinarySignature(jvmMember=DEBUG:Z, isPublishedApi=false, access=AccessFlags(access=25)),
     * FieldBinarySignature(jvmMember=LIBRARY_PACKAGE_NAME:Ljava/lang/String;, isPublishedApi=false, access=AccessFlags(access=25)),
     * FieldBinarySignature(jvmMember=BUILD_TYPE:Ljava/lang/String;, isPublishedApi=false, access=AccessFlags(access=25)),
     * MethodBinarySignature(jvmMember=<init>()V, isPublishedApi=false, access=AccessFlags(access=1))
     * ],
     * access=AccessFlags(access=49), isEffectivelyPublic=true, isNotUsedWhenEmpty=false)
     */
//    apis.forEach {
//        logger.log("classBinarySignature is ${it}\n\n\n")
//    }
    return apis.filterOutNonPublic()
        .also { publicApi ->
            abiDumpFile?.let { file ->
                file.bufferedWriter().use { writer -> publicApi.dump(writer) }
            }
        }
        .flatMap { classSignature ->
            val superTypes = classSignature.supertypes
            val memberTypes = classSignature.memberSignatures.map {
                // descriptor, e.g. `(JLjava/lang/String;JI)Lio/reactivex/Single;`
                // This one takes a long, a String, a long, and an int, and returns a Single
                it.desc
            }.flatMap {
                DESC_REGEX.findAll(it).allItems()
            }
            superTypes + memberTypes
        }.map {
            it.replace("/", ".")
        }.mapNotNull { fqcn ->
            jarDependencies.find { component ->
                component.classes.contains(fqcn)
            }?.dependency
        }.toSortedSet()
}
