package com.zpw.myplayground.transform.internal.test

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ParameterUtils {
    private val fm: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun printValueOnStack(value: Boolean) {
        println("    $value")
    }

    fun printValueOnStack(value: Byte) {
        println("    $value")
    }

    fun printValueOnStack(value: Char) {
        println("    $value")
    }

    fun printValueOnStack(value: Short) {
        println("    $value")
    }

    fun printValueOnStack(value: Int) {
        println("    $value")
    }

    fun printValueOnStack(value: Float) {
        println("    $value")
    }

    fun printValueOnStack(value: Long) {
        println("    $value")
    }

    fun printValueOnStack(value: Double) {
        println("    $value")
    }

    fun printValueOnStack(value: Any?) {
        if (value == null) {
            println("    $value")
        } else if (value is String) {
            println("    $value")
        } else if (value is Date) {
            System.out.println("    " + fm.format(value))
        } else if (value is CharArray) {
            System.out.println("    " + Arrays.toString(value as CharArray?))
        } else {
            println("    " + value.javaClass + ": " + value.toString())
        }
    }

    fun printText(str: String?) {
        println(str)
    }
}