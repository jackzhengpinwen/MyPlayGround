package com.zpw.myplayground.design.proxy

class Alice: ILawsuit {
    override fun submit() = println("老板拖欠工资，特此申请仲裁")

    override fun burden() = println("这是合同和过去一年的银行工资流水")

    override fun defend() = println("证据确凿，不需要再说什么了")

    override fun finish() = println("诉讼成功")
}