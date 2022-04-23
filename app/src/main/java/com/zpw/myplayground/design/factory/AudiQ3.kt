package com.zpw.myplayground.design.factory

class AudiQ3: AudiCar() {
    override fun drive() {
        println("${javaClass.simpleName} drive")
    }

    override fun selfNavigation() {
        println("${javaClass.simpleName} selfNavigation")
    }
}