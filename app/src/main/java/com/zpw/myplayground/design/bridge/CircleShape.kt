package com.zpw.myplayground.design.bridge

class CircleShape(private var x: Double,
                  private var y: Double,
                  private var radius: Double,
                  drawingAPI: DrawingAPI): Shape(drawingAPI) {
    override fun draw() {
        drawingAPI.drawCircle(x, y, radius)
    }

    override fun resizeByPercentage(pct: Double) {
        radius *= (1.0 + pct / 100.0)
    }
}