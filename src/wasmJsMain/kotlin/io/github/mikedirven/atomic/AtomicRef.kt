package io.github.mikedirven.atomic

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class AtomicRef<T> actual constructor(value: T) {
    private val atomicValue = AtomicReference(value)
    @OptIn(ExperimentalAtomicApi::class)
    actual fun getValue(): T = atomicValue.load()

    @OptIn(ExperimentalAtomicApi::class)
    actual fun setValue(newValue: T) = atomicValue.store(newValue)
}