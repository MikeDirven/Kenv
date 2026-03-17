package io.github.mikedirven.atomic.window

@JsName("window")
external object Window {
    // For your environment variables access
    @OptIn(ExperimentalWasmJsInterop::class)
    val environment: JsAny
}
