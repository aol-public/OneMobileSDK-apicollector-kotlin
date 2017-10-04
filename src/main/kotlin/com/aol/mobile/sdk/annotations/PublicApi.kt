package com.aol.mobile.sdk.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PublicApi(val pkg: String)
