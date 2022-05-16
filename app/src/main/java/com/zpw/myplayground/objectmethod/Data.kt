package com.zpw.myplayground.objectmethod

import android.os.SystemClock
import okhttp3.internal.notifyAll
import okhttp3.internal.wait
import java.util.*

open class Data: Cloneable {
    var data: String? = "test"
    var oldData: Data? = Data()

    override fun clone(): Any {
        val dataClone: Data = super.clone() as Data
        val oldDataClone: Data = oldData?.clone() as Data
        dataClone.oldData = oldDataClone
        return dataClone
    }
    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + data.hashCode()
        return result
    }

    /**
     * 反身性
     * 对称性
     * 传递性
     * 一致性
     * 空相等否定
     * 覆盖 equals() 时总是覆盖 hashCode()
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Data) return false
        return data === other.data
    }
    override fun toString(): String {
        return "${javaClass.name} data is ${data}"
    }
}

/**
 * 错误事例，不满足传递性
 */
class SpecialDadaWrong: Data() {
    var time = "test"
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Data && other !is SpecialDadaWrong) return super.equals(other)
        if (other is SpecialDadaWrong) return super.equals(other) && time == other.time
        return false
    }
}

class SpecialDadaRight {
    private var data: Data = Data()
    var time = "test"

    override fun equals(other: Any?): Boolean {
        if (other === time) return true
        if (other !is SpecialDadaRight) return false
        return data == other && time === other.time
    }
}

class NumberQueue {
    /**
     * 语序重拍，读时未同步，写时被修改。
     * 实现原理是基于读写栏栅处理cpu读写顺序，同时处理编译时候严格按照编码顺序处理。本质上是通过c++的atomic实现的。
     */
    @Volatile
    var numQueue = LinkedList<Int>()

    /**
     * 翻译成字节码后，本质上在虚拟机中对应两个字节码 MONITOR_ENTER 以及 MONITOR_EXIT 。
     * 通过Monitor进行临界区保护。当执行了 MONITOR_ENTER 后，本质上就会取出Object 中的LockWord 中的记录锁状态信息。
     *
     * 在Android 虚拟机中，存在3种锁。通过记录thread_id的偏向锁，瘦锁，胖锁。
     * 瘦锁是一种乐观锁，本质上就是不断的自旋让渡cpu资源。当64次之后获取不到，则转化为胖锁。
     * 胖锁的实现是基于futex进行同步处理的。
     */
    @Synchronized
    fun pushNumber(num: Int) {
        numQueue.addLast(num)
        notifyAll()
    }

    @Synchronized
    fun pullNumber(): Int {
        while (numQueue.size == 0) {
            wait()
        }
        return numQueue.removeFirst()
    }

    @Synchronized
    fun size(): Int {
        return numQueue.size
    }
}

class NumberProducer(val maxNumsInQueue: Int, val numsQueue: NumberQueue): Thread() {
    override fun run() {
        val rand = Random()
        while (true) {
            if (numsQueue.size() < maxNumsInQueue) {
                val evenNums = rand.nextInt(99) + 1
                numsQueue.pushNumber(evenNums)
            }
            SystemClock.sleep(800)
        }
    }
}

class NumberConsumer(val numsQueue: NumberQueue): Thread() {
    override fun run() {
        val rand = Random()
        while (true) {
            val pullNumber = numsQueue.pullNumber()
        }
    }
}