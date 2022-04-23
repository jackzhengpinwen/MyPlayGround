package com.zpw.myplayground.design.proxy

class Lawyer(val lawsuit: ILawsuit): ILawsuit {
    override fun submit() = lawsuit.submit()

    override fun burden() = lawsuit.burden()

    override fun defend() = lawsuit.defend()

    override fun finish() = lawsuit.finish()
}