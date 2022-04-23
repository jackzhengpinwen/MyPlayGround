package com.zpw.myplayground.design.abstractfactory

abstract class CarFactory {
    abstract fun createTire(): ITire
    abstract fun createEngine(): IEngine
    abstract fun createBrake(): IBrake
}