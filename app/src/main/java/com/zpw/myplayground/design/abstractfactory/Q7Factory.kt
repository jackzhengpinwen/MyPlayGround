package com.zpw.myplayground.design.abstractfactory

class Q7Factory: CarFactory() {
    override fun createTire(): ITire = SUVTire()

    override fun createEngine(): IEngine = ImportEngine()

    override fun createBrake(): IBrake = SeniorBrake()
}