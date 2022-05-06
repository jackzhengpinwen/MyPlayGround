package com.zpw.myplayground.design.facade

class MobilePhone {
    private val phone = PhoneImpl()
    private val camera = SamsungCamera()

    fun dial() {
        phone.dial()
    }

    fun videoChat() {
        println("video chatting ----- ")
        camera.open()
        phone.dial()
    }

    fun hangup() {
        phone.hangup()
    }

    fun takePicture() {
        camera.open()
        camera.takePicture()
    }

    fun closeCamera() {
        camera.close()
    }
}