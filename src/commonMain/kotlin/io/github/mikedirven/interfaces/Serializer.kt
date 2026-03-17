package io.github.mikedirven.interfaces

import kotlinx.serialization.json.JsonObject
import io.github.mikedirven.classes.PropertyValue

interface Serializer {
    fun serialize(value: Any?): String?

    fun <R> deserialize(value: JsonObject): PropertyValue<R>
}