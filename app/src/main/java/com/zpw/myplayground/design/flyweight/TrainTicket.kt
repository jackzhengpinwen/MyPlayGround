package com.zpw.myplayground.design.flyweight

import java.util.*

class TrainTicket(val from: String, val to: String): Ticket {
    private var price: Int = 0

    override fun showTicketInfo(bunk: String) {
        price = Random().nextInt(300)

        println("购买从${from}到${to}的${bunk}火车票，价格为$price")
    }
}