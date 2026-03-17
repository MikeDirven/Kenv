package io.github.mikedirven.window

@JsName("window")
external object Window {
    // For your environment variables access
    @OptIn(ExperimentalWasmJsInterop::class)
    val environment: JsAny
}
