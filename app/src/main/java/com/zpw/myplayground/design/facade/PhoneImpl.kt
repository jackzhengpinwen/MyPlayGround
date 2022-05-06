package com.zpw.myplayground.design.facade

class PhoneImpl: Phone {
    override fun dial() {
        println("dial")
    }

    override fun hangup() {
        println("hangup")
    }
}