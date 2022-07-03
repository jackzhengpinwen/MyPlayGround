package com.zpw.myplayground.transform.internal.test

import org.objectweb.asm.FieldVisitor

class InfoFieldVisitor(api: Int, visitField: FieldVisitor): FieldVisitor(api, visitField) {
    override fun visitEnd() {
//        println("FieldVisitor.visitEnd()")
        super.visitEnd()
    }
}