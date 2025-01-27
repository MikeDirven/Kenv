package nl.icsvertex.kotlin.env

import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.interfaces.Serializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class EnvProperty<R: Any>(
    kClass: KClass<R>,
    name: String?,
    default: R?
) {
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): R

    companion object {
        val serializers: AtomicMap<KClass<*>, Serializer>
        val properties: AtomicMap<String, String>

        fun registerSerializer(kClass: KClass<*>, serializer: Serializer)

        inline fun <reified T> registerSerializer(serializer: Serializer)

        internal fun readEnvironment()

        inline operator fun <reified R: Any> invoke(name: String?, default: R? = null) : EnvProperty<R>

        inline operator fun <reified R: Any> invoke() : EnvProperty<R>
    }
}