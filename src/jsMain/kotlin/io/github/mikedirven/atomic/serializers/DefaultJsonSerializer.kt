package io.github.mikedirven.atomic.serializers

import kotlinx.serialization.json.Json

actual val defaultJsonSerializer: Json = Json {
    ignoreUnknownKeys = true
}