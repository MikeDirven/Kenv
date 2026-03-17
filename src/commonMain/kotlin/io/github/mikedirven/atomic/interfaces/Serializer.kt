package io.github.mikedirven.atomic.interfaces

import kotlinx.serialization.json.JsonObject
import io.github.mikedirven.atomic.classes.PropertyValue

interface Serializer {
    fun serialize(value: Any?): String?

    fun <R> deserialize(value: JsonObject): PropertyValue<R>
}