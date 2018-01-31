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

import javax.lang.model.element.*

internal fun TypeElement.asTypeDescriptor() = enclosedElements
        .filter { it.modifiers.contains(Modifier.PUBLIC) }
        .groupBy { it.kind }
        .let {
            TypeDescriptor(
                    modifiers = modifiers.asModifiersSet(),
                    name = qualifiedName.toString(),
                    fields = it[ElementKind.FIELD].orEmpty()
                            .mapNotNull { (it as? VariableElement)?.asVariableDescriptor() }
                            .toSet(),
                    methods = (it[ElementKind.CONSTRUCTOR].orEmpty() + it[ElementKind.METHOD].orEmpty())
                            .mapNotNull { (it as? ExecutableElement)?.asMethodDescriptor() }
                            .toSet()
            )
        }

internal fun ExecutableElement.asMethodDescriptor() = MethodDescriptor(
        modifiers = modifiers.asModifiersSet(),
        name = if (simpleName.toString().contains("<init>")) "constructor" else simpleName.toString(),
        returnType = returnType.toString(),
        params = parameters.mapNotNull { it.asVariableDescriptor() }.toSet()
)

internal fun Set<Modifier>.asModifiersSet() = map { it.name.toLowerCase() }.toSet()

internal fun VariableElement.asVariableDescriptor() = VariableDescriptor(
        modifiers = modifiers.asModifiersSet(),
        name = simpleName.toString(),
        type = asType().toString()
)