package com.zpw.myplayground.design.singleton

/**
 * 懒汉式 doubleCheck
 */
class DoubleCheckSin private constructor(){
    companion object {
        val doubleCheckSin: DoubleCheckSin by lazy {
            DoubleCheckSin()
        }
    }

    fun doSomething() {

    }
}