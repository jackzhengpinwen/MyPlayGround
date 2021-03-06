package com.zpw.myplayground.dependency.internal.kotlin

import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.jvm.JvmMemberSignature

class ClassVisibility(
    val name: String,
    val flags: Flags?,
    val members: Map<JvmMemberSignature, MemberVisibility>,
    val facadeClassName: String? = null
) {
    val visibility get() = flags
    val isCompanion: Boolean get() = flags != null && Flag.Class.IS_COMPANION_OBJECT(flags)

    var companionVisibilities: ClassVisibility? = null
    val partVisibilities = mutableListOf<ClassVisibility>()
}

fun ClassVisibility.findMember(signature: JvmMemberSignature): MemberVisibility? =
    members[signature] ?: partVisibilities.mapNotNull { it.members[signature] }.firstOrNull()

data class MemberVisibility(val member: JvmMemberSignature, val visibility: Flags?, val isReified: Boolean)

private fun isPublic(visibility: Flags?, isPublishedApi: Boolean) =
    visibility == null
            || Flag.IS_PUBLIC(visibility)
            || Flag.IS_PROTECTED(visibility)
            || (isPublishedApi && Flag.IS_INTERNAL(visibility))

fun ClassVisibility.isPublic(isPublishedApi: Boolean) = isPublic(visibility, isPublishedApi)
fun MemberVisibility.isPublic(isPublishedApi: Boolean) =
    // Assuming isReified implies inline
    !isReified && isPublic(visibility, isPublishedApi)