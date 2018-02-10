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

package com.aol.mobile.sdk.apicollector;

import com.aol.mobile.sdk.annotations.PublicApi;

@PublicApi(pkg = "com.aol.mobile.sdk.apicollector")
public class ApiClass {
    public int publicInt;
    public FieldTestClass fieldTestClass;
    protected int protectedInt;
    int packageInt;
    private int privateInt;

    public int getPrivateInt() {
        return privateInt;
    }

    public void setPrivateInt(int privateInt) {
        this.privateInt = privateInt;
    }

    public void apiPublicFun() {

    }

    public ReturnTestClass getReturnTestClass() {
        return null;
    }

    public void setParamTestClass(ParamTestClass value) {

    }

    protected void apiProtectedFun() {

    }

    private void apiPrivateFun() {

    }

    public void unstable() throws TestException {

    }

    void apiPackageLocalFun() {

    }

    public Callback getCallback() {
        return null;
    }

    protected void setCallback(Callback callback) {

    }

    public interface Callback {
        class Callbacks {
            public void apiPublicFun() {

            }

            protected void apiProtectedFun() {

            }

            private void apiPrivateFun() {

            }

            void apiPackageLocalFun() {

            }
        }
    }

    private class Ololo {

    }
}
