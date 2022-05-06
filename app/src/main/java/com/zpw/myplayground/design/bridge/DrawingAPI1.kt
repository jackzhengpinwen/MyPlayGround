package com.zpw.myplayground.design.bridge

class DrawingAPI1: DrawingAPI {
    override fun drawCircle(x: Double, y: Double, radius: Double) {
        println("API1.circle at $x:$y radius $radius")
    }
}