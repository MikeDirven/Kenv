package nl.icsvertex.kotlin.env.classes

import kotlinx.serialization.Serializable

@Serializable
data class PropertyValue<R>(val value: R)