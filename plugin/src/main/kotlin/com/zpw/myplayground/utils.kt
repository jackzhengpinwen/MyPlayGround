@file:Suppress("unused")

package com.zpw.myplayground

import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure

fun Settings.myGitRepositories(repos: MyGitIncludeExtension.() -> Unit) = configure(repos)