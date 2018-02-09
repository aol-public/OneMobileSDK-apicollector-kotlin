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

import com.aol.mobile.sdk.annotations.PublicApi
import com.google.gson.Gson
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier.PROTECTED
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

class PublicApiGrabber : AbstractProcessor() {
    companion object {
        const val BUILD_PATH_KEY = "BUILD_PATH_KEY"
        const val PUBLIC_API_FILENAME = "public_api_manifest.json"
    }

    private lateinit var apiFile: File

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf(BUILD_PATH_KEY)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.RELEASE_8
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(PublicApi::class.java.canonicalName)
    }

    override fun init(procEnv: ProcessingEnvironment?) {
        super.init(procEnv)

        apiFile = procEnv.let { env ->
            if (env == null || !env.options.containsKey(BUILD_PATH_KEY))
                throw ConfigurationException("Provide $BUILD_PATH_KEY as processor options")

            File(env.options[BUILD_PATH_KEY], PUBLIC_API_FILENAME)
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val publicApiClasses = annotations
                .flatMap { roundEnv.getElementsAnnotatedWith(it) }
                .filterIsInstance(TypeElement::class.java)
                .map { getPublicApiClasses(it, it.getAnnotation(PublicApi::class.java).pkg) }
                .fold(mutableMapOf<String, TypeElement>()) { apiClassesMap, classesChunk ->
                    apiClassesMap.putAll(classesChunk)
                    apiClassesMap
                }

        val apiDescription = publicApiClasses.values
                .map { it.asTypeDescriptor() }
                .sortedBy { it.name }

        if (apiDescription.isEmpty()) return false

        apiFile.writeText(Gson().toJson(apiDescription))

        return true
    }

    private fun getPublicApiClasses(entryElement: TypeElement, pkg: String): Map<String, TypeElement> {
        val result = mutableMapOf<String, TypeElement>()
        val qualifiedName = entryElement.qualifiedName.toString()
        val typeUtils = processingEnv.typeUtils

        if (!qualifiedName.startsWith(pkg)) return result

        if (entryElement.modifiers.any { it == PUBLIC || it == PROTECTED }) {
            result[qualifiedName] = entryElement
        }

        entryElement.enclosedElements
                .filter { it.modifiers.any { it == PUBLIC || it == PROTECTED } }
                .forEach { element ->
                    when (element) {
                        is ExecutableElement -> {
                            (element.parameters.map { typeUtils.asElement(it.asType()) } + typeUtils.asElement(element.returnType))
                                    .filterIsInstance(TypeElement::class.java)
                                    .map { getPublicApiClasses(it, pkg) }
                                    .forEach { result.putAll(it) }
                        }

                        is VariableElement -> {
                            val type = typeUtils.asElement(element.asType())
                            if (type is TypeElement) result.putAll(getPublicApiClasses(type, pkg))
                        }

                        is TypeElement -> result.putAll(getPublicApiClasses(element, pkg))
                    }
                }

        return result
    }

    private fun TypeElement.asTypeDescriptor(): TypeDescriptor {
        val publicElements = enclosedElements.filter { it.modifiers.any { it == PUBLIC || it == PROTECTED } }
        val modifiers = modifiers.asModifiersSet()
        val name = processingEnv.elementUtils.getBinaryName(this).toString()
        val fields = publicElements.filterIsInstance(VariableElement::class.java)
                .map { it.asVariableDescriptor() }.toSet()
        val methods = publicElements.filterIsInstance(ExecutableElement::class.java)
                .map { it.asMethodDescriptor() }.toSet()

        return TypeDescriptor(modifiers, name, fields, methods)
    }

    private fun ExecutableElement.asMethodDescriptor(): MethodDescriptor {
        return with(processingEnv) {
            val modifiers = modifiers.asModifiersSet()
            val params = parameters.mapNotNull { it.asVariableDescriptor() }
            val simpleName = simpleName.toString()
            val name = if (simpleName in "<init>") "constructor" else simpleName
            val actualReturnType = typeUtils.asElement(returnType)
            val returnType = when (actualReturnType) {
                is TypeElement -> elementUtils.getBinaryName(actualReturnType).toString()
                else -> returnType.toString()
            }

            MethodDescriptor(modifiers, name, returnType, params)
        }
    }

    private fun VariableElement.asVariableDescriptor(): VariableDescriptor {
        return with(processingEnv) {
            val modifiers = modifiers.asModifiersSet()
            val name = simpleName.toString()
            val actualType = typeUtils.asElement(asType())
            val type = when (actualType) {
                is TypeElement -> elementUtils.getBinaryName(actualType).toString()
                else -> asType().toString()
            }

            VariableDescriptor(modifiers, name, type)
        }
    }

    private fun Set<Modifier>.asModifiersSet() = map { it.name.toLowerCase() }.toSet()
}