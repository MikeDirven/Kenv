package nl.icsvertex.kotlin.env.serializers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import nl.icsvertex.kotlin.env.EnvProperty
import nl.icsvertex.kotlin.env.classes.PropertyValue
import nl.icsvertex.kotlin.env.interfaces.Serializer

object DefaultSerializers {
    object DefaultSerializer : Serializer {
        override fun serialize(value: Any?): String? {
            return value?.toString()
        }

        override fun <R> deserialize(value: JsonObject): PropertyValue<R> {
            return Json.decodeFromJsonElement<PropertyValue<R>>(value)
        }
    }

    object StringSerializer : Serializer {
        override fun serialize(value: Any?): String? {
            return value?.toString()
        }

        override fun <R> deserialize(value: JsonObject): PropertyValue<R> {
            return Json.decodeFromJsonElement<PropertyValue<R>>(value)
        }
    }

    object IntSerializer : Serializer {
        override fun serialize(value: Any?): String? {
            return value?.toString()
        }

        override fun <R> deserialize(value: JsonObject): PropertyValue<R> {
            return Json.decodeFromJsonElement<PropertyValue<R>>(value)
        }
    }

    init {
        // Register default serializers
        EnvProperty.registerSerializer<Any>(DefaultSerializer)
        EnvProperty.registerSerializer<String>(StringSerializer)
        EnvProperty.registerSerializer<Int>(IntSerializer)
    }
}