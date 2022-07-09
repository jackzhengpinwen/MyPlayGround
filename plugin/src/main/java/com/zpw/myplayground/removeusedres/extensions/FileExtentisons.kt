package com.zpw.myplayground.removeusedres.extensions

import java.io.File

internal fun File.containsInDescendants(other: File, includeSelf: Boolean = true): Boolean {
    if (!isAbsolute) {
        error("can not compare if base file path is relative path: $this")
    }
    if (!other.isAbsolute) {
        throw IllegalArgumentException("can not compare if other file path is relative path: $other")
    }
    val base = normalize()
    var target = other.normalize()
    if (!includeSelf) {
        target = target.parentFile
    }
    while (target != null) {
        if (base == target) {
            return true
        }
        target = target.parentFile
    }
    return false
}