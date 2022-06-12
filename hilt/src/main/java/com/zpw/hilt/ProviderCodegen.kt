package com.zpw.hilt

import com.google.auto.service.AutoService
import com.zpw.annotation.Provided
import com.zpw.compiler.*
import com.zpw.compiler.model.Model
import com.zpw.compiler.mustache.MustacheEngine
import com.zpw.compiler.template.TemplateEngine
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

@AutoService(Processor::class)
class ProviderCodegen: CodegenProcessor<Provided>() {
    private val mustache: TemplateEngine by lazy(::MustacheEngine)

    private val interfaces: MutableSet<TypeModel> by lazy(::mutableSetOf)

    override val engine: TemplateEngine = mustache

    override fun onProcessing(
        processingEnv: ProcessingEnvironment,
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ) {
        println("ProviderCodegen onProcessing")
        roundEnv.onEachAnnotatedElement { element ->
            // element is FeatureImpl
            val implementation = element.asTypeElement()
            // implementation is io.johnsonlee.template.app.FeatureImpl
            val mirror = element.getAnnotationMirror<Provided>()
            // mirror is [io.johnsonlee.template.app.Feature],
            // {value()=io.johnsonlee.template.app.Feature.class},
            // io.johnsonlee.codegen.annotation.Provided
            mirror.value.takeIf(Set<DeclaredType>::isNotEmpty)?.map(DeclaredType::asTypeElement)?.onEach { api ->
                // api is io.johnsonlee.template.app.Feature
                if (checkImplementation(implementation, api)) {
                    // typeModel is Feature, io.johnsonlee.template.app.Feature
                    interfaces += TypeModel(api.qualifiedName.toString())
                    // generateProvider is
                    // io.johnsonlee.template.app.Feature,
                    // io.johnsonlee.template.app.FeatureImpl,
                    // [io.johnsonlee.template.app.Feature],
                    // {value()=io.johnsonlee.template.app.Feature.class},
                    // io.johnsonlee.codegen.annotation.Provided
                    generateProvider(api, implementation, mirror)
                } else {
                    processingEnv.error(
                        "${implementation.qualifiedName} does not implement ${api.qualifiedName}",
                        element,
                        mirror
                    )
                }
            } ?: processingEnv.error("No interface provided for element!", element, mirror)
        }
    }

    override fun onPostProcessing(processingEnv: ProcessingEnvironment) {
        generate(
            "template/Dsl",
            DslModel(interfaces.map {
                DslItemModel("${PKG}.${it.simpleName}_Provider", it.qualifiedName, it.simpleName)
            }.toSet()),
            Language.KOTLIN
        )
    }

    private fun generateProvider(api: TypeElement, implementation: TypeElement, mirror: AnnotationMirror) {
        generate(
            "template/Provider",
            ProviderModel(TypeModel(api.qualifiedName.toString()), TypeModel(implementation.qualifiedName.toString())),
            Language.KOTLIN
        )
    }

    private fun checkImplementation(implementation: TypeElement, api: TypeElement): Boolean {
        val verify: String? by processingEnv.options.withDefault { null }
        if (verify == null || !java.lang.Boolean.parseBoolean(verify)) {
            return true
        }
        return implementation.isSubtypeOf(api)
    }
}

open class TypeModel(
    val qualifiedName: String,
    val simpleName: String = qualifiedName.substringAfterLast('.')
)

class ProviderModel(
    val api: TypeModel,
    val implementation: TypeModel
) : Model {
    override val packageName: String = PKG
    override val qualifiedName: String = "${packageName}.${api.simpleName}_Provider"
    override val simpleName: String = qualifiedName.substringAfterLast('.')
    override val references: Set<String> = emptySet()
}

class DslItemModel(
    val providerName: String,
    qualifiedName: String,
    simpleName: String
) : TypeModel(qualifiedName, simpleName)

class DslModel(
    val interfaces: Set<DslItemModel>
): Model {
    override val packageName: String = PKG
    override val simpleName: String = "Dsl"
    override val qualifiedName: String = "${packageName}.${simpleName}"
    override val references: Set<String> = emptySet()
}