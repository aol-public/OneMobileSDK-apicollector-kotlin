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
}