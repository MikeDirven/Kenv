package nl.icsvertex.kotlin.env.extensions

import kotlin.reflect.KProperty

inline val KProperty<*>.callableName: String
    get() = this.asDynamic().callableName as String