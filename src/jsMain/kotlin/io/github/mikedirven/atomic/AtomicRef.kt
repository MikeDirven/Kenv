package io.github.mikedirven.atomic

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class AtomicRef<T> actual constructor(value: T) {
    private var _reference: T = value

    actual open fun getValue(): T {
        reentrantLock().withLock {
            return _reference
        }
    }

    actual open fun setValue(newValue: T) {
        reentrantLock().withLock {
            _reference = newValue
        }
    }
}