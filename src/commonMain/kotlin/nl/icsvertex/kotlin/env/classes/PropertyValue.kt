package nl.icsvertex.kotlin.env.classes

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PropertyValue<R>(@Contextual val value: R)