package com.zpw.myplayground

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

object logger {
  val openLog = true
  fun log(msg: String, forceOpenLog: Boolean? = false) {
    if (openLog || forceOpenLog!!) {
      println("zpw$$ $msg")
    }
  }
}

private const val LOGGING = "logging"
private const val LOG_LEVEL_DEBUG = "debug"
private const val LOG_LEVEL_WARN = "warn"
private const val LOG_LEVEL_QUIET = "quiet"

internal fun Logger.log(msg: String) {
  when (System.getProperty(LOGGING, LOG_LEVEL_DEBUG)) {
    LOG_LEVEL_DEBUG -> println("zpw$$ $msg")
    LOG_LEVEL_WARN -> println("zpw$$ $msg")
    LOG_LEVEL_QUIET -> println("zpw$$ $msg")
  }
}

internal inline fun <reified T> getLogger(): Logger = Logging.getLogger(T::class.java)