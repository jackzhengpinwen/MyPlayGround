package com.zpw.myplayground.design.decorator

class Boy: Person() {
    override fun dressed() {
        println("穿了内衣内裤")
    }
}