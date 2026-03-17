package io.github.mikedirven.serializers

import kotlinx.serialization.json.Json

actual val defaultJsonSerializer: Json = Json {
    ignoreUnknownKeys = true
}