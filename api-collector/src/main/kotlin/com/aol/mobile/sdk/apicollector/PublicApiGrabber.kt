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
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.PROTECTED
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class PublicApiGrabber : AbstractProcessor() {
    companion object {
        const val BUILD_PATH_KEY = "BUILD_PATH_KEY"
        const val PUBLIC_API_FILENAME = "public_api_manifest.json"
    }

    private lateinit var apiFile: File
    private lateinit var typeUtils: Types
    private lateinit var elementUtils: Elements

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

            typeUtils = env.typeUtils
            elementUtils = env.elementUtils
            File(env.options[BUILD_PATH_KEY], PUBLIC_API_FILENAME)
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val publicApiClasses = mutableMapOf<String, TypeElement>()

        annotations.flatMap { roundEnv.getElementsAnnotatedWith(it) }
                .filterIsInstance(TypeElement::class.java)
                .map {
                    val pkg = it.getAnnotation(PublicApi::class.java).pkg
                    getPublicApiClasses(publicApiClasses, it, pkg)
                }

        val apiDescription = publicApiClasses.values
                .map { it.descriptor }
                .sortedBy { it.name }

        if (apiDescription.isEmpty()) return false

        apiFile.writeText(Gson().toJson(apiDescription))

        return true
    }

    private fun getPublicApiClasses(result: MutableMap<String, TypeElement>, element: TypeElement, pkg: String) {
        with(element) {
            if (!name.startsWith(pkg) || !isPublic || name in result.keys) return

            result[name] = element

            publicElements.forEach {
                when (it) {
                    is ExecutableElement -> it.enclosingTypes
                            .filterIsInstance(TypeElement::class.java)
                            .forEach { getPublicApiClasses(result, it, pkg) }

                    is VariableElement -> {
                        val type = it.type
                        if (type is TypeElement) getPublicApiClasses(result, type, pkg)
                    }

                    is TypeElement -> getPublicApiClasses(result, it, pkg)
                }
            }
        }
    }

    private val Element.mods
        get() = modifiers.map { it.name.toLowerCase() }.toSet()

    private val Element.isPublic
        get() = modifiers.any { it in setOf(PUBLIC, PROTECTED) }

    private val Element.type
        get() = typeUtils.asElement(asType())

    private val TypeMirror.type
        get() = typeUtils.asElement(this)

    private val TypeElement.name
        get() = elementUtils.getBinaryName(this).toString()

    private val TypeElement.publicElements
        get() = enclosedElements.filter { it.isPublic }

    private val TypeElement.publicFields
        get() = publicElements
                .filterIsInstance(VariableElement::class.java)
                .map { it.descriptor }.toSet()

    private val TypeElement.publicMethods
        get() = publicElements
                .filterIsInstance(ExecutableElement::class.java)
                .map { it.descriptor }.toSet()

    private val TypeElement.descriptor
        get() = TypeDescriptor(mods, name, publicFields, publicMethods)

    private val ExecutableElement.methodName
        get() = let {
            val simpleName = simpleName.toString()

            if (simpleName in "<init>") "constructor" else simpleName
        }

    private val ExecutableElement.enclosingTypes
        get() = parameters.map { it.type } + thrownTypes.map { it.type } + returnType.type

    private val ExecutableElement.methodParams
        get() = parameters.map { it.descriptor }

    private val ExecutableElement.returnTypeName
        get() = let {
            val actualReturnType = returnType.type

            when (actualReturnType) {
                is TypeElement -> actualReturnType.name
                else -> returnType.toString()
            }
        }

    private val ExecutableElement.descriptor
        get () = MethodDescriptor(mods, methodName, returnTypeName, methodParams)

    private val VariableElement.name
        get() = simpleName.toString()

    private val VariableElement.typeName
        get() = let {
            val actualType = type

            when (actualType) {
                is TypeElement -> actualType.name
                else -> asType().toString()
            }
        }

    private val VariableElement.descriptor
        get() = VariableDescriptor(mods, name, typeName)
}