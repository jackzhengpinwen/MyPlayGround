@file:Suppress("unused")

package com.zpw.myplayground

import com.zpw.myplayground.gitinclude.MyGitIncludeExtension
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure

fun Settings.myGitRepositories(repos: MyGitIncludeExtension.() -> Unit) = configure(repos)