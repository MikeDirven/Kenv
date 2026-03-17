package io.github.mikedirven.atomic.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject


fun String.isObject() : JsonObject? {
    return if(
        this.startsWith("{") &&
        this.endsWith("}")
    ) {
        Json.decodeFromString<JsonObject>(this)
    } else null
}

fun String.isArray() : JsonArray? {
    return if(
        this.startsWith("[") &&
        this.endsWith("]")
    ) {
        Json.decodeFromString<JsonArray>(this)
    } else null
}