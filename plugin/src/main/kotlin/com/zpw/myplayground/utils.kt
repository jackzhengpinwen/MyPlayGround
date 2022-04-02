@file:Suppress("unused")

package com.zpw.myplayground

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure
import java.io.File

fun Settings.myGitRepositories(repos: MyGitIncludeExtension.() -> Unit) = configure(repos)