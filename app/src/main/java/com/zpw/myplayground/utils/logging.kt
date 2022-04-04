package com.zpw.myplayground.utils

import android.util.Log

val TAG_FWK = "zpw$$"

private const val LOGGING = "logging"
private const val LOG_LEVEL_DEBUG = "debug"
private const val LOG_LEVEL_WARN = "warn"
private const val LOG_LEVEL_QUIET = "quiet"

object Logger

internal fun Logger.log(tag: String, msg: String) {
  when (System.getProperty(LOGGING, LOG_LEVEL_DEBUG)) {
    LOG_LEVEL_DEBUG -> println("$tag $msg")
    LOG_LEVEL_WARN -> println("$tag$ msg")
    LOG_LEVEL_QUIET -> println("$tag $msg")
  }
}