package com.zpw.myplayground.design.abstractfactory

class NormalBrake: IBrake {
    override fun brake() {
        println("normal brake")
    }
}