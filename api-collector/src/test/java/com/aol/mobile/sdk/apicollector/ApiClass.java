/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

package com.aol.mobile.sdk.apicollector;

import com.aol.mobile.sdk.annotations.PrivateApi;
import com.aol.mobile.sdk.annotations.PublicApi;

@PublicApi
public class ApiClass {
    public int publicInt;
    public FieldTestClass fieldTestClass;
    public PrivateApiClass privateApiClass;
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

    @PrivateApi
    public enum PrivateEnum {
        ONE, TWO
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
