package com.zpw.myplayground.once

fun onceTest() {
    Gretting().hello("kotlin")
    Gretting().hello("java")
}

class Gretting {

    val once = Once<Unit>()

    fun hello(name: String): Unit = once {
        println("Hello, ${name}")
    }

}