package com.zpw.myplayground.design.adapter

class VoltAdapter(val volt220: Volt220): Volt5 {
    override fun getVolt5(): Int = volt220.getVolt220() / 44
}