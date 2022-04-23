package com.zpw.myplayground.design.builder

class CarBuilderImpl: CarBuilder {
    private var car: Car = Car()

    override fun build(): Car = car

    override fun setColor(color: String): CarBuilder {
        car.color = color
        return this
    }

    override fun setWheels(wheels: Int): CarBuilder {
        car.wheels = wheels;
        return this
    }
}