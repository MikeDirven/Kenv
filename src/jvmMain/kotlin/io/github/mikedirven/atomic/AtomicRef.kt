package io.github.mikedirven.atomic

import java.util.concurrent.atomic.AtomicReference

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class AtomicRef<T> actual constructor(value: T) : AtomicReference<T>(value) {
    actual fun getValue(): T = super.get()

    actual fun setValue(newValue: T) = super.set(newValue)
}