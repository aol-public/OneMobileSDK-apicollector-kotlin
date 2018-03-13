/*
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the MIT License. See LICENSE.md file in project root for terms.
 */

package com.aol.mobile.sdk.apicollector;

import com.aol.mobile.sdk.annotations.PublicApi;

@PublicApi
public class CircleTestClass1 {
    public CircleTestClass2 reference;

    public CircleTestClass2 getReference() {
        return reference;
    }

    public void setReference(CircleTestClass2 reference) {
        this.reference = reference;
    }
}
