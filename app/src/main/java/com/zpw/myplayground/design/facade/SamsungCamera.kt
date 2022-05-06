package com.zpw.myplayground.design.facade

class SamsungCamera: Camera {
    override fun open() {
        println("open camera")
    }

    override fun takePicture() {
        println("takePicture")
    }

    override fun close() {
        println("close camera")
    }
}