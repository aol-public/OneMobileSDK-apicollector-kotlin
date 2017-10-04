package com.aol.mobile.sdk.annotations

import com.google.gson.GsonBuilder
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

@SupportedAnnotationTypes("com.aol.mobile.sdk.annotations.PublicApi")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions()
class PublicApiGrabber : AbstractProcessor() {
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val publicApiClasses: HashMap<String, TypeElement> = HashMap()

        annotations
                .flatMap { roundEnv.getElementsAnnotatedWith(it) }
                .filterIsInstance(TypeElement::class.java)
                .forEach { fillPublicClasses(publicApiClasses, it, it.getAnnotation(PublicApi::class.java).pkg) }

        val apiDescription = publicApiClasses.values
                .map { it.asTypeDescriptor() }
                .sortedBy { it.name }

        if (apiDescription.isEmpty()) return false

        val gson = GsonBuilder().create()

        File("public_api.json").bufferedWriter().use { out ->
            out.write(gson.toJson(apiDescription))
        }

        return true
    }

    private fun fillPublicClasses(apiClasses: HashMap<String, TypeElement>, element: TypeElement, pkg: String) {
        val qualifiedName = element.qualifiedName.toString()
        if (!qualifiedName.startsWith(pkg) || apiClasses.contains(qualifiedName)) return

        apiClasses[qualifiedName] = element

        val publicElements = element.enclosedElements.filter { it.modifiers.contains(Modifier.PUBLIC) }

        publicElements
                .filterIsInstance(ExecutableElement::class.java)
                .flatMap { it.parameters.map { it.asType() } + it.returnType }
                .mapNotNull { processingEnv.typeUtils.asElement(it) as? TypeElement }
                .forEach { fillPublicClasses(apiClasses, it, pkg) }

        publicElements
                .filterIsInstance(VariableElement::class.java)
                .mapNotNull { processingEnv.typeUtils.asElement(it.asType()) as? TypeElement }
                .forEach { fillPublicClasses(apiClasses, it, pkg) }
    }
}