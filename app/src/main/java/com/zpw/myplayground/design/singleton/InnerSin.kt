package com.zpw.myplayground.design.singleton

/**
 * 静态内部类
 */
class InnerSin private constructor(){
    companion object {
        val instance = Holder.holder
    }

    private object Holder {
        val holder = InnerSin()
    }

    fun doSomething() {

    }
}