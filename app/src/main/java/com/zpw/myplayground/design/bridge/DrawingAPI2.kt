package com.zpw.myplayground.design.bridge

class DrawingAPI2: DrawingAPI {
    override fun drawCircle(x: Double, y: Double, radius: Double) {
        println("API2.circle at $x:$y radius $radius")
    }
}