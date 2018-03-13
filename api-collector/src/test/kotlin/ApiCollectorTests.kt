/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

import com.aol.mobile.sdk.apicollector.*
import com.aol.mobile.sdk.apicollector.PublicApiGrabber.Companion.PUBLIC_API_FILENAME
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File

class ApiCollectorTests {
    private lateinit var apiManifest: List<TypeDescriptor>

    @Before
    fun before() {
        val manifest = File("build/$PUBLIC_API_FILENAME").readText()
        apiManifest = Gson().fromJson(manifest)
    }

    @Test
    fun nestedClassesShouldHaveProperNames() {
        assertThat(apiManifest.any { "com.aol.mobile.sdk.apicollector.ApiClass\$Callback" in it.name }).isTrue()
        assertThat(apiManifest.any { "com.aol.mobile.sdk.apicollector.ApiClass\$Callback\$Callbacks" in it.name }).isTrue()
    }

    @Test
    fun manifestShouldHoldOnlyPubicAndProtectedMembers() {
        assertThat(apiManifest.any { "protected" in it.modifiers || "public" in it.modifiers }).isTrue()
        assertThat(apiManifest.none { "private" in it.modifiers }).isTrue()
        assertThat(apiManifest.none { it.modifiers.isEmpty() }).isTrue()

        assertThat(apiManifest.any { it.fields.any { "protected" in it.modifiers } }).isTrue()
        assertThat(apiManifest.any { it.fields.any { "public" in it.modifiers } }).isTrue()
        assertThat(apiManifest.none { it.fields.any { "private" in it.modifiers } }).isTrue()
        assertThat(apiManifest.none { it.fields.any { it.modifiers.isEmpty() } }).isTrue()

        assertThat(apiManifest.any { it.methods.any { "protected" in it.modifiers } }).isTrue()
        assertThat(apiManifest.any { it.methods.any { "public" in it.modifiers } }).isTrue()
        assertThat(apiManifest.none { it.methods.any { "private" in it.modifiers } }).isTrue()
        assertThat(apiManifest.none { it.methods.any { it.modifiers.isEmpty() } }).isTrue()
    }

    @Test
    fun nestedClassesAsReturnTypesAndVariablesShouldHaveProperNames() {
        val type = apiManifest.first { it.name == "com.aol.mobile.sdk.apicollector.ApiClass" }
        val returnType = type.methods.first { it.name == "getCallback" }.returnType
        val paramType = type.methods.first { it.name == "setCallback" }.params.first().type

        assertThat(returnType).isEqualTo("com.aol.mobile.sdk.apicollector.ApiClass\$Callback")
        assertThat(paramType).isEqualTo("com.aol.mobile.sdk.apicollector.ApiClass\$Callback")
    }

    @Test
    fun functionReturnTypesShouldBeInManifest() {
        assertThat(apiManifest.any { it.name == ReturnTestClass::class.java.name }).isTrue()
    }

    @Test
    fun functionParamTypesShouldBeInManifest() {
        assertThat(apiManifest.any { it.name == ParamTestClass::class.java.name }).isTrue()
    }

    @Test
    fun fieldTypesShouldBeInManifest() {
        assertThat(apiManifest.any { it.name == FieldTestClass::class.java.name }).isTrue()
    }

    @Test
    fun isolatedNonAnnotatedClassShouldNotBeInManifest() {
        assertThat(apiManifest.none { it.name == IsolatedTestClass::class.java.name }).isTrue()
    }

    @Test
    fun isolatedAnnotatedClassShouldBeInManifest() {
        assertThat(apiManifest.any { it.name == ManifestTestClass::class.java.name }).isTrue()
    }

    @Test
    fun circleReferencesShouldBeTakenIntoAccountOnlyOnce() {
        assertThat(apiManifest.filter { it.name == CircleTestClass1::class.java.name }).hasSize(1)
        assertThat(apiManifest.filter { it.name == CircleTestClass2::class.java.name }).hasSize(1)
    }

    @Test
    fun throwableClassesShouldBeInManifest() {
        assertThat(apiManifest.filter { it.name == TestException::class.java.name }).isNotEmpty
    }

    @Test
    fun privateApiAnnotatedClassesShouldNotBeInManifest() {
        assertThat(apiManifest.filter { it.name == ApiClass.PrivateEnum::class.java.name }).isEmpty()
        assertThat(apiManifest.filter { it.name == PrivateApiClass::class.java.name }).isEmpty()
    }
}