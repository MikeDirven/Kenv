package nl.icsvertex.kotlin.env.classes

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class PropertyValue<R>(val value: R)