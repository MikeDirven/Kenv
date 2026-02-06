package nl.icsvertex.kotlin.env.window

import kotlinx.serialization.json.JsonObject

@JsName("window")
external object Window {
    // For your environment variables access
    @OptIn(ExperimentalWasmJsInterop::class)
    val environment: JsAny
}
