package com.zpw.myplayground.design.abstractfactory

class Q3Factory: CarFactory() {
    override fun createTire(): ITire = NormalTire()

    override fun createEngine(): IEngine = DomesticEngine()

    override fun createBrake(): IBrake = NormalBrake()
}