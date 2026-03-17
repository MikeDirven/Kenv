package io.github.mikedirven.atomic.serializers

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

actual val defaultJsonSerializer: Json = Json {
    serializersModule = SerializersModule {
        contextual(FileTypeSerializer)
    }
}