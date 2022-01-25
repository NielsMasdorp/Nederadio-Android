package com.nielsmasdorp.sleeply.util

import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

inline fun <reified T> T.callPrivateFunc(name: String, vararg args: Any?): Any? =
    T::class
        .declaredMemberFunctions
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.call(this, *args)

