package io.github.mikedirven.classes

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PropertyValue<R>(@Contextual val value: R)