package com.zpw.myplayground.design.builder

class CarBuilderDirector(private var builder: CarBuilder) {
    fun construct(): Car = with(builder) {
        setWheels(4)
        setColor("Red")
        build()
    }
}