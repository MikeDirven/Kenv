package nl.icsvertex.kotlin.env.interfaces

import kotlinx.serialization.json.JsonObject
import nl.icsvertex.kotlin.env.classes.PropertyValue

interface Serializer {
    fun serialize(value: Any?): String?

    fun <R> deserialize(value: JsonObject): PropertyValue<R>
}