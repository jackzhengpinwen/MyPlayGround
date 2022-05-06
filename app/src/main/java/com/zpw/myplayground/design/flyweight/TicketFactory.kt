package com.zpw.myplayground.design.flyweight

import java.util.concurrent.ConcurrentHashMap

object TicketFactory {
    val sTicketMap = ConcurrentHashMap<String, Ticket>()

    fun getTicket(from: String, to: String): Ticket {
        val key = "${from}-${to}"
        if (sTicketMap.containsKey(key)) {
            println("使用缓存 ---- ${key}")
            return sTicketMap.get(key)!!
        } else {
            println("创建对象 ---- ${key}")
            val ticket = TrainTicket(from, to)
            sTicketMap.put(key, ticket)
            return ticket
        }
    }
}