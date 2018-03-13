/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

package com.aol.mobile.sdk.apicollector

data class TypeDescriptor(val modifiers: Collection<String>, val name: String,
                          val fields: Set<VariableDescriptor>, val methods: Set<MethodDescriptor>)

data class VariableDescriptor(val modifiers: Collection<String>, val name: String, val type: String)

data class MethodDescriptor(val modifiers: Collection<String>, val name: String,
                            val returnType: String, val params: List<VariableDescriptor>)
