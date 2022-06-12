package com.zpw.compiler

enum class Language(
    val displayName: String,
    val extension: String = displayName.lowercase()
) {
    JAVA("Java"),
    KOTLIN("Kotlin", "kt"),
    GROOVY("Groovy")
}