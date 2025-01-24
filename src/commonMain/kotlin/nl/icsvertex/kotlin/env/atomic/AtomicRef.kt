package nl.icsvertex.kotlin.env.atomic

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class AtomicRef<T>(
    value: T
){
    fun getValue(): T

    fun setValue(newValue: T)
}