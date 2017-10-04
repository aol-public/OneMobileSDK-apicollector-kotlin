package com.aol.mobile.sdk.annotations

data class VariableDescriptor(val modifiers: Collection<String>, val name: String, val type: String)

data class MethodDescriptor(val modifiers: Collection<String>, val name: String,
                            val returnType: String, val params: Set<VariableDescriptor>)

data class TypeDescriptor(val modifiers: Collection<String>, val name: String,
                          val fields: Set<VariableDescriptor>, val methods: Set<MethodDescriptor>)