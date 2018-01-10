/*
 * Copyright (c) 2018. Oath.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.aol.mobile.sdk.apicollector

import com.google.gson.GsonBuilder
import com.aol.mobile.sdk.annotations.PublicApi
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