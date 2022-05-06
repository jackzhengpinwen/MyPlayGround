package com.zpw.myplayground.design.decorator

class ExpensiveCloth(person: Person): PersonCloth(person) {
    private fun dressShirt() = println("穿件短袖")

    private fun dressLeather() = println("穿件皮衣")

    private fun dressJean() = println("穿件牛仔衣")

    override fun dressed() {
        super.dressed()
        dressShirt()
        dressLeather()
        dressJean()
    }
}