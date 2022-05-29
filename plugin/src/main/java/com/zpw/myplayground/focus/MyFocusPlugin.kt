package com.zpw.myplayground.focus

import com.zpw.myplayground.logger
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.apply

internal const val FOCUS_TASK_GROUP = "focus mode"
internal const val CREATE_FOCUS_SETTINGS_TASK_NAME = "createFocusSettings"
internal const val FOCUS_TASK_NAME = "focus"
internal const val CLEAR_FOCUS_TASK_NAME = "clearFocus"
internal val TASK_NAMES = setOf(
    CREATE_FOCUS_SETTINGS_TASK_NAME,
    FOCUS_TASK_NAME,
    CLEAR_FOCUS_TASK_NAME,
)

class MyFocusPlugin: Plugin<Settings> {

    override fun apply(target: Settings) = target.run {
        logger.log("MyFocusPlugin apply")
        val extension = extensions.create<MyFocusExtension>("myFocus")
        gradle.settingsEvaluated {
            val requestingFocusTask = startParameter.taskNames
                .map { it.substringAfter(":") }
                .any { it in TASK_NAMES }

            val focusFile = rootDir.resolve(extension.focusFileName.get())
            if (!requestingFocusTask && focusFile.exists()) {
                apply(from = rootDir.resolve(focusFile.readText()))
            } else {
                apply(from = extension.allSettingsFileName)
            }

        }
    }
}