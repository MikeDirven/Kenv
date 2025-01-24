package nl.icsvertex.kotlin.env.atomic

import kotlinx.coroutines.delay


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual open class AtomicRef<T> actual constructor(value: T) {
    private var _reference: T = value

    actual open fun getValue(): T {
        return _reference
    }

    actual open fun setValue(newValue: T) {
        _reference = newValue
    }
}