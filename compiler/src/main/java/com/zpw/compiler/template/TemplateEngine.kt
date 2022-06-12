package com.zpw.compiler.template

import com.zpw.compiler.model.Model
import java.io.Writer

interface TemplateEngine {
    val name: String
    val extension: String
    fun render(template: String, model: Model, writer: Writer): Writer
}