package com.zpw.myplayground.once

import kotlin.reflect.KProperty

private object UNDEFINED

class Once<T : Any> {
    // 保存执行结果
    @Volatile
    private var _value: Any? = UNDEFINED

    @Suppress("UNCHECKED_CAST")
    // 获取执行结果
    val value: T?
        get() = _value as? T

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(
        // 闭包，只执行一次的操作
        action: () -> T
    ): T {
        // 获取执行结果
        val v1 = _value
        // 如果执行结果等于初始值，就代表已经执行过，返回执行结果
        if (v1 !== UNDEFINED) {
            return v1 as T
        }
        // 同步加锁，避免多线程竞争
        return synchronized(this) {
            // 再次获取执行结果
            val v2 = _value
            if (v2 !== UNDEFINED) {// 如果执行结果等于初始值，就代表已经执行过，返回执行结果
                v2
            } else {
                val v3 = action()// 如果执行结果不等于初始值，就代表没执行过，执行传入的闭包
                _value = v3// 保存执行结果
                v3// 返回执行结果
            }
        } as T // 强转为传入的数据类型
    }

}

operator fun <T : Any> Once<T>.getValue(thisRef: Any?, property: KProperty<*>): T? = value
