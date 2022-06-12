package com.zpw.compiler.mustache

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheFactory
import com.zpw.compiler.model.Model
import com.zpw.compiler.template.TemplateEngine
import java.io.Writer

class MustacheEngine: TemplateEngine {
    override val name: String = "mustache"
    override val extension: String = name

    private val mustacheFactory: MustacheFactory by lazy {
        object : DefaultMustacheFactory() {
            override fun encode(value: String, writer: Writer) {
                writer.write(value)
            }
        }
    }

    override fun render(template: String, model: Model, writer: Writer): Writer {
        return mustacheFactory.compile(template).execute(writer, model)
    }
}