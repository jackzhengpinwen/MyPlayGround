package com.zpw.myplayground.design.prototype

class WordDocument: Cloneable {
    var text: String = ""
    var images: ArrayList<String> = ArrayList()

    init {
        println(" ---------- WordDocument构造函数 ---------- ")
    }

    override fun clone(): WordDocument {
        try {
            val doc = super.clone() as WordDocument
            doc.text = text
            doc.images = images.clone() as ArrayList<String>
            return doc
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return WordDocument()
    }

    override fun toString(): String {
        return "WordDocument(text='$text', images=$images)"
    }

    fun print() {
        println(" ---------- WordDocument print start ---------- ")
        println(toString())
        println(" ---------- WordDocument print end ---------- ")
    }
}