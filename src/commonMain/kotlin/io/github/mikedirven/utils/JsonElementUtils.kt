package io.github.mikedirven.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonElement.asPrimitive(): String? {
    return try {
        this.jsonPrimitive.content
    } catch (e: Exception){
        null
    }
}

fun JsonElement.asObject(): String? {
    return try {
        this.jsonObject.toString()
    } catch (e: Exception) {
        null
    }
}

fun JsonElement.asArray(): String? {
    return try {
        this.jsonArray.toString()
    } catch (e: Exception) {
        null
    }
}