package com.zpw.myplayground.design.bridge

abstract class Shape(protected var drawingAPI: DrawingAPI) {
    abstract fun draw()

    abstract fun resizeByPercentage(pct: Double)
}