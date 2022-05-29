package com.zpw.myplayground.fastbuild

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

object ModuleArchiveLogger {
    var logger: Logger = Logging.getLogger(ModuleArchiveLogger::class.java)
    var enableLogging: Boolean = true

    fun logLifecycle(message: String) {
        if (enableLogging) {
            logger.log(LogLevel.LIFECYCLE,"ModuleArchive:"+ message)
        }
    }
}