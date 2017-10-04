package com.aol.mobile.sdk.annotations

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